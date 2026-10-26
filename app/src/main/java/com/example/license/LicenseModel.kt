package com.example.license

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Status of the application license outside of Google Play.
 */
enum class LicenseStatus(val label: String, val badgeEmoji: String) {
    ACTIVE("Licença Ativa", "✅"),
    TRIAL("Período de Demonstração", "⏳"),
    GRACE_PERIOD("Modo Tolerância (Offline)", "⚠️"),
    EXPIRED("Licença Expirada / Bloqueada", "🔒")
}

/**
 * Tier of the application license.
 */
enum class LicenseTier(val displayName: String, val maxHives: Int, val description: String = "") {
    TRIAL("Demonstração (30 dias)", 10, "Acesso inicial para avaliação"),
    STANDARD("Padrão (Até 50 colmeias)", 50, "Para pequenos produtores"),
    PRO("Profissional (Ilimitado)", -1, "Produtores profissionais sem limite de colmeias"),
    ASSOCIATION("Associação de Apicultores", -1, "Pacote multi-licenças para grupos e associações"),
    ENTERPRISE("Cooperativa & Agro", -1, "Cooperativas e agroindústrias"),
    LIFETIME("Vitalícia Permanente", -1, "Acesso vitalício sem mensalidade")
}

/**
 * Complete license information held in the app.
 */
data class LicenseData(
    val status: LicenseStatus = LicenseStatus.TRIAL,
    val tier: LicenseTier = LicenseTier.TRIAL,
    val licenseKey: String = "",
    val ownerName: String = "Produtor Apícola",
    val registeredEmail: String = "",
    val deviceId: String = "",
    val activationDateMillis: Long = 0L,
    val expiryDateMillis: Long = 0L,
    val lastServerCheckMillis: Long = 0L,
    val serverVerificationUrl: String = "https://licenca.apissmart.com.br/api/v1/verify",
    val isActivatedOffline: Boolean = false,
    val adminNotes: String = "",
    val associationName: String = "",
    val licensesCount: Int = 1,
    val billingCycle: String = "MENSAL"
) {
    companion object {
        const val PRICE_MONTHLY_INITIAL = 14.90
        const val PRICE_ANNUAL_INITIAL = 149.90

        fun calculateMonthlyTotal(licenses: Int): Double = licenses.coerceAtLeast(1) * PRICE_MONTHLY_INITIAL
        fun calculateAnnualTotal(licenses: Int): Double = licenses.coerceAtLeast(1) * PRICE_ANNUAL_INITIAL
    }
    val isExpired: Boolean
        get() {
            if (tier == LicenseTier.LIFETIME) return false
            if (expiryDateMillis <= 0L) return status == LicenseStatus.EXPIRED
            return System.currentTimeMillis() > expiryDateMillis
        }

    val daysRemaining: Int
        get() {
            if (tier == LicenseTier.LIFETIME) return 9999
            if (expiryDateMillis <= 0L) return 0
            val diff = expiryDateMillis - System.currentTimeMillis()
            return if (diff > 0) (diff / (1000L * 60 * 60 * 24)).toInt() + 1 else 0
        }

    val formattedExpiry: String
        get() {
            if (tier == LicenseTier.LIFETIME) return "Vitalícia (Sem expiração)"
            if (expiryDateMillis <= 0L) return "Não definida"
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date(expiryDateMillis))
        }

    val formattedLastCheck: String
        get() {
            if (lastServerCheckMillis <= 0L) return "Nunca realizada"
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(lastServerCheckMillis))
        }

    val canUseApp: Boolean
        get() = status == LicenseStatus.ACTIVE || status == LicenseStatus.TRIAL || status == LicenseStatus.GRACE_PERIOD

    val isLifetime: Boolean
        get() = tier == LicenseTier.LIFETIME
}

/**
 * Result of remote server license verification.
 */
data class ServerVerificationResult(
    val isSuccess: Boolean,
    val message: String,
    val updatedStatus: LicenseStatus? = null,
    val updatedTier: LicenseTier? = null,
    val updatedExpiryMillis: Long? = null,
    val serverTimestamp: Long = System.currentTimeMillis()
)
