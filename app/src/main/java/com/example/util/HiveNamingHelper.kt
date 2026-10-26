package com.example.util

import com.example.data.local.entities.HiveEntity
import java.util.Locale

/**
 * Utilitário para geração de numeração incremental e validação de nomes/números de colmeias.
 */
object HiveNamingHelper {

    /**
     * Gera automaticamente o próximo número de colmeia incrementado a partir de uma colmeia base,
     * garantindo que o número gerado não colida com nenhuma colmeia já existente.
     *
     * Exemplos:
     * - "Colmeia 01" -> "Colmeia 02" (mantém padding de 2 dígitos)
     * - "CX-09" -> "CX-10"
     * - "15" -> "16"
     * - "Jataí #3" -> "Jataí #4"
     * - "Colmeia Alfa" -> "Colmeia Alfa 2"
     */
    fun generateNextHiveNumber(baseHiveNumber: String, existingHiveNumbers: List<String>): String {
        val trimmed = baseHiveNumber.trim()
        val existingNormalized = existingHiveNumbers.map { it.trim().lowercase(Locale.ROOT) }.toSet()

        // 1. Tenta identificar padrão com dígitos no final (ex: "CX-01", "Colmeia 5", "10")
        val trailingDigitRegex = Regex("^(.*?)(\\d+)$")
        val match = trailingDigitRegex.find(trimmed)

        if (match != null) {
            val prefix = match.groupValues[1]
            val digits = match.groupValues[2]
            val padLength = digits.length
            var nextNum = (digits.toLongOrNull() ?: 1L) + 1L

            while (true) {
                val formattedDigits = if (padLength > 1) {
                    String.format(Locale.US, "%0${padLength}d", nextNum)
                } else {
                    nextNum.toString()
                }
                val candidate = "$prefix$formattedDigits"
                if (!existingNormalized.contains(candidate.lowercase(Locale.ROOT))) {
                    return candidate
                }
                nextNum++
            }
        }

        // 2. Caso o nome não termine com dígitos (ex: "Colmeia Alfa", "Caixa Didática")
        var counter = 2L
        while (true) {
            val candidate = "$trimmed $counter"
            if (!existingNormalized.contains(candidate.lowercase(Locale.ROOT))) {
                return candidate
            }
            counter++
        }
    }

    /**
     * Verifica se o nome/número já está sendo utilizado por outra colmeia.
     */
    fun isHiveNumberDuplicate(
        candidateNumber: String,
        existingHives: List<HiveEntity>,
        currentHiveId: Long = 0L,
        isCloning: Boolean = false
    ): Boolean {
        val trimmedCandidate = candidateNumber.trim().lowercase(Locale.ROOT)
        if (trimmedCandidate.isBlank()) return false

        return existingHives.any { hive ->
            val isDifferent = if (isCloning) true else (hive.id != currentHiveId)
            isDifferent && hive.hiveNumber.trim().lowercase(Locale.ROOT) == trimmedCandidate
        }
    }
}
