package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import kotlin.math.roundToInt
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.HiveEntity
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.WarningAlertRed
import com.example.util.LocationHelper

/**
 * Interactive Map Dialog displaying a hive's location with a custom Hive Box Marker (Caixa de Colmeia)
 */
@Composable
fun HiveMapLocationDialog(
    hive: HiveEntity,
    apiary: ApiaryEntity?,
    otherHivesInApiary: List<HiveEntity> = emptyList(),
    onDismiss: () -> Unit,
    onUpdateGps: ((Double, Double) -> Unit)? = null
) {
    val context = LocalContext.current
    var showFlightMap by remember { mutableStateOf(true) }
    var isNavigatingInApp by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }

    val lat = hive.latitude ?: apiary?.latitude
    val lon = hive.longitude ?: apiary?.longitude

    val baseLat = lat ?: -22.7394
    val baseLon = lon ?: -45.5913
    val mapState = rememberSlippyTileMapState(baseLat, baseLon, initialZoom = 15.5f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("hive_map_location_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HoneyGoldPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(HoneyGoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📦", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Mapa da Colmeia: ${hive.hiveNumber}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${apiary?.name ?: "Apiário"} • Esri World Imagery Satélite 🛰️",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), CircleShape)
                            .testTag("close_map_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Main Slippy Tile Map Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    SlippyTileMap(
                        state = mapState,
                        modifier = Modifier.fillMaxSize(),
                        overlayContent = { widthPx, heightPx, density ->
                            val hivePos = mapState.latLonToScreen(baseLat, baseLon, density, widthPx, heightPx)

                            // Geodesic Bee Flight Range and Corridors Canvas
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerX = hivePos.x
                                val centerY = hivePos.y

                                // Dynamically adjust radius based on bee species
                                val (ecoMeters, maxMeters) = when {
                                    hive.beeSpecies.contains("Jataí", ignoreCase = true) || hive.beeSpecies.contains("Trigona", ignoreCase = true) -> 250.0 to 500.0
                                    hive.beeSpecies.contains("Mandaçaia", ignoreCase = true) || hive.beeSpecies.contains("Melipona", ignoreCase = true) -> 500.0 to 1000.0
                                    else -> 1500.0 to 3000.0 // Default (Apis mellifera)
                                }

                                val ecoFlightRadius = MapTileMath.metersToPixels(ecoMeters, baseLat, mapState.zoomLevel, density)
                                val maxFlightRadius = MapTileMath.metersToPixels(maxMeters, baseLat, mapState.zoomLevel, density)

                                // --- MAPA DE VOO DAS ABELHAS ---
                                if (showFlightMap) {
                                    // 1. Raio Máximo de Forrageamento / Voo (3.0 km)
                                    drawCircle(
                                        color = Color(0x18F59E0B),
                                        radius = maxFlightRadius,
                                        center = Offset(centerX, centerY)
                                    )
                                    drawCircle(
                                        color = Color(0x99D97706),
                                        radius = maxFlightRadius,
                                        center = Offset(centerX, centerY),
                                        style = Stroke(
                                            width = 1.5.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                                        )
                                    )

                                    // 2. Raio Econômico de Coleta (1.5 km - pasto mais rentável)
                                    drawCircle(
                                        color = Color(0x2EF59E0B),
                                        radius = ecoFlightRadius,
                                        center = Offset(centerX, centerY)
                                    )
                                    drawCircle(
                                        color = Color(0xFFD97706),
                                        radius = ecoFlightRadius,
                                        center = Offset(centerX, centerY),
                                        style = Stroke(width = 2.dp.toPx())
                                    )

                                    // 3. Corredores / Rotas de Voo e Pasto Apícola
                                    val flightAngles = listOf(35f, 120f, 215f, 305f)
                                    for (angleDeg in flightAngles) {
                                        val rad = Math.toRadians(angleDeg.toDouble())
                                        val endX = centerX + (maxFlightRadius * Math.cos(rad)).toFloat()
                                        val endY = centerY + (maxFlightRadius * Math.sin(rad)).toFloat()

                                        drawLine(
                                            color = Color(0x99D97706),
                                            start = Offset(centerX, centerY),
                                            end = Offset(endX, endY),
                                            strokeWidth = 1.5.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                        )

                                        // Ponto de forrageamento / florada na ponta do vetor
                                        drawCircle(
                                            color = Color(0xFFD97706),
                                            radius = 4.dp.toPx(),
                                            center = Offset(endX, endY)
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = 2.dp.toPx(),
                                            center = Offset(endX, endY)
                                        )
                                    }
                                }

                                // Draw other hives in apiary (if any)
                                otherHivesInApiary.forEach { other ->
                                    if (other.id != hive.id && other.latitude != null && other.longitude != null) {
                                        val oPos = mapState.latLonToScreen(other.latitude, other.longitude, density, widthPx, heightPx)
                                        drawCircle(
                                            color = Color(0xFFD97706).copy(alpha = 0.8f),
                                            radius = 8.dp.toPx(),
                                            center = oPos
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = 6.dp.toPx(),
                                            center = oPos,
                                            style = Stroke(width = 1.5.dp.toPx())
                                        )
                                    }
                                }

                                // Draw navigation route line if active
                                if (isNavigatingInApp) {
                                    val startX = centerX - 120.dp.toPx()
                                    val startY = centerY + 100.dp.toPx()

                                    drawLine(
                                        color = Color(0xFF38BDF8),
                                        start = Offset(startX, startY),
                                        end = Offset(centerX, centerY),
                                        strokeWidth = 3.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                                    )

                                    drawCircle(
                                        color = Color(0xFF0284C7),
                                        radius = 8.dp.toPx(),
                                        center = Offset(startX, startY)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 5.dp.toPx(),
                                        center = Offset(startX, startY)
                                    )
                                }
                            }

                            // Hive Box Marker Pin at exact hive position
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.TopStart
                            ) {
                                val pointerWidthDp = 72.dp
                                val pointerHeightDp = 72.dp
                                val pointerWidthPx = pointerWidthDp.value * density
                                val pointerHeightPx = pointerHeightDp.value * density
                                val pLeft = (hivePos.x - pointerWidthPx / 2f).roundToInt()
                                val pTop = (hivePos.y - pointerHeightPx - 100f).roundToInt()

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .offset { androidx.compose.ui.unit.IntOffset(pLeft, pTop) }
                                ) {
                                    // Callout Header Badge
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                        modifier = Modifier
                                            .padding(bottom = 6.dp)
                                            .border(2.dp, HoneyGoldPrimary, RoundedCornerShape(12.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = hive.hiveNumber,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (hive.healthStatus == "Excelente") "🟢" else "🟡",
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Text(
                                                text = "${hive.beeSpecies} • ${hive.hiveType}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (hive.currentWeightKg != null) {
                                                Text(
                                                    text = "⚖️ Peso: ${hive.currentWeightKg} kg (${hive.strengthLevel})",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = HoneyGoldPrimary
                                                )
                                            }
                                        }
                                    }

                                    // Custom Drawn Hive Box Pin Pointer (Caixa de Colmeia 3D)
                                    HiveBoxMapPointerMarker(modifier = Modifier.size(72.dp))
                                }
                            }
                        }
                    )

                    // Esri Layer & Attribution Badge (Top Left)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(mapState.mapType.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = mapState.mapType.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HoneyGoldPrimary
                                )
                                Text(
                                    text = "Powered by Esri ArcGIS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Map Controls (Zoom / Layers / Recenter)
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Map Style Switcher Button
                        FloatingActionButton(
                            onClick = {
                                mapState.mapType = when (mapState.mapType) {
                                    MapType.SATELLITE -> MapType.TERRAIN
                                    MapType.TERRAIN -> MapType.STANDARD
                                    MapType.STANDARD -> MapType.SATELLITE
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = HoneyGoldPrimary,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("map_type_btn")
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = "Camadas do Mapa")
                        }

                        // Bee Flight Map Toggle Button
                        FloatingActionButton(
                            onClick = { showFlightMap = !showFlightMap },
                            containerColor = if (showFlightMap) HoneyGoldPrimary else MaterialTheme.colorScheme.surface,
                            contentColor = if (showFlightMap) Color.White else HoneyGoldPrimary,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("map_flight_toggle_btn")
                        ) {
                            Text("🐝", fontSize = 18.sp)
                        }

                        // Zoom In Button
                        FloatingActionButton(
                            onClick = { mapState.zoomIn() },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("map_zoom_in_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Aumentar Zoom")
                        }

                        // Zoom Out Button
                        FloatingActionButton(
                            onClick = { mapState.zoomOut() },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("map_zoom_out_btn")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Diminuir Zoom")
                        }

                        // Recenter on Hive Marker Button
                        FloatingActionButton(
                            onClick = {
                                mapState.recenter(baseLat, baseLon, 15.5f)
                            },
                            containerColor = HoneyGoldPrimary,
                            contentColor = Color.White,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("map_recenter_btn")
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Centralizar")
                        }

                        // Mark/Pick Location on Map Button
                        if (onUpdateGps != null) {
                            FloatingActionButton(
                                onClick = { showMapPicker = true },
                                containerColor = HoneyGoldPrimary,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("map_picker_float_btn")
                            ) {
                                Icon(Icons.Default.EditLocation, contentDescription = "Marcar Ponto no Mapa")
                            }
                        }
                    }

                    // In-App Navigation HUD Card
                    if (isNavigatingInApp) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                                .fillMaxWidth(0.85f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Navegação Ativa (Nativa)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0284C7)
                                    )
                                    Text(
                                        text = "Siga na direção Nordeste por 120 metros para alcançar o ninho.",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Distância restante: 142 m • Margem de erro do GPS: 1.5 m",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Flight radius legend chip
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        shadowElevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🐝", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Mapa de Voo das Abelhas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = HoneyGoldPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "🟡 Raio Econômico: 1.5 km (forrageamento ideal)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "🟠 Raio Máximo: 3.0 km (alcance limite de coleta)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!apiary?.predominantFlora.isNullOrBlank()) {
                                Text(
                                    text = "🌸 Florada: ${apiary?.predominantFlora}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MeadowGreenSecondary
                                )
                            }
                        }
                    }
                }

                // Bottom Location Details & Action Buttons
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = "GPS",
                                        tint = HoneyGoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (lat != null && lon != null) LocationHelper.formatCoordinates(lat, lon) else "Sem coordenadas cadastradas",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (lat != null && lon != null) {
                                    Text(
                                        text = LocationHelper.formatDms(lat, lon) + (hive.altitude?.let { " • Altitude: ${it.toInt()}m" } ?: ""),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (onUpdateGps != null) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = HoneyGoldPrimary.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.EditLocation, contentDescription = null, tint = HoneyGoldPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Marcar no mapa", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HoneyGoldPrimary)
                                    }
                                }
                            }
                        }

                        if (showMapPicker && onUpdateGps != null) {
                            MapLocationPickerDialog(
                                title = "Redefinir Ponto da Colmeia",
                                subtitle = "Marque o ponto exato onde a colmeia ${hive.hiveNumber} está posicionada",
                                initialLatitude = lat,
                                initialLongitude = lon,
                                onDismiss = { showMapPicker = false },
                                onConfirmLocation = { newLat, newLon ->
                                    onUpdateGps(newLat, newLon)
                                    android.widget.Toast.makeText(context, "Posição da Colmeia atualizada pelo mapa!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Open external maps & Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (lat != null && lon != null) {
                                Button(
                                    onClick = { isNavigatingInApp = !isNavigatingInApp },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isNavigatingInApp) WarningAlertRed else MeadowGreenSecondary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("open_external_map_btn")
                                ) {
                                    Icon(
                                        imageVector = if (isNavigatingInApp) Icons.Default.Close else Icons.Default.Navigation,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isNavigatingInApp) "Parar Guia" else "Navegar (GPS)")
                                }

                                Button(
                                    onClick = {
                                        val intent = LocationHelper.createShareLocationIntent(
                                            title = "Colmeia ${hive.hiveNumber}",
                                            lat = lat,
                                            lon = lon,
                                            altitude = hive.altitude,
                                            extraInfo = "Apiário: ${apiary?.name ?: "N/A"} | Espécie: ${hive.beeSpecies}"
                                        )
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("share_hive_location_btn")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Compartilhar")
                                }
                            } else {
                                Text(
                                    text = "💡 Dica: Clique em 'Capturar GPS' para registrar a localização exata no apiário.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas Drawing of a Hive Box Pointer Marker (Desenho de Caixa de Colmeia como Ponteiro do Mapa)
 */
@Composable
fun HiveBoxMapPointerMarker(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val woodColor = Color(0xFFD4A359)
        val woodDarkColor = Color(0xFFB07F37)
        val woodLightColor = Color(0xFFE8C07A)
        val roofColor = Color(0xFF7A5229)
        val entranceColor = Color(0xFF332211)
        val pinTailColor = Color(0xFFE65100)

        // 1. Draw Pointer Pin Shadow & Pin Tail (Ponteiro de sinalização)
        val tailPath = Path().apply {
            moveTo(w * 0.5f, h)
            lineTo(w * 0.35f, h * 0.72f)
            lineTo(w * 0.65f, h * 0.72f)
            close()
        }
        drawPath(path = tailPath, color = pinTailColor)

        // Drop shadow circle under tip
        drawOval(
            color = Color(0x66000000),
            topLeft = Offset(w * 0.35f, h * 0.92f),
            size = Size(w * 0.3f, h * 0.08f)
        )

        // 2. Draw Hive Stand (Pé da Caixa)
        drawRect(
            color = woodDarkColor,
            topLeft = Offset(w * 0.25f, h * 0.66f),
            size = Size(w * 0.5f, h * 0.06f)
        )

        // 3. Draw Main Hive Body - Ninho (Caixa Inferior)
        drawRoundRect(
            color = woodColor,
            topLeft = Offset(w * 0.2f, h * 0.42f),
            size = Size(w * 0.6f, h * 0.24f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // Ninho highlight strip
        drawRect(
            color = woodLightColor,
            topLeft = Offset(w * 0.22f, h * 0.44f),
            size = Size(w * 0.56f, h * 0.04f)
        )

        // Entrance Slot (Alvado / Entrada das abelhas)
        drawRoundRect(
            color = entranceColor,
            topLeft = Offset(w * 0.38f, h * 0.60f),
            size = Size(w * 0.24f, h * 0.04f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )

        // 4. Draw Super - Melgueira (Caixa Superior)
        drawRoundRect(
            color = woodColor,
            topLeft = Offset(w * 0.22f, h * 0.22f),
            size = Size(w * 0.56f, h * 0.18f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )

        // Joint line separating boxes
        drawLine(
            color = woodDarkColor,
            start = Offset(w * 0.2f, h * 0.41f),
            end = Offset(w * 0.8f, h * 0.41f),
            strokeWidth = 3.dp.toPx()
        )

        // Handle bar on Super (Alça de madeira)
        drawRoundRect(
            color = woodDarkColor,
            topLeft = Offset(w * 0.38f, h * 0.28f),
            size = Size(w * 0.24f, h * 0.04f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )

        // 5. Draw Roof / Cover (Tampa Telescópica de Madeira)
        val roofPath = Path().apply {
            moveTo(w * 0.12f, h * 0.22f)
            lineTo(w * 0.88f, h * 0.22f)
            lineTo(w * 0.82f, h * 0.10f)
            lineTo(w * 0.18f, h * 0.10f)
            close()
        }
        drawPath(path = roofPath, color = roofColor)

        // Metallic top rim
        drawRect(
            color = Color(0xFFD0D0D0),
            topLeft = Offset(w * 0.18f, h * 0.09f),
            size = Size(w * 0.64f, h * 0.03f)
        )

        // 6. Draw Bees 🐝 floating near the hive entrance
        drawCircle(
            color = Color(0xFFFFD54F),
            radius = 3.dp.toPx(),
            center = Offset(w * 0.72f, h * 0.55f)
        )
        drawCircle(
            color = Color(0xFFFFD54F),
            radius = 2.5f.dp.toPx(),
            center = Offset(w * 0.26f, h * 0.50f)
        )
    }
}
