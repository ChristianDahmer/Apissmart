package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.WarningAlertRed

@Composable
fun HealthBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        "Excelente" -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        "Boa" -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        "Alerta Sanitário" -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        "Em Tratamento" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun QueenColorBadge(colorYear: String, modifier: Modifier = Modifier) {
    // Official International Queen Marking Colors
    // White (years ending in 1, 6 - e.g. 2026)
    // Yellow (years ending in 2, 7 - e.g. 2027)
    // Red (years ending in 3, 8 - e.g. 2028)
    // Green (years ending in 4, 9 - e.g. 2024)
    // Blue (years ending in 5, 0 - e.g. 2025)
    val dotColor = when {
        colorYear.contains("Branco", ignoreCase = true) -> Color.White
        colorYear.contains("Amarelo", ignoreCase = true) -> Color(0xFFEAB308)
        colorYear.contains("Vermelho", ignoreCase = true) -> Color(0xFFEF4444)
        colorYear.contains("Verde", ignoreCase = true) -> Color(0xFF22C55E)
        colorYear.contains("Azul", ignoreCase = true) -> Color(0xFF3B82F6)
        else -> Color.LightGray
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = " $colorYear",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PriorityBadge(priority: String, modifier: Modifier = Modifier) {
    val (bg, txt) = when (priority) {
        "URGENTE_SANITÁRIA" -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
        "ALTA" -> Color(0xFFFFEDD5) to Color(0xFFC2410C)
        "MÉDIA" -> Color(0xFFFEF9C3) to Color(0xFF854D0E)
        else -> Color(0xFFE2E8F0) to Color(0xFF475569)
    }

    val label = if (priority == "URGENTE_SANITÁRIA") "🚨 URGENTE SANITÁRIA" else priority

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = txt,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
