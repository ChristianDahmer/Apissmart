package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.HiveEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerDialog(
    hives: List<HiveEntity>,
    apiaries: List<ApiaryEntity>,
    onDismiss: () -> Unit,
    onHiveScanned: (HiveEntity) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("qr_scanner_dialog")
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
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Leitor de QR da Caixa",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Aponte para o QR Code fixado na frente da colmeia ou selecione a caixa abaixo:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Scanner Viewfinder
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(90.dp)
                    )

                    // Laser line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .offset(y = (laserOffset - 90).dp)
                            .background(Color(0xFF22C55E))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Colmeias Detectadas no Raio do Apiário:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // List of selectable quick-scan hives
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(hives) { hive ->
                        val apiary = apiaries.find { it.id == hive.apiaryId }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onHiveScanned(hive)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Hive,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = hive.hiveNumber,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${apiary?.name ?: "Apiário"} • Tag: ${hive.qrCode.ifBlank { "APIS-A${hive.apiaryId}-${hive.hiveNumber}" }}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                QueenColorBadge(colorYear = hive.queenColor)
                            }
                        }
                    }
                }
            }
        }
    }
}
