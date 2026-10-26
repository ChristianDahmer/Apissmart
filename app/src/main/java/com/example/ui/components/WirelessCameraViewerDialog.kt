package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WirelessCameraViewerDialog(
    hiveNumber: String = "01",
    onDismiss: () -> Unit,
    onPhotoCaptured: (photoUri: String, category: String, caption: String) -> Unit
) {
    var cameraSource by remember { mutableStateOf("Câmera Endoscópica USB/OTG") }
    var connectionStatus by remember { mutableStateOf("Conectado (1080p 30fps)") }
    var isConnected by remember { mutableStateOf(true) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var isLedOn by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("Ninho & Postura") }
    var isCapturing by remember { mutableStateOf(false) }
    var captureSuccessMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(HoneyGoldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = HoneyGoldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "📷 Câmera Sem Fio / Endoscópica",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Inspeção Visual Externa e Interna (Colmeia $hiveNumber)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Source Selector Chips
                Text(
                    text = "Fonte de Vídeo Conectada:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("USB/OTG", "Wi-Fi IP", "Bluetooth", "Padrão").forEach { src ->
                        val isSelected = cameraSource.contains(src, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                cameraSource = when(src) {
                                    "USB/OTG" -> "Câmera Endoscópica USB/OTG"
                                    "Wi-Fi IP" -> "Câmera Wi-Fi IP (RTSP/MJPEG)"
                                    "Bluetooth" -> "Câmera Borescope Bluetooth"
                                    else -> "Câmera do Dispositivo"
                                }
                                connectionStatus = "Conectado ($src)"
                            },
                            label = { Text(src, fontSize = 12.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Viewfinder Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        )
                        .border(2.dp, HoneyGoldPrimary, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Simulated Live Camera Feed Content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🐝 [TRANSMISSÃO AO VIVO - ${cameraSource.uppercase()}]",
                            style = MaterialTheme.typography.labelSmall,
                            color = HoneyGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Dynamic Mock Feed Visual
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MeadowGreenSecondary.copy(alpha = 0.3f * pulseAlpha)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (selectedCategory) {
                                    "Ninho & Postura" -> "🥚"
                                    "Melgueira & Mel" -> "🍯"
                                    "Rainha & Marcação" -> "👑"
                                    "Sanidade & Diagnóstico" -> "🔬"
                                    else -> "📦"
                                },
                                fontSize = 48.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Foco: Quadro de Cria #${hiveNumber} • Zoom: ${String.format(java.util.Locale.US, "%.1f", zoomLevel)}x",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = if (isLedOn) "💡 LED Endoscópio: Ligado (Alto Brilho)" else "💡 LED Endoscópio: Desligado",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }

                    // Top overlay badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.Green)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = connectionStatus,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "HD 1080p",
                                style = MaterialTheme.typography.labelSmall,
                                color = HoneyGoldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Capture Success Overlay
                    if (captureSuccessMessage != null) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.85f),
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.Green,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = captureSuccessMessage ?: "",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Camera Controls Row (Zoom, LED)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (zoomLevel > 1.0f) zoomLevel -= 0.5f }) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "Diminuir Zoom")
                        }
                        Text(
                            text = "Zoom: ${String.format(java.util.Locale.US, "%.1f", zoomLevel)}x",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { if (zoomLevel < 4.0f) zoomLevel += 0.5f }) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Aumentar Zoom")
                        }
                    }

                    FilledTonalButton(
                        onClick = { isLedOn = !isLedOn },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isLedOn) HoneyGoldPrimary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = if (isLedOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isLedOn) "LED Ligado" else "LED Desligado")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Selection for Capture
                Text(
                    text = "Categoria da Foto:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HIVE_PHOTO_CATEGORIES.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Capture Button
                Button(
                    onClick = {
                        isCapturing = true
                        scope.launch {
                            delay(400)
                            val sampleUri = when (selectedCategory) {
                                "Ninho & Postura" -> "sample://comb_uniform_brood"
                                "Melgueira & Mel" -> "sample://honey_supers"
                                "Rainha & Marcação" -> "sample://queen_marked"
                                "Sanidade & Diagnóstico" -> "sample://varroa_alert"
                                else -> "sample://hive_box_general"
                            }
                            captureSuccessMessage = "📸 Foto capturada via câmera sem fio e salva com sucesso!"
                            onPhotoCaptured(sampleUri, selectedCategory, "Captura com ${cameraSource} (Zoom ${zoomLevel}x)")
                            delay(1200)
                            isCapturing = false
                            captureSuccessMessage = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Salvando Foto...")
                    } else {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tirar Foto com Câmera Sem Fio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
