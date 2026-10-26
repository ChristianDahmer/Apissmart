package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.HiveNamingHelper
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.BeeTrapEntity
import com.example.data.local.entities.HiveEntity
import com.example.ui.components.HealthBadge
import com.example.ui.components.HiveMapLocationDialog
import com.example.ui.components.MapLocationPickerDialog
import com.example.ui.components.QueenColorBadge
import com.example.ui.components.QueenColorTableDialog
import com.example.ui.components.WeatherForecastModalDialog
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.WarningAlertRed
import com.example.util.LocationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ApiaryScreenSubTab {
    HIVES_AND_APIARIES,
    BEE_TRAPS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiariesScreen(
    apiaries: List<ApiaryEntity>,
    hives: List<HiveEntity>,
    beeTraps: List<BeeTrapEntity>,
    capturedTrapsCount: Int,
    activeTrapsCount: Int,
    onSaveApiary: (name: String, location: String, flora: String, capacity: Int, notes: String, lat: Double?, lon: Double?, quickHivesCount: Int, beeSpecies: String, speciesCategory: String, hiveType: String, id: Long) -> Unit,
    onDeleteApiary: (ApiaryEntity) -> Unit,
    onSaveHive: (
        apiaryId: Long,
        hiveNumber: String,
        beeSpecies: String,
        speciesCategory: String,
        hiveType: String,
        hiveDimensions: String,
        queenStatus: String,
        queenColor: String,
        queenLineage: String,
        queenAgeMonths: Int,
        strengthLevel: String,
        temperament: String,
        framesCount: Int,
        supersCount: Int,
        framesPerSuper: Int,
        estimatedYieldPerFrameKg: Double,
        healthStatus: String,
        qrCode: String,
        latitude: Double?,
        longitude: Double?,
        altitude: Double?,
        locationAccuracy: Float?,
        currentWeightKg: Double?,
        tareWeightKg: Double,
        notes: String,
        id: Long
    ) -> Unit,
    onDeleteHive: (HiveEntity) -> Unit,
    onInspectHive: (ApiaryEntity, HiveEntity) -> Unit,
    onShowHiveQr: (ApiaryEntity, HiveEntity) -> Unit,
    onOpenHiveHistory: (HiveEntity) -> Unit,
    onSaveBeeTrap: (
        name: String,
        trapType: String,
        targetSpecies: String,
        attractantUsed: String,
        status: String,
        latitude: Double?,
        longitude: Double?,
        altitude: Double?,
        locationAccuracy: Float?,
        locationDescription: String,
        apiaryId: Long?,
        notes: String,
        id: Long
    ) -> Unit,
    onDeleteBeeTrap: (BeeTrapEntity) -> Unit,
    onRecordTrapCheck: (trap: BeeTrapEntity, isCaptured: Boolean, notes: String) -> Unit,
    onTransferTrapToHive: (
        trap: BeeTrapEntity,
        apiaryId: Long,
        hiveNumber: String,
        hiveType: String,
        beeSpecies: String,
        speciesCategory: String,
        framesCount: Int,
        notes: String
    ) -> Unit,
    onOpenGeneralMap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentSubTab by remember { mutableStateOf(ApiaryScreenSubTab.HIVES_AND_APIARIES) }

    // Dialog state holders
    var showAddApiaryDialog by remember { mutableStateOf(false) }
    var editingApiary by remember { mutableStateOf<ApiaryEntity?>(null) }
    var cloningApiary by remember { mutableStateOf<ApiaryEntity?>(null) }
    var showAddHiveDialogForApiary by remember { mutableStateOf<ApiaryEntity?>(null) }
    var editingHive by remember { mutableStateOf<HiveEntity?>(null) }
    var cloningHive by remember { mutableStateOf<HiveEntity?>(null) }
    var showAddTrapDialog by remember { mutableStateOf(false) }
    var editingTrap by remember { mutableStateOf<BeeTrapEntity?>(null) }
    var cloningTrap by remember { mutableStateOf<BeeTrapEntity?>(null) }
    var checkingTrap by remember { mutableStateOf<BeeTrapEntity?>(null) }
    var transferringTrap by remember { mutableStateOf<BeeTrapEntity?>(null) }
    var selectedHiveForMap by remember { mutableStateOf<HiveEntity?>(null) }
    var selectedApiaryForWeather by remember { mutableStateOf<ApiaryEntity?>(null) }

    Scaffold(
        modifier = modifier.testTag("apiaries_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (currentSubTab == ApiaryScreenSubTab.HIVES_AND_APIARIES) {
                        showAddApiaryDialog = true
                    } else {
                        showAddTrapDialog = true
                    }
                },
                containerColor = HoneyGoldPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("add_apiary_or_trap_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (currentSubTab == ApiaryScreenSubTab.HIVES_AND_APIARIES) "Novo Apiário" else "Nova Isca de Abelha"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sub Tabs: Colmeias & Apiários vs Iscas de Abelhas
            PrimaryTabRow(
                selectedTabIndex = currentSubTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = HoneyGoldPrimary
            ) {
                Tab(
                    selected = currentSubTab == ApiaryScreenSubTab.HIVES_AND_APIARIES,
                    onClick = { currentSubTab = ApiaryScreenSubTab.HIVES_AND_APIARIES },
                    text = { Text("🐝 Colmeias & Apiários (${hives.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_hives_apiaries")
                )
                Tab(
                    selected = currentSubTab == ApiaryScreenSubTab.BEE_TRAPS,
                    onClick = { currentSubTab = ApiaryScreenSubTab.BEE_TRAPS },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪤 Iscas & Capturas (${beeTraps.size})", fontWeight = FontWeight.Bold)
                            if (capturedTrapsCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MeadowGreenSecondary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                    Text("$capturedTrapsCount", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_bee_traps")
                )
            }

            // Quick Toolbar / Shortcut Row with General Map Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentSubTab == ApiaryScreenSubTab.HIVES_AND_APIARIES)
                        "Apiários e Caixas no Pasto"
                    else
                        "Iscas de Captura Armadas",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FilledTonalButton(
                    onClick = onOpenGeneralMap,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = HoneyGoldPrimary.copy(alpha = 0.15f),
                        contentColor = HoneyGoldPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("apiaries_open_map_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mapa Geral Satélite 🛰️",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (currentSubTab == ApiaryScreenSubTab.HIVES_AND_APIARIES) {
                val filteredHives = hives

                if (apiaries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏡", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Nenhum apiário cadastrado ainda.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Toque no botão '+' abaixo para cadastrar seu primeiro apiário.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(apiaries, key = { it.id }) { apiary ->
                            val apiaryHives = filteredHives.filter { it.apiaryId == apiary.id }
                            ApiaryCard(
                                apiary = apiary,
                                hives = apiaryHives,
                                onEditApiary = { editingApiary = apiary },
                                onCloneApiary = { cloningApiary = apiary },
                                onDeleteApiary = { onDeleteApiary(apiary) },
                                onAddHive = { showAddHiveDialogForApiary = apiary },
                                onEditHive = { editingHive = it },
                                onCloneHive = { cloningHive = it },
                                onDeleteHive = { onDeleteHive(it) },
                                onInspectHive = { onInspectHive(apiary, it) },
                                onShowHiveQr = { onShowHiveQr(apiary, it) },
                                onOpenHiveHistory = { onOpenHiveHistory(it) },
                                onOpenHiveMap = { selectedHiveForMap = it },
                                onOpenWeatherForecast = { selectedApiaryForWeather = apiary },
                                onOpenMap = { lat, lon, name ->
                                    selectedHiveForMap = HiveEntity(
                                        apiaryId = apiary.id,
                                        hiveNumber = apiary.name,
                                        beeSpecies = "Apiário",
                                        speciesCategory = "Geral",
                                        hiveType = "Nativo",
                                        latitude = lat,
                                        longitude = lon
                                    )
                                },
                                onShareLocation = { lat, lon, title, extra ->
                                    val intent = LocationHelper.createShareLocationIntent(title, lat, lon, extraInfo = extra)
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            } else {
                // Bee Traps (Iscas) View
                BeeTrapsView(
                    traps = beeTraps,
                    apiaries = apiaries,
                    capturedCount = capturedTrapsCount,
                    activeCount = activeTrapsCount,
                    onEditTrap = { editingTrap = it },
                    onCloneTrap = { cloningTrap = it },
                    onDeleteTrap = { onDeleteBeeTrap(it) },
                    onCheckTrap = { checkingTrap = it },
                    onTransferTrap = { transferringTrap = it },
                    onOpenMap = { lat, lon, name ->
                        selectedHiveForMap = HiveEntity(
                            apiaryId = 0L,
                            hiveNumber = name,
                            beeSpecies = "Isca PET / Caixa-Isca",
                            speciesCategory = "Isca",
                            hiveType = "Atração Silvestre",
                            latitude = lat,
                            longitude = lon
                        )
                    },
                    onShareLocation = { lat, lon, title, extra ->
                        val intent = LocationHelper.createShareLocationIntent(title, lat, lon, extraInfo = extra)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    // Add/Edit/Clone Apiary Dialog
    if (showAddApiaryDialog || editingApiary != null || cloningApiary != null) {
        val isCloning = cloningApiary != null
        val target = editingApiary ?: cloningApiary?.let {
            it.copy(id = 0L, name = "${it.name} (Cópia)")
        }
        ApiaryFormDialog(
            initialApiary = target,
            isCloning = isCloning,
            onDismiss = {
                showAddApiaryDialog = false
                editingApiary = null
                cloningApiary = null
            },
            onSave = { name, loc, flora, cap, notes, lat, lon, qCount, species, cat, hType ->
                onSaveApiary(name, loc, flora, cap, notes, lat, lon, qCount, species, cat, hType, if (isCloning) 0L else (editingApiary?.id ?: 0L))
                showAddApiaryDialog = false
                editingApiary = null
                cloningApiary = null
            }
        )
    }

    // Add/Edit/Clone Hive Dialog
    if (showAddHiveDialogForApiary != null || editingHive != null || cloningHive != null) {
        val isCloning = cloningHive != null
        val targetHive = editingHive ?: cloningHive?.let { source ->
            val allHiveNumbers = hives.map { it.hiveNumber }
            val nextNumber = HiveNamingHelper.generateNextHiveNumber(source.hiveNumber, allHiveNumbers)
            source.copy(
                id = 0L,
                hiveNumber = nextNumber,
                qrCode = ""
            )
        }
        val currentApiary = showAddHiveDialogForApiary
            ?: apiaries.find { it.id == (targetHive?.apiaryId ?: editingHive?.apiaryId ?: cloningHive?.apiaryId) }
            ?: apiaries.firstOrNull()

        if (currentApiary != null) {
            HiveFormDialog(
                apiary = currentApiary,
                initialHive = targetHive,
                isCloning = isCloning,
                allHives = hives,
                onDismiss = {
                    showAddHiveDialogForApiary = null
                    editingHive = null
                    cloningHive = null
                },
                onSave = { apiaryId, num, species, cat, type, dims, qStatus, qColor, qLineage, qAge, str, temp, frames, supers, framesPerSup, estYield, health, qr, lat, lon, alt, acc, curWeight, tare, notes ->
                    onSaveHive(
                        apiaryId, num, species, cat, type, dims, qStatus, qColor, qLineage, qAge, str, temp, frames, supers, framesPerSup, estYield, health, qr, lat, lon, alt, acc, curWeight, tare, notes,
                        if (isCloning) 0L else (editingHive?.id ?: 0L)
                    )
                    showAddHiveDialogForApiary = null
                    editingHive = null
                    cloningHive = null
                }
            )
        }
    }

    // Add/Edit/Clone Bee Trap Dialog
    if (showAddTrapDialog || editingTrap != null || cloningTrap != null) {
        val isCloning = cloningTrap != null
        val targetTrap = editingTrap ?: cloningTrap?.let {
            it.copy(id = 0L, name = "${it.name} (Cópia)")
        }
        BeeTrapFormDialog(
            apiaries = apiaries,
            initialTrap = targetTrap,
            isCloning = isCloning,
            onDismiss = {
                showAddTrapDialog = false
                editingTrap = null
                cloningTrap = null
            },
            onSave = { name, type, spec, attr, stat, lat, lon, alt, acc, desc, apiaryId, notes ->
                onSaveBeeTrap(name, type, spec, attr, stat, lat, lon, alt, acc, desc, apiaryId, notes, if (isCloning) 0L else (editingTrap?.id ?: 0L))
                showAddTrapDialog = false
                editingTrap = null
                cloningTrap = null
            }
        )
    }

    // Trap Check Dialog (Vistoria de Isca)
    checkingTrap?.let { trap ->
        TrapCheckDialog(
            trap = trap,
            onDismiss = { checkingTrap = null },
            onConfirm = { isCaptured, notes ->
                onRecordTrapCheck(trap, isCaptured, notes)
                checkingTrap = null
            }
        )
    }

    // Trap Transfer to Hive Dialog
    transferringTrap?.let { trap ->
        TrapTransferDialog(
            trap = trap,
            apiaries = apiaries,
            onDismiss = { transferringTrap = null },
            onTransfer = { apiaryId, hiveNumber, hiveType, species, category, frames, notes ->
                onTransferTrapToHive(trap, apiaryId, hiveNumber, hiveType, species, category, frames, notes)
                transferringTrap = null
            }
        )
    }

    // Hive Custom Map Location Dialog (Caixa de Colmeia Marker)
    selectedHiveForMap?.let { hive ->
        val apiary = apiaries.find { it.id == hive.apiaryId }
        val otherHives = hives.filter { it.apiaryId == hive.apiaryId && it.id != hive.id }
        HiveMapLocationDialog(
            hive = hive,
            apiary = apiary,
            otherHivesInApiary = otherHives,
            onDismiss = { selectedHiveForMap = null },
            onUpdateGps = { lat, lon ->
                onSaveHive(
                    hive.apiaryId, hive.hiveNumber, hive.beeSpecies, hive.speciesCategory,
                    hive.hiveType, hive.hiveDimensions, hive.queenStatus, hive.queenColor,
                    hive.queenLineage, hive.queenAgeMonths, hive.strengthLevel, hive.temperament,
                    hive.framesCount, hive.supersCount, hive.framesPerSuper, hive.estimatedYieldPerFrameKg,
                    hive.healthStatus, hive.qrCode, lat, lon, hive.altitude, hive.locationAccuracy,
                    hive.currentWeightKg, hive.tareWeightKg, hive.notes, hive.id
                )
                selectedHiveForMap = null
            }
        )
    }

    // Weather & Flight Conditions Full-screen Modal Dialog
    selectedApiaryForWeather?.let { apiary ->
        val apiaryWeather = com.example.util.ApiaryWeatherEngine.computeWeatherFromCoordinates(apiary)
        WeatherForecastModalDialog(
            weatherInfo = apiaryWeather,
            apiaries = apiaries,
            selectedApiaryId = apiary.id,
            onDismiss = { selectedApiaryForWeather = null }
        )
    }
}

// -------------------------------------------------------------
// APIARY CARD & HIVES LIST
// -------------------------------------------------------------
@Composable
fun ApiaryCard(
    apiary: ApiaryEntity,
    hives: List<HiveEntity>,
    onEditApiary: () -> Unit,
    onCloneApiary: () -> Unit,
    onDeleteApiary: () -> Unit,
    onAddHive: () -> Unit,
    onEditHive: (HiveEntity) -> Unit,
    onCloneHive: (HiveEntity) -> Unit,
    onDeleteHive: (HiveEntity) -> Unit,
    onInspectHive: (HiveEntity) -> Unit,
    onShowHiveQr: (HiveEntity) -> Unit,
    onOpenHiveHistory: (HiveEntity) -> Unit,
    onOpenHiveMap: (HiveEntity) -> Unit = {},
    onOpenWeatherForecast: (() -> Unit)? = null,
    onOpenMap: (Double, Double, String) -> Unit,
    onShareLocation: (Double, Double, String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("apiary_card_${apiary.id}"),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(HoneyGoldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏡", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = apiary.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Local",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = apiary.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row {
                    if (apiary.latitude != null && apiary.longitude != null) {
                        IconButton(
                            onClick = { onOpenMap(apiary.latitude, apiary.longitude, apiary.name) },
                            modifier = Modifier.testTag("apiary_map_btn")
                        ) {
                            Icon(Icons.Default.Map, contentDescription = "Mapa", tint = MeadowGreenSecondary)
                        }
                    }
                    IconButton(onClick = onCloneApiary, modifier = Modifier.testTag("apiary_clone_btn_${apiary.id}")) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Clonar Apiário", tint = HoneyGoldPrimary)
                    }
                    IconButton(onClick = onEditApiary) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Apiário", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDeleteApiary) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir Apiário", tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expandir"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Capacity and flora tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌸 Florada: ${apiary.predominantFlora}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = "Ocupação: ${hives.size} / ${apiary.capacity}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (hives.size >= apiary.capacity) MaterialTheme.colorScheme.error else HoneyGoldPrimary
                )
            }

            // Geolocation and Weather Forecast based on Apiary Coordinates
            if (apiary.latitude != null && apiary.longitude != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { onOpenMap(apiary.latitude, apiary.longitude, apiary.name) },
                        label = { Text("📍 GPS: ${LocationHelper.formatCoordinates(apiary.latitude, apiary.longitude)}", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                    OutlinedButton(
                        onClick = { onShareLocation(apiary.latitude, apiary.longitude, apiary.name, "Florada: ${apiary.predominantFlora}") },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Compartilhar", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartilhar", fontSize = 11.sp)
                    }
                }

                // Previsão do tempo e condições de voo baseadas na geolocalização do apiário
                val weather = remember(apiary.latitude, apiary.longitude, apiary.id) {
                    com.example.util.ApiaryWeatherEngine.computeWeatherFromCoordinates(apiary)
                }
                val isIdeal = weather.status == com.example.ui.viewmodel.ManejoWindowStatus.IDEAL
                val isCaution = weather.status == com.example.ui.viewmodel.ManejoWindowStatus.CAUTION
                val bgCol = if (isIdeal) Color(0xFFF0FDF4) else if (isCaution) Color(0xFFFEFCE8) else Color(0xFFFEF2F2)
                val borderCol = if (isIdeal) Color(0xFFBBF7D0) else if (isCaution) Color(0xFFFEF08A) else Color(0xFFFECACA)
                val textCol = if (isIdeal) Color(0xFF166534) else if (isCaution) Color(0xFF854D0E) else Color(0xFF991B1B)
                val latVal = apiary.latitude ?: -22.7394
                val lonVal = apiary.longitude ?: -45.5913

                Spacer(modifier = Modifier.height(6.dp))
                val weatherSurfaceModifier = if (onOpenWeatherForecast != null) {
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpenWeatherForecast() }
                } else {
                    Modifier.fillMaxWidth()
                }
                Surface(
                    modifier = weatherSurfaceModifier,
                    shape = RoundedCornerShape(10.dp),
                    color = bgCol,
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌤️", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = "Clima & Condições de Voo",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textCol
                                    )
                                    Text(
                                        text = "📍 ${apiary.location.ifBlank { "Local do Apiário" }}",
                                        fontSize = 9.sp,
                                        color = textCol.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = weather.flightConditionText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textCol
                                )
                                if (onOpenWeatherForecast != null) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "↗",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textCol
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🌡️ ${weather.temperatureC}°C", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("💧 ${weather.humidityPercent}%", fontSize = 11.sp)
                            Text("💨 ${weather.windSpeedKmh} km/h", fontSize = 11.sp)
                            Text("🌧️ Chuva: ${weather.rainProbabilityPercent}%", fontSize = 11.sp)
                        }
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Colmeias no Apiário (${hives.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        FilledTonalButton(
                            onClick = onAddHive,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("add_hive_btn_${apiary.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Adicionar Colmeia", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (hives.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhuma colmeia neste apiário. Toque em 'Adicionar Colmeia'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            hives.forEach { hive ->
                                HiveItemCard(
                                    hive = hive,
                                    apiary = apiary,
                                    onEdit = { onEditHive(hive) },
                                    onClone = { onCloneHive(hive) },
                                    onDelete = { onDeleteHive(hive) },
                                    onInspect = { onInspectHive(hive) },
                                    onQr = { onShowHiveQr(hive) },
                                    onOpenHistory = { onOpenHiveHistory(hive) },
                                    onOpenMap = { _, _ -> onOpenHiveMap(hive) },
                                    onShare = { lat, lon -> onShareLocation(lat, lon, "Colmeia ${hive.hiveNumber} - ${apiary.name}", "Espécie: ${hive.beeSpecies} | Tipo: ${hive.hiveType}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HiveItemCard(
    hive: HiveEntity,
    apiary: ApiaryEntity,
    onEdit: () -> Unit,
    onClone: () -> Unit,
    onDelete: () -> Unit,
    onInspect: () -> Unit,
    onQr: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenMap: (Double, Double) -> Unit,
    onShare: (Double, Double) -> Unit
) {
    val honeyStock = hive.estimatedHoneyStockKg ?: 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* normal click */ },
                onDoubleClick = onOpenHistory
            )
            .testTag("hive_item_${hive.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {
            // Header: Icon + Hive Number + Species & Health Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = if (hive.speciesCategory.contains("Melipon")) "🌸" else "🐝"
                    Text(icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = hive.hiveNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${hive.beeSpecies}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                HealthBadge(status = hive.healthStatus)
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Compact Specs & Weight telemetry row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QueenColorBadge(colorYear = hive.queenColor)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${hive.framesCount}q ninho • ${hive.supersCount} melg",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⚖️ ${hive.currentWeightKg?.let { "${it} kg" } ?: "--"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🍯 Mel: ${String.format(Locale.US, "%.1f", honeyStock)} kg",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (honeyStock < 4.0 && hive.currentWeightKg != null) WarningAlertRed else HoneyGoldPrimary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Action Row: Timeline Button on left, and quick action icon buttons on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onOpenHistory,
                        colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp).testTag("hive_timeline_btn_${hive.id}")
                    ) {
                        Text("📋 Prontuário", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• 2x cliques abre",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onOpenMap(hive.latitude ?: 0.0, hive.longitude ?: 0.0) }, modifier = Modifier.size(26.dp).testTag("hive_map_btn_${hive.id}")) {
                        Icon(Icons.Default.Map, contentDescription = "Mapa da Colmeia (Caixa)", modifier = Modifier.size(15.dp), tint = MeadowGreenSecondary)
                    }
                    IconButton(onClick = onOpenHistory, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Fotos da Colmeia", modifier = Modifier.size(15.dp), tint = HoneyGoldPrimary)
                    }
                    IconButton(onClick = onQr, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.QrCode, contentDescription = "Ver QR Code", modifier = Modifier.size(15.dp), tint = HoneyGoldPrimary)
                    }
                    IconButton(onClick = onInspect, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Visibility, contentDescription = "Inspecionar", modifier = Modifier.size(15.dp), tint = MeadowGreenSecondary)
                    }
                    IconButton(onClick = onClone, modifier = Modifier.size(26.dp).testTag("hive_clone_btn_${hive.id}")) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Clonar", modifier = Modifier.size(15.dp), tint = HoneyGoldPrimary)
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// BEE TRAPS (ISCAS DE ABELHAS) VIEW & CARDS
// -------------------------------------------------------------
@Composable
fun BeeTrapsView(
    traps: List<BeeTrapEntity>,
    apiaries: List<ApiaryEntity>,
    capturedCount: Int,
    activeCount: Int,
    onEditTrap: (BeeTrapEntity) -> Unit,
    onCloneTrap: (BeeTrapEntity) -> Unit,
    onDeleteTrap: (BeeTrapEntity) -> Unit,
    onCheckTrap: (BeeTrapEntity) -> Unit,
    onTransferTrap: (BeeTrapEntity) -> Unit,
    onOpenMap: (Double, Double, String) -> Unit,
    onShareLocation: (Double, Double, String, String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // KPI Summary Card for Traps
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🪤 Gestão de Iscas e Capturas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Pontos georreferenciados para captura de enxames silvestres", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = HoneyGoldPrimary.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$activeCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = HoneyGoldPrimary)
                                Text("Armadas", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MeadowGreenSecondary.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$capturedCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MeadowGreenSecondary)
                                Text("Capturadas 🎉", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${traps.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("Total Iscas", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        if (traps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🪤", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Nenhuma isca cadastrada.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Toque no botão '+' para registrar uma nova isca com GPS e atrativo.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(traps, key = { it.id }) { trap ->
                val apiary = apiaries.find { it.id == trap.apiaryId }
                BeeTrapCard(
                    trap = trap,
                    apiaryName = apiary?.name,
                    dateFormat = dateFormat,
                    onEdit = { onEditTrap(trap) },
                    onClone = { onCloneTrap(trap) },
                    onDelete = { onDeleteTrap(trap) },
                    onCheck = { onCheckTrap(trap) },
                    onTransfer = { onTransferTrap(trap) },
                    onOpenMap = { lat, lon -> onOpenMap(lat, lon, trap.name) },
                    onShare = { lat, lon -> onShareLocation(lat, lon, trap.name, "Espécie Alvo: ${trap.targetSpecies} | Atrativo: ${trap.attractantUsed}") }
                )
            }
        }
    }
}

@Composable
fun BeeTrapCard(
    trap: BeeTrapEntity,
    apiaryName: String?,
    dateFormat: SimpleDateFormat,
    onEdit: () -> Unit,
    onClone: () -> Unit,
    onDelete: () -> Unit,
    onCheck: () -> Unit,
    onTransfer: () -> Unit,
    onOpenMap: (Double, Double) -> Unit,
    onShare: (Double, Double) -> Unit
) {
    val statusColor = when (trap.status) {
        "Capturada" -> MeadowGreenSecondary
        "Armada" -> HoneyGoldPrimary
        "Transferida" -> Color(0xFF1E88E5)
        else -> Color.Gray
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trap_card_${trap.id}"),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (trap.status == "Capturada") "🐝" else "🪤", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(trap.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${trap.trapType} • ${trap.targetSpecies}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Attractant & Date info
            Text(
                text = "🧪 Atrativo: ${trap.attractantUsed}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "📅 Instalada em: ${dateFormat.format(Date(trap.installationDate))}" + (trap.captureDate?.let { " • Capturada em: ${dateFormat.format(Date(it))}" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (trap.locationDescription.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📌 Local: ${trap.locationDescription}" + (apiaryName?.let { " (Próximo ao $it)" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            // GPS info badge
            if (trap.latitude != null && trap.longitude != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = "GPS", modifier = Modifier.size(14.dp), tint = HoneyGoldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = LocationHelper.formatCoordinates(trap.latitude, trap.longitude) + (trap.altitude?.let { " (${it.toInt()}m)" } ?: ""),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        OutlinedButton(
                            onClick = { onOpenMap(trap.latitude, trap.longitude) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = "Navegar", modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Navegar", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { onShare(trap.latitude, trap.longitude) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Compartilhar", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons & Status Label na Linha Inferior
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Label com o status da isca posicionado na linha inferior
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (trap.status) {
                            "Capturada" -> "Capturada 🎉"
                            "Armada" -> "Armada / Ativa"
                            "Transferida" -> "Transferida"
                            else -> trap.status
                        },
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilledTonalButton(
                        onClick = onCheck,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Vistoria (${trap.checkCount})", fontSize = 11.sp)
                    }

                    if (trap.status == "Capturada") {
                        Button(
                            onClick = onTransfer,
                            colors = ButtonDefaults.buttonColors(containerColor = MeadowGreenSecondary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Transferir", fontSize = 11.sp)
                        }
                    }

                    IconButton(onClick = onClone, modifier = Modifier.size(30.dp).testTag("trap_clone_btn_${trap.id}")) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Clonar Isca", modifier = Modifier.size(16.dp), tint = HoneyGoldPrimary)
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Isca", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir Isca", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOG: APIARY FORM WITH QUICK HIVE REGISTRATION WIZARD
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiaryFormDialog(
    initialApiary: ApiaryEntity?,
    isCloning: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (name: String, location: String, flora: String, capacity: Int, notes: String, lat: Double?, lon: Double?, quickHivesCount: Int, beeSpecies: String, speciesCategory: String, hiveType: String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialApiary?.name ?: "") }
    var location by remember { mutableStateOf(initialApiary?.location ?: "") }
    var flora by remember { mutableStateOf(initialApiary?.predominantFlora ?: "Eucalipto, Silvestre, Alecrim-do-campo") }
    var capacityText by remember { mutableStateOf(initialApiary?.capacity?.toString() ?: "20") }
    var notes by remember { mutableStateOf(initialApiary?.notes ?: "") }
    var latitude by remember { mutableStateOf(initialApiary?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(initialApiary?.longitude?.toString() ?: "") }
    var showMapPicker by remember { mutableStateOf(false) }

    // Quick Hive Wizard state (only active when creating new apiary)
    var currentStep by remember { mutableIntStateOf(0) } // 0: Apiary details, 1: Hive Count, 2: Bee Species & Category, 3: Hive Type
    var quickHivesCountText by remember { mutableStateOf("10") }
    var speciesCategory by remember { mutableStateOf("Apis (Com Ferrão)") }
    var beeSpecies by remember { mutableStateOf("Apis mellifera (Africanizada)") }
    var hiveType by remember { mutableStateOf("Langstroth (Padrão 10 Quadros)") }

    var expCategory by remember { mutableStateOf(false) }
    var expSpecies by remember { mutableStateOf(false) }
    var expHiveType by remember { mutableStateOf(false) }

    val totalSteps = if (initialApiary == null && !isCloning) 4 else 1

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val coords = LocationHelper.getCurrentLocation(context)
            if (coords != null) {
                latitude = coords.latitude.toString()
                longitude = coords.longitude.toString()
                Toast.makeText(context, "GPS capturado com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Localização capturada.", Toast.LENGTH_SHORT).show()
                latitude = "-22.7394"
                longitude = "-45.5913"
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    tonalElevation = 3.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = if (isCloning) "👯 Clonar Apiário" else if (initialApiary == null) "Novo Apiário & Assistente de Colmeias" else "Editar Apiário",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (totalSteps > 1) {
                                    val stepTitle = when (currentStep) {
                                        0 -> "Passo 1/4: Dados Básicos do Apiário"
                                        1 -> "Passo 2/4: Número de Colmeias"
                                        2 -> "Passo 3/4: Categoria e Espécie da Abelha"
                                        3 -> "Passo 4/4: Modelo / Tipo de Colmeia"
                                        else -> ""
                                    }
                                    Text(
                                        text = stepTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HoneyGoldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), CircleShape)
                                    .testTag("apiary_dialog_close_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Horizontal Progress Line for Wizard
                        if (totalSteps > 1) {
                            LinearProgressIndicator(
                                progress = { (currentStep + 1).toFloat() / totalSteps.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = HoneyGoldPrimary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .imePadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (totalSteps > 1 && currentStep == 1) {
                        // STEP 2: NUMBER OF HIVES WIZARD QUESTION
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🐝 Quantas colmeias deseja cadastrar neste apiário agora?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("O assistente irá gerar automaticamente o número especificado de colmeias com configurações padrão.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = quickHivesCountText,
                                    onValueChange = { quickHivesCountText = it },
                                    label = { Text("Número Inicial de Colmeias") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else if (totalSteps > 1 && currentStep == 2) {
                        // STEP 3: BEE CATEGORY & SPECIES
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("🧬 Categoria e Espécie da Abelha", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                
                                // Category Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = expCategory,
                                    onExpandedChange = { expCategory = !expCategory }
                                ) {
                                    OutlinedTextField(
                                        value = speciesCategory,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Categoria da Abelha") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expCategory) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expCategory,
                                        onDismissRequest = { expCategory = false }
                                    ) {
                                        listOf("Apis (Com Ferrão)", "Meliponínea (Sem Ferrão)").forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat) },
                                                onClick = {
                                                    speciesCategory = cat
                                                    expCategory = false
                                                    beeSpecies = if (cat.contains("Melipon")) "Tetragonisca angustula (Jataí)" else "Apis mellifera (Africanizada)"
                                                }
                                            )
                                        }
                                    }
                                }

                                // Species Dropdown
                                val speciesOpts = if (speciesCategory.contains("Melipon")) {
                                    listOf(
                                        "Melipona quadrifasciata (Mandaçaia MQA)",
                                        "Tetragonisca angustula (Jataí)",
                                        "Melipona scutellaris (Uruçu Nordestina)",
                                        "Melipona fasciculata (Tiúba)",
                                        "Scaptotrigona postica (Tubuna/Mandaguari)",
                                        "Melipona rufiventris (Uruçu Amarela)",
                                        "Plebeia droryana (Mirim)",
                                        "Nannotrigona testaceicornis (Iraí)",
                                        "Tetragona clavipes (Borá)",
                                        "Outra Abelha Sem Ferrão (Personalizada)"
                                    )
                                } else {
                                    listOf(
                                        "Apis mellifera (Africanizada)",
                                        "Apis mellifera ligustica (Italiana)",
                                        "Apis mellifera carnica (Cárnica)",
                                        "Apis mellifera caucasia (Caucasiana)",
                                        "Buckfast (Híbrida Selecionada)",
                                        "Outra Linhagem Apis"
                                    )
                                }

                                ExposedDropdownMenuBox(
                                    expanded = expSpecies,
                                    onExpandedChange = { expSpecies = !expSpecies }
                                ) {
                                    OutlinedTextField(
                                        value = beeSpecies,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Espécie / Linhagem") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expSpecies) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expSpecies,
                                        onDismissRequest = { expSpecies = false }
                                    ) {
                                        speciesOpts.forEach { sp ->
                                            DropdownMenuItem(
                                                text = { Text(sp) },
                                                onClick = {
                                                    beeSpecies = sp
                                                    expSpecies = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (totalSteps > 1 && currentStep == 3) {
                        // STEP 4: HIVE MODEL / TYPE
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("📦 Modelo / Tipo de Colmeia", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                
                                val hiveTypeOpts = if (speciesCategory.contains("Melipon")) {
                                    listOf(
                                        "Caixa INPA Modular (Ninho + Sobre-ninho + Melgueiras)",
                                        "Caixa Fernando Oliveira (Vertical Inteligente)",
                                        "Caixa PNN (Paulo Nogueira Neto)",
                                        "Caixa Baiana / Horizontal",
                                        "Caixa Sextavada / Octogonal",
                                        "Caixa AF (Ailton Fontana)",
                                        "Caixa Rústica / Cortiço",
                                        "Caixa Didática de Vidro"
                                    )
                                } else {
                                    listOf(
                                        "Langstroth (Padrão 10 Quadros)",
                                        "Langstroth 8 Quadros",
                                        "Dadant-Blatt (Ninho Alto)",
                                        "Top Bar Hive (TBH / Barra Superior Quêniana)",
                                        "Warré (Colmeia Ecológica do Povo)",
                                        "Layens (Horizontal Espanhola)",
                                        "Schenk (Padrão Sulista)",
                                        "Núcleo de Fecundação / Transporte (5q)"
                                    )
                                }

                                ExposedDropdownMenuBox(
                                    expanded = expHiveType,
                                    onExpandedChange = { expHiveType = !expHiveType }
                                ) {
                                    OutlinedTextField(
                                        value = hiveType,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Modelo de Colmeia") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expHiveType) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expHiveType,
                                        onDismissRequest = { expHiveType = false }
                                    ) {
                                        hiveTypeOpts.forEach { ht ->
                                            DropdownMenuItem(
                                                text = { Text(ht) },
                                                onClick = {
                                                    hiveType = ht
                                                    expHiveType = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // STEP 0 (or editing): APIARY DETAILS
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nome do Apiário") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Localização / Região") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = flora,
                            onValueChange = { flora = it },
                            label = { Text("Florada Predominante") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = capacityText,
                            onValueChange = { capacityText = it },
                            label = { Text("Capacidade Máxima (Colmeias)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // GPS Coordinate Capture
                        Text("📍 Geolocalização GPS:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = latitude,
                                onValueChange = { latitude = it },
                                label = { Text("Latitude") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = longitude,
                                onValueChange = { longitude = it },
                                label = { Text("Longitude") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    if (hasPerm) {
                                        val coords = LocationHelper.getCurrentLocation(context)
                                        if (coords != null) {
                                            latitude = coords.latitude.toString()
                                            longitude = coords.longitude.toString()
                                            Toast.makeText(context, "GPS obtido com sucesso!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            latitude = "-22.7394"
                                            longitude = "-45.5913"
                                        }
                                    } else {
                                        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MeadowGreenSecondary)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GPS Atual", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { showMapPicker = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Marcar no Mapa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (showMapPicker) {
                            MapLocationPickerDialog(
                                title = "Marcar Ponto do Apiário",
                                subtitle = "Posicione o pino exatamente na entrada ou centro do apiário",
                                initialLatitude = latitude.toDoubleOrNull(),
                                initialLongitude = longitude.toDoubleOrNull(),
                                onDismiss = { showMapPicker = false },
                                onConfirmLocation = { newLat, newLon ->
                                    latitude = String.format(java.util.Locale.US, "%.6f", newLat)
                                    longitude = String.format(java.util.Locale.US, "%.6f", newLon)
                                    Toast.makeText(context, "Coordenadas do Apiário marcadas pelo mapa!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Observações Técnicas") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }

                // Bottom Action Bar: Wizard Navigation / Save
                Surface(
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (totalSteps > 1 && currentStep > 0) {
                            OutlinedButton(
                                onClick = { currentStep-- }
                            ) {
                                Text("Anterior")
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Button(
                            onClick = {
                                if (totalSteps > 1 && currentStep < totalSteps - 1) {
                                    if (currentStep == 0 && name.isBlank()) {
                                        Toast.makeText(context, "Informe o nome do apiário para continuar.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        currentStep++
                                    }
                                } else {
                                    if (name.isNotBlank()) {
                                        val cap = capacityText.toIntOrNull() ?: 20
                                        val lat = latitude.toDoubleOrNull()
                                        val lon = longitude.toDoubleOrNull()
                                        val qCount = quickHivesCountText.toIntOrNull() ?: 10
                                        onSave(name, location, flora, cap, notes, lat, lon, qCount, beeSpecies, speciesCategory, hiveType)
                                    } else {
                                        Toast.makeText(context, "Preencha o nome do apiário.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                        ) {
                            Text(if (totalSteps > 1 && currentStep < totalSteps - 1) "Próximo" else "Salvar Apiário & Colmeias")
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOG: HIVE FORM (COM PESO E ESTOQUE DE MEL)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveFormDialog(
    apiary: ApiaryEntity,
    initialHive: HiveEntity?,
    isCloning: Boolean = false,
    allHives: List<HiveEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        apiaryId: Long,
        hiveNumber: String,
        beeSpecies: String,
        speciesCategory: String,
        hiveType: String,
        hiveDimensions: String,
        queenStatus: String,
        queenColor: String,
        queenLineage: String,
        queenAgeMonths: Int,
        strengthLevel: String,
        temperament: String,
        framesCount: Int,
        supersCount: Int,
        framesPerSuper: Int,
        estimatedYieldPerFrameKg: Double,
        healthStatus: String,
        qrCode: String,
        latitude: Double?,
        longitude: Double?,
        altitude: Double?,
        locationAccuracy: Float?,
        currentWeightKg: Double?,
        tareWeightKg: Double,
        notes: String
    ) -> Unit
) {
    val context = LocalContext.current

    var hiveNumber by remember { mutableStateOf(initialHive?.hiveNumber ?: "Colmeia 01") }

    val isDuplicateName = remember(hiveNumber, allHives, initialHive, isCloning) {
        HiveNamingHelper.isHiveNumberDuplicate(
            candidateNumber = hiveNumber,
            existingHives = allHives,
            currentHiveId = initialHive?.id ?: 0L,
            isCloning = isCloning
        )
    }
    var speciesCategory by remember { mutableStateOf(initialHive?.speciesCategory ?: "Apis (Com Ferrão)") }
    var beeSpecies by remember { mutableStateOf(initialHive?.beeSpecies ?: "Apis mellifera (Africanizada)") }
    var hiveType by remember { mutableStateOf(initialHive?.hiveType ?: "Langstroth (Padrão 10 Quadros)") }
    var hiveDimensions by remember { mutableStateOf(initialHive?.hiveDimensions ?: "Ninho 10q (448x232mm)") }

    var queenStatus by remember { mutableStateOf(initialHive?.queenStatus ?: "Presente e Marcada") }
    var queenColor by remember { mutableStateOf(initialHive?.queenColor ?: "Branco (2026)") }
    var queenLineage by remember { mutableStateOf(initialHive?.queenLineage ?: "Italiana Selecionada") }
    var queenAgeText by remember { mutableStateOf(initialHive?.queenAgeMonths?.toString() ?: "4") }

    var strengthLevel by remember { mutableStateOf(initialHive?.strengthLevel ?: "Forte") }
    var temperament by remember { mutableStateOf(initialHive?.temperament ?: "Mansa") }
    var healthStatus by remember { mutableStateOf(initialHive?.healthStatus ?: "Excelente") }

    var framesCountText by remember { mutableStateOf(initialHive?.framesCount?.toString() ?: "10") }
    var supersCountText by remember { mutableStateOf(initialHive?.supersCount?.toString() ?: "1") }
    var framesPerSuperText by remember { mutableStateOf(initialHive?.framesPerSuper?.toString() ?: "10") }
    var estimatedYieldText by remember { mutableStateOf(initialHive?.estimatedYieldPerFrameKg?.toString() ?: "2.2") }

    var currentWeightText by remember { mutableStateOf(initialHive?.currentWeightKg?.toString() ?: "38.0") }
    var tareWeightText by remember { mutableStateOf(initialHive?.tareWeightKg?.toString() ?: "18.0") }

    var qrCode by remember { mutableStateOf(initialHive?.qrCode ?: "") }
    var latitude by remember { mutableStateOf(initialHive?.latitude?.toString() ?: (apiary.latitude?.toString() ?: "")) }
    var longitude by remember { mutableStateOf(initialHive?.longitude?.toString() ?: (apiary.longitude?.toString() ?: "")) }
    var altitude by remember { mutableStateOf(initialHive?.altitude?.toString() ?: "1240") }
    var notes by remember { mutableStateOf(initialHive?.notes ?: "") }
    var showQueenColorTable by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }

    var expCategory by remember { mutableStateOf(false) }
    var expSpecies by remember { mutableStateOf(false) }
    var expHiveType by remember { mutableStateOf(false) }
    var expQueenStatus by remember { mutableStateOf(false) }
    var expQueenColor by remember { mutableStateOf(false) }

    val speciesOptions = if (speciesCategory.contains("Melipon")) {
        listOf(
            "Melipona quadrifasciata (Mandaçaia MQA)",
            "Tetragonisca angustula (Jataí)",
            "Melipona scutellaris (Uruçu Nordestina)",
            "Melipona fasciculata (Tiúba)",
            "Scaptotrigona postica (Tubuna/Mandaguari)",
            "Melipona rufiventris (Uruçu Amarela)",
            "Plebeia droryana (Mirim)",
            "Nannotrigona testaceicornis (Iraí)",
            "Tetragona clavipes (Borá)",
            "Outra Abelha Sem Ferrão (Personalizada)"
        )
    } else {
        listOf(
            "Apis mellifera (Africanizada)",
            "Apis mellifera ligustica (Italiana)",
            "Apis mellifera carnica (Cárnica)",
            "Apis mellifera caucasia (Caucasiana)",
            "Buckfast (Híbrida Selecionada)",
            "Outra Linhagem Apis"
        )
    }

    val hiveTypeOptions = if (speciesCategory.contains("Melipon")) {
        listOf(
            "Caixa INPA Modular (Ninho + Sobre-ninho + Melgueiras)",
            "Caixa Fernando Oliveira (Vertical Inteligente)",
            "Caixa PNN (Paulo Nogueira Neto)",
            "Caixa Baiana / Horizontal",
            "Caixa Sextavada / Octogonal",
            "Caixa AF (Ailton Fontana)",
            "Caixa Rústica / Cortiço",
            "Caixa Didática de Vidro"
        )
    } else {
        listOf(
            "Langstroth (Padrão 10 Quadros)",
            "Langstroth 8 Quadros",
            "Dadant-Blatt (Ninho Alto)",
            "Top Bar Hive (TBH / Barra Superior Quêniana)",
            "Warré (Colmeia Ecológica do Povo)",
            "Layens (Horizontal Espanhola)",
            "Schenk (Padrão Sulista)",
            "Núcleo de Fecundação / Transporte (5q)"
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val coords = LocationHelper.getCurrentLocation(context)
            if (coords != null) {
                latitude = coords.latitude.toString()
                longitude = coords.longitude.toString()
                if (coords.altitude != null) altitude = coords.altitude.toInt().toString()
                Toast.makeText(context, "GPS da Colmeia obtido!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
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
                            text = if (isCloning) "👯 Clonar Colmeia" else if (initialHive == null) "Nova Colmeia" else "Editar Colmeia ${initialHive.hiveNumber}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Button(
                            onClick = {
                                val trimmed = hiveNumber.trim()
                                if (trimmed.isNotBlank()) {
                                    if (isDuplicateName) {
                                        Toast.makeText(context, "Não é permitido cadastrar colmeias com o mesmo nome/número!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val frames = framesCountText.toIntOrNull() ?: 10
                                    val supers = supersCountText.toIntOrNull() ?: 1
                                    val framesPerSup = framesPerSuperText.toIntOrNull() ?: 10
                                    val estYield = estimatedYieldText.toDoubleOrNull() ?: 2.2
                                    val qAge = queenAgeText.toIntOrNull() ?: 4
                                    val curWeight = currentWeightText.toDoubleOrNull()
                                    val tare = tareWeightText.toDoubleOrNull() ?: 18.0
                                    val lat = latitude.toDoubleOrNull()
                                    val lon = longitude.toDoubleOrNull()
                                    val alt = altitude.toDoubleOrNull()

                                    onSave(
                                        apiary.id, trimmed, beeSpecies, speciesCategory, hiveType, hiveDimensions,
                                        queenStatus, queenColor, queenLineage, qAge, strengthLevel, temperament,
                                        frames, supers, framesPerSup, estYield, healthStatus, qrCode, lat, lon, alt, 3.0f,
                                        curWeight, tare, notes
                                    )
                                }
                            },
                            enabled = hiveNumber.trim().isNotBlank() && !isDuplicateName
                        ) {
                            Text("Salvar")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), CircleShape)
                                .testTag("hive_dialog_close_btn")
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
                    OutlinedTextField(
                        value = hiveNumber,
                        onValueChange = { hiveNumber = it },
                        label = { Text("Número / Identificador da Colmeia") },
                        isError = isDuplicateName || hiveNumber.trim().isBlank(),
                        supportingText = {
                            if (isDuplicateName) {
                                Text(
                                    text = "⚠️ Já existe uma colmeia com este nome/número. Não são permitidas colmeias duplicadas.",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp
                                )
                            } else if (hiveNumber.trim().isBlank()) {
                                Text(
                                    text = "O nome/número da colmeia não pode ficar em branco.",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hive_number_input")
                    )

                // Species Category Selector
                ExposedDropdownMenuBox(
                    expanded = expCategory,
                    onExpandedChange = { expCategory = it }
                ) {
                    OutlinedTextField(
                        value = speciesCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria da Abelha") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expCategory) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expCategory,
                        onDismissRequest = { expCategory = false }
                    ) {
                        listOf("Apis (Com Ferrão)", "Meliponíneo (Sem Ferrão / ASF)").forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    speciesCategory = cat
                                    beeSpecies = if (cat.contains("Melipon")) "Melipona quadrifasciata (Mandaçaia MQA)" else "Apis mellifera (Africanizada)"
                                    hiveType = if (cat.contains("Melipon")) "Caixa INPA Modular (Ninho + Sobre-ninho + Melgueiras)" else "Langstroth (Padrão 10 Quadros)"
                                    framesPerSuperText = if (cat.contains("Melipon")) "6" else "10"
                                    estimatedYieldText = if (cat.contains("Melipon")) "0.4" else "2.2"
                                    tareWeightText = if (cat.contains("Melipon")) "4.5" else "18.0"
                                    expCategory = false
                                }
                            )
                        }
                    }
                }

                // Bee Species Selector
                ExposedDropdownMenuBox(
                    expanded = expSpecies,
                    onExpandedChange = { expSpecies = it }
                ) {
                    OutlinedTextField(
                        value = beeSpecies,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Espécie da Abelha") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expSpecies) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expSpecies,
                        onDismissRequest = { expSpecies = false }
                    ) {
                        speciesOptions.forEach { spec ->
                            DropdownMenuItem(
                                text = { Text(spec) },
                                onClick = {
                                    beeSpecies = spec
                                    expSpecies = false
                                }
                            )
                        }
                    }
                }

                // Hive Model/Type Selector
                ExposedDropdownMenuBox(
                    expanded = expHiveType,
                    onExpandedChange = { expHiveType = it }
                ) {
                    OutlinedTextField(
                        value = hiveType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Modelo / Tipo de Colmeia") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expHiveType) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expHiveType,
                        onDismissRequest = { expHiveType = false }
                    ) {
                        hiveTypeOptions.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    hiveType = type
                                    expHiveType = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = hiveDimensions,
                    onValueChange = { hiveDimensions = it },
                    label = { Text("Dimensões / Padrão da Caixa") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Production & Super Configs
                Text("🍯 Configuração de Melgueiras & Rendimento:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = HoneyGoldPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = framesCountText,
                        onValueChange = { framesCountText = it },
                        label = { Text("Quadros Ninho") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = supersCountText,
                        onValueChange = { supersCountText = it },
                        label = { Text("Melgueiras") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = framesPerSuperText,
                        onValueChange = { framesPerSuperText = it },
                        label = { Text("Quadros/Melg.") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = estimatedYieldText,
                        onValueChange = { estimatedYieldText = it },
                        label = { Text("kg/Quadro Est.") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Pesagem da Colmeia & Estoque de Mel
                Text("⚖️ Pesagem & Estimativa de Estoque de Mel:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = HoneyGoldPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currentWeightText,
                        onValueChange = { currentWeightText = it },
                        label = { Text("Peso Atual (kg)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tareWeightText,
                        onValueChange = { tareWeightText = it },
                        label = { Text("Tara Caixa (kg)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Queen Status & Color
                Text("👑 Manejo da Rainha:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = expQueenStatus,
                    onExpandedChange = { expQueenStatus = it }
                ) {
                    OutlinedTextField(
                        value = queenStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status da Rainha") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expQueenStatus) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expQueenStatus,
                        onDismissRequest = { expQueenStatus = false }
                    ) {
                        listOf("Presente e Marcada", "Presente sem marcação", "Troca Necessária", "Ausente / Órfã", "Virgem").forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    queenStatus = s
                                    expQueenStatus = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cor Oficial da Rainha:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { showQueenColorTable = true },
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("🎨 Ver Tabela por Ano", fontSize = 11.sp, color = HoneyGoldPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expQueenColor,
                    onExpandedChange = { expQueenColor = it }
                ) {
                    OutlinedTextField(
                        value = queenColor,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cor Internacional do Ano da Rainha") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expQueenColor) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expQueenColor,
                        onDismissRequest = { expQueenColor = false }
                    ) {
                        listOf(
                            "Branco (2026 / 2031 - Anos 1 e 6)",
                            "Amarelo (2027 / 2032 - Anos 2 e 7)",
                            "Vermelho (2028 / 2033 - Anos 3 e 8)",
                            "Verde (2024 / 2029 - Anos 4 e 9)",
                            "Azul (2025 / 2030 - Anos 5 e 0)"
                        ).forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    queenColor = c
                                    expQueenColor = false
                                }
                            )
                        }
                    }
                }

                if (showQueenColorTable) {
                    QueenColorTableDialog(
                        onDismiss = { showQueenColorTable = false }
                    )
                }

                // GPS for Hive
                Text("📍 Geolocalização da Colmeia:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                val coords = LocationHelper.getCurrentLocation(context)
                                if (coords != null) {
                                    latitude = coords.latitude.toString()
                                    longitude = coords.longitude.toString()
                                    if (coords.altitude != null) altitude = coords.altitude.toInt().toString()
                                    Toast.makeText(context, "GPS da Colmeia capturado!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MeadowGreenSecondary)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GPS Atual", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showMapPicker = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Marcar no Mapa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (showMapPicker) {
                    MapLocationPickerDialog(
                        title = "Marcar Posição da Colmeia",
                        subtitle = "Posicione o pino exatamente no cavalete ou suporte desta caixa",
                        initialLatitude = latitude.toDoubleOrNull() ?: apiary.latitude,
                        initialLongitude = longitude.toDoubleOrNull() ?: apiary.longitude,
                        onDismiss = { showMapPicker = false },
                        onConfirmLocation = { newLat, newLon ->
                            latitude = String.format(java.util.Locale.US, "%.6f", newLat)
                            longitude = String.format(java.util.Locale.US, "%.6f", newLon)
                            Toast.makeText(context, "Coordenadas da Colmeia marcadas no mapa!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas de Campo") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        }
    }
}
}

// -------------------------------------------------------------
// DIALOG: BEE TRAP (ISCA) FORM
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeeTrapFormDialog(
    apiaries: List<ApiaryEntity>,
    initialTrap: BeeTrapEntity?,
    isCloning: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        trapType: String,
        targetSpecies: String,
        attractantUsed: String,
        status: String,
        latitude: Double?,
        longitude: Double?,
        altitude: Double?,
        locationAccuracy: Float?,
        locationDescription: String,
        apiaryId: Long?,
        notes: String
    ) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(initialTrap?.name ?: "Isca PET 2L #${System.currentTimeMillis() % 100}") }
    var trapType by remember { mutableStateOf(initialTrap?.trapType ?: "Garrafa PET 2L") }
    var targetSpecies by remember { mutableStateOf(initialTrap?.targetSpecies ?: "Tetragonisca angustula (Jataí)") }
    var attractantUsed by remember { mutableStateOf(initialTrap?.attractantUsed ?: "Extrato Alcoólico de Própolis de Jataí") }
    var status by remember { mutableStateOf(initialTrap?.status ?: "Armada") }
    var locationDescription by remember { mutableStateOf(initialTrap?.locationDescription ?: "Fixada em tronco a 2.5m de altura, virada para o leste.") }
    var selectedApiaryId by remember { mutableStateOf(initialTrap?.apiaryId ?: apiaries.firstOrNull()?.id) }
    var latitude by remember { mutableStateOf(initialTrap?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(initialTrap?.longitude?.toString() ?: "") }
    var altitude by remember { mutableStateOf(initialTrap?.altitude?.toString() ?: "1240") }
    var notes by remember { mutableStateOf(initialTrap?.notes ?: "") }
    var showMapPicker by remember { mutableStateOf(false) }

    var expTrapType by remember { mutableStateOf(false) }
    var expSpecies by remember { mutableStateOf(false) }
    var expAttractant by remember { mutableStateOf(false) }
    var expStatus by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val coords = LocationHelper.getCurrentLocation(context)
            if (coords != null) {
                latitude = coords.latitude.toString()
                longitude = coords.longitude.toString()
                if (coords.altitude != null) altitude = coords.altitude.toInt().toString()
                Toast.makeText(context, "GPS da Isca capturado com precisão!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
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
                            text = if (isCloning) "🪤 Clonar Isca" else if (initialTrap == null) "Nova Isca" else "Editar Isca",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val lat = latitude.toDoubleOrNull()
                                    val lon = longitude.toDoubleOrNull()
                                    val alt = altitude.toDoubleOrNull()
                                    onSave(name, trapType, targetSpecies, attractantUsed, status, lat, lon, alt, 3.0f, locationDescription, selectedApiaryId, notes)
                                }
                            }
                        ) {
                            Text("Salvar")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), CircleShape)
                                .testTag("trap_dialog_close_btn")
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
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                    label = { Text("Identificação da Isca") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Trap Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expTrapType,
                    onExpandedChange = { expTrapType = it }
                ) {
                    OutlinedTextField(
                        value = trapType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Recipiente da Isca") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expTrapType) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expTrapType,
                        onDismissRequest = { expTrapType = false }
                    ) {
                        listOf("Garrafa PET 2L", "Garrafa PET 5L", "Caixa de Papelão Resinada", "Caixa Núcleo Madeira", "Caixa Bambu / Tronco", "Caixa INPA").forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    trapType = t
                                    expTrapType = false
                                }
                            )
                        }
                    }
                }

                // Target Species
                ExposedDropdownMenuBox(
                    expanded = expSpecies,
                    onExpandedChange = { expSpecies = it }
                ) {
                    OutlinedTextField(
                        value = targetSpecies,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Espécie Alvo da Captura") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expSpecies) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expSpecies,
                        onDismissRequest = { expSpecies = false }
                    ) {
                        listOf(
                            "Tetragonisca angustula (Jataí)",
                            "Melipona quadrifasciata (Mandaçaia)",
                            "Melipona scutellaris (Uruçu)",
                            "Apis mellifera (Africanizada)",
                            "Scaptotrigona postica (Tubuna/Mandaguari)",
                            "Qualquer Enxame Silvestre"
                        ).forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    targetSpecies = s
                                    expSpecies = false
                                }
                            )
                        }
                    }
                }

                // Attractant Used
                ExposedDropdownMenuBox(
                    expanded = expAttractant,
                    onExpandedChange = { expAttractant = it }
                ) {
                    OutlinedTextField(
                        value = attractantUsed,
                        onValueChange = { attractantUsed = it },
                        label = { Text("Atrativo Utilizado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expAttractant) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expAttractant,
                        onDismissRequest = { expAttractant = false }
                    ) {
                        listOf(
                            "Extrato Alcoólico de Própolis de Jataí",
                            "Própolis Bruto + Capim Cidreira / Capim Limão",
                            "Feromônio de Rainha (Apis Nasonov)",
                            "Loção Concentrada Mista (Cera + Própolis + Geleia)",
                            "Cera Natural e Própolis de Mandaçaia"
                        ).forEach { a ->
                            DropdownMenuItem(
                                text = { Text(a) },
                                onClick = {
                                    attractantUsed = a
                                    expAttractant = false
                                }
                            )
                        }
                    }
                }

                // Status
                ExposedDropdownMenuBox(
                    expanded = expStatus,
                    onExpandedChange = { expStatus = it }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status da Isca") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expStatus) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expStatus,
                        onDismissRequest = { expStatus = false }
                    ) {
                        listOf("Armada", "Capturada", "Transferida", "Desarmada").forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st) },
                                onClick = {
                                    status = st
                                    expStatus = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = locationDescription,
                    onValueChange = { locationDescription = it },
                    label = { Text("Descrição do Local / Ponto de Fixação") },
                    modifier = Modifier.fillMaxWidth()
                )

                // GPS
                Text("📍 Coordenadas Geográficas (GPS):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                val coords = LocationHelper.getCurrentLocation(context)
                                if (coords != null) {
                                    latitude = coords.latitude.toString()
                                    longitude = coords.longitude.toString()
                                    if (coords.altitude != null) altitude = coords.altitude.toInt().toString()
                                    Toast.makeText(context, "GPS da Isca capturado com sucesso!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MeadowGreenSecondary)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GPS Atual", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showMapPicker = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Marcar no Mapa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (showMapPicker) {
                    val defaultRefApiary = apiaries.find { it.id == selectedApiaryId } ?: apiaries.firstOrNull()
                    MapLocationPickerDialog(
                        title = "Marcar Posição da Isca",
                        subtitle = "Posicione o pino exatamente na árvore ou ponto de fixação da isca",
                        initialLatitude = latitude.toDoubleOrNull() ?: defaultRefApiary?.latitude,
                        initialLongitude = longitude.toDoubleOrNull() ?: defaultRefApiary?.longitude,
                        onDismiss = { showMapPicker = false },
                        onConfirmLocation = { newLat, newLon ->
                            latitude = String.format(java.util.Locale.US, "%.6f", newLat)
                            longitude = String.format(java.util.Locale.US, "%.6f", newLon)
                            Toast.makeText(context, "Coordenadas da Isca marcadas pelo mapa!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        }
    }
}
}

// -------------------------------------------------------------
// DIALOG: VISTORIA DE ISCA
// -------------------------------------------------------------
@Composable
fun TrapCheckDialog(
    trap: BeeTrapEntity,
    onDismiss: () -> Unit,
    onConfirm: (isCaptured: Boolean, notes: String) -> Unit
) {
    var isCaptured by remember { mutableStateOf(trap.status == "Capturada") }
    var checkNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("👀 Vistoria na Isca: ${trap.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Registre a situação atual da isca:")

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCaptured) MeadowGreenSecondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCaptured = !isCaptured }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCaptured) Icons.Default.CheckCircle else Icons.Default.AddLocationAlt,
                            contentDescription = null,
                            tint = if (isCaptured) MeadowGreenSecondary else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isCaptured) "🎉 Enxame Capturado com Sucesso!" else "Aguardando Captura (Sem enxame)",
                                fontWeight = FontWeight.Bold,
                                color = if (isCaptured) MeadowGreenSecondary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isCaptured) "Tubo de cera/batedoras ativas confirmadas." else "Isca ainda vazia, atrativo ativo.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = checkNotes,
                    onValueChange = { checkNotes = it },
                    label = { Text("Anotações da Vistoria (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(isCaptured, checkNotes) }) {
                Text("Registrar Vistoria")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// -------------------------------------------------------------
// DIALOG: TRANSFERÊNCIA DE ISCA PARA COLMEIA DEFINITIVA
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrapTransferDialog(
    trap: BeeTrapEntity,
    apiaries: List<ApiaryEntity>,
    onDismiss: () -> Unit,
    onTransfer: (apiaryId: Long, hiveNumber: String, hiveType: String, species: String, category: String, frames: Int, notes: String) -> Unit
) {
    var selectedApiaryId by remember { mutableStateOf(trap.apiaryId ?: apiaries.firstOrNull()?.id ?: 0L) }
    var hiveNumber by remember { mutableStateOf("Colmeia Captura #${trap.id}") }
    var beeSpecies by remember { mutableStateOf(trap.targetSpecies) }
    val isMelipona = trap.targetSpecies.contains("Jataí") || trap.targetSpecies.contains("Mandaçaia") || trap.targetSpecies.contains("Uruçu")
    var category by remember { mutableStateOf(if (isMelipona) "Meliponíneo (Sem Ferrão / ASF)" else "Apis (Com Ferrão)") }
    var hiveType by remember { mutableStateOf(if (isMelipona) "Caixa INPA Modular (ASF)" else "Langstroth (Padrão 10 Quadros)") }
    var framesText by remember { mutableStateOf(if (isMelipona) "4" else "6") }
    var notes by remember { mutableStateOf("Transferido da isca '${trap.name}'.") }

    var expApiary by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📦 Transferir Enxame para Colmeia Oficial", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Crie a ficha da colmeia definitiva para alojar o enxame capturado na isca '${trap.name}':")

                // Apiary Destination
                ExposedDropdownMenuBox(
                    expanded = expApiary,
                    onExpandedChange = { expApiary = it }
                ) {
                    val currentApiaryName = apiaries.find { it.id == selectedApiaryId }?.name ?: "Selecione o Apiário"
                    OutlinedTextField(
                        value = currentApiaryName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Apiário de Destino") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expApiary) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expApiary,
                        onDismissRequest = { expApiary = false }
                    ) {
                        apiaries.forEach { a ->
                            DropdownMenuItem(
                                text = { Text(a.name) },
                                onClick = {
                                    selectedApiaryId = a.id
                                    expApiary = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = hiveNumber,
                    onValueChange = { hiveNumber = it },
                    label = { Text("Número / Nome da Nova Colmeia") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = beeSpecies,
                    onValueChange = { beeSpecies = it },
                    label = { Text("Espécie Confirmada") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hiveType,
                    onValueChange = { hiveType = it },
                    label = { Text("Modelo de Caixa Utilizado") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = framesText,
                    onValueChange = { framesText = it },
                    label = { Text("Número de Quadros / Discos Povoados") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas do Manejo de Transferência") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedApiaryId > 0 && hiveNumber.isNotBlank()) {
                        val frames = framesText.toIntOrNull() ?: 5
                        onTransfer(selectedApiaryId, hiveNumber, hiveType, beeSpecies, category, frames, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MeadowGreenSecondary)
            ) {
                Text("Confirmar Transferência")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
