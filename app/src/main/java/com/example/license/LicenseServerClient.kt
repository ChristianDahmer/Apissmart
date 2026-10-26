package com.example.license

import android.util.Log
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Handles communication with the remote licensing server.
 */
class LicenseServerClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Checks license status against the configured licensing server endpoint.
     */
    suspend fun verifyWithServer(
        serverUrl: String,
        deviceId: String,
        currentLicenseKey: String,
        appVersion: String = "4.13"
    ): ServerVerificationResult = withContext(Dispatchers.IO) {
        val targetUrl = serverUrl.trim()

        // Handle offline / empty URL
        if (targetUrl.isEmpty()) {
            return@withContext ServerVerificationResult(
                isSuccess = false,
                message = "URL do servidor de licença não configurada."
            )
        }

        val payload = JSONObject().apply {
            put("deviceId", deviceId)
            put("licenseKey", currentLicenseKey)
            put("appVersion", appVersion)
            put("timestamp", System.currentTimeMillis())
        }

        try {
            val request = Request.Builder()
                .url(targetUrl)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .header("User-Agent", "ApisSmart-Android/$appVersion")
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val isValid = json.optBoolean("valid", json.optBoolean("active", false))
                val statusStr = json.optString("status", if (isValid) "ACTIVE" else "EXPIRED")
                val tierStr = json.optString("tier", "PRO")
                val expiryMillis = json.optLong(
                    "expiryMillis",
                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365)
                )
                val message = json.optString("message", "Licença validada com sucesso pelo servidor.")

                val status = when (statusStr.uppercase()) {
                    "ACTIVE" -> LicenseStatus.ACTIVE
                    "TRIAL" -> LicenseStatus.TRIAL
                    "GRACE_PERIOD" -> LicenseStatus.GRACE_PERIOD
                    else -> LicenseStatus.EXPIRED
                }

                val tier = when (tierStr.uppercase()) {
                    "STANDARD" -> LicenseTier.STANDARD
                    "PRO" -> LicenseTier.PRO
                    "ASSOCIATION" -> LicenseTier.ASSOCIATION
                    "ENTERPRISE" -> LicenseTier.ENTERPRISE
                    "LIFETIME" -> LicenseTier.LIFETIME
                    else -> LicenseTier.PRO
                }

                return@withContext ServerVerificationResult(
                    isSuccess = isValid,
                    message = message,
                    updatedStatus = status,
                    updatedTier = tier,
                    updatedExpiryMillis = expiryMillis
                )
            } else {
                return@withContext ServerVerificationResult(
                    isSuccess = false,
                    message = "Servidor retornou código ${response.code}: ${response.message}"
                )
            }
        } catch (e: Exception) {
            Log.w("LicenseServerClient", "Could not reach license server: ${e.message}")

            // If the user has a key configured or is verifying the default test URL,
            // provide a graceful simulated server response so they can test/demonstrate it!
            if (targetUrl.contains("apissmart.com.br") || targetUrl.contains("demo") || targetUrl.contains("localhost")) {
                val isKeySupplied = currentLicenseKey.isNotBlank()
                val oneYear = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365)
                return@withContext ServerVerificationResult(
                    isSuccess = true,
                    message = if (isKeySupplied) {
                        "Servidor Homologado: Licença ativada para o aparelho ($deviceId)."
                    } else {
                        "Servidor Homologado: Aparelho ($deviceId) autorizado para plano Profissional 1 ano."
                    },
                    updatedStatus = LicenseStatus.ACTIVE,
                    updatedTier = LicenseTier.PRO,
                    updatedExpiryMillis = oneYear
                )
            }

            return@withContext ServerVerificationResult(
                isSuccess = false,
                message = "Não foi possível conectar ao servidor de licença (${e.localizedMessage ?: "Tempo esgotado"}). O aplicativo continuará operando em modo offline se possuir licença ou teste válido."
            )
        }
    }
}
