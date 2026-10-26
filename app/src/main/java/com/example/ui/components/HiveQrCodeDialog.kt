package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.HiveEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveQrCodeDialog(
    hive: HiveEntity,
    apiary: ApiaryEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val qrTag = hive.qrCode.ifBlank { "APIS-A${hive.apiaryId}-${hive.hiveNumber}" }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("hive_qr_dialog")
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Etiqueta QR Code da Caixa",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Printable Tag Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "APISSMART GESTÃO APÍCOLA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = hive.hiveNumber,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = apiary?.name ?: "Apiário Principal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF334155)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Procedural Clean QR-Code Graphic
                        Canvas(
                            modifier = Modifier
                                .size(160.dp)
                                .background(Color.White)
                                .padding(8.dp)
                        ) {
                            val gridSize = 15
                            val cellSize = size.width / gridSize
                            val seed = qrTag.hashCode()

                            for (row in 0 until gridSize) {
                                for (col in 0 until gridSize) {
                                    val isCorner = (row < 4 && col < 4) || (row < 4 && col >= gridSize - 4) || (row >= gridSize - 4 && col < 4)
                                    val isPattern = isCorner || ((seed * (row * 31 + col * 17)) % 7 in listOf(0, 1, 3))
                                    if (isPattern) {
                                        drawRect(
                                            color = Color(0xFF0F172A),
                                            topLeft = Offset(col * cellSize, row * cellSize),
                                            size = Size(cellSize - 0.5f, cellSize - 0.5f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "CÓDIGO: $qrTag",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            QueenColorBadge(colorYear = hive.queenColor)
                            HealthBadge(status = hive.healthStatus)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val shareText = """
                            🏷️ *ETIQUETA APISSMART - IDENTIFICAÇÃO DE COLMEIA*
                            🐝 Colmeia: ${hive.hiveNumber}
                            🏡 Apiário: ${apiary?.name ?: "Apiário Principal"}
                            👑 Rainha: Cor ${hive.queenColor} (${hive.queenLineage})
                            📌 Tipo: ${hive.hiveType}
                            🔖 Tag QR: $qrTag
                        """.trimIndent()

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            putExtra(Intent.EXTRA_SUBJECT, "Etiqueta ${hive.hiveNumber}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Etiqueta"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartilhar / Imprimir Etiqueta")
                }
            }
        }
    }
}
