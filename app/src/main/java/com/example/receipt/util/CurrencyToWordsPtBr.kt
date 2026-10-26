package com.example.receipt.util

import java.util.Locale
import kotlin.math.roundToLong

/**
 * Utility to convert Brazilian Real currency values into full Portuguese words.
 * Example: 154.50 -> "Cento e cinquenta e quatro reais e cinquenta centavos"
 */
object CurrencyToWordsPtBr {

    private val UNITS = arrayOf(
        "", "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove",
        "dez", "onze", "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete",
        "dezoito", "dezenove"
    )

    private val TENS = arrayOf(
        "", "", "vinte", "trinta", "quarenta", "cinquenta", "sessenta", "setenta", "oitenta", "noventa"
    )

    private val HUNDREDS = arrayOf(
        "", "cento", "duzentos", "trezentos", "quatrocentos", "quinhentos",
        "seiscentos", "setecentos", "oitocentos", "novecentos"
    )

    fun convert(value: Double): String {
        if (value <= 0.0) return "Zero reais"

        val totalCents = (value * 100).roundToLong()
        val reais = totalCents / 100
        val cents = totalCents % 100

        val parts = mutableListOf<String>()

        if (reais > 0) {
            val reaisText = convertIntegerToWords(reais)
            val reaisSuffix = if (reais == 1L) "real" else "reais"
            parts.add("$reaisText $reaisSuffix")
        }

        if (cents > 0) {
            val centsText = convertIntegerToWords(cents)
            val centsSuffix = if (cents == 1L) "centavo" else "centavos"
            parts.add("$centsText $centsSuffix")
        }

        val result = parts.joinToString(" e ")
        return result.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    private fun convertIntegerToWords(number: Long): String {
        if (number == 0L) return "zero"
        if (number == 100L) return "cem"

        val millions = number / 1_000_000
        val thousands = (number % 1_000_000) / 1000
        val remainder = number % 1000

        val segments = mutableListOf<String>()

        if (millions > 0) {
            val millionsText = convertUpTo999(millions.toInt())
            val suffix = if (millions == 1L) "milhão" else "milhões"
            segments.add("$millionsText $suffix")
        }

        if (thousands > 0) {
            if (thousands == 1L) {
                segments.add("mil")
            } else {
                val thousandsText = convertUpTo999(thousands.toInt())
                segments.add("$thousandsText mil")
            }
        }

        if (remainder > 0) {
            val remainderText = convertUpTo999(remainder.toInt())
            segments.add(remainderText)
        }

        return segments.joinToString(" e ")
    }

    private fun convertUpTo999(n: Int): String {
        if (n == 100) return "cem"
        val h = n / 100
        val remainder = n % 100
        val t = remainder / 10
        val u = remainder % 10

        val words = mutableListOf<String>()

        if (h > 0) {
            words.add(HUNDREDS[h])
        }

        if (remainder in 1..19) {
            words.add(UNITS[remainder])
        } else if (remainder >= 20) {
            words.add(TENS[t])
            if (u > 0) {
                words.add(UNITS[u])
            }
        }

        return words.joinToString(" e ")
    }
}
