package com.example.license

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Resultado da consulta e validação de CNPJ via Brasil API.
 */
data class CnpjAssociationVerificationResult(
    val isValidFormat: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val cnpjRaw: String = "",
    val cnpjFormatted: String = "",
    val razaoSocial: String = "",
    val nomeFantasia: String = "",
    val situacaoCadastral: String = "",
    val naturezaJuridica: String = "",
    val cnaeFiscal: String = "",
    val cnaeFiscalDescricao: String = "",
    val cnaesSecundarios: List<String> = emptyList(),
    val municipio: String = "",
    val uf: String = "",
    val logradouro: String = "",
    val numero: String = "",
    val complemento: String = "",
    val bairro: String = "",
    val cep: String = "",
    val telefone: String = "",
    val email: String = "",
    val fullAddress: String = "",
    val preferredName: String = "",
    val isAssociationOrCoop: Boolean = false,
    val isApicultureRelated: Boolean = false,
    val isConfirmedBeekeeperAssociation: Boolean = false,
    val statusBadgeText: String = "",
    val statusMessage: String = "",
    val errorMessage: String? = null
)

/**
 * Serviço responsável por consultar a Brasil API (https://brasilapi.com.br/api/cnpj/v1/{cnpj})
 * e fornecer validação e autopreenchimento de CNPJ/CPF para licenças e emissão de recibos.
 */
object BrasilApiCnpjService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Aplica máscara de CNPJ (00.000.000/0000-00)
     */
    fun formatCnpj(input: String): String {
        val clean = input.filter { it.isDigit() }.take(14)
        val sb = StringBuilder()
        for (i in clean.indices) {
            when (i) {
                2, 5 -> sb.append(".")
                8 -> sb.append("/")
                12 -> sb.append("-")
            }
            sb.append(clean[i])
        }
        return sb.toString()
    }

    /**
     * Aplica máscara de CPF (000.000.000-00)
     */
    fun formatCpf(input: String): String {
        val clean = input.filter { it.isDigit() }.take(11)
        val sb = StringBuilder()
        for (i in clean.indices) {
            when (i) {
                3, 6 -> sb.append(".")
                9 -> sb.append("-")
            }
            sb.append(clean[i])
        }
        return sb.toString()
    }

    /**
     * Formata documento de forma inteligente (CPF se <= 11 dígitos, CNPJ se > 11 dígitos)
     */
    fun formatDocument(input: String): String {
        val clean = input.filter { it.isDigit() }
        return if (clean.length > 11) formatCnpj(clean) else formatCpf(clean)
    }

    /**
     * Valida dígito verificador básico de CNPJ
     */
    fun isValidCnpjDigits(digits: String): Boolean {
        val clean = digits.filter { it.isDigit() }
        if (clean.length != 14) return false
        if (clean.all { it == clean[0] }) return false

        try {
            val weights1 = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
            val weights2 = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)

            var sum1 = 0
            for (i in 0..11) {
                sum1 += (clean[i] - '0') * weights1[i]
            }
            val rem1 = sum1 % 11
            val d1 = if (rem1 < 2) 0 else 11 - rem1
            if (clean[12] - '0' != d1) return false

            var sum2 = 0
            for (i in 0..12) {
                sum2 += (clean[i] - '0') * weights2[i]
            }
            val rem2 = sum2 % 11
            val d2 = if (rem2 < 2) 0 else 11 - rem2
            return clean[13] - '0' == d2
        } catch (_: Exception) {
            return false
        }
    }

    /**
     * Valida dígito verificador oficial de CPF (módulo 11)
     */
    fun isValidCpfDigits(input: String): Boolean {
        val clean = input.filter { it.isDigit() }
        if (clean.length != 11) return false
        if (clean.all { it == clean[0] }) return false

        try {
            var sum1 = 0
            for (i in 0..8) {
                sum1 += (clean[i] - '0') * (10 - i)
            }
            var rem1 = (sum1 * 10) % 11
            if (rem1 == 10) rem1 = 0
            if (clean[9] - '0' != rem1) return false

            var sum2 = 0
            for (i in 0..9) {
                sum2 += (clean[i] - '0') * (11 - i)
            }
            var rem2 = (sum2 * 10) % 11
            if (rem2 == 10) rem2 = 0
            return clean[10] - '0' == rem2
        } catch (_: Exception) {
            return false
        }
    }

    /**
     * Consulta a Brasil API e analisa dados da empresa/entidade jurídica.
     */
    suspend fun consultAndValidateAssociation(cnpjInput: String): CnpjAssociationVerificationResult = withContext(Dispatchers.IO) {
        val cleanCnpj = cnpjInput.filter { it.isDigit() }

        if (cleanCnpj.length != 14) {
            return@withContext CnpjAssociationVerificationResult(
                isValidFormat = false,
                isSuccess = false,
                cnpjRaw = cleanCnpj,
                cnpjFormatted = formatCnpj(cleanCnpj),
                errorMessage = "CNPJ deve conter exatamente 14 dígitos numéricos."
            )
        }

        val formatted = formatCnpj(cleanCnpj)
        val url = "https://brasilapi.com.br/api/cnpj/v1/$cleanCnpj"

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ApisSmart-Android/4.44")
                .header("Accept", "application/json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val code = response.code
            val bodyString = response.body?.string().orEmpty()

            if (code == 404) {
                return@withContext CnpjAssociationVerificationResult(
                    isValidFormat = true,
                    isSuccess = false,
                    cnpjRaw = cleanCnpj,
                    cnpjFormatted = formatted,
                    errorMessage = "CNPJ $formatted não foi localizado na Receita Federal."
                )
            }

            if (code != 200 || bodyString.isBlank()) {
                val errorMsg = try {
                    JSONObject(bodyString).optString("message", "Erro $code ao consultar Brasil API.")
                } catch (_: Exception) {
                    "Erro de comunicação com a Receita Federal / Brasil API (HTTP $code)."
                }
                return@withContext CnpjAssociationVerificationResult(
                    isValidFormat = true,
                    isSuccess = false,
                    cnpjRaw = cleanCnpj,
                    cnpjFormatted = formatted,
                    errorMessage = errorMsg
                )
            }

            val json = JSONObject(bodyString)

            val razaoSocial = json.optString("razao_social", "").trim()
            val nomeFantasia = json.optString("nome_fantasia", "").trim()
            val situacao = json.optString("descricao_situacao_cadastral", "").trim()
            val naturezaJuridica = json.optString("natureza_juridica", "").trim()
            val cnaeFiscal = json.optString("cnae_fiscal", "").trim()
            val cnaeDescricao = json.optString("cnae_fiscal_descricao", "").trim()
            val municipio = json.optString("municipio", "").trim()
            val uf = json.optString("uf", "").trim()

            // Endereço e Contato detalhado para autopreenchimento no Recibo
            val tipoLogradouro = json.optString("descricao_tipo_de_logradouro", "").trim()
            val logradouro = json.optString("logradouro", "").trim()
            val numero = json.optString("numero", "").trim()
            val complemento = json.optString("complemento", "").trim()
            val bairro = json.optString("bairro", "").trim()
            val cepRaw = json.optString("cep", "").trim()
            val cepFormatted = if (cepRaw.length == 8) "${cepRaw.substring(0, 5)}-${cepRaw.substring(5)}" else cepRaw

            val tel1 = json.optString("ddd_telefone_1", "").trim()
            val tel2 = json.optString("ddd_telefone_2", "").trim()
            val telefone = tel1.ifBlank { tel2 }
            val email = json.optString("email", "").trim()

            val streetPart = listOf(tipoLogradouro, logradouro, numero).filter { it.isNotBlank() }.joinToString(" ")
            val addrParts = listOf(streetPart, complemento, bairro, "$municipio - $uf", if (cepFormatted.isNotBlank()) "CEP $cepFormatted" else "")
                .filter { it.isNotBlank() }
            val fullAddress = addrParts.joinToString(", ")

            val preferredName = nomeFantasia.ifBlank { razaoSocial }

            val secondaryCnaes = mutableListOf<String>()
            val secArray = json.optJSONArray("cnaes_secundarios")
            if (secArray != null) {
                for (i in 0 until secArray.length()) {
                    val item = secArray.optJSONObject(i)
                    if (item != null) {
                        val desc = item.optString("descricao", "")
                        if (desc.isNotBlank()) secondaryCnaes.add(desc)
                    }
                }
            }

            // 1. Verificação se é Associação ou Cooperativa
            val natureNorm = naturezaJuridica.uppercase()
            val razaoNorm = razaoSocial.uppercase()
            val fantasiaNorm = nomeFantasia.uppercase()

            val isAssocNature = natureNorm.contains("ASSOCIA") ||
                    natureNorm.contains("COOPERATIVA") ||
                    natureNorm.contains("ENTIDADE") ||
                    natureNorm.contains("399-9") ||
                    natureNorm.contains("214-3")

            val isAssocName = razaoNorm.contains("ASSOCIACAO") ||
                    razaoNorm.contains("ASSOCIAÇÃO") ||
                    razaoNorm.contains("COOPERATIVA") ||
                    razaoNorm.contains("SINDICATO") ||
                    razaoNorm.contains("SOCIEDADE APICOLA") ||
                    razaoNorm.contains("SOCIEDADE APÍCOLA") ||
                    fantasiaNorm.contains("ASSOCIACAO") ||
                    fantasiaNorm.contains("ASSOCIAÇÃO") ||
                    fantasiaNorm.contains("COOPERATIVA")

            val isAssociationOrCoop = isAssocNature || isAssocName

            // 2. Verificação de Apicultura
            val beekeepingKeywords = listOf(
                "APICUL", "APÍCUL", "ABELHA", "MELIPON", "MEL", "APIARIO", "APIÁRIO",
                "POLEN", "PÓLEN", "PROPOLIS", "PRÓPOLIS", "COLMEIA", "CRIAÇÃO DE ABELHAS",
                "CRIACAO DE ABELHAS"
            )

            val allTextsToSearch = (
                    razaoNorm + " " +
                    fantasiaNorm + " " +
                    cnaeDescricao.uppercase() + " " +
                    secondaryCnaes.joinToString(" ").uppercase()
            )

            val isCnaeApicultura = cnaeDescricao.uppercase().contains("APICULTURA") ||
                    cnaeFiscal == "0159801" || cnaeFiscal == "159801" || cnaeFiscal == "0159-8/01"

            val containsBeekeepingKeyword = beekeepingKeywords.any { allTextsToSearch.contains(it) }
            val isApicultureRelated = isCnaeApicultura || containsBeekeepingKeyword

            val isAtiva = situacao.equals("ATIVA", ignoreCase = true)
            val isConfirmedBeekeeperAssoc = isAssociationOrCoop && isApicultureRelated && isAtiva

            val badgeText: String
            val statusMsg: String

            when {
                !isAtiva -> {
                    badgeText = "SITUAÇÃO: $situacao ⚠️"
                    statusMsg = "O CNPJ foi localizado, porém sua situação na Receita Federal é '$situacao'."
                }
                isConfirmedBeekeeperAssoc -> {
                    badgeText = "ASSOCIAÇÃO DE APICULTORES CONFIRMADA ✅"
                    statusMsg = "Entidade ativa e confirmada no ramo apícola via Receita Federal / Brasil API."
                }
                isAssociationOrCoop -> {
                    badgeText = "ASSOCIAÇÃO / COOPERATIVA AGROPECUÁRIA REGISTRADA ℹ️"
                    statusMsg = "CNPJ ativo de natureza associativa ($naturezaJuridica)."
                }
                isApicultureRelated -> {
                    badgeText = "EMPRESA DO RAMO APÍCOLA 🐝"
                    statusMsg = "CNPJ com atividade apícola identificada."
                }
                else -> {
                    badgeText = "CNPJ ATIVO ENCONTRADO ✅"
                    statusMsg = "Dados cadastrais recuperados da Receita Federal."
                }
            }

            return@withContext CnpjAssociationVerificationResult(
                isValidFormat = true,
                isSuccess = true,
                cnpjRaw = cleanCnpj,
                cnpjFormatted = formatted,
                razaoSocial = razaoSocial,
                nomeFantasia = nomeFantasia,
                situacaoCadastral = situacao,
                naturezaJuridica = naturezaJuridica,
                cnaeFiscal = cnaeFiscal,
                cnaeFiscalDescricao = cnaeDescricao,
                cnaesSecundarios = secondaryCnaes,
                municipio = municipio,
                uf = uf,
                logradouro = logradouro,
                numero = numero,
                complemento = complemento,
                bairro = bairro,
                cep = cepFormatted,
                telefone = telefone,
                email = email,
                fullAddress = fullAddress,
                preferredName = preferredName,
                isAssociationOrCoop = isAssociationOrCoop,
                isApicultureRelated = isApicultureRelated,
                isConfirmedBeekeeperAssociation = isConfirmedBeekeeperAssoc,
                statusBadgeText = badgeText,
                statusMessage = statusMsg,
                errorMessage = null
            )
        } catch (e: Exception) {
            return@withContext CnpjAssociationVerificationResult(
                isValidFormat = true,
                isSuccess = false,
                cnpjRaw = cleanCnpj,
                cnpjFormatted = formatted,
                errorMessage = "Falha ao consultar Receita Federal: ${e.localizedMessage ?: "Verifique sua conexão de internet."}"
            )
        }
    }
}
