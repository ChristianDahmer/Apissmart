package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.BluetoothTagTracker
import com.example.bluetooth.DiscoveredBleTag
import com.example.data.ai.StructuredInspectionResult
import com.example.data.backup.BackupManager
import com.example.data.backup.BackupRestoreResult
import com.example.data.backup.BackupSummary
import com.example.data.local.entities.AiConsultationEntity
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.BeeTrapEntity
import com.example.data.local.entities.BluetoothTagEntity
import com.example.data.local.entities.FinancialTransactionEntity
import com.example.data.local.entities.FloralObservationEntity
import com.example.data.local.entities.HarvestEntity
import com.example.data.local.entities.HiveEntity
import com.example.data.local.entities.HivePhotoEntity
import com.example.data.local.entities.HiveWeightEntity
import com.example.data.local.entities.InspectionEntity
import com.example.data.local.entities.InventoryEntity
import com.example.data.local.entities.QualitySealGoalEntity
import com.example.data.local.entities.QueenHistoryEntity
import com.example.data.local.entities.TaskScheduleEntity
import com.example.data.repository.ApisSmartRepository
import com.example.license.LicenseData
import com.example.license.LicenseManager
import com.example.license.LicenseStatus
import com.example.license.LicenseTier
import com.example.ui.theme.AppThemeConfig
import com.example.ui.theme.ThemePreferencesManager
import com.example.util.ApiaryWeatherEngine
import com.example.util.HiveNamingHelper
import com.example.util.LocationHelper
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab(val title: String, val iconEmoji: String) {
    DASHBOARD("Iniciar", "🐝"),
    APIARIES("Apiários", "🌿"),
    INSPECTIONS("Inspeções", "📋"),
    HARVESTS("Colheita", "⚖️"),
    INVENTORY("Estoque", "📦"),
    SCHEDULE("Manejos", "📅"),
    FINANCE("Finanças", "💰"),
    AI_ASSISTANT("IA Apis", "🤖"),
    BLUETOOTH_TAGS("Tags BLE", "📡"),
    FLORAL_CALENDAR("Floradas", "🌸")
}

enum class DashboardCardType(val title: String, val iconEmoji: String, val description: String) {
    ALERTS("Card de Alertas", "🚨", "Alertas sanitários, avisos de clima e tarefas urgentes"),
    OVERVIEW("Card de Visão Geral", "📊", "Apiários, colmeias ativas, produção e plantel"),
    BLUETOOTH_RADAR("Card de Radar BLE", "📡", "Localizador e rastreador de tags de colmeias"),
    VOICE_INSPECTION("Card de Inspeção por Voz", "🎙️", "Gravação em campo com mãos livres e IA"),
    APIARY_MAP("Card de Mapa do Apiário", "🗺️", "Geolocalização, coordenadas GPS e mapa geral"),
    WEATHER_MANEJO("Card de Clima & Voo", "🌤️", "Condições climáticas e janela de manejo"),
    QUICK_TOOLS("Card de Ferramentas", "⚡", "Atalhos rápidos para finanças, QR, nutrição e laudos"),
    SCHEDULED_TASKS("Card de Manejos Agendados", "📅", "Próximas intervenções e tarefas do dia")
}

data class ApiaryWeatherInfo(
    val temperatureC: Int = 24,
    val humidityPercent: Int = 58,
    val windSpeedKmh: Int = 9,
    val rainProbabilityPercent: Int = 5,
    val conditionDescription: String = "Ensolarado com poucas nuvens",
    val status: ManejoWindowStatus = ManejoWindowStatus.IDEAL,
    val advice: String = "Condição ideal para revisão profunda, troca de quadros e manejo de rainhas.",
    val flightConditionText: String = "🟢 Voo Ideal (Forrageamento Pleno)",
    val apiaryId: Long = 0L,
    val apiaryName: String = "Apiário Sol Nascente",
    val location: String = "Serra da Mantiqueira, SP",
    val latitude: Double? = -22.7394,
    val longitude: Double? = -45.5913,
    val isOnlineData: Boolean = false,
    val floraInfo: String = "Florada sazonal baseada na região"
)

enum class ManejoWindowStatus {
    IDEAL,
    CAUTION,
    PROHIBITED
}

class ApisSmartViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ApisSmartRepository(application)
    private val themePreferencesManager = ThemePreferencesManager(application)
    private val licenseManager = LicenseManager(application)
    val backupManager = BackupManager(application)
    val receiptManager = com.example.receipt.ReceiptManager(application)
    val receiptLayoutConfig: StateFlow<com.example.receipt.model.ReceiptLayoutConfig> = receiptManager.layoutConfig
    val receiptsList: StateFlow<List<com.example.receipt.model.ReceiptData>> = receiptManager.receipts

    private val _databaseSummary = MutableStateFlow<BackupSummary?>(null)
    val databaseSummary: StateFlow<BackupSummary?> = _databaseSummary.asStateFlow()

    private val _isBackupOperationInProgress = MutableStateFlow(false)
    val isBackupOperationInProgress: StateFlow<Boolean> = _isBackupOperationInProgress.asStateFlow()

    private val _lastBackupResult = MutableStateFlow<BackupRestoreResult?>(null)
    val lastBackupResult: StateFlow<BackupRestoreResult?> = _lastBackupResult.asStateFlow()

    val licenseData: StateFlow<LicenseData> = licenseManager.licenseData

    private val _isVerifyingLicenseServer = MutableStateFlow(false)
    val isVerifyingLicenseServer: StateFlow<Boolean> = _isVerifyingLicenseServer.asStateFlow()

    private val _licenseServerMessage = MutableStateFlow<String?>(null)
    val licenseServerMessage: StateFlow<String?> = _licenseServerMessage.asStateFlow()

    init {
        checkLicenseStartup()
        loadDatabaseSummary()
    }

    fun loadDatabaseSummary() {
        viewModelScope.launch {
            _databaseSummary.value = backupManager.getCurrentDatabaseSummary()
        }
    }

    fun exportBackupToUri(uri: Uri, onComplete: (BackupRestoreResult) -> Unit) {
        viewModelScope.launch {
            _isBackupOperationInProgress.value = true
            val result = backupManager.exportBackupToUri(uri)
            _lastBackupResult.value = result
            _isBackupOperationInProgress.value = false
            if (result.success) {
                _userMessage.value = "☁️ Backup completo exportado com sucesso!"
                loadDatabaseSummary()
            } else {
                _userMessage.value = "❌ Erro ao exportar backup: ${result.message}"
            }
            onComplete(result)
        }
    }

    fun shareBackupToGoogleDrive(onShareIntentReady: (Intent, BackupSummary) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isBackupOperationInProgress.value = true
            try {
                val (file, summary) = backupManager.createLocalBackupFile()
                val sendIntent = backupManager.getGoogleDriveShareIntent(file)
                val chooser = Intent.createChooser(sendIntent, "Salvar no Google Drive / Compartilhar Backup")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                _isBackupOperationInProgress.value = false
                onShareIntentReady(chooser, summary)
            } catch (e: Exception) {
                _isBackupOperationInProgress.value = false
                onError(e.localizedMessage ?: "Erro ao gerar arquivo de backup")
            }
        }
    }

    fun restoreBackupFromUri(uri: Uri, wipeExisting: Boolean, onComplete: (BackupRestoreResult) -> Unit) {
        viewModelScope.launch {
            _isBackupOperationInProgress.value = true
            val result = backupManager.restoreBackupFromUri(uri, wipeExisting)
            _lastBackupResult.value = result
            _isBackupOperationInProgress.value = false
            if (result.success) {
                _userMessage.value = "✅ Restauração concluída com sucesso! ${result.summary?.totalCount ?: 0} registros recuperados."
                loadDatabaseSummary()
            } else {
                _userMessage.value = "❌ Falha na restauração: ${result.message}"
            }
            onComplete(result)
        }
    }

    fun clearBackupResult() {
        _lastBackupResult.value = null
    }

    fun checkLicenseStartup() {
        val current = licenseManager.checkOnStartup()
        if (current.status == LicenseStatus.EXPIRED) {
            _userMessage.value = "🔒 Licença ApisSmart expirada. Ative sua chave de acesso ou valide no servidor."
        } else if (current.status == LicenseStatus.TRIAL && current.daysRemaining <= 3) {
            _userMessage.value = "⏳ Atenção: Restam apenas ${current.daysRemaining} dias de avaliação."
        }
    }

    val themeConfig: StateFlow<AppThemeConfig> = themePreferencesManager.themeConfig

    fun updateThemeConfig(config: AppThemeConfig) {
        themePreferencesManager.saveConfig(config)
        _userMessage.value = "🎨 Personalização de visual e fontes atualizada!"
    }

    fun resetThemeConfig() {
        themePreferencesManager.resetToDefaults()
        _userMessage.value = "🎨 Configurações de tema restauradas para o padrão."
    }

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(ScreenTab.DASHBOARD)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    fun navigateTo(tab: ScreenTab) {
        _currentTab.value = tab
    }

    // Database Streams
    val apiaries: StateFlow<List<ApiaryEntity>> = repository.allApiaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hives: StateFlow<List<HiveEntity>> = repository.allHives
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val beeTraps: StateFlow<List<BeeTrapEntity>> = repository.allBeeTraps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val capturedTrapsCount: StateFlow<Int> = repository.capturedTrapsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeTrapsCount: StateFlow<Int> = repository.activeTrapsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val inspections: StateFlow<List<InspectionEntity>> = repository.allInspections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val harvests: StateFlow<List<HarvestEntity>> = repository.allHarvests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventory: StateFlow<List<InventoryEntity>> = repository.allInventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockItems: StateFlow<List<InventoryEntity>> = repository.lowStockInventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskScheduleEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasksCount: StateFlow<Int> = repository.pendingTasksCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val sanitaryAlertCount: StateFlow<Int> = repository.sanitaryAlertCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allHivePhotos: StateFlow<List<HivePhotoEntity>> = repository.getAllPhotos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financialTransactions: StateFlow<List<FinancialTransactionEntity>> = repository.allFinancialTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalHoneyKg: StateFlow<Double?> = repository.totalHoneyHarvested
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val aiConsultations: StateFlow<List<AiConsultationEntity>> = repository.allConsultations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val floralObservations: StateFlow<List<FloralObservationEntity>> = repository.allFloralObservations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedApiaryForFloralCalendar = MutableStateFlow<ApiaryEntity?>(null)
    val selectedApiaryForFloralCalendar: StateFlow<ApiaryEntity?> = _selectedApiaryForFloralCalendar.asStateFlow()

    fun selectApiaryForFloralCalendar(apiary: ApiaryEntity?) {
        _selectedApiaryForFloralCalendar.value = apiary
    }

    fun saveFloralObservation(
        apiaryId: Long,
        plantName: String,
        scientificName: String,
        resourceType: String,
        startMonth: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val observation = FloralObservationEntity(
                apiaryId = apiaryId,
                plantName = plantName,
                scientificName = scientificName,
                resourceType = resourceType,
                startMonth = startMonth,
                endMonth = startMonth,
                notes = notes,
                createdAt = System.currentTimeMillis()
            )
            repository.insertFloralObservation(observation)
            _userMessage.value = "🌸 Observação botânica de '$plantName' registrada!"
        }
    }

    fun deleteFloralObservation(observation: FloralObservationEntity) {
        viewModelScope.launch {
            repository.deleteFloralObservation(observation)
            _userMessage.value = "Observação removida."
        }
    }

    // --- Bluetooth Low Energy Tag Tracking Streams ---
    private val bluetoothTracker = BluetoothTagTracker(application)

    val bluetoothTags: StateFlow<List<BluetoothTagEntity>> = repository.allBluetoothTags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val outOfRangeAlertCount: StateFlow<Int> = repository.outOfRangeAlertCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isBleScanning: StateFlow<Boolean> = bluetoothTracker.isScanning
    val discoveredBleTags: StateFlow<Map<String, DiscoveredBleTag>> = bluetoothTracker.discoveredTags
    val isBleSimulationActive: StateFlow<Boolean> = bluetoothTracker.isSimulationActive
    val isBluetoothSupported: StateFlow<Boolean> = bluetoothTracker.isBluetoothSupported
    val isBluetoothEnabled: StateFlow<Boolean> = bluetoothTracker.isBluetoothEnabled

    private val _targetRadarTag = MutableStateFlow<BluetoothTagEntity?>(null)
    val targetRadarTag: StateFlow<BluetoothTagEntity?> = _targetRadarTag.asStateFlow()

    private val _isProximityAudioEnabled = MutableStateFlow(true)
    val isProximityAudioEnabled: StateFlow<Boolean> = _isProximityAudioEnabled.asStateFlow()

    private val _isProximityHapticEnabled = MutableStateFlow(true)
    val isProximityHapticEnabled: StateFlow<Boolean> = _isProximityHapticEnabled.asStateFlow()

    // --- Quality Seal Goals & Metas de Certificação Streams ---
    val qualitySealGoals: StateFlow<List<QualitySealGoalEntity>> = repository.allQualitySealGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedSealGoalsCount: StateFlow<Int> = repository.completedSealGoalsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // UI Feedback States
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _isAiAnalyzing = MutableStateFlow(false)
    val isAiAnalyzing: StateFlow<Boolean> = _isAiAnalyzing.asStateFlow()

    private val _isAiChatLoading = MutableStateFlow(false)
    val isAiChatLoading: StateFlow<Boolean> = _isAiChatLoading.asStateFlow()

    private val _lastAiResult = MutableStateFlow<StructuredInspectionResult?>(null)
    val lastAiResult: StateFlow<StructuredInspectionResult?> = _lastAiResult.asStateFlow()

    private val _selectedHive = MutableStateFlow<HiveEntity?>(null)
    val selectedHive: StateFlow<HiveEntity?> = _selectedHive.asStateFlow()

    // Weather State
    private val _selectedWeatherApiaryId = MutableStateFlow<Long?>(null)
    val selectedWeatherApiaryId: StateFlow<Long?> = _selectedWeatherApiaryId.asStateFlow()

    private val _weatherState = MutableStateFlow(
        ApiaryWeatherInfo(
            temperatureC = 24,
            humidityPercent = 55,
            windSpeedKmh = 8,
            rainProbabilityPercent = 5,
            conditionDescription = "Aguardando cadastro de apiário",
            status = ManejoWindowStatus.IDEAL,
            advice = "Cadastre um apiário para calcular as condições de voo e telemetria climática em tempo real.",
            apiaryName = ""
        )
    )
    val weatherState: StateFlow<ApiaryWeatherInfo> = _weatherState.asStateFlow()

    // Customizable Bottom Menu Tabs
    private val menuPrefs = application.getSharedPreferences("apis_smart_menu_prefs", Context.MODE_PRIVATE)
    private val _visibleTabs = MutableStateFlow<List<ScreenTab>>(loadVisibleTabs())
    val visibleTabs: StateFlow<List<ScreenTab>> = _visibleTabs.asStateFlow()

    // Customizable Iniciar / Dashboard Screen Cards
    private val dashboardPrefs = application.getSharedPreferences("apis_smart_dashboard_prefs", Context.MODE_PRIVATE)
    private val _dashboardCards = MutableStateFlow<List<DashboardCardType>>(loadDashboardCards())
    val dashboardCards: StateFlow<List<DashboardCardType>> = _dashboardCards.asStateFlow()

    private fun loadDashboardCards(): List<DashboardCardType> {
        val cardsStr = dashboardPrefs.getString("dashboard_cards", null)
        if (cardsStr.isNullOrBlank()) {
            return DashboardCardType.entries.toList()
        }
        val loaded = cardsStr.split(",").mapNotNull {
            try { DashboardCardType.valueOf(it) } catch (_: Exception) { null }
        }
        return if (loaded.isEmpty()) DashboardCardType.entries.toList() else loaded
    }

    fun updateDashboardCards(cards: List<DashboardCardType>) {
        val validCards = if (cards.isEmpty()) DashboardCardType.entries.toList() else cards
        dashboardPrefs.edit().putString("dashboard_cards", validCards.joinToString(",") { it.name }).apply()
        _dashboardCards.value = validCards
        _userMessage.value = "⚙️ Disposição dos cards da tela Iniciar atualizada!"
    }

    fun resetDashboardCards() {
        dashboardPrefs.edit().remove("dashboard_cards").apply()
        _dashboardCards.value = DashboardCardType.entries.toList()
        _userMessage.value = "🔄 Cards da tela Iniciar restaurados para o padrão."
    }

    private fun loadVisibleTabs(): List<ScreenTab> {
        val tabsStr = menuPrefs.getString("visible_tabs", null)
        if (tabsStr.isNullOrBlank()) {
            return listOf(
                ScreenTab.APIARIES,
                ScreenTab.INSPECTIONS,
                ScreenTab.DASHBOARD,
                ScreenTab.HARVESTS,
                ScreenTab.SCHEDULE,
                ScreenTab.AI_ASSISTANT
            )
        }
        val loaded = tabsStr.split(",").mapNotNull {
            try { ScreenTab.valueOf(it) } catch (_: Exception) { null }
        }
        return if (loaded.isEmpty()) {
            listOf(
                ScreenTab.APIARIES,
                ScreenTab.INSPECTIONS,
                ScreenTab.DASHBOARD,
                ScreenTab.HARVESTS,
                ScreenTab.SCHEDULE,
                ScreenTab.AI_ASSISTANT
            )
        } else {
            loaded
        }
    }

    fun updateVisibleTabs(tabs: List<ScreenTab>) {
        val cleanedTabs = if (tabs.isEmpty()) {
            listOf(ScreenTab.DASHBOARD, ScreenTab.APIARIES)
        } else {
            tabs
        }
        menuPrefs.edit().putString("visible_tabs", cleanedTabs.joinToString(",") { it.name }).apply()
        _visibleTabs.value = cleanedTabs
        _userMessage.value = "⚙️ Menu inferior personalizado com sucesso!"
    }

    init {
        // 1. Monitoramento ativo do apiário selecionado para o clima
        viewModelScope.launch {
            apiaries.collect { list ->
                if (list.isNotEmpty()) {
                    val currentId = _selectedWeatherApiaryId.value
                    val currentApiary = list.find { it.id == currentId } ?: list.first()
                    if (_selectedWeatherApiaryId.value != currentApiary.id) {
                        _selectedWeatherApiaryId.value = currentApiary.id
                    }
                    recalculateWeatherForApiary(currentApiary)
                } else {
                    _selectedWeatherApiaryId.value = null
                    _weatherState.value = ApiaryWeatherInfo(
                        temperatureC = 24,
                        humidityPercent = 55,
                        windSpeedKmh = 8,
                        rainProbabilityPercent = 5,
                        conditionDescription = "Nenhum apiário cadastrado",
                        status = ManejoWindowStatus.IDEAL,
                        advice = "Cadastre um apiário para calcular as condições de voo e telemetria climática em tempo real.",
                        apiaryName = ""
                    )
                }
            }
        }

        // 2. Loop de atualização automática do clima e condições de voo a cada 15 minutos (900000ms)
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(15 * 60 * 1000L)
                val currentId = _selectedWeatherApiaryId.value
                val target = apiaries.value.find { it.id == currentId } ?: apiaries.value.firstOrNull()
                target?.let {
                    recalculateWeatherForApiary(it)
                    android.util.Log.d("ApisSmartViewModel", "🌤️ Card clima e condições de voo atualizado automaticamente!")
                }
            }
        }

        // 3. Sincronização em tempo real das Tags BLE detectadas e feedback de proximidade
        viewModelScope.launch {
            discoveredBleTags.collect { map ->
                val registered = bluetoothTags.value
                val target = _targetRadarTag.value
                for ((mac, discTag) in map) {
                    val matching = registered.find { it.tagMacAddress.equals(mac, ignoreCase = true) }
                    if (matching != null) {
                        val currentLoc = LocationHelper.getCurrentLocation(application)
                        val isOut = matching.isAntiTheftEnabled && discTag.distanceMeters > matching.alertDistanceThresholdMeters
                        repository.updateTagTelemetry(
                            macAddress = mac,
                            rssi = discTag.rssi,
                            distanceMeters = discTag.distanceMeters,
                            latitude = currentLoc?.latitude ?: matching.lastLatitude,
                            longitude = currentLoc?.longitude ?: matching.lastLongitude,
                            battery = discTag.batteryPercent ?: matching.batteryPercent,
                            temperatureC = discTag.temperatureC ?: matching.temperatureC,
                            humidityPercent = discTag.humidityPercent ?: matching.humidityPercent,
                            isOutOfRange = isOut
                        )

                        // Se a tag sendo rastreada no Radar for esta, aciona o bip de proximidade
                        if (target != null && target.tagMacAddress.equals(mac, ignoreCase = true)) {
                            bluetoothTracker.triggerProximityFeedback(
                                distanceMeters = discTag.distanceMeters,
                                isAudioEnabled = _isProximityAudioEnabled.value,
                                isHapticEnabled = _isProximityHapticEnabled.value
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun clearLastAiResult() {
        _lastAiResult.value = null
    }

    fun selectHive(hive: HiveEntity?) {
        _selectedHive.value = hive
    }

    // --- Queen History & Replacement ---
    fun getQueenHistoryForHive(hiveId: Long): Flow<List<QueenHistoryEntity>> {
        return repository.getQueenHistoryByHive(hiveId)
    }

    fun recordQueenChange(
        hiveId: Long,
        previousLineage: String,
        previousColor: String,
        newLineage: String,
        newColor: String,
        reason: String,
        method: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.recordQueenChange(
                hiveId = hiveId,
                previousLineage = previousLineage,
                previousColor = previousColor,
                newLineage = newLineage,
                newColor = newColor,
                reason = reason,
                method = method,
                notes = notes
            )
            _userMessage.value = "👑 Troca de rainha registrada com sucesso! Ficha da colmeia atualizada."
        }
    }

    // --- Hive Weight & Honey Stock Estimation ---
    fun getWeightsForHive(hiveId: Long): Flow<List<HiveWeightEntity>> {
        return repository.getWeightsByHive(hiveId)
    }

    fun recordHiveWeight(
        hiveId: Long,
        totalWeightKg: Double,
        tareWeightKg: Double,
        notes: String
    ) {
        viewModelScope.launch {
            repository.recordHiveWeight(
                hiveId = hiveId,
                totalWeightKg = totalWeightKg,
                tareWeightKg = tareWeightKg,
                notes = notes
            )
            val honeyStock = (totalWeightKg - tareWeightKg).coerceAtLeast(0.0)
            val alert = if (honeyStock < 4.0) " ⚠️ ALERTA: Estoque de mel em ${String.format(java.util.Locale.US, "%.1f", honeyStock)} kg (reserva crítica!). Alimente o enxame." else ""
            _userMessage.value = "⚖️ Pesagem de ${totalWeightKg} kg registrada! Estoque de mel estimado: ${String.format(java.util.Locale.US, "%.1f", honeyStock)} kg.$alert"
        }
    }

    // --- Hive Field Photos ---
    fun getPhotosForHive(hiveId: Long): Flow<List<HivePhotoEntity>> {
        return repository.getPhotosByHive(hiveId)
    }

    fun saveHivePhoto(
        hiveId: Long,
        photoUri: String,
        category: String,
        caption: String,
        notes: String,
        inspectorName: String = "Apicultor"
    ) {
        viewModelScope.launch {
            repository.saveHivePhoto(
                hiveId = hiveId,
                photoUri = photoUri,
                category = category,
                caption = caption,
                notes = notes,
                inspectorName = inspectorName
            )
            _userMessage.value = "📸 Foto adicionada à colmeia com sucesso!"
        }
    }

    // --- Financial Management Actions ---
    fun addFinancialTransaction(
        type: String,
        category: String,
        amount: Double,
        dateMillis: Long,
        description: String,
        apiaryId: Long?,
        paymentStatus: String,
        paymentMethod: String,
        quantityKgOrUnits: Double?
    ) {
        viewModelScope.launch {
            val transaction = FinancialTransactionEntity(
                type = type,
                category = category,
                amount = amount,
                dateMillis = dateMillis,
                description = description,
                apiaryId = apiaryId,
                paymentStatus = paymentStatus,
                paymentMethod = paymentMethod,
                quantityKgOrUnits = quantityKgOrUnits
            )
            repository.insertFinancialTransaction(transaction)
            _userMessage.value = "💰 Lançamento de $type (R$ %.2f) registrado!".format(Locale.US, amount)
        }
    }

    fun updateFinancialTransaction(transaction: FinancialTransactionEntity) {
        viewModelScope.launch {
            repository.updateFinancialTransaction(transaction)
            _userMessage.value = "💰 Lançamento financeiro atualizado com sucesso!"
        }
    }

    fun togglePaymentStatus(transaction: FinancialTransactionEntity) {
        viewModelScope.launch {
            val newStatus = if (transaction.paymentStatus == "PAGO") "PENDENTE" else "PAGO"
            val updated = transaction.copy(paymentStatus = newStatus)
            repository.updateFinancialTransaction(updated)
            _userMessage.value = "Status alterado para: $newStatus"
        }
    }

    fun deleteFinancialTransaction(transaction: FinancialTransactionEntity) {
        viewModelScope.launch {
            repository.deleteFinancialTransaction(transaction)
            _userMessage.value = "🗑️ Lançamento financeiro removido."
        }
    }

    // --- Receipt Module Actions ---
    fun saveReceiptLayoutConfig(config: com.example.receipt.model.ReceiptLayoutConfig) {
        receiptManager.saveLayoutConfig(config)
        _userMessage.value = "🎨 Layout do recibo atualizado com sucesso!"
    }

    fun resetReceiptLayoutConfig() {
        receiptManager.resetLayoutConfigToDefault()
        _userMessage.value = "🔄 Layout do recibo restaurado para o padrão."
    }

    fun saveReceipt(receipt: com.example.receipt.model.ReceiptData) {
        receiptManager.saveReceipt(receipt)
        _userMessage.value = "📄 Recibo ${receipt.id} salvo com sucesso!"
    }

    fun deleteReceipt(receiptId: String) {
        receiptManager.deleteReceipt(receiptId)
        _userMessage.value = "🗑️ Recibo removido."
    }

    fun deleteHivePhoto(photo: HivePhotoEntity) {
        viewModelScope.launch {
            repository.deleteHivePhoto(photo)
            _userMessage.value = "🗑️ Foto excluída com sucesso."
        }
    }

    // --- Weather Evaluation & Geolocation Flight Conditions ---
    fun selectApiaryForWeather(apiaryId: Long) {
        _selectedWeatherApiaryId.value = apiaryId
        val target = apiaries.value.find { it.id == apiaryId } ?: apiaries.value.firstOrNull()
        target?.let { recalculateWeatherForApiary(it) }
    }

    fun refreshWeather() {
        val currentId = _selectedWeatherApiaryId.value
        val target = apiaries.value.find { it.id == currentId } ?: apiaries.value.firstOrNull()
        target?.let {
            recalculateWeatherForApiary(it)
            _userMessage.value = "🌤️ Dados de clima e voo atualizados para ${it.name}!"
        }
    }

    fun updateWeatherForApiary(apiary: ApiaryEntity) {
        _selectedWeatherApiaryId.value = apiary.id
        recalculateWeatherForApiary(apiary)
    }

    private fun recalculateWeatherForApiary(apiary: ApiaryEntity) {
        viewModelScope.launch {
            // 1. Cálculo imediato do microclima e condições de voo baseadas no GPS do apiário
            val initial = ApiaryWeatherEngine.computeWeatherFromCoordinates(apiary)
            _weatherState.value = initial

            // 2. Consulta meteorológica precisa por coordenadas via Open-Meteo em background
            val realtime = ApiaryWeatherEngine.fetchRealtimeWeather(apiary)
            _weatherState.value = realtime
        }
    }

    // --- Apiary Actions ---
    fun saveApiary(
        name: String,
        location: String,
        predominantFlora: String,
        capacity: Int,
        notes: String,
        latitude: Double? = null,
        longitude: Double? = null,
        quickHivesCount: Int = 0,
        beeSpecies: String = "Apis mellifera (Africanizada)",
        speciesCategory: String = "Apis (Com Ferrão)",
        hiveType: String = "Langstroth (Padrão 10 Quadros)",
        id: Long = 0
    ) {
        viewModelScope.launch {
            val entity = ApiaryEntity(
                id = id,
                name = name,
                location = location,
                predominantFlora = predominantFlora,
                capacity = capacity,
                latitude = latitude,
                longitude = longitude,
                notes = notes
            )
            val savedApiaryId = if (id == 0L) {
                val newId = repository.insertApiary(entity)
                _userMessage.value = "Apiário cadastrado com sucesso!"
                newId
            } else {
                repository.updateApiary(entity)
                _userMessage.value = "Apiário atualizado com sucesso!"
                id
            }

            if (id == 0L && quickHivesCount > 0 && savedApiaryId > 0) {
                for (i in 1..quickHivesCount) {
                    val hiveNumber = String.format(java.util.Locale.US, "Colmeia %02d", i)
                    val hiveEntity = HiveEntity(
                        apiaryId = savedApiaryId,
                        hiveNumber = hiveNumber,
                        beeSpecies = beeSpecies,
                        speciesCategory = speciesCategory,
                        hiveType = hiveType,
                        hiveDimensions = if (hiveType.contains("Langstroth")) "Ninho 10q (448x232mm)" else "Padrão",
                        queenStatus = "Presente e Marcada",
                        queenColor = "Branco (2026)",
                        queenLineage = "Selecionada",
                        queenAgeMonths = 4,
                        strengthLevel = "Forte",
                        temperament = "Mansa",
                        framesCount = 10,
                        supersCount = 1,
                        framesPerSuper = 10,
                        estimatedYieldPerFrameKg = 2.5,
                        healthStatus = "Excelente",
                        qrCode = "HIVE-${savedApiaryId}-${i}-${System.currentTimeMillis()}",
                        latitude = latitude,
                        longitude = longitude,
                        currentWeightKg = 35.0,
                        tareWeightKg = 15.0,
                        notes = "Gerado automaticamente pelo assistente de cadastro rápido de colmeias."
                    )
                    repository.insertHive(hiveEntity)
                }
                _userMessage.value = "Apiário e $quickHivesCount colmeias cadastradas com sucesso!"
            }
        }
    }

    fun deleteApiary(apiary: ApiaryEntity) {
        viewModelScope.launch {
            repository.deleteApiary(apiary)
            _userMessage.value = "Apiário removido."
        }
    }

    // --- Hive Actions ---
    fun saveHive(
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
        currentWeightKg: Double? = null,
        tareWeightKg: Double = 18.0,
        notes: String,
        id: Long = 0
    ) {
        viewModelScope.launch {
            val trimmedNumber = hiveNumber.trim()
            val allHives = hives.value
            val isDuplicate = HiveNamingHelper.isHiveNumberDuplicate(
                candidateNumber = trimmedNumber,
                existingHives = allHives,
                currentHiveId = id,
                isCloning = (id == 0L)
            )

            if (isDuplicate) {
                _userMessage.value = "⚠️ Não é permitido cadastrar colmeias com o mesmo nome ('$trimmedNumber'). Escolha uma numeração diferente."
                return@launch
            }

            val qr = if (qrCode.isBlank()) "APIS-A$apiaryId-$trimmedNumber" else qrCode
            val honeyStock = if (currentWeightKg != null) (currentWeightKg - tareWeightKg).coerceAtLeast(0.0) else null

            val hive = HiveEntity(
                id = id,
                apiaryId = apiaryId,
                hiveNumber = trimmedNumber,
                beeSpecies = beeSpecies,
                speciesCategory = speciesCategory,
                hiveType = hiveType,
                hiveDimensions = hiveDimensions,
                queenStatus = queenStatus,
                queenColor = queenColor,
                queenLineage = queenLineage,
                queenAgeMonths = queenAgeMonths,
                strengthLevel = strengthLevel,
                temperament = temperament,
                framesCount = framesCount,
                supersCount = supersCount,
                framesPerSuper = framesPerSuper,
                estimatedYieldPerFrameKg = estimatedYieldPerFrameKg,
                currentWeightKg = currentWeightKg,
                tareWeightKg = tareWeightKg,
                estimatedHoneyStockKg = honeyStock,
                healthStatus = healthStatus,
                qrCode = qr,
                latitude = latitude,
                longitude = longitude,
                altitude = altitude,
                locationAccuracy = locationAccuracy,
                locationTimestamp = if (latitude != null) System.currentTimeMillis() else null,
                notes = notes
            )
            if (id == 0L) {
                val newId = repository.insertHive(hive)
                if (currentWeightKg != null) {
                    repository.recordHiveWeight(newId, currentWeightKg, tareWeightKg, "Pesagem inicial de cadastro.")
                }
                _userMessage.value = "Colmeia $hiveNumber ($beeSpecies) cadastrada com sucesso!"
            } else {
                repository.updateHive(hive)
                _userMessage.value = "Colmeia $hiveNumber atualizada com sucesso!"
            }
        }
    }

    fun deleteHive(hive: HiveEntity) {
        viewModelScope.launch {
            repository.deleteHive(hive)
            if (_selectedHive.value?.id == hive.id) {
                _selectedHive.value = null
            }
            _userMessage.value = "Colmeia removida."
        }
    }

    fun updateHiveGps(hiveId: Long, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val current = hives.value.find { it.id == hiveId }
            if (current != null) {
                repository.updateHive(
                    current.copy(
                        latitude = latitude,
                        longitude = longitude,
                        locationTimestamp = System.currentTimeMillis()
                    )
                )
                _userMessage.value = "Localização GPS da colmeia ${current.hiveNumber} atualizada!"
            }
        }
    }

    // --- Bee Trap Actions ---
    fun saveBeeTrap(
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
        id: Long = 0
    ) {
        viewModelScope.launch {
            val trap = BeeTrapEntity(
                id = id,
                name = name,
                trapType = trapType,
                targetSpecies = targetSpecies,
                attractantUsed = attractantUsed,
                status = status,
                latitude = latitude,
                longitude = longitude,
                altitude = altitude,
                locationAccuracy = locationAccuracy,
                locationDescription = locationDescription,
                apiaryId = apiaryId,
                notes = notes
            )
            if (id == 0L) {
                repository.insertBeeTrap(trap)
                _userMessage.value = "📍 Isca cadastrada com geolocalização com sucesso!"
            } else {
                repository.updateBeeTrap(trap)
                _userMessage.value = "Isca atualizada com sucesso!"
            }
        }
    }

    fun deleteBeeTrap(trap: BeeTrapEntity) {
        viewModelScope.launch {
            repository.deleteBeeTrap(trap)
            _userMessage.value = "Isca removida."
        }
    }

    fun recordTrapCheck(trap: BeeTrapEntity, isCaptured: Boolean, notes: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newStatus = if (isCaptured) "Capturada" else trap.status
            val updated = trap.copy(
                status = newStatus,
                captureDate = if (isCaptured && trap.captureDate == null) now else trap.captureDate,
                lastCheckedDate = now,
                checkCount = trap.checkCount + 1,
                notes = if (notes.isNotBlank()) "${trap.notes} | Vistoria: $notes" else trap.notes
            )
            repository.updateBeeTrap(updated)
            if (isCaptured) {
                _userMessage.value = "🎉 Parabéns! Captura confirmada na isca '${trap.name}'!"
            } else {
                _userMessage.value = "Vistoria registrada com sucesso (Check #${updated.checkCount})."
            }
        }
    }

    fun transferTrapToHive(
        trap: BeeTrapEntity,
        apiaryId: Long,
        hiveNumber: String,
        hiveType: String,
        beeSpecies: String,
        speciesCategory: String,
        framesCount: Int,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                repository.transferTrapToHive(
                    trap = trap,
                    apiaryId = apiaryId,
                    hiveNumber = hiveNumber,
                    hiveType = hiveType,
                    beeSpecies = beeSpecies,
                    speciesCategory = speciesCategory,
                    framesCount = framesCount,
                    notes = notes
                )
                _userMessage.value = "🐝 Enxame transferido com sucesso para a Colmeia #$hiveNumber!"
            } catch (e: Exception) {
                _userMessage.value = "Erro na transferência: ${e.message}"
            }
        }
    }

    // --- Inspection Actions with AI ---
    fun processFieldNoteWithAi(
        rawNotes: String,
        apiaryId: Long,
        hiveId: Long,
        inspectorName: String = "Apicultor Responsável",
        onCompleted: (StructuredInspectionResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isAiAnalyzing.value = true
            try {
                val result = repository.analyzeAndSaveFieldInspection(rawNotes, apiaryId, hiveId, inspectorName)
                _lastAiResult.value = result
                onCompleted(result)
                _userMessage.value = if (result.isSanitaryAlert) "🚨 Alerta sanitário registrado com tarefas de manejo!" else "Inspeção registrada com sucesso!"
            } catch (e: Exception) {
                _userMessage.value = "Erro ao processar inspeção: ${e.message}"
            } finally {
                _isAiAnalyzing.value = false
            }
        }
    }

    fun processCombImageWithAi(
        base64Image: String,
        mimeType: String,
        apiaryId: Long,
        hiveId: Long,
        inspectorName: String = "Apicultor Responsável",
        onCompleted: (StructuredInspectionResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isAiAnalyzing.value = true
            try {
                val result = repository.analyzeAndSaveCombImage(base64Image, mimeType, apiaryId, hiveId, inspectorName)
                _lastAiResult.value = result
                onCompleted(result)
                _userMessage.value = "📷 Diagnóstico visual do favo concluído!"
            } catch (e: Exception) {
                _userMessage.value = "Erro ao analisar imagem: ${e.message}"
            } finally {
                _isAiAnalyzing.value = false
            }
        }
    }

    fun saveManualInspection(inspection: InspectionEntity) {
        viewModelScope.launch {
            repository.insertInspection(inspection)
            _userMessage.value = "Inspeção salva com sucesso!"
        }
    }

    fun updateInspection(inspection: InspectionEntity) {
        viewModelScope.launch {
            repository.updateInspection(inspection)
            _userMessage.value = "Inspeção atualizada com sucesso!"
        }
    }

    fun deleteInspection(inspection: InspectionEntity) {
        viewModelScope.launch {
            repository.deleteInspection(inspection)
            _userMessage.value = "Inspeção removida."
        }
    }

    // --- Nutrition Calculator Actions ---
    fun deductNutritionIngredients(sugarKg: Int, yeastKg: Int, soyKg: Int) {
        viewModelScope.launch {
            val message = repository.deductNutritionIngredients(sugarKg, yeastKg, soyKg)
            _userMessage.value = message
        }
    }

    fun scheduleFeedingTask(
        title: String,
        desc: String,
        apiaryId: Long?,
        apiaryName: String?
    ) {
        viewModelScope.launch {
            repository.insertTask(
                TaskScheduleEntity(
                    title = title,
                    description = desc,
                    taskType = "Alimentação Artificial",
                    apiaryId = apiaryId,
                    apiaryName = apiaryName,
                    dueDate = System.currentTimeMillis() + (24 * 60 * 60 * 1000L),
                    priority = "ALTA",
                    isCompleted = false
                )
            )
            _userMessage.value = "Manejo de alimentação agendado para amanhã!"
        }
    }

    // --- Export Reports Actions ---
    fun shareCsvExport(onShareIntentReady: (Intent) -> Unit) {
        viewModelScope.launch {
            val csvContent = repository.generateFieldBookExportCsv()
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, csvContent)
                putExtra(Intent.EXTRA_SUBJECT, "Caderno de Campo Apícola - ApisSmart")
                type = "text/csv"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Exportar Caderno de Campo CSV")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            onShareIntentReady(shareIntent)
        }
    }

    fun shareTraceabilityReport(onShareIntentReady: (Intent) -> Unit) {
        viewModelScope.launch {
            val textContent = repository.generateTraceabilityReportText()
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, textContent)
                putExtra(Intent.EXTRA_SUBJECT, "Laudo de Rastreabilidade de Mel - ApisSmart")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Compartilhar Laudo de Rastreabilidade")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            onShareIntentReady(shareIntent)
        }
    }

    // --- Harvest Actions ---
    fun recordHarvest(
        apiaryId: Long,
        hiveId: Long?,
        productType: String,
        quantityKg: Double,
        moisturePercent: Double?,
        floralSource: String,
        supersHarvestedCount: Int? = 1,
        framesHarvestedCount: Int? = 10,
        framesPerSuper: Int? = 10,
        avgYieldPerFrameKg: Double? = null,
        notes: String
    ) {
        viewModelScope.launch {
            val date = System.currentTimeMillis()
            val batch = "LOTE-${productType.take(3).uppercase()}-${date % 100000}"
            val calculatedAvgPerFrame = avgYieldPerFrameKg ?: if (framesHarvestedCount != null && framesHarvestedCount > 0) {
                quantityKg / framesHarvestedCount
            } else null

            val harvest = HarvestEntity(
                apiaryId = apiaryId,
                hiveId = hiveId,
                date = date,
                batchNumber = batch,
                productType = productType,
                quantityKg = quantityKg,
                moisturePercent = moisturePercent,
                floralSource = floralSource,
                supersHarvestedCount = supersHarvestedCount,
                framesHarvestedCount = framesHarvestedCount,
                framesPerSuper = framesPerSuper,
                avgYieldPerFrameKg = calculatedAvgPerFrame,
                notes = notes
            )
            repository.insertHarvest(harvest)
            val moistureAlert = if (moisturePercent != null && moisturePercent > 19.5) {
                " ⚠️ ATENÇÃO: Umidade em $moisturePercent% (acima do limite de 19.5% - risco de fermentação!)."
            } else ""
            _userMessage.value = "Colheita de $quantityKg kg ($productType - $batch) registrada com sucesso!$moistureAlert"
        }
    }

    fun deleteHarvest(harvest: HarvestEntity) {
        viewModelScope.launch {
            repository.deleteHarvest(harvest)
            _userMessage.value = "Registro de colheita removido."
        }
    }

    // --- Inventory Actions ---
    fun saveInventoryItem(
        name: String,
        category: String,
        quantity: Int,
        unit: String,
        minThreshold: Int,
        notes: String,
        id: Long = 0
    ) {
        viewModelScope.launch {
            val item = InventoryEntity(
                id = id,
                name = name,
                category = category,
                quantity = quantity,
                unit = unit,
                minThreshold = minThreshold,
                notes = notes
            )
            if (id == 0L) {
                repository.insertInventoryItem(item)
                _userMessage.value = "Item adicionado ao estoque!"
            } else {
                repository.updateInventoryItem(item)
                _userMessage.value = "Estoque atualizado!"
            }
        }
    }

    fun updateInventoryQuantity(item: InventoryEntity, delta: Int) {
        viewModelScope.launch {
            val newQty = (item.quantity + delta).coerceAtLeast(0)
            repository.updateInventoryItem(item.copy(quantity = newQty))
        }
    }

    fun deleteInventoryItem(item: InventoryEntity) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
            _userMessage.value = "Item removido do estoque."
        }
    }

    // --- Tasks Schedule Actions ---
    fun saveTask(
        title: String,
        description: String,
        taskType: String,
        apiaryId: Long?,
        hiveId: Long?,
        apiaryName: String?,
        hiveNumber: String?,
        dueDate: Long,
        priority: String,
        id: Long = 0
    ) {
        viewModelScope.launch {
            val task = TaskScheduleEntity(
                id = id,
                title = title,
                description = description,
                taskType = taskType,
                apiaryId = apiaryId,
                hiveId = hiveId,
                apiaryName = apiaryName,
                hiveNumber = hiveNumber,
                dueDate = dueDate,
                priority = priority,
                isCompleted = false
            )
            if (id == 0L) {
                repository.insertTask(task)
                _userMessage.value = "Manejo agendado com sucesso!"
            } else {
                repository.updateTask(task)
                _userMessage.value = "Tarefa atualizada com sucesso!"
            }
        }
    }

    fun toggleTaskCompletion(task: TaskScheduleEntity) {
        viewModelScope.launch {
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                completedDate = if (!task.isCompleted) System.currentTimeMillis() else null
            )
            repository.updateTask(updated)
            _userMessage.value = if (updated.isCompleted) "Manejo marcado como concluído!" else "Manejo reaberto."
        }
    }

    fun deleteTask(task: TaskScheduleEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _userMessage.value = "Tarefa excluída."
        }
    }

    // --- AI Chat Assistant Actions ---
    fun sendAiMessage(query: String, contextInfo: String = "") {
        viewModelScope.launch {
            _isAiChatLoading.value = true
            try {
                repository.askAssistant(query, contextInfo)
            } catch (e: Exception) {
                _userMessage.value = "Erro na consulta: ${e.message}"
            } finally {
                _isAiChatLoading.value = false
            }
        }
    }

    fun clearAiHistory() {
        viewModelScope.launch {
            repository.clearConsultationHistory()
            _userMessage.value = "Histórico de conversas limpo."
        }
    }

    // --- License Management Actions (Outside Google Play) ---
    fun activateLicenseWithKey(key: String, owner: String = "") {
        val result = licenseManager.activateWithKey(key, owner)
        result.onSuccess { msg ->
            _userMessage.value = "🎉 $msg"
            _licenseServerMessage.value = msg
        }.onFailure { err ->
            _userMessage.value = "❌ Falha na ativação: ${err.message}"
            _licenseServerMessage.value = "Erro: ${err.message}"
        }
    }

    fun verifyLicenseWithServer(customUrl: String? = null) {
        viewModelScope.launch {
            _isVerifyingLicenseServer.value = true
            _licenseServerMessage.value = "Consultando servidor de licença..."
            try {
                val result = licenseManager.verifyWithServer(customUrl)
                result.onSuccess { msg ->
                    _userMessage.value = "✅ $msg"
                    _licenseServerMessage.value = msg
                }.onFailure { err ->
                    _userMessage.value = "⚠️ ${err.message}"
                    _licenseServerMessage.value = err.message
                }
            } catch (e: Exception) {
                _userMessage.value = "Erro de conexão: ${e.message}"
                _licenseServerMessage.value = "Erro: ${e.message}"
            } finally {
                _isVerifyingLicenseServer.value = false
            }
        }
    }

    fun updateLicenseServerUrl(url: String) {
        licenseManager.updateServerUrl(url)
        _userMessage.value = "URL do servidor atualizada."
    }

    fun generateAdminLicenseKey(
        tier: LicenseTier,
        days: Int,
        targetDevId: String?,
        licensesCount: Int = 1,
        associationName: String = ""
    ): String {
        return licenseManager.generateAdminKey(tier, days, targetDevId, licensesCount, associationName)
    }

    fun updateAssociationDetails(associationName: String, quota: Int, cycle: String = "MENSAL") {
        licenseManager.updateAssociationDetails(associationName, quota, cycle)
        _userMessage.value = "Dados da Associação atualizados com sucesso."
    }

    fun resetLicenseToTrial() {
        licenseManager.resetToTrial()
        _userMessage.value = "Período de testes de 30 dias reiniciado com sucesso."
    }

    fun deactivateLicense() {
        licenseManager.deactivateLicense()
        _userMessage.value = "Licença desativada. Aplicativo em modo restrito."
    }

    // --- Bluetooth Tag Tracking & Traceability Operations ---
    fun startBleScan() {
        bluetoothTracker.startScan()
        _userMessage.value = "📡 Escaneando tags Bluetooth próximas..."
    }

    fun stopBleScan() {
        bluetoothTracker.stopScan()
        _userMessage.value = "⏹️ Scanner Bluetooth pausado."
    }

    fun toggleBleScan() {
        if (isBleScanning.value) {
            stopBleScan()
        } else {
            startBleScan()
        }
    }

    fun startBleSimulation() {
        bluetoothTracker.startSimulation(bluetoothTags.value.map { it.tagMacAddress })
        _userMessage.value = "✨ Simulação de Tags BLE no Apiário ativada!"
    }

    fun stopBleSimulation() {
        bluetoothTracker.stopSimulation()
        _userMessage.value = "⏹️ Simulação de Tags BLE desativada."
    }

    fun selectTargetRadarTag(tag: BluetoothTagEntity?) {
        _targetRadarTag.value = tag
        if (tag != null) {
            _userMessage.value = "🎯 Rastreando ${tag.tagName} no Radar"
        }
    }

    fun toggleProximityAudio() {
        _isProximityAudioEnabled.value = !_isProximityAudioEnabled.value
    }

    fun toggleProximityHaptic() {
        _isProximityHapticEnabled.value = !_isProximityHapticEnabled.value
    }

    fun triggerRadarProximityFeedback(distanceMeters: Double) {
        bluetoothTracker.triggerProximityFeedback(
            distanceMeters = distanceMeters,
            isAudioEnabled = _isProximityAudioEnabled.value,
            isHapticEnabled = _isProximityHapticEnabled.value
        )
    }

    fun saveBluetoothTag(
        name: String,
        mac: String,
        type: String,
        hiveId: Long?,
        apiaryId: Long?,
        antiTheft: Boolean,
        notes: String = "",
        id: Long = 0L
    ) {
        viewModelScope.launch {
            val assignedName = if (hiveId != null) {
                hives.value.find { it.id == hiveId }?.let { "${it.hiveNumber} (${apiaries.value.find { a -> a.id == it.apiaryId }?.name ?: ""})" } ?: ""
            } else ""

            val loc = LocationHelper.getCurrentLocation(getApplication())
            val entity = BluetoothTagEntity(
                id = id,
                tagMacAddress = mac.uppercase(Locale.ROOT).trim(),
                tagName = name.ifBlank { "Tag ${mac.takeLast(5)}" },
                tagType = type,
                hiveId = hiveId,
                apiaryId = apiaryId,
                assignedItemName = assignedName,
                lastSeenTimestamp = System.currentTimeMillis(),
                lastLatitude = loc?.latitude,
                lastLongitude = loc?.longitude,
                isAntiTheftEnabled = antiTheft,
                notes = notes
            )

            if (id == 0L) {
                repository.insertBluetoothTag(entity)
                _userMessage.value = "✅ Tag Bluetooth vinculada com sucesso!"
            } else {
                repository.updateBluetoothTag(entity)
                _userMessage.value = "✏️ Tag Bluetooth atualizada!"
            }
        }
    }

    fun deleteBluetoothTag(tag: BluetoothTagEntity) {
        viewModelScope.launch {
            if (_targetRadarTag.value?.id == tag.id) {
                _targetRadarTag.value = null
            }
            repository.deleteBluetoothTag(tag)
            _userMessage.value = "🗑️ Tag ${tag.tagName} removida."
        }
    }

    fun linkTagToHive(tagId: Long, hive: HiveEntity) {
        viewModelScope.launch {
            repository.linkTagToHive(tagId, hive)
            _userMessage.value = "🔗 Tag vinculada à colmeia ${hive.hiveNumber}!"
        }
    }

    fun unlinkTag(tagId: Long) {
        viewModelScope.launch {
            repository.unlinkTag(tagId)
            _userMessage.value = "🔓 Tag desvinculada."
        }
    }

    fun toggleAntiTheft(tag: BluetoothTagEntity) {
        viewModelScope.launch {
            val updated = tag.copy(isAntiTheftEnabled = !tag.isAntiTheftEnabled)
            repository.updateBluetoothTag(updated)
            val status = if (updated.isAntiTheftEnabled) "ativado" else "desativado"
            _userMessage.value = "🛡️ Alerta anti-furto $status para ${tag.tagName}."
        }
    }

    fun updateTagGpsLocation(tag: BluetoothTagEntity) {
        val loc = LocationHelper.getCurrentLocation(getApplication())
        if (loc != null) {
            viewModelScope.launch {
                val updated = tag.copy(
                    lastLatitude = loc.latitude,
                    lastLongitude = loc.longitude,
                    lastSeenTimestamp = System.currentTimeMillis()
                )
                repository.updateBluetoothTag(updated)
                _userMessage.value = "📍 Coordenadas GPS da tag atualizadas: ${LocationHelper.formatCoordinates(loc.latitude, loc.longitude)}"
            }
        } else {
            _userMessage.value = "⚠️ Não foi possível obter localização GPS no momento."
        }
    }

    // --- Quality Seal Goals & Certifications Actions ---
    fun toggleSealGoal(goal: QualitySealGoalEntity) {
        viewModelScope.launch {
            val willBeCompleted = !goal.isCompleted
            repository.toggleSealGoal(goal.id, willBeCompleted)
            val status = if (willBeCompleted) "cumprida ✅" else "reaberta"
            _userMessage.value = "Meta '${goal.title}' marcada como $status!"
        }
    }

    fun saveSealGoal(
        sealType: String,
        title: String,
        description: String,
        category: String,
        evidence: String,
        mandatory: Boolean,
        notes: String = "",
        id: Long = 0L
    ) {
        viewModelScope.launch {
            val entity = QualitySealGoalEntity(
                id = id,
                sealType = sealType,
                title = title,
                description = description,
                requirementCategory = category,
                verificationEvidence = evidence,
                isMandatory = mandatory,
                notes = notes
            )
            if (id == 0L) {
                repository.insertSealGoal(entity)
                _userMessage.value = "Nova meta adicionada ao Selo $sealType!"
            } else {
                repository.updateSealGoal(entity)
                _userMessage.value = "Meta atualizada!"
            }
        }
    }

    fun deleteSealGoal(goal: QualitySealGoalEntity) {
        viewModelScope.launch {
            repository.deleteSealGoal(goal.id)
            _userMessage.value = "Meta removida."
        }
    }

    fun shareSealComplianceReport(sealType: String, onShare: (Intent) -> Unit) {
        viewModelScope.launch {
            val report = repository.generateQualitySealComplianceReport(sealType)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Dossiê de Conformidade - Selo $sealType")
                putExtra(Intent.EXTRA_TEXT, report)
            }
            val chooser = Intent.createChooser(intent, "Compartilhar Dossiê de Conformidade")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            onShare(chooser)
        }
    }

    fun createMaintenanceTaskFromTip(
        title: String,
        description: String,
        taskType: String = "Manejo Preventivo",
        daysAhead: Int = 3
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val due = now + (daysAhead * 24 * 60 * 60 * 1000L)
            val defaultApiary = apiaries.value.firstOrNull()
            val defaultHive = hives.value.firstOrNull()

            saveTask(
                title = title,
                description = description,
                taskType = taskType,
                apiaryId = defaultApiary?.id,
                hiveId = defaultHive?.id,
                apiaryName = defaultApiary?.name ?: "Apiário Principal",
                hiveNumber = defaultHive?.hiveNumber ?: "Múltiplas Colmeias",
                dueDate = due,
                priority = "MÉDIA"
            )
            _userMessage.value = "📅 Tarefa de manejo '$title' agendada para daqui a $daysAhead dias!"
        }
    }
}
