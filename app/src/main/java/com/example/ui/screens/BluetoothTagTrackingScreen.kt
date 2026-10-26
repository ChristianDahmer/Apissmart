package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.bluetooth.DiscoveredBleTag
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.BluetoothTagEntity
import com.example.data.local.entities.HiveEntity
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.WarningAlertRed
import com.example.util.LocationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

enum class BluetoothTagSubTab(val title: String, val icon: String) {
    RADAR("Radar de Proximidade", "🎯"),
    MONITORED_TAGS("Tags Monitoradas", "📋"),
    DISCOVERED_SCANNER("Escanear Tags", "🔍")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothTagTrackingScreen(
    bluetoothTags: List<BluetoothTagEntity>,
    discoveredTags: Map<String, DiscoveredBleTag>,
    targetRadarTag: BluetoothTagEntity?,
    isScanning: Boolean,
    isSimulationActive: Boolean,
    isAudioEnabled: Boolean,
    isHapticEnabled: Boolean,
    hives: List<HiveEntity>,
    apiaries: List<ApiaryEntity>,
    onToggleScan: () -> Unit,
    onToggleSimulation: () -> Unit,
    onSelectRadarTag: (BluetoothTagEntity?) -> Unit,
    onToggleAudio: () -> Unit,
    onToggleHaptic: () -> Unit,
    onSaveTag: (name: String, mac: String, type: String, hiveId: Long?, apiaryId: Long?, antiTheft: Boolean, notes: String, id: Long) -> Unit,
    onDeleteTag: (BluetoothTagEntity) -> Unit,
    onToggleAntiTheft: (BluetoothTagEntity) -> Unit,
    onUpdateGpsLocation: (BluetoothTagEntity) -> Unit,
    onOpenInspectionForHive: (HiveEntity) -> Unit,
    onOpenMapForTag: (lat: Double, lon: Double, title: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var tagToEdit by remember { mutableStateOf<BluetoothTagEntity?>(null) }
    var candidateMacFromScanner by remember { mutableStateOf<String?>(null) }

    // Gerenciador de permissões de Bluetooth e Localização
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted && !isScanning) {
            onToggleScan()
        }
    }

    fun requestBlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Se nenhuma tag estiver selecionada para o radar, seleciona a primeira tag cadastrada
    LaunchedEffect(bluetoothTags, targetRadarTag) {
        if (targetRadarTag == null && bluetoothTags.isNotEmpty()) {
            onSelectRadarTag(bluetoothTags.first())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("bluetooth_tag_screen")
    ) {
        // --- CABEÇALHO DO MÓDULO BLE ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(HoneyGoldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isScanning) Icons.Default.BluetoothSearching else Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = HoneyGoldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Rastreabilidade de Tags BLE",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isScanning) "Varredura ativa • Rastreando sinais" else "Scanner pausado • Economia de bateria",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isScanning) MeadowGreenSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Botão de alternar Scan
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            onClick = {
                                requestBlePermissions()
                                onToggleScan()
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isScanning) WarningAlertRed.copy(alpha = 0.2f) else MeadowGreenSecondary.copy(alpha = 0.2f),
                                contentColor = if (isScanning) WarningAlertRed else MeadowGreenSecondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("toggle_ble_scan_btn")
                        ) {
                            Icon(
                                imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isScanning) "Pausar" else "Escanear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Barra de Status Rápido e Botão Modo Simulação em Campo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "📡 ${bluetoothTags.size} tags salvas",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "🔍 ${discoveredTags.size} no alcance",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium,
                                color = if (discoveredTags.isNotEmpty()) MeadowGreenSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Botão de simulação no apiário (ideal para demonstração sem beacons físicos no local)
                    OutlinedButton(
                        onClick = onToggleSimulation,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isSimulationActive) HoneyGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("toggle_simulation_btn")
                    ) {
                        Text(
                            text = if (isSimulationActive) "✨ Simulação Ativa" else "✨ Testar no Apiário",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- SUB-ABAS DE NAVEGAÇÃO ---
        SecondaryTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = HoneyGoldPrimary
        ) {
            BluetoothTagSubTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            "${tab.icon} ${tab.title}",
                            fontSize = 12.sp,
                            fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                )
            }
        }

        // --- CONTEÚDO DA SUB-ABA SELECIONADA ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (BluetoothTagSubTab.entries[selectedSubTab]) {
                BluetoothTagSubTab.RADAR -> {
                    RadarProximityTab(
                        bluetoothTags = bluetoothTags,
                        discoveredTags = discoveredTags,
                        targetRadarTag = targetRadarTag,
                        isAudioEnabled = isAudioEnabled,
                        isHapticEnabled = isHapticEnabled,
                        hives = hives,
                        onSelectRadarTag = onSelectRadarTag,
                        onToggleAudio = onToggleAudio,
                        onToggleHaptic = onToggleHaptic,
                        onOpenInspectionForHive = onOpenInspectionForHive,
                        onOpenMapForTag = onOpenMapForTag,
                        onUpdateGpsLocation = onUpdateGpsLocation,
                        onAddNewTag = {
                            candidateMacFromScanner = null
                            tagToEdit = null
                            showAddEditDialog = true
                        }
                    )
                }

                BluetoothTagSubTab.MONITORED_TAGS -> {
                    MonitoredTagsTab(
                        bluetoothTags = bluetoothTags,
                        discoveredTags = discoveredTags,
                        targetRadarTag = targetRadarTag,
                        hives = hives,
                        onSelectRadarTag = { tag ->
                            onSelectRadarTag(tag)
                            selectedSubTab = 0 // Vai para o Radar
                        },
                        onEditTag = { tag ->
                            tagToEdit = tag
                            candidateMacFromScanner = null
                            showAddEditDialog = true
                        },
                        onDeleteTag = onDeleteTag,
                        onToggleAntiTheft = onToggleAntiTheft,
                        onUpdateGpsLocation = onUpdateGpsLocation,
                        onOpenMapForTag = onOpenMapForTag,
                        onAddNewTag = {
                            candidateMacFromScanner = null
                            tagToEdit = null
                            showAddEditDialog = true
                        }
                    )
                }

                BluetoothTagSubTab.DISCOVERED_SCANNER -> {
                    DiscoveredScannerTab(
                        discoveredTags = discoveredTags.values.toList(),
                        bluetoothTags = bluetoothTags,
                        isScanning = isScanning,
                        onStartScan = {
                            requestBlePermissions()
                            if (!isScanning) onToggleScan()
                        },
                        onAssignCandidateToHive = { discTag ->
                            candidateMacFromScanner = discTag.macAddress
                            tagToEdit = null
                            showAddEditDialog = true
                        }
                    )
                }
            }
        }
    }

    // Diálogo de Cadastro / Edição e Vínculo de Tag Bluetooth à Colmeia
    if (showAddEditDialog) {
        AddEditBluetoothTagDialog(
            tagToEdit = tagToEdit,
            prefilledMac = candidateMacFromScanner,
            hives = hives,
            apiaries = apiaries,
            onDismiss = {
                showAddEditDialog = false
                tagToEdit = null
                candidateMacFromScanner = null
            },
            onSave = { name, mac, type, hiveId, apiaryId, antiTheft, notes, id ->
                onSaveTag(name, mac, type, hiveId, apiaryId, antiTheft, notes, id)
                showAddEditDialog = false
                tagToEdit = null
                candidateMacFromScanner = null
            }
        )
    }
}

// =========================================================================
// ABA 1: RADAR DE PROXIMIDADE ("QUENTE OU FRIO" - LOCALIZADOR DE COLMEIAS)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RadarProximityTab(
    bluetoothTags: List<BluetoothTagEntity>,
    discoveredTags: Map<String, DiscoveredBleTag>,
    targetRadarTag: BluetoothTagEntity?,
    isAudioEnabled: Boolean,
    isHapticEnabled: Boolean,
    hives: List<HiveEntity>,
    onSelectRadarTag: (BluetoothTagEntity?) -> Unit,
    onToggleAudio: () -> Unit,
    onToggleHaptic: () -> Unit,
    onOpenInspectionForHive: (HiveEntity) -> Unit,
    onOpenMapForTag: (lat: Double, lon: Double, title: String) -> Unit,
    onUpdateGpsLocation: (BluetoothTagEntity) -> Unit,
    onAddNewTag: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    if (bluetoothTags.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📡", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Nenhuma Tag Vinculada",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Cadastre sua primeira tag Bluetooth (SmartTag, iBeacon, RuuviTag ou sensor) para rastrear colmeias no apiário.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onAddNewTag,
                        colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vincular Nova Tag BLE", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val currentTarget = targetRadarTag ?: bluetoothTags.first()
    val liveData = discoveredTags[currentTarget.tagMacAddress]
    val currentDistance = liveData?.distanceMeters ?: currentTarget.lastDistanceMeters
    val currentRssi = liveData?.rssi ?: currentTarget.lastRssi
    val isLiveInRange = liveData != null

    val linkedHive = hives.find { it.id == currentTarget.hiveId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("radar_proximity_view"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Seletor de Colmeia / Tag Alvo
        item {
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = "${currentTarget.tagName} • ${currentTarget.assignedItemName.ifBlank { "Sem Colmeia" }}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Colmeia / Tag a Localizar") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(14.dp)
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    bluetoothTags.forEach { tag ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(tag.tagName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "${tag.assignedItemName.ifBlank { "Sem colmeia vinculada" }} • MAC: ${tag.tagMacAddress}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onSelectRadarTag(tag)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Radar Visual Animado (Canvas Compose)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isLiveInRange) MeadowGreenSecondary else WarningAlertRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLiveInRange) "Sinal Ativo em Tempo Real" else "Última Leitura Gravada",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isLiveInRange) MeadowGreenSecondary else WarningAlertRed
                            )
                        }

                        // Controles de Áudio e Vibração do Radar
                        Row {
                            IconButton(onClick = onToggleAudio, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = if (isAudioEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = "Bip de proximidade",
                                    tint = if (isAudioEnabled) HoneyGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = onToggleHaptic, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = "Vibração de proximidade",
                                    tint = if (isHapticEnabled) MeadowGreenSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Radar circular gráfico com animação de varredura
                    RadarCanvasView(
                        distanceMeters = currentDistance,
                        isSignalActive = isLiveInRange,
                        modifier = Modifier
                            .size(240.dp)
                            .testTag("radar_canvas_graphic")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Indicador Numérico de Distância e Temperatura de Proximidade
                    val proximityStatus = when {
                        currentDistance < 1.0 -> Pair("Muito Perto! 🔥", MeadowGreenSecondary)
                        currentDistance < 2.5 -> Pair("Quente! 🟢", MeadowGreenSecondary)
                        currentDistance < 5.0 -> Pair("Morno 🟡", HoneyGoldPrimary)
                        currentDistance < 10.0 -> Pair("Frio ❄️", Color(0xFF64B5F6))
                        else -> Pair("Muito Distante ⚪", MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Text(
                        text = "%.1f m".format(Locale.US, currentDistance),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = proximityStatus.second
                    )
                    Text(
                        text = proximityStatus.first,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = proximityStatus.second
                    )
                    Text(
                        text = "Potência: $currentRssi dBm • Calibração: ${currentTarget.txPower} dBm",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Telemetria do Sensor da Colmeia (Se a Tag emitir temperatura e umidade)
        item {
            val temp = liveData?.temperatureC ?: currentTarget.temperatureC
            val hum = liveData?.humidityPercent ?: currentTarget.humidityPercent
            val bat = liveData?.batteryPercent ?: currentTarget.batteryPercent

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🌡️ Telemetria Ambiental da Colmeia",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Temperatura
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Thermostat, contentDescription = null, tint = WarningAlertRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (temp != null) "%.1f °C".format(Locale.US, temp) else "N/A",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Text(
                                text = when {
                                    temp == null -> "Sensor s/ temp"
                                    temp in 34.0..36.0 -> "🟢 Ideal de Cria"
                                    temp < 32.0 -> "❄️ Frio no Ninho"
                                    else -> "🔥 Superaquecimento"
                                },
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Umidade
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF42A5F5), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (hum != null) "%.0f %%".format(Locale.US, hum) else "N/A",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Text("Umidade do Ninho", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Bateria da Tag
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BatteryFull, contentDescription = null, tint = MeadowGreenSecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (bat != null) "$bat%" else "N/A",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Text("Bateria da Tag", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Ações Rápidas de Campo com 1 Toque
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "⚡ Ações de Campo para Esta Colmeia",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Se a tag detectada estiver muito perto (< 2.0m), destaca o botão de inspeção imediata!
                    if (linkedHive != null) {
                        Button(
                            onClick = { onOpenInspectionForHive(linkedHive) },
                            colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("radar_quick_inspect_btn")
                        ) {
                            Text("📋 Iniciar Inspeção da ${linkedHive.hiveNumber}", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onUpdateGpsLocation(currentTarget) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gravar GPS", fontSize = 11.sp)
                        }

                        if (currentTarget.lastLatitude != null && currentTarget.lastLongitude != null) {
                            OutlinedButton(
                                onClick = {
                                    onOpenMapForTag(
                                        currentTarget.lastLatitude,
                                        currentTarget.lastLongitude,
                                        currentTarget.tagName
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ver no Mapa", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Desenho do Radar Circular com varredura rotativa e pulso de sinal
 */
@Composable
private fun RadarCanvasView(
    distanceMeters: Double,
    isSignalActive: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "radar_anim")
    val sweepAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    val pulseScale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension / 2f - 12f

        // Círculos concêntricos de distância (1m, 3m, 7m, 15m)
        val rings = listOf(0.25f, 0.50f, 0.75f, 1.0f)
        rings.forEach { fraction ->
            drawCircle(
                color = Color(0xFF334155),
                radius = maxRadius * fraction,
                center = center,
                style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
            )
        }

        // Linhas de mira (eixos X e Y)
        drawLine(
            color = Color(0xFF334155),
            start = Offset(center.x - maxRadius, center.y),
            end = Offset(center.x + maxRadius, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color(0xFF334155),
            start = Offset(center.x, center.y - maxRadius),
            end = Offset(center.x, center.y + maxRadius),
            strokeWidth = 1.dp.toPx()
        )

        // Feixe rotativo do radar
        if (isSignalActive) {
            val sweepRad = Math.toRadians(sweepAngle.toDouble())
            val sweepEnd = Offset(
                x = (center.x + maxRadius * cos(sweepRad)).toFloat(),
                y = (center.y + maxRadius * sin(sweepRad)).toFloat()
            )
            drawLine(
                brush = Brush.radialGradient(
                    colors = listOf(HoneyGoldPrimary.copy(alpha = 0.6f), Color.Transparent),
                    center = center,
                    radius = maxRadius
                ),
                start = center,
                end = sweepEnd,
                strokeWidth = 3.dp.toPx()
            )
        }

        // Centro: Ícone do Apicultor / Smartphone
        drawCircle(
            color = Color.White,
            radius = 6.dp.toPx(),
            center = center
        )

        // Ponto da Tag BLE alvo calculado proporcionalmente à distância (clamp em 15m)
        val normalizedDist = (distanceMeters / 15.0).coerceIn(0.12, 0.95)
        val tagRadius = (maxRadius * normalizedDist).toFloat()
        // Posição angular fixa com leve deslocamento para representar vetor
        val targetAngleRad = Math.toRadians(45.0)
        val tagCenter = Offset(
            x = (center.x + tagRadius * cos(targetAngleRad)).toFloat(),
            y = (center.y + tagRadius * sin(targetAngleRad)).toFloat()
        )

        val blipColor = when {
            distanceMeters < 1.5 -> MeadowGreenSecondary
            distanceMeters < 4.0 -> HoneyGoldPrimary
            else -> Color(0xFF42A5F5)
        }

        // Pulso ao redor da tag
        if (isSignalActive) {
            drawCircle(
                color = blipColor.copy(alpha = 0.35f),
                radius = 16.dp.toPx() * pulseScale,
                center = tagCenter
            )
        }

        // Ponto principal da tag
        drawCircle(
            color = blipColor,
            radius = 9.dp.toPx(),
            center = tagCenter
        )
        drawCircle(
            color = Color.White,
            radius = 3.5.dp.toPx(),
            center = tagCenter
        )
    }
}

// =========================================================================
// ABA 2: LISTA DE TAGS MONITORADAS & TELEMETRIA DO PLANTEL
// =========================================================================
@Composable
private fun MonitoredTagsTab(
    bluetoothTags: List<BluetoothTagEntity>,
    discoveredTags: Map<String, DiscoveredBleTag>,
    targetRadarTag: BluetoothTagEntity?,
    hives: List<HiveEntity>,
    onSelectRadarTag: (BluetoothTagEntity) -> Unit,
    onEditTag: (BluetoothTagEntity) -> Unit,
    onDeleteTag: (BluetoothTagEntity) -> Unit,
    onToggleAntiTheft: (BluetoothTagEntity) -> Unit,
    onUpdateGpsLocation: (BluetoothTagEntity) -> Unit,
    onOpenMapForTag: (lat: Double, lon: Double, title: String) -> Unit,
    onAddNewTag: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("monitored_tags_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Colmeias & Beacons Cadastrados (${bluetoothTags.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = onAddNewTag,
                    colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nova Tag", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (bluetoothTags.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Nenhuma tag Bluetooth cadastrada.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        items(bluetoothTags, key = { it.id }) { tag ->
            val liveData = discoveredTags[tag.tagMacAddress]
            val isLive = liveData != null
            val currentDist = liveData?.distanceMeters ?: tag.lastDistanceMeters
            val currentRssi = liveData?.rssi ?: tag.lastRssi
            val currentTemp = liveData?.temperatureC ?: tag.temperatureC
            val currentHum = liveData?.humidityPercent ?: tag.humidityPercent
            val currentBat = liveData?.batteryPercent ?: tag.batteryPercent

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (targetRadarTag?.id == tag.id)
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    else
                        MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Topo do card: Nome da Tag, Status e Anti-furto
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isLive) MeadowGreenSecondary else WarningAlertRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tag.tagName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        // Badge de Tipo
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HoneyGoldPrimary.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = tag.tagType,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = HoneyGoldPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vínculo: ${tag.assignedItemName.ifBlank { "Nenhuma colmeia associada" }}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "MAC: ${tag.tagMacAddress}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Indicadores em linha: Distância, RSSI, Bateria, Sensores
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Distância / RSSI", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "%.1f m (%d dBm)".format(Locale.US, currentDist, currentRssi),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isLive) MeadowGreenSecondary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (currentTemp != null) {
                            Column {
                                Text("Temp. Ninho", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "%.1f °C".format(Locale.US, currentTemp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (currentTemp in 34.0..36.0) MeadowGreenSecondary else WarningAlertRed
                                )
                            }
                        }

                        if (currentBat != null) {
                            Column {
                                Text("Bateria", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "$currentBat%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chave Anti-Furto e Última Localização
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (tag.isAntiTheftEnabled) MeadowGreenSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Anti-Furto:", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = tag.isAntiTheftEnabled,
                                onCheckedChange = { onToggleAntiTheft(tag) },
                                modifier = Modifier.size(36.dp),
                                colors = SwitchDefaults.colors(checkedThumbColor = MeadowGreenSecondary)
                            )
                        }

                        Text(
                            text = "Visto: ${dateFormat.format(Date(tag.lastSeenTimestamp))}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ações do Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onSelectRadarTag(tag) },
                            colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🎯 Radar", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onEditTag(tag) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Editar", fontSize = 12.sp)
                        }

                        IconButton(onClick = { onDeleteTag(tag) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir Tag", tint = WarningAlertRed)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// ABA 3: SCANNER DE NOVAS TAGS BLUETOOTH DESCOBERTAS
// =========================================================================
@Composable
private fun DiscoveredScannerTab(
    discoveredTags: List<DiscoveredBleTag>,
    bluetoothTags: List<BluetoothTagEntity>,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onAssignCandidateToHive: (DiscoveredBleTag) -> Unit
) {
    val registeredMacs = remember(bluetoothTags) {
        bluetoothTags.map { it.tagMacAddress.uppercase(Locale.ROOT) }.toSet()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("discovered_scanner_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dispositivos BLE Detectados", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = if (isScanning) "Varrendo canais BLE próximos..." else "Scanner pausado",
                            fontSize = 11.sp,
                            color = if (isScanning) MeadowGreenSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!isScanning) {
                        FilledTonalButton(onClick = onStartScan) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Iniciar")
                        }
                    }
                }
            }
        }

        if (discoveredTags.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.BluetoothSearching,
                            contentDescription = null,
                            tint = HoneyGoldPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Procurando tags e beacons no alcance...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Aproxime seu smartphone da colmeia ou ative 'Testar no Apiário'.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(discoveredTags, key = { it.macAddress }) { discTag ->
            val isAlreadyRegistered = registeredMacs.contains(discTag.macAddress.uppercase(Locale.ROOT))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(discTag.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Text(
                                    discTag.tagType,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text("MAC: ${discTag.macAddress}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "Sinal: ${discTag.rssi} dBm • Distância estimada: %.1f m".format(Locale.US, discTag.distanceMeters),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (discTag.distanceMeters < 2.0) MeadowGreenSecondary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (isAlreadyRegistered) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MeadowGreenSecondary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "✅ Vinculada",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MeadowGreenSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = { onAssignCandidateToHive(discTag) },
                            colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Vincular", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// DIÁLOGO: CADASTRAR / EDITAR / VINCULAR TAG BLUETOOTH À COLMEIA
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditBluetoothTagDialog(
    tagToEdit: BluetoothTagEntity?,
    prefilledMac: String?,
    hives: List<HiveEntity>,
    apiaries: List<ApiaryEntity>,
    onDismiss: () -> Unit,
    onSave: (name: String, mac: String, type: String, hiveId: Long?, apiaryId: Long?, antiTheft: Boolean, notes: String, id: Long) -> Unit
) {
    var tagName by remember { mutableStateOf(tagToEdit?.tagName ?: "") }
    var tagMac by remember { mutableStateOf(tagToEdit?.tagMacAddress ?: prefilledMac ?: "") }
    var tagType by remember { mutableStateOf(tagToEdit?.tagType ?: "SmartTag Apis Pro") }
    var selectedHiveId by remember { mutableStateOf(tagToEdit?.hiveId) }
    var antiTheft by remember { mutableStateOf(tagToEdit?.isAntiTheftEnabled ?: true) }
    var notes by remember { mutableStateOf(tagToEdit?.notes ?: "") }

    var hiveDropdownExpanded by remember { mutableStateOf(false) }

    val tagTypes = listOf("SmartTag Apis Pro", "RuuviTag Pro (Sensor)", "iBeacon / Anti-Furto", "Tile / SmartTag", "Inkbird Sensor BLE", "BLE Genérico")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (tagToEdit != null) "Editar Tag Bluetooth" else "Vincular Tag Bluetooth",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = tagMac,
                    onValueChange = { tagMac = it },
                    label = { Text("Endereço MAC da Tag (ex: C4:7C:8D:11:22:33)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("Nome da Tag (ex: SmartTag Colmeia 01)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Seletor de Colmeia
                val selectedHive = hives.find { it.id == selectedHiveId }
                ExposedDropdownMenuBox(
                    expanded = hiveDropdownExpanded,
                    onExpandedChange = { hiveDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedHive?.let { "${it.hiveNumber} (${apiaries.find { a -> a.id == it.apiaryId }?.name ?: ""})" } ?: "Nenhuma colmeia associada",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vincular à Colmeia") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hiveDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = hiveDropdownExpanded,
                        onDismissRequest = { hiveDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nenhuma (Não vincular no momento)") },
                            onClick = {
                                selectedHiveId = null
                                hiveDropdownExpanded = false
                            }
                        )
                        hives.forEach { hive ->
                            val apiary = apiaries.find { it.id == hive.apiaryId }
                            DropdownMenuItem(
                                text = { Text("${hive.hiveNumber} • ${apiary?.name ?: ""}") },
                                onClick = {
                                    selectedHiveId = hive.id
                                    if (tagName.isBlank()) {
                                        tagName = "Tag ${hive.hiveNumber}"
                                    }
                                    hiveDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Switch Alerta Anti-furto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Alerta Anti-Furto & Perda de Sinal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Notifica se o sinal cair drasticamente ou se a colmeia for movida.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = antiTheft,
                        onCheckedChange = { antiTheft = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MeadowGreenSecondary)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (tagMac.isNotBlank()) {
                                val linkedApiaryId = selectedHive?.apiaryId
                                onSave(
                                    tagName,
                                    tagMac,
                                    tagType,
                                    selectedHiveId,
                                    linkedApiaryId,
                                    antiTheft,
                                    notes,
                                    tagToEdit?.id ?: 0L
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Salvar Tag", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
