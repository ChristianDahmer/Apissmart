package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.HoneyGoldPrimary
import com.example.util.LocationHelper

@Composable
fun MapLocationPickerDialog(
    title: String,
    subtitle: String,
    initialLatitude: Double?,
    initialLongitude: Double?,
    onDismiss: () -> Unit,
    onConfirmLocation: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current.density

    // Default coordinate if none provided
    val defaultLat = initialLatitude ?: -22.7394
    val defaultLon = initialLongitude ?: -45.5913

    val mapState = rememberSlippyTileMapState(defaultLat, defaultLon, initialZoom = 16.0f)

    var selectedLat by remember { mutableStateOf(defaultLat) }
    var selectedLon by remember { mutableStateOf(defaultLon) }
    var latText by remember { mutableStateOf(String.format(java.util.Locale.US, "%.6f", defaultLat)) }
    var lonText by remember { mutableStateOf(String.format(java.util.Locale.US, "%.6f", defaultLon)) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val loc = LocationHelper.getCurrentLocation(context)
            if (loc != null) {
                selectedLat = loc.latitude
                selectedLon = loc.longitude
                latText = String.format(java.util.Locale.US, "%.6f", loc.latitude)
                lonText = String.format(java.util.Locale.US, "%.6f", loc.longitude)
                mapState.recenter(loc.latitude, loc.longitude, 16.5f)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("map_location_picker_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Slippy Tile Map Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                ) {
                    SlippyTileMap(
                        state = mapState,
                        modifier = Modifier.fillMaxSize(),
                        onTap = { clickedLat, clickedLon ->
                            selectedLat = clickedLat
                            selectedLon = clickedLon
                            latText = String.format(java.util.Locale.US, "%.6f", clickedLat)
                            lonText = String.format(java.util.Locale.US, "%.6f", clickedLon)
                            mapState.recenter(clickedLat, clickedLon)
                        },
                        overlayContent = { widthPx, heightPx, _ ->
                            // Update coordinates to track screen center while dragging/zooming
                            LaunchedEffect(mapState.panOffsetX, mapState.panOffsetY, mapState.zoomLevel) {
                                val (cLat, cLon) = mapState.getCurrentCenter(density, widthPx, heightPx)
                                selectedLat = cLat
                                selectedLon = cLon
                                latText = String.format(java.util.Locale.US, "%.6f", cLat)
                                lonText = String.format(java.util.Locale.US, "%.6f", cLon)
                            }

                            // Center Target Pin & Concentric Crosshair
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerX = widthPx / 2f
                                val centerY = heightPx / 2f

                                // Target crosshairs
                                drawLine(
                                    color = Color.White.copy(alpha = 0.5f),
                                    start = Offset(centerX - 24.dp.toPx(), centerY),
                                    end = Offset(centerX + 24.dp.toPx(), centerY),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                                drawLine(
                                    color = Color.White.copy(alpha = 0.5f),
                                    start = Offset(centerX, centerY - 24.dp.toPx()),
                                    end = Offset(centerX, centerY + 24.dp.toPx()),
                                    strokeWidth = 1.5.dp.toPx()
                                )

                                // Concentric radius circles
                                drawCircle(
                                    color = Color(0xFFEAB308).copy(alpha = 0.25f),
                                    radius = 28.dp.toPx(),
                                    center = Offset(centerX, centerY),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                drawCircle(
                                    color = Color(0xFFEAB308).copy(alpha = 0.12f),
                                    radius = 56.dp.toPx(),
                                    center = Offset(centerX, centerY),
                                    style = Stroke(width = 1.dp.toPx())
                                )

                                // Pin head & point
                                val pinColor = Color(0xFFEF4444)
                                val pinRadius = 10.dp.toPx()
                                val pinOffset = Offset(centerX, centerY - 14.dp.toPx())

                                val path = Path().apply {
                                    moveTo(centerX, centerY)
                                    lineTo(centerX - 8.dp.toPx(), centerY - 14.dp.toPx())
                                    lineTo(centerX + 8.dp.toPx(), centerY - 14.dp.toPx())
                                    close()
                                }
                                drawPath(path, color = pinColor)

                                drawCircle(
                                    color = pinColor,
                                    radius = pinRadius,
                                    center = pinOffset
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 4.dp.toPx(),
                                    center = pinOffset
                                )
                            }
                        }
                    )

                    // Map controls overlay (Top Right)
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Layer switcher
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    mapState.mapType = when (mapState.mapType) {
                                        MapType.SATELLITE -> MapType.TERRAIN
                                        MapType.TERRAIN -> MapType.STANDARD
                                        MapType.STANDARD -> MapType.SATELLITE
                                    }
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.Layers, contentDescription = "Camadas", tint = HoneyGoldPrimary, modifier = Modifier.size(20.dp))
                            }
                        }

                        // GPS Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    if (fine || coarse) {
                                        val loc = LocationHelper.getCurrentLocation(context)
                                        if (loc != null) {
                                            selectedLat = loc.latitude
                                            selectedLon = loc.longitude
                                            latText = String.format(java.util.Locale.US, "%.6f", loc.latitude)
                                            lonText = String.format(java.util.Locale.US, "%.6f", loc.longitude)
                                            mapState.recenter(loc.latitude, loc.longitude, 16.5f)
                                        }
                                    } else {
                                        locationPermissionLauncher.launch(
                                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                        )
                                    }
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = "Meu GPS", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }

                        // Zoom In
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { mapState.zoomIn() },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
                            }
                        }

                        // Zoom Out
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { mapState.zoomOut() },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // Helper badge on bottom left of map
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.65f)
                    ) {
                        Text(
                            text = "Toque ou arraste para ajustar a posição do pino",
                            fontSize = 10.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Coordinate input fields for fine-grained manual precision
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = {
                            latText = it
                            it.toDoubleOrNull()?.let { lat ->
                                selectedLat = lat
                                mapState.recenter(lat = lat, lon = selectedLon)
                            }
                        },
                        label = { Text("Latitude", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = lonText,
                        onValueChange = {
                            lonText = it
                            it.toDoubleOrNull()?.let { lon ->
                                selectedLon = lon
                                mapState.recenter(lat = selectedLat, lon = lon)
                            }
                        },
                        label = { Text("Longitude", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val finalLat = latText.toDoubleOrNull() ?: selectedLat
                            val finalLon = lonText.toDoubleOrNull() ?: selectedLon
                            onConfirmLocation(finalLat, finalLon)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirmar")
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}
