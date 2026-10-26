package com.example.license

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages the lifecycle, persistence, startup verification, and administration
 * of the application license outside Google Play.
 */
class LicenseManager(private val context: Context) {
    companion object {
        const val DEFAULT_TRIAL_DAYS = 30L
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences("apis_smart_license_prefs", Context.MODE_PRIVATE)

    private val serverClient = LicenseServerClient()

    private val _licenseData = MutableStateFlow(loadPersistedLicense())
    val licenseData: StateFlow<LicenseData> = _licenseData.asStateFlow()

    init {
        // Run initial verification on startup
        checkOnStartup()
    }

    /**
     * Checks license state at system startup.
     */
    fun checkOnStartup(): LicenseData {
        val current = loadPersistedLicense()
        val now = System.currentTimeMillis()

        var newStatus = current.status

        when (current.status) {
            LicenseStatus.ACTIVE -> {
                if (current.tier != LicenseTier.LIFETIME && current.expiryDateMillis in 1 until now) {
                    // Check grace period (3 days)
                    val graceLimit = current.expiryDateMillis + TimeUnit.DAYS.toMillis(3)
                    newStatus = if (now <= graceLimit) {
                        LicenseStatus.GRACE_PERIOD
                    } else {
                        LicenseStatus.EXPIRED
                    }
                }
            }
            LicenseStatus.TRIAL -> {
                if (current.expiryDateMillis in 1 until now) {
                    newStatus = LicenseStatus.EXPIRED
                }
            }
            LicenseStatus.GRACE_PERIOD -> {
                val graceLimit = current.expiryDateMillis + TimeUnit.DAYS.toMillis(3)
                if (now > graceLimit) {
                    newStatus = LicenseStatus.EXPIRED
                }
            }
            LicenseStatus.EXPIRED -> {
                // If it was expired, check if key is now valid or still expired
                if (current.licenseKey.isNotBlank()) {
                    val validation = LicenseSecurityHelper.validateKey(current.licenseKey, current.deviceId)
                    if (validation is KeyValidationResult.Valid) {
                        newStatus = LicenseStatus.ACTIVE
                    }
                }
            }
        }

        val updated = current.copy(status = newStatus)
        saveLicense(updated)
        _licenseData.value = updated
        return updated
    }

    /**
     * Activates the app using an access key.
     */
    fun activateWithKey(key: String, owner: String = ""): Result<String> {
        val current = _licenseData.value
        val result = LicenseSecurityHelper.validateKey(key, current.deviceId)

        return when (result) {
            is KeyValidationResult.Valid -> {
                val updated = current.copy(
                    status = LicenseStatus.ACTIVE,
                    tier = result.tier,
                    licenseKey = key.trim().uppercase(),
                    ownerName = if (owner.isNotBlank()) owner.trim() else result.owner,
                    expiryDateMillis = result.expiryMillis,
                    activationDateMillis = System.currentTimeMillis(),
                    isActivatedOffline = true,
                    adminNotes = "Ativado localmente via Chave de Acesso.",
                    licensesCount = result.licensesCount,
                    associationName = result.associationName.ifBlank { current.associationName }
                )
                saveLicense(updated)
                _licenseData.value = updated
                val tierMsg = if (result.tier == LicenseTier.ASSOCIATION) {
                    "Licença de Associação ativada com sucesso! Cota: ${result.licensesCount} licenças para associados."
                } else {
                    "Licença ativada com sucesso! Categoria: ${result.tier.displayName}."
                }
                Result.success(tierMsg)
            }
            is KeyValidationResult.Expired -> {
                Result.failure(Exception(result.reason))
            }
            is KeyValidationResult.Invalid -> {
                Result.failure(Exception(result.reason))
            }
        }
    }

    /**
     * Activates the app license via verified Google Play Billing purchase.
     */
    fun activateFromGooglePlay(orderId: String?, purchaseToken: String, productId: String): LicenseData {
        val current = _licenseData.value
        val now = System.currentTimeMillis()
        val isLifetime = productId.contains("lifetime", ignoreCase = true)
        val isAnnual = productId.contains("annual", ignoreCase = true)

        val targetTier = if (isLifetime) LicenseTier.LIFETIME else LicenseTier.PRO
        val targetCycle = if (isLifetime) "VITALÍCIO" else if (isAnnual) "ANUAL" else "MENSAL"
        val expiryMillis = if (isLifetime) 0L else now + (if (isAnnual) 366L else 32L) * 24L * 3600L * 1000L
        val cleanOrderId = orderId ?: "GPA.${purchaseToken.takeLast(12).uppercase()}"

        val updated = current.copy(
            status = LicenseStatus.ACTIVE,
            tier = targetTier,
            licenseKey = "GP-$cleanOrderId",
            ownerName = if (current.ownerName.isBlank() || current.ownerName == "Produtor Apícola") "Assinante Google Play" else current.ownerName,
            activationDateMillis = now,
            expiryDateMillis = expiryMillis,
            lastServerCheckMillis = now,
            isActivatedOffline = false,
            adminNotes = "Ativado e verificado via Google Play Billing. Pedido: $cleanOrderId",
            billingCycle = targetCycle
        )
        saveLicense(updated)
        _licenseData.value = updated
        Log.i("LicenseManager", "App activated via Google Play Billing: tier=$targetTier cycle=$targetCycle")
        return updated
    }

    /**
     * Verifies the license against the remote licensing server.
     */
    suspend fun verifyWithServer(customUrl: String? = null): Result<String> {
        val current = _licenseData.value
        val urlToUse = customUrl ?: current.serverVerificationUrl

        val verification = serverClient.verifyWithServer(
            serverUrl = urlToUse,
            deviceId = current.deviceId,
            currentLicenseKey = current.licenseKey
        )

        return if (verification.isSuccess) {
            val updated = current.copy(
                status = verification.updatedStatus ?: LicenseStatus.ACTIVE,
                tier = verification.updatedTier ?: current.tier,
                expiryDateMillis = verification.updatedExpiryMillis ?: current.expiryDateMillis,
                lastServerCheckMillis = System.currentTimeMillis(),
                serverVerificationUrl = urlToUse,
                isActivatedOffline = false,
                adminNotes = verification.message
            )
            saveLicense(updated)
            _licenseData.value = updated
            Result.success(verification.message)
        } else {
            // Update last check timestamp even on failure if server was contacted
            val updated = current.copy(
                lastServerCheckMillis = System.currentTimeMillis(),
                serverVerificationUrl = urlToUse
            )
            saveLicense(updated)
            _licenseData.value = updated
            Result.failure(Exception(verification.message))
        }
    }

    /**
     * Generates a new administrative key to provide to users.
     */
    fun generateAdminKey(
        tier: LicenseTier,
        validDays: Int,
        targetDeviceId: String? = null,
        licensesCount: Int = 1,
        associationName: String = ""
    ): String {
        return LicenseSecurityHelper.generateAdminKey(
            tier = tier,
            validDaysFromNow = validDays,
            targetDeviceId = targetDeviceId,
            licensesCount = licensesCount,
            associationName = associationName
        )
    }

    /**
     * Updates details for an association license.
     */
    fun updateAssociationDetails(associationName: String, quota: Int, cycle: String = "MENSAL") {
        val current = _licenseData.value
        val updated = current.copy(
            associationName = associationName.trim(),
            licensesCount = quota.coerceAtLeast(1),
            billingCycle = cycle
        )
        saveLicense(updated)
        _licenseData.value = updated
    }

    /**
     * Updates the custom server verification URL.
     */
    fun updateServerUrl(newUrl: String) {
        val updated = _licenseData.value.copy(serverVerificationUrl = newUrl.trim())
        saveLicense(updated)
        _licenseData.value = updated
    }

    /**
     * Resets back to trial mode (useful for testing or demo).
     */
    fun resetToTrial() {
        val now = System.currentTimeMillis()
        val trialExpiry = now + TimeUnit.DAYS.toMillis(DEFAULT_TRIAL_DAYS)
        val devId = getOrCreateDeviceId()

        val trialLicense = LicenseData(
            status = LicenseStatus.TRIAL,
            tier = LicenseTier.TRIAL,
            licenseKey = "",
            ownerName = "Produtor Apícola",
            deviceId = devId,
            activationDateMillis = now,
            expiryDateMillis = trialExpiry,
            lastServerCheckMillis = 0L,
            adminNotes = "Período de avaliação de $DEFAULT_TRIAL_DAYS dias reiniciado."
        )
        saveLicense(trialLicense)
        _licenseData.value = trialLicense
    }

    /**
     * Deactivates the current license and sets status to EXPIRED.
     */
    fun deactivateLicense() {
        val current = _licenseData.value
        val updated = current.copy(
            status = LicenseStatus.EXPIRED,
            licenseKey = "",
            adminNotes = "Licença desativada manualmente pelo usuário."
        )
        saveLicense(updated)
        _licenseData.value = updated
    }

    // -------------------------------------------------------------
    // Persistence Helpers
    // -------------------------------------------------------------

    private fun loadPersistedLicense(): LicenseData {
        val devId = getOrCreateDeviceId()

        val statusStr = prefs.getString("status", null)
        val tierStr = prefs.getString("tier", null)
        val licenseKey = prefs.getString("license_key", "") ?: ""
        val ownerName = prefs.getString("owner_name", "Produtor Apícola") ?: "Produtor Apícola"
        val registeredEmail = prefs.getString("registered_email", "") ?: ""
        val activationDateMillis = prefs.getLong("activation_date", 0L)
        val expiryDateMillis = prefs.getLong("expiry_date", 0L)
        val lastServerCheckMillis = prefs.getLong("last_server_check", 0L)
        val serverVerificationUrl = prefs.getString(
            "server_url",
            "https://licenca.apissmart.com.br/api/v1/verify"
        ) ?: "https://licenca.apissmart.com.br/api/v1/verify"
        val isActivatedOffline = prefs.getBoolean("activated_offline", false)
        val adminNotes = prefs.getString("admin_notes", "") ?: ""
        val associationName = prefs.getString("association_name", "") ?: ""
        val licensesCount = prefs.getInt("licenses_count", 1)
        val billingCycle = prefs.getString("billing_cycle", "MENSAL") ?: "MENSAL"

        // If never initialized, start a 30-day trial
        if (statusStr == null || expiryDateMillis == 0L) {
            val now = System.currentTimeMillis()
            val trialExpiry = now + TimeUnit.DAYS.toMillis(DEFAULT_TRIAL_DAYS)
            val initialTrial = LicenseData(
                status = LicenseStatus.TRIAL,
                tier = LicenseTier.TRIAL,
                licenseKey = "",
                ownerName = ownerName,
                deviceId = devId,
                activationDateMillis = now,
                expiryDateMillis = trialExpiry,
                serverVerificationUrl = serverVerificationUrl,
                adminNotes = "Instalação inicial: $DEFAULT_TRIAL_DAYS dias de avaliação gratuita concedidos."
            )
            saveLicense(initialTrial)
            return initialTrial
        }

        val status = try {
            LicenseStatus.valueOf(statusStr)
        } catch (_: Exception) {
            LicenseStatus.TRIAL
        }

        val tier = try {
            LicenseTier.valueOf(tierStr ?: "TRIAL")
        } catch (_: Exception) {
            LicenseTier.TRIAL
        }

        var finalExpiry = expiryDateMillis
        if (status == LicenseStatus.TRIAL) {
            val totalTrialMillis = TimeUnit.DAYS.toMillis(DEFAULT_TRIAL_DAYS)
            // Upgrade previous 14-day trial to 30 days
            if (finalExpiry - activationDateMillis <= TimeUnit.DAYS.toMillis(16)) {
                finalExpiry = activationDateMillis + totalTrialMillis
                if (finalExpiry < System.currentTimeMillis()) {
                    finalExpiry = System.currentTimeMillis() + totalTrialMillis
                }
                prefs.edit().putLong("expiry_date", finalExpiry).apply()
            }
        }

        return LicenseData(
            status = status,
            tier = tier,
            licenseKey = licenseKey,
            ownerName = ownerName,
            registeredEmail = registeredEmail,
            deviceId = devId,
            activationDateMillis = activationDateMillis,
            expiryDateMillis = finalExpiry,
            lastServerCheckMillis = lastServerCheckMillis,
            serverVerificationUrl = serverVerificationUrl,
            isActivatedOffline = isActivatedOffline,
            adminNotes = adminNotes,
            associationName = associationName,
            licensesCount = licensesCount,
            billingCycle = billingCycle
        )
    }

    private fun saveLicense(data: LicenseData) {
        prefs.edit().apply {
            putString("status", data.status.name)
            putString("tier", data.tier.name)
            putString("license_key", data.licenseKey)
            putString("owner_name", data.ownerName)
            putString("registered_email", data.registeredEmail)
            putLong("activation_date", data.activationDateMillis)
            putLong("expiry_date", data.expiryDateMillis)
            putLong("last_server_check", data.lastServerCheckMillis)
            putString("server_url", data.serverVerificationUrl)
            putBoolean("activated_offline", data.isActivatedOffline)
            putString("admin_notes", data.adminNotes)
            putString("association_name", data.associationName)
            putInt("licenses_count", data.licensesCount)
            putString("billing_cycle", data.billingCycle)
            apply()
        }
    }

    private fun getOrCreateDeviceId(): String {
        var id = prefs.getString("device_id", null)
        if (id.isNullOrBlank()) {
            // Stable 12-char alphanumeric ID for ease of typing/copying
            val raw = UUID.randomUUID().toString().replace("-", "").uppercase()
            id = "${raw.substring(0, 4)}-${raw.substring(4, 8)}-${raw.substring(8, 12)}"
            prefs.edit().putString("device_id", id).apply()
        }
        return id
    }
}
