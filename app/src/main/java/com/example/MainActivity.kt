package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.HiveEntity
import com.example.license.LicenseStatus
import com.example.ui.components.ApisBottomNav
import com.example.ui.components.BottomMenuCustomizationDialog
import com.example.ui.components.GeneralMapDialog
import com.example.ui.components.HiveMapLocationDialog
import com.example.ui.components.ApisTopBar
import com.example.ui.components.CombImageInspectionDialog
import com.example.ui.components.CombSampleType
import com.example.ui.components.DiagnosticResultDialog
import com.example.ui.components.ExportReportsDialog
import com.example.ui.components.GoogleDriveBackupDialog
import com.example.ui.components.HandsFreeFieldDialog
import com.example.ui.components.HiveDetailHistoryDialog
import com.example.ui.components.HiveQrCodeDialog
import com.example.ui.components.LicenseDialog
import com.example.ui.components.MaintenanceManejoTipsDialog
import com.example.ui.components.MarketingMaterialDialog
import com.example.ui.screens.FloralCalendarScreen
import com.example.ui.components.NotificationCenterDialog
import com.example.ui.components.NutritionCalculatorDialog
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.QrScannerDialog
import com.example.ui.components.QualitySealsDialog
import com.example.ui.components.QuickInspectionDialog
import com.example.ui.components.SystemSearchDialog
import com.example.ui.components.ThemeCustomizationDialog
import com.example.ui.components.SupportFeedbackDialog
import com.example.ui.screens.AiConsultantScreen
import com.example.ui.screens.ApiariesScreen
import com.example.ui.screens.BluetoothTagTrackingScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinancialScreen
import com.example.ui.screens.HarvestScreen
import com.example.ui.screens.InspectionsScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.theme.ApisSmartTheme
import com.example.ui.viewmodel.ApisSmartViewModel
import com.example.ui.viewmodel.ScreenTab
import com.example.util.LocationHelper

class MainActivity : ComponentActivity() {
    private val viewModel: ApisSmartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeConfig by viewModel.themeConfig.collectAsState()
            ApisSmartTheme(themeConfig = themeConfig) {
                ApisSmartApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ApisSmartApp(viewModel: ApisSmartViewModel) {
    val context = LocalContext.current
    val themeConfig by viewModel.themeConfig.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val visibleTabs by viewModel.visibleTabs.collectAsState()
    val dashboardCards by viewModel.dashboardCards.collectAsState()
    val apiaries by viewModel.apiaries.collectAsState()
    val hives by viewModel.hives.collectAsState()
    val beeTraps by viewModel.beeTraps.collectAsState()
    val capturedTrapsCount by viewModel.capturedTrapsCount.collectAsState()
    val activeTrapsCount by viewModel.activeTrapsCount.collectAsState()
    val inspections by viewModel.inspections.collectAsState()
    val harvests by viewModel.harvests.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val lowStockItems by viewModel.lowStockItems.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val pendingTasksCount by viewModel.pendingTasksCount.collectAsState()
    val sanitaryAlertCount by viewModel.sanitaryAlertCount.collectAsState()
    val totalHoneyKg by viewModel.totalHoneyKg.collectAsState()
    val aiConsultations by viewModel.aiConsultations.collectAsState()
    val isAiAnalyzing by viewModel.isAiAnalyzing.collectAsState()
    val isAiChatLoading by viewModel.isAiChatLoading.collectAsState()
    val lastAiResult by viewModel.lastAiResult.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val weatherInfo by viewModel.weatherState.collectAsState()
    val selectedWeatherApiaryId by viewModel.selectedWeatherApiaryId.collectAsState()
    val financialTransactions by viewModel.financialTransactions.collectAsState()
    val receiptLayoutConfig by viewModel.receiptLayoutConfig.collectAsState()
    val receiptsList by viewModel.receiptsList.collectAsState()
    val licenseData by viewModel.licenseData.collectAsState()
    val isVerifyingLicenseServer by viewModel.isVerifyingLicenseServer.collectAsState()
    val licenseServerMessage by viewModel.licenseServerMessage.collectAsState()
    val qualitySealGoals by viewModel.qualitySealGoals.collectAsState()

    // Bluetooth Tag Tracking States
    val bluetoothTags by viewModel.bluetoothTags.collectAsState()
    val discoveredTags by viewModel.discoveredBleTags.collectAsState()
    val targetRadarTag by viewModel.targetRadarTag.collectAsState()
    val isBleScanning by viewModel.isBleScanning.collectAsState()
    val isBleSimulationActive by viewModel.isBleSimulationActive.collectAsState()
    val isProximityAudioEnabled by viewModel.isProximityAudioEnabled.collectAsState()
    val isProximityHapticEnabled by viewModel.isProximityHapticEnabled.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog Control States
    var showLicenseDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showMenuCustomizationDialog by remember { mutableStateOf(false) }
    var showQuickInspectionDialog by remember { mutableStateOf(false) }
    var showNutritionDialog by remember { mutableStateOf(false) }
    var showCombImageDialog by remember { mutableStateOf(false) }
    var showHandsFreeDialog by remember { mutableStateOf(false) }
    var showQrScannerDialog by remember { mutableStateOf(false) }
    var showExportReportsDialog by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var showGeneralMapDialog by remember { mutableStateOf(false) }
    var showMarketingMaterialDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showMaintenanceTipsDialog by remember { mutableStateOf(false) }
    var showQualitySealsDialog by remember { mutableStateOf(false) }
    var showSupportFeedbackDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var selectedHiveForQr by remember { mutableStateOf<HiveEntity?>(null) }
    var selectedHiveForHistory by remember { mutableStateOf<HiveEntity?>(null) }
    var selectedHiveForMap by remember { mutableStateOf<HiveEntity?>(null) }

    // Auto-open license dialog if license is expired on startup or re-check
    LaunchedEffect(licenseData.status) {
        if (licenseData.status == LicenseStatus.EXPIRED) {
            showLicenseDialog = true
        }
    }

    // Display user messages via Snackbar
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("apis_smart_app_root"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            val totalNotifications = pendingTasksCount + sanitaryAlertCount + lowStockItems.size
            ApisTopBar(
                currentTab = currentTab,
                visibleTabs = visibleTabs,
                onNavigateToTab = { tab -> viewModel.navigateTo(tab) },
                onOpenThemeCustomization = { showThemeDialog = true },
                onOpenMenuCustomization = { showMenuCustomizationDialog = true },
                onOpenLicense = { showLicenseDialog = true },
                onOpenExportReports = { showExportReportsDialog = true },
                onOpenBackupRestore = { showBackupRestoreDialog = true },
                onOpenGeneralMap = { showGeneralMapDialog = true },
                onOpenQrScanner = { showQrScannerDialog = true },
                onOpenNutrition = { showNutritionDialog = true },
                onOpenSearch = { showSearchDialog = true },
                onOpenNotifications = { showNotificationsDialog = true },
                onOpenHandsFreeMode = { showHandsFreeDialog = true },
                onOpenMaintenanceTips = { showMaintenanceTipsDialog = true },
                onOpenQualitySeals = { showQualitySealsDialog = true },
                onOpenMarketingMaterial = { showMarketingMaterialDialog = true },
                onOpenSupport = { showSupportFeedbackDialog = true },
                onOpenPrivacyPolicy = { showPrivacyPolicyDialog = true },
                notificationCount = totalNotifications,
                licenseStatus = licenseData.status
            )
        },
        bottomBar = {
            ApisBottomNav(
                currentTab = currentTab,
                visibleTabs = visibleTabs,
                onTabSelected = { tab -> viewModel.navigateTo(tab) },
                onOpenMenuCustomization = { showMenuCustomizationDialog = true },
                pendingTasksCount = pendingTasksCount,
                sanitaryAlertCount = sanitaryAlertCount
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.honeycomb_bg_3d_1790683773548),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.35f)
            )
            when (currentTab) {
                ScreenTab.DASHBOARD -> {
                    DashboardScreen(
                        apiaries = apiaries,
                        hives = hives,
                        beeTraps = beeTraps,
                        tasks = tasks,
                        harvests = harvests,
                        totalHoneyKg = totalHoneyKg ?: 0.0,
                        sanitaryAlertCount = sanitaryAlertCount,
                        weatherInfo = weatherInfo,
                        selectedWeatherApiaryId = selectedWeatherApiaryId,
                        dashboardCards = dashboardCards,
                        onUpdateDashboardCards = { cards -> viewModel.updateDashboardCards(cards) },
                        onResetDashboardCards = { viewModel.resetDashboardCards() },
                        onSelectWeatherApiary = { id -> viewModel.selectApiaryForWeather(id) },
                        onRefreshWeather = { viewModel.refreshWeather() },
                        onNavigateTab = { tab -> viewModel.navigateTo(tab) },
                        onOpenQuickInspection = { showQuickInspectionDialog = true },
                        onOpenNutritionCalculator = { showNutritionDialog = true },
                        onOpenCombImageAnalysis = { showCombImageDialog = true },
                        onOpenHandsFreeMode = { showHandsFreeDialog = true },
                        onOpenQrScanner = { showQrScannerDialog = true },
                        onOpenExportReports = { showExportReportsDialog = true },
                        onOpenGeneralMap = { showGeneralMapDialog = true },
                        onOpenThemeCustomization = { showThemeDialog = true },
                        onOpenLicense = { showLicenseDialog = true },
                        onOpenMaintenanceTips = { showMaintenanceTipsDialog = true },
                        onOpenQualitySeals = { showQualitySealsDialog = true },
                        onOpenMarketingMaterial = { showMarketingMaterialDialog = true },
                        licenseData = licenseData,
                        onToggleTask = { task -> viewModel.toggleTaskCompletion(task) }
                    )
                }

                ScreenTab.APIARIES -> {
                    ApiariesScreen(
                        apiaries = apiaries,
                        hives = hives,
                        beeTraps = beeTraps,
                        capturedTrapsCount = capturedTrapsCount,
                        activeTrapsCount = activeTrapsCount,
                        onSaveApiary = { name, loc, flora, cap, notes, lat, lon, qCount, species, cat, hType, id ->
                            viewModel.saveApiary(name, loc, flora, cap, notes, lat, lon, qCount, species, cat, hType, id)
                        },
                        onDeleteApiary = { apiary -> viewModel.deleteApiary(apiary) },
                        onSaveHive = { apiaryId, num, species, cat, type, dims, qStatus, qColor, qLineage, qAge, str, temp, frames, supers, framesPerSup, estYield, health, qr, lat, lon, alt, acc, curWeight, tare, notes, id ->
                            viewModel.saveHive(
                                apiaryId, num, species, cat, type, dims, qStatus, qColor, qLineage, qAge, str, temp,
                                frames, supers, framesPerSup, estYield, health, qr, lat, lon, alt, acc, curWeight, tare, notes, id
                            )
                        },
                        onDeleteHive = { hive -> viewModel.deleteHive(hive) },
                        onInspectHive = { _, _ -> showQuickInspectionDialog = true },
                        onShowHiveQr = { _, hive -> selectedHiveForQr = hive },
                        onOpenHiveHistory = { hive -> selectedHiveForHistory = hive },
                        onSaveBeeTrap = { name, type, spec, attr, stat, lat, lon, alt, acc, desc, apiaryId, notes, id ->
                            viewModel.saveBeeTrap(name, type, spec, attr, stat, lat, lon, alt, acc, desc, apiaryId, notes, id)
                        },
                        onDeleteBeeTrap = { trap -> viewModel.deleteBeeTrap(trap) },
                        onRecordTrapCheck = { trap, isCaptured, notes -> viewModel.recordTrapCheck(trap, isCaptured, notes) },
                        onTransferTrapToHive = { trap, apiaryId, hiveNumber, hiveType, species, category, frames, notes ->
                            viewModel.transferTrapToHive(trap, apiaryId, hiveNumber, hiveType, species, category, frames, notes)
                        },
                        onOpenGeneralMap = { showGeneralMapDialog = true }
                    )
                }

                ScreenTab.INSPECTIONS -> {
                    InspectionsScreen(
                        inspections = inspections,
                        apiaries = apiaries,
                        hives = hives,
                        onOpenNewInspection = { showQuickInspectionDialog = true },
                        onOpenCombImageAnalysis = { showCombImageDialog = true },
                        onOpenHandsFreeMode = { showHandsFreeDialog = true },
                        onOpenQrScanner = { showQrScannerDialog = true },
                        onOpenMaintenanceTips = { showMaintenanceTipsDialog = true },
                        onUpdateInspection = { inspection -> viewModel.updateInspection(inspection) },
                        onDeleteInspection = { inspection -> viewModel.deleteInspection(inspection) }
                    )
                }

                ScreenTab.HARVESTS -> {
                    HarvestScreen(
                        harvests = harvests,
                        apiaries = apiaries,
                        hives = hives,
                        totalHoneyKg = totalHoneyKg ?: 0.0,
                        onRecordHarvest = { apiaryId, hiveId, prod, qty, moist, flora, supers, frames, framesPerSup, avgFrame, notes ->
                            viewModel.recordHarvest(apiaryId, hiveId, prod, qty, moist, flora, supers, frames, framesPerSup, avgFrame, notes)
                        },
                        onDeleteHarvest = { harvest -> viewModel.deleteHarvest(harvest) },
                        onOpenQualitySeals = { showQualitySealsDialog = true }
                    )
                }

                ScreenTab.INVENTORY -> {
                    InventoryScreen(
                        inventory = inventory,
                        lowStockItems = lowStockItems,
                        onUpdateQuantity = { item, delta -> viewModel.updateInventoryQuantity(item, delta) },
                        onSaveItem = { name, cat, qty, unit, min, notes, id ->
                            viewModel.saveInventoryItem(name, cat, qty, unit, min, notes, id)
                        },
                        onDeleteItem = { item -> viewModel.deleteInventoryItem(item) }
                    )
                }

                ScreenTab.SCHEDULE -> {
                    ScheduleScreen(
                        tasks = tasks,
                        apiaries = apiaries,
                        onToggleTask = { task -> viewModel.toggleTaskCompletion(task) },
                        onSaveTask = { title, desc, type, apiaryId, hiveId, apiaryName, hiveNumber, dueDate, priority ->
                            viewModel.saveTask(title, desc, type, apiaryId, hiveId, apiaryName, hiveNumber, dueDate, priority)
                        },
                        onDeleteTask = { task -> viewModel.deleteTask(task) },
                        onOpenMaintenanceTips = { showMaintenanceTipsDialog = true }
                    )
                }

                ScreenTab.FINANCE -> {
                    FinancialScreen(
                        transactions = financialTransactions,
                        apiaries = apiaries,
                        totalHivesCount = hives.size,
                        onAddTransaction = { type, cat, amt, dt, desc, apId, status, method, qty ->
                            viewModel.addFinancialTransaction(type, cat, amt, dt, desc, apId, status, method, qty)
                        },
                        onUpdateTransaction = { trans -> viewModel.updateFinancialTransaction(trans) },
                        onTogglePaymentStatus = { trans -> viewModel.togglePaymentStatus(trans) },
                        onDeleteTransaction = { trans -> viewModel.deleteFinancialTransaction(trans) },
                        receiptManager = viewModel.receiptManager,
                        receiptLayoutConfig = receiptLayoutConfig,
                        receiptsList = receiptsList,
                        onSaveReceiptLayout = { config -> viewModel.saveReceiptLayoutConfig(config) },
                        onResetReceiptLayout = { viewModel.resetReceiptLayoutConfig() },
                        onSaveReceipt = { receipt -> viewModel.saveReceipt(receipt) },
                        onDeleteReceipt = { receiptId -> viewModel.deleteReceipt(receiptId) }
                    )
                }

                ScreenTab.AI_ASSISTANT -> {
                    AiConsultantScreen(
                        consultations = aiConsultations,
                        apiaries = apiaries,
                        hives = hives,
                        isLoading = isAiChatLoading,
                        onSendMessage = { query, ctx -> viewModel.sendAiMessage(query, ctx) },
                        onClearHistory = { viewModel.clearAiHistory() }
                    )
                }

                ScreenTab.BLUETOOTH_TAGS -> {
                    BluetoothTagTrackingScreen(
                        bluetoothTags = bluetoothTags,
                        discoveredTags = discoveredTags,
                        targetRadarTag = targetRadarTag,
                        isScanning = isBleScanning,
                        isSimulationActive = isBleSimulationActive,
                        isAudioEnabled = isProximityAudioEnabled,
                        isHapticEnabled = isProximityHapticEnabled,
                        hives = hives,
                        apiaries = apiaries,
                        onToggleScan = { viewModel.toggleBleScan() },
                        onToggleSimulation = {
                            if (isBleSimulationActive) viewModel.stopBleSimulation()
                            else viewModel.startBleSimulation()
                        },
                        onSelectRadarTag = { tag -> viewModel.selectTargetRadarTag(tag) },
                        onToggleAudio = { viewModel.toggleProximityAudio() },
                        onToggleHaptic = { viewModel.toggleProximityHaptic() },
                        onSaveTag = { name, mac, type, hiveId, apiaryId, antiTheft, notes, id ->
                            viewModel.saveBluetoothTag(name, mac, type, hiveId, apiaryId, antiTheft, notes, id)
                        },
                        onDeleteTag = { tag -> viewModel.deleteBluetoothTag(tag) },
                        onToggleAntiTheft = { tag -> viewModel.toggleAntiTheft(tag) },
                        onUpdateGpsLocation = { tag -> viewModel.updateTagGpsLocation(tag) },
                        onOpenInspectionForHive = { hive ->
                            showQuickInspectionDialog = true
                        },
                        onOpenMapForTag = { lat, lon, title ->
                            val intent = LocationHelper.createShareLocationIntent(title, lat, lon)
                            context.startActivity(intent)
                        }
                    )
                }

                ScreenTab.FLORAL_CALENDAR -> {
                    FloralCalendarScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Theme Customization Dialog
    if (showThemeDialog) {
        ThemeCustomizationDialog(
            currentConfig = themeConfig,
            visibleTabs = visibleTabs,
            onDismiss = { showThemeDialog = false },
            onSaveConfig = { newConfig ->
                viewModel.updateThemeConfig(newConfig)
                showThemeDialog = false
            },
            onResetDefaults = {
                viewModel.resetThemeConfig()
                showThemeDialog = false
            },
            onUpdateVisibleTabs = { updatedTabs ->
                viewModel.updateVisibleTabs(updatedTabs)
            }
        )
    }

    // Bottom Menu Customization Dialog
    if (showMenuCustomizationDialog) {
        BottomMenuCustomizationDialog(
            visibleTabs = visibleTabs,
            onDismiss = { showMenuCustomizationDialog = false },
            onSaveTabs = { updatedTabs ->
                viewModel.updateVisibleTabs(updatedTabs)
                showMenuCustomizationDialog = false
            },
            onResetDefaults = {
                viewModel.updateVisibleTabs(
                    listOf(
                        ScreenTab.APIARIES,
                        ScreenTab.INSPECTIONS,
                        ScreenTab.DASHBOARD,
                        ScreenTab.HARVESTS,
                        ScreenTab.SCHEDULE,
                        ScreenTab.AI_ASSISTANT
                    )
                )
            }
        )
    }

    // General Map Dialog
    if (showGeneralMapDialog) {
        GeneralMapDialog(
            apiaries = apiaries,
            hives = hives,
            traps = beeTraps,
            onDismiss = { showGeneralMapDialog = false }
        )
    }

    // Native Location Map Dialog
    selectedHiveForMap?.let { hive ->
        val apiary = apiaries.find { it.id == hive.apiaryId }
        val otherHives = hives.filter { it.apiaryId == hive.apiaryId && it.id != hive.id }
        HiveMapLocationDialog(
            hive = hive,
            apiary = apiary,
            otherHivesInApiary = otherHives,
            onDismiss = { selectedHiveForMap = null }
        )
    }

    // Individual Hive History & Prontuário Dialog
    selectedHiveForHistory?.let { hive ->
        val apiary = apiaries.find { it.id == hive.apiaryId }
        HiveDetailHistoryDialog(
            hive = hive,
            apiary = apiary,
            viewModel = viewModel,
            inspections = inspections,
            harvests = harvests,
            onDismiss = { selectedHiveForHistory = null },
            onOpenMap = { lat, lon, name ->
                selectedHiveForMap = HiveEntity(
                    apiaryId = hive.apiaryId,
                    hiveNumber = hive.hiveNumber,
                    beeSpecies = hive.beeSpecies,
                    speciesCategory = hive.speciesCategory,
                    hiveType = hive.hiveType,
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

    // Quick Inspection Dialog
    if (showQuickInspectionDialog) {
        QuickInspectionDialog(
            apiaries = apiaries,
            hives = hives,
            isAiAnalyzing = isAiAnalyzing,
            onDismiss = { showQuickInspectionDialog = false },
            onSubmitAiFieldNote = { apiaryId, hiveId, rawNotes, inspectorName ->
                viewModel.processFieldNoteWithAi(
                    rawNotes = rawNotes,
                    apiaryId = apiaryId,
                    hiveId = hiveId,
                    inspectorName = inspectorName,
                    onCompleted = {
                        showQuickInspectionDialog = false
                    }
                )
            },
            onSubmitManualInspection = { entity ->
                viewModel.saveManualInspection(entity)
                showQuickInspectionDialog = false
            }
        )
    }

    // Module 2: Nutrition Calculator Dialog
    if (showNutritionDialog) {
        NutritionCalculatorDialog(
            apiaries = apiaries,
            onDismiss = { showNutritionDialog = false },
            onDeductStock = { sugar, yeast, soy ->
                viewModel.deductNutritionIngredients(sugar, yeast, soy)
            },
            onScheduleTask = { title, desc, apiaryId, apiaryName ->
                viewModel.scheduleFeedingTask(title, desc, apiaryId, apiaryName)
            }
        )
    }

    // Module 1: Multimodal Comb Image Inspection Dialog
    if (showCombImageDialog) {
        CombImageInspectionDialog(
            apiaries = apiaries,
            hives = hives,
            isAnalyzing = isAiAnalyzing,
            onDismiss = { showCombImageDialog = false },
            onSubmitImageAnalysis = { apiaryId, hiveId, sampleType ->
                val sampleBase64 = when (sampleType) {
                    CombSampleType.UNIFORM_BROOD -> "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
                    CombSampleType.VARROA_ALERT -> "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
                    CombSampleType.HONEY_SUPERS -> "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg=="
                }
                viewModel.processCombImageWithAi(
                    base64Image = sampleBase64,
                    mimeType = "image/png",
                    apiaryId = apiaryId,
                    hiveId = hiveId,
                    inspectorName = "Diagnóstico Óptico IA",
                    onCompleted = {
                        showCombImageDialog = false
                    }
                )
            }
        )
    }

    // Module 6: Hands-free Voice Mode Dialog
    if (showHandsFreeDialog) {
        HandsFreeFieldDialog(
            apiaries = apiaries,
            hives = hives,
            isProcessing = isAiAnalyzing,
            onDismiss = { showHandsFreeDialog = false },
            onPhotoCaptured = { hiveId, photoUri ->
                viewModel.saveHivePhoto(
                    hiveId = hiveId,
                    photoUri = photoUri,
                    category = "Sanidade & Diagnóstico",
                    caption = "Foto do favo capturada em modo de voz",
                    inspectorName = "Manejo por Voz",
                    notes = "Registro fotográfico capturado com comando de voz"
                )
            },
            onSubmitVoiceDictation = { apiaryId, hiveId, transcription ->
                viewModel.processFieldNoteWithAi(
                    rawNotes = transcription,
                    apiaryId = apiaryId,
                    hiveId = hiveId,
                    inspectorName = "Manejo por Voz",
                    onCompleted = {
                        showHandsFreeDialog = false
                    }
                )
            }
        )
    }

    // Module 4: QR Code Scanner Dialog
    if (showQrScannerDialog) {
        QrScannerDialog(
            hives = hives,
            apiaries = apiaries,
            onDismiss = { showQrScannerDialog = false },
            onHiveScanned = { hive ->
                showQrScannerDialog = false
                showQuickInspectionDialog = true
            }
        )
    }

    // Module 4: Hive QR Tag Badge Modal
    selectedHiveForQr?.let { hive ->
        val apiary = apiaries.find { it.id == hive.apiaryId }
        HiveQrCodeDialog(
            hive = hive,
            apiary = apiary,
            onDismiss = { selectedHiveForQr = null }
        )
    }

    // Module 5: Export CSV & Traceability Reports
    if (showExportReportsDialog) {
        ExportReportsDialog(
            onDismiss = { showExportReportsDialog = false },
            onExportCsv = {
                viewModel.shareCsvExport { shareIntent ->
                    context.startActivity(shareIntent)
                }
            },
            onShareTraceability = {
                viewModel.shareTraceabilityReport { shareIntent ->
                    context.startActivity(shareIntent)
                }
            },
            onOpenBackupRestore = {
                showBackupRestoreDialog = true
            }
        )
    }

    // Google Drive Cloud Backup & Restore Dialog
    if (showBackupRestoreDialog) {
        GoogleDriveBackupDialog(
            viewModel = viewModel,
            onDismiss = { showBackupRestoreDialog = false }
        )
    }

    // Last AI Diagnostic Dialog popup
    lastAiResult?.let { result ->
        DiagnosticResultDialog(
            result = result,
            onDismiss = { viewModel.clearLastAiResult() }
        )
    }

    // License Administration & Activation Dialog (Outside Google Play)
    if (showLicenseDialog || licenseData.status == LicenseStatus.EXPIRED) {
        LicenseDialog(
            licenseData = licenseData,
            isVerifyingServer = isVerifyingLicenseServer,
            serverMessage = licenseServerMessage,
            onDismiss = { showLicenseDialog = false },
            onActivateKey = { key -> viewModel.activateLicenseWithKey(key) },
            onVerifyServer = { customUrl -> viewModel.verifyLicenseWithServer(customUrl) },
            onUpdateServerUrl = { url -> viewModel.updateLicenseServerUrl(url) },
            onGenerateAdminKey = { tier, days, devId, count, assocName ->
                viewModel.generateAdminLicenseKey(tier, days, devId, count, assocName)
            },
            onResetTrial = { viewModel.resetLicenseToTrial() },
            onDeactivate = { viewModel.deactivateLicense() },
            onShowPrivacyPolicy = { showPrivacyPolicyDialog = true }
        )
    }

    if (showSearchDialog) {
        SystemSearchDialog(
            apiaries = apiaries,
            hives = hives,
            inventory = inventory,
            tasks = tasks,
            onDismiss = { showSearchDialog = false },
            onNavigateToTab = { tab -> viewModel.navigateTo(tab) }
        )
    }

    if (showNotificationsDialog) {
        NotificationCenterDialog(
            tasks = tasks,
            lowStockItems = lowStockItems,
            sanitaryAlertCount = sanitaryAlertCount,
            onDismiss = { showNotificationsDialog = false },
            onNavigateToTab = { tab -> viewModel.navigateTo(tab) }
        )
    }

    if (showMaintenanceTipsDialog) {
        MaintenanceManejoTipsDialog(
            onScheduleTaskFromTip = { title, desc, taskType, days ->
                viewModel.createMaintenanceTaskFromTip(title, desc, taskType, days)
            },
            onDismiss = { showMaintenanceTipsDialog = false }
        )
    }

    if (showQualitySealsDialog) {
        QualitySealsDialog(
            goals = qualitySealGoals,
            onToggleGoal = { goal -> viewModel.toggleSealGoal(goal) },
            onSaveGoal = { sealType, title, desc, category, evidence, mandatory ->
                viewModel.saveSealGoal(sealType, title, desc, category, evidence, mandatory)
            },
            onDeleteGoal = { goal -> viewModel.deleteSealGoal(goal) },
            onShareComplianceReport = { sealType, onShare ->
                viewModel.shareSealComplianceReport(sealType, onShare)
            },
            onDismiss = { showQualitySealsDialog = false }
        )
    }

    if (showMarketingMaterialDialog) {
        MarketingMaterialDialog(
            onDismiss = { showMarketingMaterialDialog = false }
        )
    }

    if (showSupportFeedbackDialog) {
        SupportFeedbackDialog(
            onDismiss = { showSupportFeedbackDialog = false }
        )
    }

    if (showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyPolicyDialog = false }
        )
    }
}
