package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.HarvestEntity
import com.example.data.local.entities.BluetoothTagEntity
import com.example.data.local.entities.HiveEntity
import com.example.data.local.entities.HivePhotoEntity
import com.example.data.local.entities.HiveWeightEntity
import com.example.data.local.entities.InspectionEntity
import com.example.data.local.entities.QueenHistoryEntity
import com.example.ui.components.EditInspectionDialog
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.WarningAlertRed
import com.example.ui.viewmodel.ApisSmartViewModel
import com.example.ui.viewmodel.ScreenTab
import com.example.util.LocationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class HiveHistoryTab {
    TIMELINE,
    PRONTUARIO,
    PESAGENS_ESTOQUE,
    HISTORICO_RAINHA,
    FOTOS,
    INSPECOES,
    COLHEITAS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveDetailHistoryDialog(
    hive: HiveEntity,
    apiary: ApiaryEntity?,
    viewModel: ApisSmartViewModel,
    inspections: List<InspectionEntity>,
    harvests: List<HarvestEntity>,
    initialTab: HiveHistoryTab = HiveHistoryTab.TIMELINE,
    onDismiss: () -> Unit,
    onOpenMap: (Double, Double, String) -> Unit,
    onShareLocation: (Double, Double, String, String) -> Unit
) {
    var currentTab by remember { mutableStateOf(initialTab) }

    val queenHistory by viewModel.getQueenHistoryForHive(hive.id).collectAsState(initial = emptyList())
    val weightHistory by viewModel.getWeightsForHive(hive.id).collectAsState(initial = emptyList())
    val photoHistory by viewModel.getPhotosForHive(hive.id).collectAsState(initial = emptyList())

    val hiveInspections = inspections.filter { it.hiveId == hive.id }
    val hiveHarvests = harvests.filter { it.hiveId == hive.id }

    val bluetoothTags by viewModel.bluetoothTags.collectAsState()
    val linkedBluetoothTag = bluetoothTags.find { it.hiveId == hive.id }

    var showAddWeightDialog by remember { mutableStateOf(false) }
    var showAddQueenChangeDialog by remember { mutableStateOf(false) }
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var selectedPhotoForFullView by remember { mutableStateOf<HivePhotoEntity?>(null) }
    var showMapLocationDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(8.dp)
                .testTag("hive_detail_history_dialog"),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar no topo superior com botão simplificado X
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HoneyGoldPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(HoneyGoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🐝", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "Prontuário: ${hive.hiveNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${apiary?.name ?: "Apiário"} • ${hive.beeSpecies}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = { showMapLocationDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("hive_header_map_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Ver Mapa da Colmeia (Caixa)",
                                tint = HoneyGoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Botão simplificado X para fechar no topo superior
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                                    shape = CircleShape
                                )
                                .testTag("close_prontuario_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar Prontuário",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Scrollable Tabs for the Hive
                ScrollableTabRow(
                    selectedTabIndex = currentTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = HoneyGoldPrimary,
                    edgePadding = 12.dp
                ) {
                    Tab(
                        selected = currentTab == HiveHistoryTab.TIMELINE,
                        onClick = { currentTab = HiveHistoryTab.TIMELINE },
                        text = { Text("⏳ Linha do Tempo", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = currentTab == HiveHistoryTab.PRONTUARIO,
                        onClick = { currentTab = HiveHistoryTab.PRONTUARIO },
                        text = { Text("📋 Prontuário", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = currentTab == HiveHistoryTab.PESAGENS_ESTOQUE,
                        onClick = { currentTab = HiveHistoryTab.PESAGENS_ESTOQUE },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚖️ Peso & Mel", fontWeight = FontWeight.Bold)
                                if (hive.estimatedHoneyStockKg != null && hive.estimatedHoneyStockKg < 4.0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("⚠️", fontSize = 11.sp)
                                }
                            }
                        }
                    )
                    Tab(
                        selected = currentTab == HiveHistoryTab.HISTORICO_RAINHA,
                        onClick = { currentTab = HiveHistoryTab.HISTORICO_RAINHA },
                        text = { Text("👑 Rainha & Trocas (${queenHistory.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = currentTab == HiveHistoryTab.FOTOS,
                        onClick = { currentTab = HiveHistoryTab.FOTOS },
                        text = { Text("📸 Fotos (${photoHistory.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = currentTab == HiveHistoryTab.INSPECOES,
                        onClick = { currentTab = HiveHistoryTab.INSPECOES },
                        text = { Text("🔍 Inspeções (${hiveInspections.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = currentTab == HiveHistoryTab.COLHEITAS,
                        onClick = { currentTab = HiveHistoryTab.COLHEITAS },
                        text = { Text("🍯 Colheitas (${hiveHarvests.size})", fontWeight = FontWeight.Bold) }
                    )
                }

                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    when (currentTab) {
                        HiveHistoryTab.TIMELINE -> {
                            HiveTimelineTabContent(
                                hive = hive,
                                apiary = apiary,
                                inspections = hiveInspections,
                                weights = weightHistory,
                                queens = queenHistory,
                                photos = photoHistory,
                                harvests = hiveHarvests,
                                dateFormat = dateFormat
                            )
                        }

                        HiveHistoryTab.PRONTUARIO -> {
                            HiveOverviewTabContent(
                                hive = hive,
                                apiary = apiary,
                                weightHistory = weightHistory,
                                queenHistory = queenHistory,
                                photoHistory = photoHistory,
                                inspectionsCount = hiveInspections.size,
                                harvestsCount = hiveHarvests.size,
                                linkedTag = linkedBluetoothTag,
                                onLocateTagOnRadar = {
                                    if (linkedBluetoothTag != null) {
                                        viewModel.selectTargetRadarTag(linkedBluetoothTag)
                                        onDismiss()
                                        viewModel.navigateTo(ScreenTab.BLUETOOTH_TAGS)
                                    }
                                },
                                onOpenMap = { _, _ -> showMapLocationDialog = true },
                                onShare = { lat, lon -> onShareLocation(lat, lon, "Colmeia ${hive.hiveNumber}", "Espécie: ${hive.beeSpecies}") },
                                onOpenAddPhoto = { showAddPhotoDialog = true },
                                onOpenPhotosTab = { currentTab = HiveHistoryTab.FOTOS }
                            )
                        }

                        HiveHistoryTab.PESAGENS_ESTOQUE -> {
                            HiveWeightHistoryTabContent(
                                hive = hive,
                                weightHistory = weightHistory,
                                dateFormat = dateFormat,
                                onOpenAddWeight = { showAddWeightDialog = true }
                            )
                        }

                        HiveHistoryTab.HISTORICO_RAINHA -> {
                            HiveQueenHistoryTabContent(
                                hive = hive,
                                queenHistory = queenHistory,
                                dateFormat = dateFormat,
                                onOpenRecordChange = { showAddQueenChangeDialog = true }
                            )
                        }

                        HiveHistoryTab.FOTOS -> {
                            HivePhotosTabContent(
                                photos = photoHistory,
                                hive = hive,
                                dateFormat = dateFormat,
                                onOpenAddPhoto = { showAddPhotoDialog = true },
                                onSelectPhoto = { selectedPhotoForFullView = it }
                            )
                        }

                        HiveHistoryTab.INSPECOES -> {
                            HiveInspectionsHistoryTabContent(
                                inspections = hiveInspections,
                                dateFormat = dateFormat,
                                hive = hive,
                                apiary = apiary,
                                onUpdateInspection = { viewModel.updateInspection(it) }
                            )
                        }

                        HiveHistoryTab.COLHEITAS -> {
                            HiveHarvestsHistoryTabContent(
                                hive = hive,
                                harvests = hiveHarvests,
                                dateFormat = dateFormat
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Adicionar Foto da Colmeia
    if (showMapLocationDialog) {
        HiveMapLocationDialog(
            hive = hive,
            apiary = apiary,
            onDismiss = { showMapLocationDialog = false },
            onUpdateGps = { lat, lon ->
                viewModel.updateHiveGps(hive.id, lat, lon)
                showMapLocationDialog = false
            }
        )
    }

    if (showAddPhotoDialog) {
        AddHivePhotoDialog(
            hive = hive,
            onDismiss = { showAddPhotoDialog = false },
            onSavePhoto = { uri, category, caption, notes, inspector ->
                viewModel.saveHivePhoto(hive.id, uri, category, caption, notes, inspector)
                showAddPhotoDialog = false
            }
        )
    }

    // Modal: Visualizador de Foto em Tela Cheia
    selectedPhotoForFullView?.let { photo ->
        FullPhotoViewDialog(
            photo = photo,
            hive = hive,
            onDismiss = { selectedPhotoForFullView = null },
            onDelete = {
                viewModel.deleteHivePhoto(it)
                selectedPhotoForFullView = null
            }
        )
    }

    // Modal: Adicionar Pesagem
    if (showAddWeightDialog) {
        AddHiveWeightDialog(
            hive = hive,
            onDismiss = { showAddWeightDialog = false },
            onSave = { totalWeight, tare, notes ->
                viewModel.recordHiveWeight(hive.id, totalWeight, tare, notes)
                showAddWeightDialog = false
            }
        )
    }

    // Modal: Registrar Troca de Rainha
    if (showAddQueenChangeDialog) {
        RecordQueenChangeDialog(
            hive = hive,
            onDismiss = { showAddQueenChangeDialog = false },
            onSave = { prevLineage, prevColor, newLineage, newColor, reason, method, notes ->
                viewModel.recordQueenChange(hive.id, prevLineage, prevColor, newLineage, newColor, reason, method, notes)
                showAddQueenChangeDialog = false
            }
        )
    }
}

// -------------------------------------------------------------
// TAB 1: PRONTUÁRIO & STATUS GERAL
// -------------------------------------------------------------
@Composable
fun HiveOverviewTabContent(
    hive: HiveEntity,
    apiary: ApiaryEntity?,
    weightHistory: List<HiveWeightEntity>,
    queenHistory: List<QueenHistoryEntity>,
    photoHistory: List<HivePhotoEntity> = emptyList(),
    inspectionsCount: Int,
    harvestsCount: Int,
    linkedTag: BluetoothTagEntity? = null,
    onLocateTagOnRadar: (() -> Unit)? = null,
    onOpenMap: (Double, Double) -> Unit,
    onShare: (Double, Double) -> Unit,
    onOpenAddPhoto: () -> Unit = {},
    onOpenPhotosTab: () -> Unit = {}
) {
    val currentHoney = hive.estimatedHoneyStockKg ?: (weightHistory.firstOrNull()?.estimatedHoneyKg ?: 0.0)
    val maxHoneyCapacity = (hive.supersCount * hive.framesPerSuper * hive.estimatedYieldPerFrameKg).coerceAtLeast(10.0)
    val progress = (currentHoney / maxHoneyCapacity).toFloat().coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Card de Rastreabilidade BLE da Colmeia
        if (linkedTag != null) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BluetoothConnected,
                                contentDescription = null,
                                tint = HoneyGoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Tag BLE: ${linkedTag.tagName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "MAC: ${linkedTag.tagMacAddress} • ${linkedTag.tagType}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (onLocateTagOnRadar != null) {
                            Button(
                                onClick = onLocateTagOnRadar,
                                colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("🎯 Radar", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Distância estimada: ~%.1f m (%d dBm)".format(Locale.US, linkedTag.lastDistanceMeters, linkedTag.lastRssi),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MeadowGreenSecondary
                        )
                        if (linkedTag.batteryPercent != null) {
                            Text(
                                text = "🔋 ${linkedTag.batteryPercent}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (linkedTag.temperatureC != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🌡️ Sensor de Ninho: %.1f °C • Umidade: %.0f %%".format(Locale.US, linkedTag.temperatureC, linkedTag.humidityPercent ?: 0.0),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Stock & Weight Hero Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🍯 Estoque de Mel Estimado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${String.format(Locale.US, "%.1f", currentHoney)} kg",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (currentHoney < 4.0) WarningAlertRed else HoneyGoldPrimary
                        )
                        Text(
                            text = "≈ ${String.format(Locale.US, "%.1f", currentHoney / 1.42)} Litros disponíveis",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Peso Bruto Atual:", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${hive.currentWeightKg ?: (hive.tareWeightKg + currentHoney)} kg",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Tara: ${hive.tareWeightKg} kg", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ocupação do Estoque:", style = MaterialTheme.typography.labelSmall)
                        Text("${(progress * 100).toInt()}% da capacidade (~${String.format(Locale.US, "%.1f", maxHoneyCapacity)} kg)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (currentHoney < 4.0) WarningAlertRed else HoneyGoldPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Active Queen Details Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👑 Rainha Atual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    QueenColorBadge(colorYear = hive.queenColor)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("• Linhagem: ${hive.queenLineage}", style = MaterialTheme.typography.bodyMedium)
                Text("• Status: ${hive.queenStatus}", style = MaterialTheme.typography.bodyMedium)
                Text("• Idade Estimada: ${hive.queenAgeMonths} meses" + (if (hive.queenAgeMonths >= 18) " ⚠️ (Recomenda-se troca preventiva)" else ""), style = MaterialTheme.typography.bodyMedium)
                Text("• Mansidão & Temperamento: ${hive.temperament}", style = MaterialTheme.typography.bodyMedium)
                Text("• Força da Colônia: ${hive.strengthLevel}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Hive Structure & Specs
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📦 Estrutura & Madeiramento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Modelo de Colmeia: ${hive.hiveType}", style = MaterialTheme.typography.bodyMedium)
                Text("• Dimensões: ${hive.hiveDimensions}", style = MaterialTheme.typography.bodyMedium)
                Text("• Quadros no Ninho: ${hive.framesCount} quadros", style = MaterialTheme.typography.bodyMedium)
                Text("• Melgueiras Instaladas: ${hive.supersCount} (${hive.framesPerSuper} quadros por melgueira)", style = MaterialTheme.typography.bodyMedium)
                Text("• Rendimento Médio Estimado: ${hive.estimatedYieldPerFrameKg} kg / quadro", style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Geolocation Card
        if (hive.latitude != null && hive.longitude != null) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("📍 Ponto Georreferenciado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = LocationHelper.formatCoordinates(hive.latitude, hive.longitude) + (hive.altitude?.let { " • Altitude: ${it.toInt()}m" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row {
                            OutlinedButton(
                                onClick = { onOpenMap(hive.latitude, hive.longitude) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Navegar", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = { onShare(hive.latitude, hive.longitude) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Enviar", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Quick Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$inspectionsCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Inspeções", style = MaterialTheme.typography.labelSmall)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$harvestsCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Colheitas", style = MaterialTheme.typography.labelSmall)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${queenHistory.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Rainhas", style = MaterialTheme.typography.labelSmall)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenPhotosTab() },
                colors = CardDefaults.cardColors(containerColor = HoneyGoldPrimary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${photoHistory.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = HoneyGoldPrimary)
                    Text("📸 Fotos", style = MaterialTheme.typography.labelSmall, color = HoneyGoldPrimary)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: PESAGENS & ESTOQUE DE MEL
// -------------------------------------------------------------
@Composable
fun HiveWeightHistoryTabContent(
    hive: HiveEntity,
    weightHistory: List<HiveWeightEntity>,
    dateFormat: SimpleDateFormat,
    onOpenAddWeight: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("⚖️ Histórico de Pesagens da Colmeia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Monitoramento de entrada de néctar e reservas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onOpenAddWeight,
                    colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nova Pesagem", fontSize = 12.sp)
                }
            }
        }

        if (weightHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚖️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhuma pesagem registrada.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Toque em '+ Nova Pesagem' para registrar o peso da colmeia na balança.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(weightHistory, key = { it.id }) { record ->
                WeightRecordCard(record = record, dateFormat = dateFormat)
            }
        }
    }
}

@Composable
fun WeightRecordCard(record: HiveWeightEntity, dateFormat: SimpleDateFormat) {
    val isGain = (record.weightDeltaKg ?: 0.0) > 0.0
    val isLoss = (record.weightDeltaKg ?: 0.0) < 0.0

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📅 ${dateFormat.format(Date(record.date))}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Peso Bruto: ${record.totalWeightKg} kg",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Delta badge
                if (record.weightDeltaKg != null && record.weightDeltaKg != 0.0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isGain) MeadowGreenSecondary.copy(alpha = 0.15f) else WarningAlertRed.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isGain) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isGain) MeadowGreenSecondary else WarningAlertRed
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (isGain) "+" else ""}${String.format(Locale.US, "%.1f", record.weightDeltaKg)} kg",
                                color = if (isGain) MeadowGreenSecondary else WarningAlertRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🍯 Estoque de Mel Estimado: ${String.format(Locale.US, "%.1f", record.estimatedHoneyKg)} kg (Tara: ${record.tareWeightKg}kg)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (record.estimatedHoneyKg < 4.0) WarningAlertRed else HoneyGoldPrimary
                )
            }

            Text(
                text = "Status: ${record.weightStatus}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (record.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notas: ${record.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: HISTÓRICO DA RAINHA & TROCAS
// -------------------------------------------------------------
@Composable
fun HiveQueenHistoryTabContent(
    hive: HiveEntity,
    queenHistory: List<QueenHistoryEntity>,
    dateFormat: SimpleDateFormat,
    onOpenRecordChange: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("👑 Manejo de Rainhas & Trocas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Registro genético e substituições na colmeia", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onOpenRecordChange,
                    colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Registrar Troca", fontSize = 12.sp)
                }
            }
        }

        // Active Queen Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HoneyGoldPrimary.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(HoneyGoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Rainha em Atividade:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(hive.queenLineage, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Cor do Ano: ${hive.queenColor} • Idade: ${hive.queenAgeMonths} meses", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (queenHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👑", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhuma troca anterior registrada.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("A rainha atual é a matriz registrada no cadastro inicial.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(queenHistory, key = { it.id }) { event ->
                QueenChangeEventCard(event = event, dateFormat = dateFormat)
            }
        }
    }
}

@Composable
fun QueenChangeEventCard(event: QueenHistoryEntity, dateFormat: SimpleDateFormat) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📅 ${dateFormat.format(Date(event.date))}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                QueenColorBadge(colorYear = event.newQueenColor)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("Nova Rainha: ${event.newQueenLineage}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Rainha Anterior: ${event.previousQueenLineage} (${event.previousQueenColor})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(4.dp))

            Text("• Motivo: ${event.reason}", style = MaterialTheme.typography.bodySmall)
            Text("• Método: ${event.introductionMethod}", style = MaterialTheme.typography.bodySmall)

            if (event.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Notas: ${event.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 4: HISTÓRICO DE INSPEÇÕES
// -------------------------------------------------------------
@Composable
fun HiveInspectionsHistoryTabContent(
    inspections: List<InspectionEntity>,
    dateFormat: SimpleDateFormat,
    hive: HiveEntity? = null,
    apiary: ApiaryEntity? = null,
    onUpdateInspection: (InspectionEntity) -> Unit = {}
) {
    var editingInspection by remember { mutableStateOf<InspectionEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (inspections.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma inspeção de campo registrada para esta colmeia.")
                }
            }
        } else {
            items(inspections, key = { it.id }) { insp ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📅 ${dateFormat.format(Date(insp.date))}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                HealthBadge(status = if (insp.aiSanitaryAlert) "Alerta Sanitário" else "Boa")
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(onClick = { editingInspection = insp }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar Inspeção", tint = HoneyGoldPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Inspetor: ${insp.inspectorName}", style = MaterialTheme.typography.bodySmall)
                        Text("• Rainha Vista: ${if (insp.queenSeen) "Sim ✅" else "Não ❌"} • Postura: ${insp.broodPattern}", style = MaterialTheme.typography.bodySmall)
                        Text("• Reservas Mel: ${insp.honeyStores} • Pólen: ${insp.pollenStores}", style = MaterialTheme.typography.bodySmall)
                        Text("• Praga: ${insp.pestDetected} (${insp.pestSeverity})", style = MaterialTheme.typography.bodySmall)

                        if (insp.notesRaw.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Relato: ${insp.notesRaw}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (editingInspection != null) {
        EditInspectionDialog(
            inspection = editingInspection!!,
            hiveNumber = hive?.hiveNumber ?: "Colmeia",
            apiaryName = apiary?.name ?: "Apiário",
            onDismiss = { editingInspection = null },
            onSaveInspection = { updated ->
                editingInspection = null
                onUpdateInspection(updated)
            }
        )
    }
}

// -------------------------------------------------------------
// TAB 5: HISTÓRICO DE COLHEITAS
// -------------------------------------------------------------
@Composable
fun HiveHarvestsHistoryTabContent(
    hive: HiveEntity,
    harvests: List<HarvestEntity>,
    dateFormat: SimpleDateFormat
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (harvests.isNotEmpty()) {
            item {
                HoneyProductionGrowthChart(
                    harvests = harvests,
                    hives = listOf(hive)
                )
            }
        }

        if (harvests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma colheita individual registrada para esta colmeia.")
                }
            }
        } else {
            items(harvests, key = { it.id }) { har ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${har.quantityKg} kg de ${har.productType}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = HoneyGoldPrimary)
                            Text("📅 ${dateFormat.format(Date(har.date))}", style = MaterialTheme.typography.labelMedium)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("• Melgueiras: ${har.supersHarvestedCount ?: 1} (${har.framesPerSuper ?: 10}q/melg)", style = MaterialTheme.typography.bodySmall)
                        Text("• Florada: ${har.floralSource}", style = MaterialTheme.typography.bodySmall)
                        if (har.moisturePercent != null) {
                            Text("• Umidade: ${har.moisturePercent}%", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOG: ADICIONAR PESAGEM DA COLMEIA
// -------------------------------------------------------------
@Composable
fun AddHiveWeightDialog(
    hive: HiveEntity,
    onDismiss: () -> Unit,
    onSave: (totalWeight: Double, tare: Double, notes: String) -> Unit
) {
    var totalWeightText by remember { mutableStateOf(hive.currentWeightKg?.toString() ?: "38.0") }
    var tareWeightText by remember { mutableStateOf(hive.tareWeightKg.toString()) }
    var notes by remember { mutableStateOf("") }

    val totalWeight = totalWeightText.toDoubleOrNull() ?: 0.0
    val tareWeight = tareWeightText.toDoubleOrNull() ?: 18.0
    val estimatedHoney = (totalWeight - tareWeight).coerceAtLeast(0.0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().testTag("add_hive_weight_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    tonalElevation = 3.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚖️ Pesagem da Colmeia",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Button(
                            onClick = {
                                if (totalWeight > 0) {
                                    onSave(totalWeight, tareWeight, notes)
                                }
                            },
                            enabled = totalWeight > 0
                        ) {
                            Text("Salvar")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), CircleShape)
                                .testTag("weight_dialog_close_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Aferição na balança mecânica ou digital de gancho para ${hive.hiveNumber}:")

                OutlinedTextField(
                    value = totalWeightText,
                    onValueChange = { totalWeightText = it },
                    label = { Text("Peso Bruto Total na Balança (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tareWeightText,
                    onValueChange = { tareWeightText = it },
                    label = { Text("Tara da Estrutura (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Peso da madeira + quadros vazios + cera + enxame base") }
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = HoneyGoldPrimary.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("ESTOQUE DE MEL ESTIMADO:", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${String.format(Locale.US, "%.1f", estimatedHoney)} kg (≈ ${String.format(Locale.US, "%.1f", estimatedHoney / 1.42)} Litros)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (estimatedHoney < 4.0) WarningAlertRed else HoneyGoldPrimary
                        )
                        if (estimatedHoney < 4.0) {
                            Text("⚠️ Alerta: Estoque crítico para a sobrevivência do enxame!", fontSize = 11.sp, color = WarningAlertRed)
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Anotações da Pesagem (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        }
    }
}
}

// -------------------------------------------------------------
// DIALOG: REGISTRAR TROCA DE RAINHA
// -------------------------------------------------------------
@Composable
fun RecordQueenChangeDialog(
    hive: HiveEntity,
    onDismiss: () -> Unit,
    onSave: (prevLineage: String, prevColor: String, newLineage: String, newColor: String, reason: String, method: String, notes: String) -> Unit
) {
    var previousLineage by remember { mutableStateOf(hive.queenLineage) }
    var previousColor by remember { mutableStateOf(hive.queenColor) }

    var newLineage by remember { mutableStateOf("Italiana Selecionada F1") }
    var newColor by remember { mutableStateOf("Branco (2026 / 2031)") }
    var reason by remember { mutableStateOf("Substituição por Idade Avançada (>18 meses)") }
    var method by remember { mutableStateOf("Gaiola de Introdução com Pasta Cândi") }
    var notes by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().testTag("record_queen_change_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    tonalElevation = 3.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👑 Troca de Rainha",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Button(
                            onClick = {
                                if (newLineage.isNotBlank()) {
                                    onSave(previousLineage, previousColor, newLineage, newColor, reason, method, notes)
                                }
                            },
                            enabled = newLineage.isNotBlank()
                        ) {
                            Text("Confirmar")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), CircleShape)
                                .testTag("queen_dialog_close_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Registro de substituição da matriz na ${hive.hiveNumber}:")

                    OutlinedTextField(
                        value = newLineage,
                        onValueChange = { newLineage = it },
                        label = { Text("Linhagem da Nova Rainha") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newColor,
                        onValueChange = { newColor = it },
                        label = { Text("Cor Internacional de Marcação") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Motivo da Substituição") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = method,
                        onValueChange = { method = it },
                        label = { Text("Método de Introdução") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas do Manejo") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: GALERIA DE FOTOS DA COLMEIA
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HivePhotosTabContent(
    photos: List<HivePhotoEntity>,
    hive: HiveEntity,
    dateFormat: SimpleDateFormat,
    onOpenAddPhoto: () -> Unit,
    onSelectPhoto: (HivePhotoEntity) -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf("Todas") }

    val filteredPhotos = if (selectedCategoryFilter == "Todas") {
        photos
    } else {
        photos.filter { it.category == selectedCategoryFilter }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Row with Add Photo Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📸 Fotos da Colmeia (${photos.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Acompanhamento visual de postura, melgueiras e sanidade",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onOpenAddPhoto,
                colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                modifier = Modifier.testTag("add_photo_tab_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar Foto", fontSize = 13.sp)
            }
        }

        // Category Filter Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filterOptions = listOf("Todas") + HIVE_PHOTO_CATEGORIES
            filterOptions.forEach { cat ->
                val count = if (cat == "Todas") photos.size else photos.count { it.category == cat }
                if (count > 0 || cat == "Todas") {
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text("$cat ($count)", fontSize = 11.sp) }
                    )
                }
            }
        }

        if (filteredPhotos.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = HoneyGoldPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (photos.isEmpty()) "Nenhuma foto cadastrada nesta colmeia" else "Nenhuma foto nesta categoria",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Registre fotos do ninho, postura, rainha, melgueiras e entrada para acompanhamento temporal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenAddPhoto,
                        colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                    ) {
                        Text("Adicionar Primeira Foto")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPhotos) { photo ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPhoto(photo) }
                            .testTag("hive_photo_item_${photo.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                            ) {
                                HivePhotoThumbnail(
                                    photoUri = photo.photoUri,
                                    contentDescription = photo.caption,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = photo.category,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = if (photo.caption.isNotBlank()) photo.caption else "Foto de Manejo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateFormat.format(Date(photo.dateTaken)),
                                    fontSize = 10.sp,
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

private data class TimelineItem(
    val id: String,
    val timestamp: Long,
    val category: String, // "Inspeção", "Pesagem", "Rainha", "Colheita", "Foto", "Cadastro"
    val title: String,
    val subtitle: String,
    val notes: String,
    val emoji: String,
    val badgeColor: Color,
    val photoUri: String? = null,
    val keyMetrics: List<Pair<String, String>> = emptyList()
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HiveTimelineTabContent(
    hive: HiveEntity,
    apiary: ApiaryEntity?,
    inspections: List<InspectionEntity>,
    weights: List<HiveWeightEntity>,
    queens: List<QueenHistoryEntity>,
    photos: List<HivePhotoEntity>,
    harvests: List<HarvestEntity>,
    dateFormat: SimpleDateFormat
) {
    var selectedCategoryFilter by remember { mutableStateOf("Todas") }
    var searchQuery by remember { mutableStateOf("") }

    val fullTimeline = remember(hive, inspections, weights, queens, photos, harvests) {
        val list = mutableListOf<TimelineItem>()

        // 1. Inspections
        inspections.forEach { insp ->
            val metrics = mutableListOf<Pair<String, String>>()
            metrics.add("Sanidade" to if (insp.aiSanitaryAlert) "Alerta Sanitário ⚠️" else "Normal")
            metrics.add("Rainha" to if (insp.queenSeen) "Avistada" else "Não vista")
            metrics.add("Cria" to insp.broodPattern)
            if (insp.pestDetected.isNotBlank() && insp.pestDetected != "Nenhuma") {
                metrics.add("Pragas" to "${insp.pestDetected} (${insp.pestSeverity})")
            }
            if (insp.honeyStores.isNotBlank()) {
                metrics.add("Mel" to insp.honeyStores)
            }

            val notesText = if (insp.notesRaw.isNotBlank()) {
                insp.notesRaw + if (insp.actionsTaken.isNotBlank()) " | Ação: ${insp.actionsTaken}" else ""
            } else {
                insp.actionsTaken
            }

            list.add(
                TimelineItem(
                    id = "insp_${insp.id}",
                    timestamp = insp.date,
                    category = "Inspeções",
                    title = "🔍 Inspeção de Campo",
                    subtitle = "Inspetor: ${insp.inspectorName.ifBlank { "Não informado" }}",
                    notes = notesText,
                    emoji = "🔍",
                    badgeColor = MeadowGreenSecondary,
                    keyMetrics = metrics
                )
            )
        }

        // 2. Weights & Stock
        val sortedWeights = weights.sortedBy { it.date }
        sortedWeights.forEachIndexed { idx, w ->
            val prevWeight = if (idx > 0) sortedWeights[idx - 1].totalWeightKg else null
            val diff = if (prevWeight != null) w.totalWeightKg - prevWeight else w.weightDeltaKg

            val metrics = mutableListOf<Pair<String, String>>()
            metrics.add("Peso Bruto" to "${w.totalWeightKg} kg")
            metrics.add("Estoque Mel Est." to "${String.format(Locale.US, "%.1f", w.estimatedHoneyKg)} kg")
            metrics.add("Status" to w.weightStatus)
            if (diff != null) {
                val symbol = if (diff >= 0) "+${String.format(Locale.US, "%.1f", diff)} kg 📈" else "${String.format(Locale.US, "%.1f", diff)} kg 📉"
                metrics.add("Variação de Peso" to symbol)
            }

            list.add(
                TimelineItem(
                    id = "weight_${w.id}",
                    timestamp = w.date,
                    category = "Pesagens",
                    title = "⚖️ Aferição de Peso & Mel",
                    subtitle = "Status: ${w.weightStatus}",
                    notes = w.notes,
                    emoji = "⚖️",
                    badgeColor = HoneyGoldPrimary,
                    keyMetrics = metrics
                )
            )
        }

        // 3. Queen Changes
        queens.forEach { q ->
            val metrics = mutableListOf<Pair<String, String>>()
            metrics.add("Motivo" to q.reason)
            metrics.add("Linhagem" to q.newQueenLineage)
            metrics.add("Ano/Cor" to q.newQueenColor)
            metrics.add("Método" to q.introductionMethod)

            list.add(
                TimelineItem(
                    id = "queen_${q.id}",
                    timestamp = q.date,
                    category = "Rainha",
                    title = "👑 Troca / Introdução de Rainha",
                    subtitle = "Método: ${q.introductionMethod}",
                    notes = q.notes,
                    emoji = "👑",
                    badgeColor = Color(0xFF9C27B0),
                    keyMetrics = metrics
                )
            )
        }

        // 4. Harvests
        harvests.forEach { h ->
            val metrics = mutableListOf<Pair<String, String>>()
            metrics.add("Colhido" to "${h.quantityKg} kg")
            h.framesHarvestedCount?.let { metrics.add("Quadros" to "$it") }
            h.moisturePercent?.let { metrics.add("Umidade" to "$it%") }
            if (h.floralSource.isNotBlank()) metrics.add("Florada" to h.floralSource)

            list.add(
                TimelineItem(
                    id = "harvest_${h.id}",
                    timestamp = h.date,
                    category = "Colheitas",
                    title = "🍯 Colheita: ${h.productType} (${h.quantityKg} kg)",
                    subtitle = "Florada: ${h.floralSource} • Apiário: ${apiary?.name ?: "N/A"}",
                    notes = h.notes,
                    emoji = "🍯",
                    badgeColor = Color(0xFFFF8F00),
                    keyMetrics = metrics
                )
            )
        }

        // 5. Photos
        photos.forEach { p ->
            list.add(
                TimelineItem(
                    id = "photo_${p.id}",
                    timestamp = p.dateTaken,
                    category = "Fotos",
                    title = "📸 Registro Fotográfico: ${p.category}",
                    subtitle = "Inspetor: ${p.inspectorName.ifBlank { "Não informado" }}",
                    notes = p.caption + if (p.notes.isNotBlank()) " — ${p.notes}" else "",
                    emoji = "📸",
                    badgeColor = Color(0xFF0288D1),
                    photoUri = p.photoUri
                )
            )
        }

        // 6. Hive Registration / Creation
        list.add(
            TimelineItem(
                id = "hive_creation",
                timestamp = hive.locationTimestamp ?: hive.lastInspectionDate ?: System.currentTimeMillis() - (86400000L * 30),
                category = "Cadastro",
                title = "🐝 Cadastro Inicial da Colmeia ${hive.hiveNumber}",
                subtitle = "Apiário: ${apiary?.name ?: "Não atribuído"}",
                notes = "Instalação da colmeia da espécie ${hive.beeSpecies} (${hive.speciesCategory}) no modelo ${hive.hiveType}.",
                emoji = "🐝",
                badgeColor = Color(0xFF43A047),
                keyMetrics = listOf(
                    "Espécie" to hive.beeSpecies,
                    "Modelo" to hive.hiveType,
                    "Quadros Ninho" to "${hive.framesCount}",
                    "Melgueiras Inicial" to "${hive.supersCount}"
                )
            )
        )

        list.sortedByDescending { it.timestamp }
    }

    val filteredTimeline = fullTimeline.filter { item ->
        val matchesCategory = (selectedCategoryFilter == "Todas" || item.category == selectedCategoryFilter)
        val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.notes.contains(searchQuery, ignoreCase = true) ||
                item.subtitle.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Summary Header Bar
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HoneyGoldPrimary.copy(alpha = 0.12f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "⏱️ Linha do Tempo da Colmeia ${hive.hiveNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${fullTimeline.size} eventos registrados desde a instalação inicial",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HoneyGoldPrimary,
                    contentColor = Color.White
                ) {
                    Text(
                        text = "${filteredTimeline.size} Exibidos",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Search Bar & Filter Chips
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar na linha do tempo...", fontSize = 13.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("timeline_search_input"),
            shape = RoundedCornerShape(12.dp)
        )

        // Single horizontal line for timeline filters
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 10.dp)
        ) {
            val categories = listOf("Todas", "Inspeções", "Pesagens", "Rainha", "Colheitas", "Fotos", "Cadastro")
            categories.forEach { cat ->
                val count = if (cat == "Todas") fullTimeline.size else fullTimeline.count { it.category == cat }
                if (count > 0 || cat == "Todas") {
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text("$cat ($count)", fontSize = 11.sp) }
                    )
                }
            }
        }

        if (filteredTimeline.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⏳", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nenhum evento encontrado na linha do tempo",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tente alterar os filtros ou realizar novas inspeções/pesagens.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTimeline, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("timeline_item_${item.id}"),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left Column: Date & Connecting Timeline Line
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(60.dp)
                                .padding(top = 4.dp)
                        ) {
                            val formattedDate = dateFormat.format(Date(item.timestamp))
                            Text(
                                text = formattedDate.take(5), // dd/MM
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formattedDate.takeLast(4), // yyyy
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Circular Event Badge Node
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(item.badgeColor)
                                    .shadow(2.dp, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.emoji, fontSize = 16.sp)
                            }

                            // Vertical Line connector
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(48.dp)
                                    .background(item.badgeColor.copy(alpha = 0.4f))
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right Column: Detailed Event Card
                        ElevatedCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )

                                if (item.keyMetrics.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        item.keyMetrics.forEach { (label, value) ->
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "$label: ",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = value,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (item.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp
                                    )
                                }

                                if (item.photoUri != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    ) {
                                        HivePhotoThumbnail(
                                            photoUri = item.photoUri,
                                            contentDescription = item.title,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                // Linha inferior: Label e Badge da Situação
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), thickness = 0.8.dp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Situação: ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = item.badgeColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = item.category,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = item.badgeColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
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

