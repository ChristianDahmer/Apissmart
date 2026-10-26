package com.example.license

import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Handles cryptographic license key generation, algorithmic verification,
 * and device binding for offline use.
 */
object LicenseSecurityHelper {

    private const val LICENSE_SALT = "ApisSmartAgroSecretLic2026!#"

    // Built-in universal master access keys for tech support, VIPs, or offline emergency unlock
    private val MASTER_KEYS = setOf(
        "APIS-MASTER-LIBERADO-2026",
        "APIS-PRO-UNLIMITED-VIP",
        "APIS-AGRO-BR-LIFETIME",
        "APIS-CAMPO-OFFLINE-KEY",
        "APIS-ASSOCIACAO-APICULTORES-2026"
    )

    /**
     * Verifies if a given key is valid and parses its tier, expiration, and device lock.
     */
    fun validateKey(
        rawKey: String,
        currentDeviceId: String
    ): KeyValidationResult {
        val sanitized = rawKey.trim().uppercase(Locale.ROOT)

        if (sanitized.isEmpty()) {
            return KeyValidationResult.Invalid("A chave de acesso não pode ser vazia.")
        }

        // Check master keys
        if (sanitized == "APIS-ASSOCIACAO-APICULTORES-2026") {
            return KeyValidationResult.Valid(
                tier = LicenseTier.ASSOCIATION,
                expiryMillis = Long.MAX_VALUE,
                isLifetime = true,
                owner = "Associação de Apicultores Master",
                isDeviceBound = false,
                licensesCount = 50,
                associationName = "Associação Nacional de Apicultura"
            )
        }

        if (MASTER_KEYS.contains(sanitized)) {
            return KeyValidationResult.Valid(
                tier = LicenseTier.LIFETIME,
                expiryMillis = Long.MAX_VALUE,
                isLifetime = true,
                owner = "Administrador / Licença Master",
                isDeviceBound = false
            )
        }

        // Special demo 30 days key
        if (sanitized == "APIS-DEMO-TEST-KEY-30D") {
            val thirtyDays = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30)
            return KeyValidationResult.Valid(
                tier = LicenseTier.PRO,
                expiryMillis = thirtyDays,
                isLifetime = false,
                owner = "Avaliação Estendida 30 Dias",
                isDeviceBound = false
            )
        }

        // Format expected: APIS-[TIER]-[DAYS_HEX]-[CHECKSUM]
        // or APIS-[TIER]-[DAYS_HEX]-[CHECKSUM]-[DEV_PREFIX]
        val parts = sanitized.split("-")
        if (parts.size < 4 || parts[0] != "APIS") {
            return KeyValidationResult.Invalid("Formato de chave inválido. Exemplo: APIS-PRO-050A-A9F1")
        }

        val rawTierCode = parts[1]
        var parsedLicensesCount = 1
        val tier = when {
            rawTierCode.startsWith("ASSO") -> {
                parsedLicensesCount = rawTierCode.removePrefix("ASSO").toIntOrNull() ?: 10
                LicenseTier.ASSOCIATION
            }
            rawTierCode == "ASC" -> {
                parsedLicensesCount = 10
                LicenseTier.ASSOCIATION
            }
            rawTierCode == "STD" -> LicenseTier.STANDARD
            rawTierCode == "PRO" -> LicenseTier.PRO
            rawTierCode == "ENT" -> LicenseTier.ENTERPRISE
            rawTierCode == "LIFE" -> LicenseTier.LIFETIME
            else -> return KeyValidationResult.Invalid("Categoria de licença desconhecida ($rawTierCode).")
        }
        val tierCode = rawTierCode

        val daysHex = parts[2]
        val checksumPart = parts[3]
        val deviceBoundPart = if (parts.size >= 5) parts[4] else null

        // Parse epoch days
        val epochDays = try {
            daysHex.toLong(16)
        } catch (_: Exception) {
            return KeyValidationResult.Invalid("Código de validade da chave corrompido.")
        }

        val expiryMillis = if (tier == LicenseTier.LIFETIME || epochDays >= 0xFFFF) {
            Long.MAX_VALUE
        } else {
            TimeUnit.DAYS.toMillis(epochDays)
        }

        // Check if expired
        if (expiryMillis != Long.MAX_VALUE && System.currentTimeMillis() > expiryMillis) {
            return KeyValidationResult.Expired("Esta chave de licença expirou na data configurada.")
        }

        // Check device binding if present
        if (deviceBoundPart != null && deviceBoundPart != "ANY") {
            val shortDevId = currentDeviceId.take(4).uppercase(Locale.ROOT)
            if (deviceBoundPart != shortDevId) {
                return KeyValidationResult.Invalid(
                    "Esta chave está vinculada a outro aparelho ($deviceBoundPart). Seu aparelho é ($shortDevId)."
                )
            }
        }

        // Checksum verification
        val targetDevStr = deviceBoundPart ?: "ANY"
        val expectedChecksum = generateChecksum(tierCode, daysHex, targetDevStr)

        if (!checksumPart.equals(expectedChecksum, ignoreCase = true)) {
            return KeyValidationResult.Invalid("Checksum de segurança da chave inválido ou chave adulterada.")
        }

        return KeyValidationResult.Valid(
            tier = tier,
            expiryMillis = expiryMillis,
            isLifetime = (tier == LicenseTier.LIFETIME || expiryMillis == Long.MAX_VALUE),
            owner = if (tier == LicenseTier.ASSOCIATION) "Associação de Apicultores ($parsedLicensesCount Licenças)" else "Licença Homologada ($tierCode)",
            isDeviceBound = deviceBoundPart != null && deviceBoundPart != "ANY",
            licensesCount = parsedLicensesCount,
            associationName = if (tier == LicenseTier.ASSOCIATION) "Associação de Apicultores" else ""
        )
    }

    /**
     * Generates a valid algorithmic license key for the administrator to deliver to customers.
     */
    fun generateAdminKey(
        tier: LicenseTier,
        validDaysFromNow: Int,
        targetDeviceId: String? = null,
        licensesCount: Int = 1,
        associationName: String = ""
    ): String {
        val tierCode = when (tier) {
            LicenseTier.STANDARD -> "STD"
            LicenseTier.PRO -> "PRO"
            LicenseTier.ASSOCIATION -> if (licensesCount > 1) "ASSO$licensesCount" else "ASSO"
            LicenseTier.ENTERPRISE -> "ENT"
            LicenseTier.LIFETIME -> "LIFE"
            LicenseTier.TRIAL -> "PRO"
        }

        val epochDays = if (tier == LicenseTier.LIFETIME || validDaysFromNow >= 9999) {
            0xFFFFL
        } else {
            val targetTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(validDaysFromNow.toLong())
            TimeUnit.MILLISECONDS.toDays(targetTime)
        }

        val daysHex = epochDays.toString(16).uppercase(Locale.ROOT).padStart(4, '0')
        val devPart = if (!targetDeviceId.isNullOrBlank() && targetDeviceId.length >= 4) {
            targetDeviceId.take(4).uppercase(Locale.ROOT)
        } else {
            "ANY"
        }

        val checksum = generateChecksum(tierCode, daysHex, devPart)

        return if (devPart == "ANY") {
            "APIS-$tierCode-$daysHex-$checksum"
        } else {
            "APIS-$tierCode-$daysHex-$checksum-$devPart"
        }
    }

    private fun generateChecksum(tierCode: String, daysHex: String, devPart: String): String {
        val raw = "$LICENSE_SALT:$tierCode:$daysHex:$devPart:$LICENSE_SALT"
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(raw.toByteArray(Charsets.UTF_8))
        val hex = bytes.joinToString("") { "%02X".format(it) }
        return hex.substring(0, 4)
    }
}

sealed class KeyValidationResult {
    data class Valid(
        val tier: LicenseTier,
        val expiryMillis: Long,
        val isLifetime: Boolean,
        val owner: String,
        val isDeviceBound: Boolean,
        val licensesCount: Int = 1,
        val associationName: String = ""
    ) : KeyValidationResult()

    data class Expired(val reason: String) : KeyValidationResult()
    data class Invalid(val reason: String) : KeyValidationResult()
}
