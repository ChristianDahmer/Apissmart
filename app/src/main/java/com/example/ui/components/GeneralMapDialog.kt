package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.BeeTrapEntity
import com.example.data.local.entities.HiveEntity
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.WarningAlertRed
import com.example.util.LocationHelper

sealed class MapMarkerItem {
    data class Apiary(val apiary: ApiaryEntity) : MapMarkerItem()
    data class Hive(val hive: HiveEntity, val apiaryName: String) : MapMarkerItem()
    data class Trap(val trap: BeeTrapEntity) : MapMarkerItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralMapDialog(
    apiaries: List<ApiaryEntity>,
    hives: List<HiveEntity>,
    traps: List<BeeTrapEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Default coordinate: first valid apiary, hive, trap, or baseline coordinates
    val defaultCenter = remember(apiaries, hives, traps) {
        val firstApiary = apiaries.firstOrNull { it.latitude != null && it.longitude != null }
        if (firstApiary != null) {
            Pair(firstApiary.latitude!!, firstApiary.longitude!!)
        } else {
            val firstHive = hives.firstOrNull { it.latitude != null && it.longitude != null }
            if (firstHive != null) {
                Pair(firstHive.latitude!!, firstHive.longitude!!)
            } else {
                val firstTrap = traps.firstOrNull { it.latitude != null && it.longitude != null }
                if (firstTrap != null) {
                    Pair(firstTrap.latitude!!, firstTrap.longitude!!)
                } else {
                    Pair(-22.7394, -45.5913)
                }
            }
        }
    }

    val mapState = rememberSlippyTileMapState(defaultCenter.first, defaultCenter.second, initialZoom = 15.5f)
    var selectedMarker by remember { mutableStateOf<MapMarkerItem?>(null) }
    var customPinCoordinate by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var isPinModeEnabled by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("general_map_dialog"),
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
                            Text("🗺️", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Mapa Geral de Produção",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Visualização nativa de apiários, colmeias e iscas 🐝",
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
                            .testTag("close_general_map_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Interactive Slippy Tile Map Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    SlippyTileMap(
                        state = mapState,
                        modifier = Modifier.fillMaxSize(),
                        overlayContent = { widthPx, heightPx, density ->
                            // Interactive Canvas for marker rendering and touch detection
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(apiaries, hives, traps, mapState.zoomLevel, mapState.panOffsetX, mapState.panOffsetY) {
                                        detectTapGestures { tapOffset ->
                                            val maxDist = 28.dp.toPx()
                                            var closestItem: MapMarkerItem? = null
                                            var minDist = Double.MAX_VALUE

                                            apiaries.forEach { apiary ->
                                                if (apiary.latitude != null && apiary.longitude != null) {
                                                    val pos = mapState.latLonToScreen(apiary.latitude, apiary.longitude, density, widthPx, heightPx)
                                                    val dist = (pos - tapOffset).getDistance()
                                                    if (dist < minDist) {
                                                        minDist = dist.toDouble()
                                                        closestItem = MapMarkerItem.Apiary(apiary)
                                                    }
                                                }
                                            }

                                            hives.forEach { hive ->
                                                if (hive.latitude != null && hive.longitude != null) {
                                                    val apiaryName = apiaries.find { it.id == hive.apiaryId }?.name ?: "N/A"
                                                    val pos = mapState.latLonToScreen(hive.latitude, hive.longitude, density, widthPx, heightPx)
                                                    val dist = (pos - tapOffset).getDistance()
                                                    if (dist < minDist) {
                                                        minDist = dist.toDouble()
                                                        closestItem = MapMarkerItem.Hive(hive, apiaryName)
                                                    }
                                                }
                                            }

                                            traps.forEach { trap ->
                                                if (trap.latitude != null && trap.longitude != null) {
                                                    val pos = mapState.latLonToScreen(trap.latitude, trap.longitude, density, widthPx, heightPx)
                                                    val dist = (pos - tapOffset).getDistance()
                                                    if (dist < minDist) {
                                                        minDist = dist.toDouble()
                                                        closestItem = MapMarkerItem.Trap(trap)
                                                    }
                                                }
                                            }

                                            if (minDist < maxDist) {
                                                selectedMarker = closestItem
                                            } else if (isPinModeEnabled) {
                                                customPinCoordinate = mapState.screenToLatLon(tapOffset, density, widthPx, heightPx)
                                            }
                                        }
                                    }
                            ) {
                                // Draw Custom Pin (if dropped)
                                customPinCoordinate?.let { (pLat, pLon) ->
                                    val pos = mapState.latLonToScreen(pLat, pLon, density, widthPx, heightPx)
                                    drawCircle(
                                        color = WarningAlertRed.copy(alpha = 0.35f),
                                        radius = 24.dp.toPx(),
                                        center = pos
                                    )
                                    drawCircle(
                                        color = WarningAlertRed,
                                        radius = 11.dp.toPx(),
                                        center = pos
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 8.dp.toPx(),
                                        center = pos,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 3.5.dp.toPx(),
                                        center = pos
                                    )
                                }

                                // Draw Apiaries (Emerald green circles)
                                apiaries.forEach { apiary ->
                                    if (apiary.latitude != null && apiary.longitude != null) {
                                        val pos = mapState.latLonToScreen(apiary.latitude, apiary.longitude, density, widthPx, heightPx)
                                        drawCircle(
                                            color = Color(0xFF059669),
                                            radius = 13.dp.toPx(),
                                            center = pos
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = 11.dp.toPx(),
                                            center = pos,
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                    }
                                }

                                // Draw Hives (Honey gold squares)
                                hives.forEach { hive ->
                                    if (hive.latitude != null && hive.longitude != null) {
                                        val pos = mapState.latLonToScreen(hive.latitude, hive.longitude, density, widthPx, heightPx)
                                        drawRect(
                                            color = Color(0xFFD97706),
                                            topLeft = Offset(pos.x - 7.dp.toPx(), pos.y - 7.dp.toPx()),
                                            size = Size(14.dp.toPx(), 14.dp.toPx())
                                        )
                                        drawRect(
                                            color = Color.White,
                                            topLeft = Offset(pos.x - 8.dp.toPx(), pos.y - 8.dp.toPx()),
                                            size = Size(16.dp.toPx(), 16.dp.toPx()),
                                            style = Stroke(width = 1.5.dp.toPx())
                                        )
                                    }
                                }

                                // Draw Traps (Orange triangles)
                                traps.forEach { trap ->
                                    if (trap.latitude != null && trap.longitude != null) {
                                        val pos = mapState.latLonToScreen(trap.latitude, trap.longitude, density, widthPx, heightPx)
                                        val path = Path().apply {
                                            moveTo(pos.x, pos.y - 9.dp.toPx())
                                            lineTo(pos.x - 9.dp.toPx(), pos.y + 9.dp.toPx())
                                            lineTo(pos.x + 9.dp.toPx(), pos.y + 9.dp.toPx())
                                            close()
                                        }
                                        drawPath(path = path, color = Color(0xFFEA580C))
                                    }
                                }
                            }
                        }
                    )

                    // Floating Controls Overlay (Top Right)
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = "Camadas do Mapa")
                        }

                        FloatingActionButton(
                            onClick = { mapState.zoomIn() },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom In")
                        }

                        FloatingActionButton(
                            onClick = { mapState.zoomOut() },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                        }

                        FloatingActionButton(
                            onClick = {
                                isPinModeEnabled = !isPinModeEnabled
                                if (isPinModeEnabled && customPinCoordinate == null) {
                                    customPinCoordinate = Pair(mapState.centerLatitude, mapState.centerLongitude)
                                }
                            },
                            containerColor = if (isPinModeEnabled) WarningAlertRed else MaterialTheme.colorScheme.surface,
                            contentColor = if (isPinModeEnabled) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.PinDrop, contentDescription = "Modo Marcar Ponto")
                        }

                        FloatingActionButton(
                            onClick = {
                                mapState.recenter(defaultCenter.first, defaultCenter.second, 15.5f)
                            },
                            containerColor = HoneyGoldPrimary,
                            contentColor = Color.White,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Centralizar")
                        }
                    }

                    // Floating Legend Badge (Top Left)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Legenda do Mapa", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏢", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Apiários", fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🐝", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Colmeias Ativas", fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🪤", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Iscas Armadas", fontSize = 10.sp)
                            }
                        }
                    }

                    // Dynamic Selected Marker Card Overlay (Bottom)
                    selectedMarker?.let { item ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .fillMaxWidth(0.95f)
                                .border(1.5.dp, HoneyGoldPrimary, RoundedCornerShape(16.dp))
                        ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = when (item) {
                                                is MapMarkerItem.Apiary -> "🌿 Apiário: ${item.apiary.name}"
                                                is MapMarkerItem.Hive -> "🍯 Colmeia: ${item.hive.hiveNumber}"
                                                is MapMarkerItem.Trap -> "🍊 Isca: ${item.trap.name}"
                                            },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = HoneyGoldPrimary
                                        )
                                        IconButton(
                                            onClick = { selectedMarker = null },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Fechar Detalhes", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (item) {
                                            is MapMarkerItem.Apiary -> "📍 Localização: ${item.apiary.location}\n🌸 Flora: ${item.apiary.predominantFlora}\n📦 Capacidade: ${item.apiary.capacity} colmeias"
                                            is MapMarkerItem.Hive -> "📍 Apiário de Origem: ${item.apiaryName}\n🐝 Espécie: ${item.hive.beeSpecies}\n⚡ Força: ${item.hive.strengthLevel} | Saúde: ${item.hive.healthStatus}"
                                            is MapMarkerItem.Trap -> "📦 Tipo: ${item.trap.trapType}\n💡 Atrativo: ${item.trap.attractantUsed}\n🟢 Situação: ${item.trap.status}"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val mLat = when (item) {
                                            is MapMarkerItem.Apiary -> item.apiary.latitude
                                            is MapMarkerItem.Hive -> item.hive.latitude
                                            is MapMarkerItem.Trap -> item.trap.latitude
                                        }
                                        val mLon = when (item) {
                                            is MapMarkerItem.Apiary -> item.apiary.longitude
                                            is MapMarkerItem.Hive -> item.hive.longitude
                                            is MapMarkerItem.Trap -> item.trap.longitude
                                        }
                                        if (mLat != null && mLon != null) {
                                            Button(
                                                onClick = {
                                                    mapState.recenter(mLat, mLon, 17.5f)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MeadowGreenSecondary),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Focar Aqui", fontSize = 11.sp)
                                            }
                                            Button(
                                                onClick = {
                                                    val intent = LocationHelper.createShareLocationIntent(
                                                        title = when (item) {
                                                            is MapMarkerItem.Apiary -> item.apiary.name
                                                            is MapMarkerItem.Hive -> item.hive.hiveNumber
                                                            is MapMarkerItem.Trap -> item.trap.name
                                                        },
                                                        lat = mLat,
                                                        lon = mLon,
                                                        extraInfo = "Visualizado no mapa geral nativo do ApisSmart."
                                                    )
                                                    context.startActivity(intent)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Compartilhar", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                }
            }
        }
    }
}
