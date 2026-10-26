package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Calendar

data class QueenColorItem(
    val colorName: String,
    val hexColor: Color,
    val textColor: Color,
    val finalDigits: String,
    val recentYears: String,
    val isCurrentYear: Boolean = false
)

@Composable
fun QueenColorTableDialog(
    onDismiss: () -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val lastDigit = currentYear % 10

    val colorList = listOf(
        QueenColorItem(
            colorName = "Branco (White)",
            hexColor = Color(0xFFF8FAFC),
            textColor = Color(0xFF0F172A),
            finalDigits = "1 e 6",
            recentYears = "2021, 2026, 2031",
            isCurrentYear = lastDigit == 1 || lastDigit == 6
        ),
        QueenColorItem(
            colorName = "Amarelo (Yellow)",
            hexColor = Color(0xFFEAB308),
            textColor = Color(0xFF78350F),
            finalDigits = "2 e 7",
            recentYears = "2022, 2027, 2032",
            isCurrentYear = lastDigit == 2 || lastDigit == 7
        ),
        QueenColorItem(
            colorName = "Vermelho (Red)",
            hexColor = Color(0xFFEF4444),
            textColor = Color.White,
            finalDigits = "3 e 8",
            recentYears = "2023, 2028, 2033",
            isCurrentYear = lastDigit == 3 || lastDigit == 8
        ),
        QueenColorItem(
            colorName = "Verde (Green)",
            hexColor = Color(0xFF22C55E),
            textColor = Color.White,
            finalDigits = "4 e 9",
            recentYears = "2024, 2029, 2034",
            isCurrentYear = lastDigit == 4 || lastDigit == 9
        ),
        QueenColorItem(
            colorName = "Azul (Blue)",
            hexColor = Color(0xFF3B82F6),
            textColor = Color.White,
            finalDigits = "5 e 0",
            recentYears = "2025, 2030, 2035",
            isCurrentYear = lastDigit == 5 || lastDigit == 0
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "👑 Código de Cores de Rainhas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Padrão Internacional Apícola (Ciclo de 5 Anos)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ano atual ($currentYear): Marque as novas rainhas com a cor oficial para identificar a idade exata da matriz nas inspeções.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                colorList.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isCurrentYear)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(item.hexColor),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.isCurrentYear) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Ano Atual",
                                        tint = item.textColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.colorName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (item.isCurrentYear) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "★ ANO VIGENTE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Text(
                                    text = "Dígitos finais: ${item.finalDigits} • Ex: ${item.recentYears}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Entendido")
                }
            }
        }
    }
}
