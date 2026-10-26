package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.local.entities.AiConsultationEntity
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.BeeTrapEntity
import com.example.data.local.entities.FinancialTransactionEntity
import com.example.data.local.entities.HarvestEntity
import com.example.data.local.entities.HiveEntity
import com.example.data.local.entities.InspectionEntity
import com.example.data.local.entities.InventoryEntity
import com.example.data.local.entities.TaskScheduleEntity
import com.example.license.LicenseData
import com.example.license.LicenseStatus
import com.example.license.LicenseTier
import com.example.ui.components.LicenseManagementCard
import com.example.ui.screens.AiConsultantScreen
import com.example.ui.screens.ApiariesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinancialScreen
import com.example.ui.screens.HarvestScreen
import com.example.ui.screens.InspectionsScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.theme.ApisSmartTheme
import com.example.ui.theme.AppThemeConfig
import com.example.ui.theme.AppThemeMode
import com.example.ui.viewmodel.ApiaryWeatherInfo
import com.example.ui.viewmodel.ManejoWindowStatus
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class SystemScreenshotsDarkTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val darkThemeConfig = AppThemeConfig(themeMode = AppThemeMode.DARK)

    // Realistic Mock Datasets for Pristine Screenshots
    private val sampleApiaries = listOf(
        ApiaryEntity(
            id = 1L,
            name = "Apiário Sol Nascente",
            location = "Serra da Mantiqueira, SP - Gleba 4",
            predominantFlora = "Silvestre, Eucalipto e Assa-Peixe",
            capacity = 45,
            notes = "Área cercada com pasto apícola exuberante e florada ativa.",
            latitude = -22.7394,
            longitude = -45.5913
        ),
        ApiaryEntity(
            id = 2L,
            name = "Meliponário Vale Encantado",
            location = "Campos do Jordão, SP",
            predominantFlora = "Flora Nativa de Altitude e Bracatinga",
            capacity = 30,
            notes = "Dedicado a abelhas nativas sem ferrão (Jataí, Mandaçaia e Uruçu).",
            latitude = -22.7410,
            longitude = -45.5890
        )
    )

    private val sampleHives = listOf(
        HiveEntity(
            id = 1L,
            apiaryId = 1L,
            hiveNumber = "Colmeia 01",
            beeSpecies = "Apis mellifera (Africanizada)",
            speciesCategory = "Apis (Com Ferrão)",
            hiveType = "Langstroth",
            queenStatus = "Presente e Marcada",
            queenColor = "Branco (2026)",
            queenLineage = "Italiana Selecionada",
            queenAgeMonths = 4,
            strengthLevel = "Forte",
            temperament = "Mansa",
            framesCount = 10,
            supersCount = 2,
            healthStatus = "Excelente",
            currentWeightKg = 42.5,
            tareWeightKg = 18.0,
            estimatedHoneyStockKg = 24.5
        ),
        HiveEntity(
            id = 2L,
            apiaryId = 1L,
            hiveNumber = "Colmeia 02",
            beeSpecies = "Apis mellifera (Carnica)",
            speciesCategory = "Apis (Com Ferrão)",
            hiveType = "Langstroth",
            queenStatus = "Presente e Marcada",
            queenColor = "Azul (2025)",
            queenLineage = "Carnica F1",
            queenAgeMonths = 8,
            strengthLevel = "Muito Forte",
            temperament = "Muito Mansa",
            framesCount = 10,
            supersCount = 3,
            healthStatus = "Excelente",
            currentWeightKg = 55.0,
            tareWeightKg = 18.0,
            estimatedHoneyStockKg = 37.0
        ),
        HiveEntity(
            id = 3L,
            apiaryId = 2L,
            hiveNumber = "Mandaçaia MQA-04",
            beeSpecies = "Melipona quadrifasciata (Mandaçaia)",
            speciesCategory = "Meliponíneo (Sem Ferrão / ASF)",
            hiveType = "Caixa INPA",
            queenStatus = "Fecundada Ativa",
            strengthLevel = "Forte",
            temperament = "Mansa",
            healthStatus = "Excelente",
            currentWeightKg = 8.2,
            tareWeightKg = 4.5,
            estimatedHoneyStockKg = 3.7
        )
    )

    private val sampleTraps = listOf(
        BeeTrapEntity(
            id = 1L,
            name = "Isca Pet Mata Sul",
            trapType = "Garrafa PET 5L com Atrativo Feromônio",
            targetSpecies = "Melipona / Jataí",
            status = "Capturada",
            installationDate = System.currentTimeMillis() - (15L * 86400000L),
            checkCount = 3,
            notes = "Enxame de Jataí forte capturado! Pronto para transferência."
        ),
        BeeTrapEntity(
            id = 2L,
            name = "Isca Caixa Madeira Eucalipto",
            trapType = "Caixa de Papelão Resinada 10L",
            targetSpecies = "Apis mellifera",
            status = "Armada",
            installationDate = System.currentTimeMillis() - (4L * 86400000L),
            checkCount = 1,
            notes = "Instalada na borda da mata com própolis e capim-limão."
        )
    )

    private val sampleTasks = listOf(
        TaskScheduleEntity(
            id = 1L,
            title = "Colheita de Melgueiras Operculadas",
            description = "Retirar melgueiras operculadas com mais de 80% de favos maduros.",
            taskType = "Colheita",
            apiaryId = 1L,
            hiveId = 1L,
            apiaryName = "Apiário Sol Nascente",
            hiveNumber = "Colmeia 01 e 02",
            dueDate = System.currentTimeMillis() + 86400000L,
            priority = "Alta",
            isCompleted = false
        ),
        TaskScheduleEntity(
            id = 2L,
            title = "Suplementação Proteica (Bife)",
            description = "Fornecer 150g de pasta proteica à base de levedura e soja micronizada.",
            taskType = "Alimentação",
            apiaryId = 1L,
            apiaryName = "Apiário Sol Nascente",
            dueDate = System.currentTimeMillis() + (3L * 86400000L),
            priority = "Normal",
            isCompleted = false
        )
    )

    private val sampleHarvests = listOf(
        HarvestEntity(
            id = 1L,
            apiaryId = 1L,
            hiveId = 1L,
            date = System.currentTimeMillis() - (5L * 86400000L),
            productType = "Mel Silvestre",
            quantityKg = 38.5,
            moisturePercent = 17.2,
            floralSource = "Assa-Peixe e Silvestre",
            supersHarvestedCount = 2,
            framesHarvestedCount = 18,
            notes = "Mel límpido de excelente densidade e aroma floral."
        ),
        HarvestEntity(
            id = 2L,
            apiaryId = 1L,
            hiveId = 2L,
            date = System.currentTimeMillis() - (12L * 86400000L),
            productType = "Mel Floral Eucalipto",
            quantityKg = 46.0,
            moisturePercent = 17.5,
            floralSource = "Eucalipto Grandis",
            supersHarvestedCount = 3,
            framesHarvestedCount = 27,
            notes = "Padrão de exportação, cor âmbar claro."
        )
    )

    private val sampleInventory = listOf(
        InventoryEntity(
            id = 1L,
            name = "Cera Alveolada (kg)",
            category = "Quadros e Cera",
            quantity = 25,
            unit = "kg",
            minThreshold = 5,
            notes = "Cera pura 100% esterilizada."
        ),
        InventoryEntity(
            id = 2L,
            name = "Xarope de Açúcar Invertido 1:1",
            category = "Alimentação",
            quantity = 120,
            unit = "L",
            minThreshold = 30,
            notes = "Para alimentação de incentivo pré-safra."
        ),
        InventoryEntity(
            id = 3L,
            name = "Quadro de Melgueira com Arame e Ilhós",
            category = "Quadros e Cera",
            quantity = 80,
            unit = "un",
            minThreshold = 20,
            notes = "Quadros montados em eucalipto rosa."
        ),
        InventoryEntity(
            id = 4L,
            name = "Pote de Vidro Sextavado 500g c/ Tampa",
            category = "Embalagens & Envase",
            quantity = 240,
            unit = "un",
            minThreshold = 50,
            notes = "Embalagens higienizadas para envase final."
        )
    )

    private val sampleTransactions = listOf(
        FinancialTransactionEntity(
            id = 1L,
            type = "RECEITA",
            category = "Venda de Mel Fracionado",
            amount = 3450.0,
            dateMillis = System.currentTimeMillis() - (2L * 86400000L),
            description = "Venda de 115 potes de Mel 500g para Empório Regional",
            paymentStatus = "RECEBIDO",
            paymentMethod = "PIX",
            quantityKgOrUnits = 57.5
        ),
        FinancialTransactionEntity(
            id = 2L,
            type = "DESPESA",
            category = "Insumos e Cera Alveolada",
            amount = 890.0,
            dateMillis = System.currentTimeMillis() - (6L * 86400000L),
            description = "Aquisição de 20kg de cera e 50 quadros",
            paymentStatus = "PAGO",
            paymentMethod = "Boleto",
            quantityKgOrUnits = 20.0
        )
    )

    private val sampleConsultations = listOf(
        AiConsultationEntity(
            id = 1L,
            userPrompt = "Qual a proporção recomendada para o xarope de incentivo de postura da rainha?",
            assistantResponse = "Recomenda-se a proporção **1:1** (1 kg de açúcar cristal para 1 litro de água filtrada ou fervida), fornecido morno em alimentadores tipo Boardman ou de cobertura. Adicione 5ml de suco de limão ou promotor para acidificação ótima.",
            timestamp = System.currentTimeMillis() - 3600000L
        )
    )

    private val sampleWeather = ApiaryWeatherInfo(
        temperatureC = 25,
        humidityPercent = 58,
        windSpeedKmh = 7,
        rainProbabilityPercent = 5,
        conditionDescription = "Ensolarado com céu aberto",
        status = ManejoWindowStatus.IDEAL,
        advice = "Condição excelente para manejo profundo, colheita e avaliação da postura da rainha.",
        flightConditionText = "🟢 Voo Pleno (Forrageamento Intenso)",
        apiaryName = "Apiário Sol Nascente",
        location = "Serra da Mantiqueira, SP"
    )

    private val sampleLicense = LicenseData(
        status = LicenseStatus.ACTIVE,
        tier = LicenseTier.PRO,
        licenseKey = "APIS-PRO-2026-F89A-7B1C",
        ownerName = "Christian Dahmer",
        deviceId = "A4F2-89C1-72BE",
        expiryDateMillis = System.currentTimeMillis() + (365L * 86400000L)
    )

    // ==========================================
    // Dark Mode Screenshot Tests
    // ==========================================

    @Test
    fun test_01_dashboard_screen_dark() {
        composeTestRule.setContent {
            ApisSmartTheme(themeConfig = darkThemeConfig) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DashboardScreen(
                        apiaries = sampleApiaries,
                        hives = sampleHives,
                        beeTraps = sampleTraps,
                        tasks = sampleTasks,
                        harvests = sampleHarvests,
                        totalHoneyKg = 84.5,
                        sanitaryAlertCount = 0,
                        weatherInfo = sampleWeather,
                        onNavigateTab = {},
                        onOpenQuickInspection = {},
                        onOpenNutritionCalculator = {},
                        onOpenCombImageAnalysis = {},
                        onOpenHandsFreeMode = {},
                        onOpenQrScanner = {},
                        onOpenExportReports = {},
                        onOpenLicense = {},
                        licenseData = sampleLicense,
                        onToggleTask = {}
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dark/01_dashboard_screen_dark.png")
    }

    @Test
    fun test_02_apiaries_screen_dark() {
        composeTestRule.setContent {
            ApisSmartTheme(themeConfig = darkThemeConfig) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ApiariesScreen(
                        apiaries = sampleApiaries,
                        hives = sampleHives,
                        beeTraps = sampleTraps,
                        capturedTrapsCount = 1,
                        activeTrapsCount = 1,
                        onSaveApiary = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
                        onDeleteApiary = {},
                        onSaveHive = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
                        onDeleteHive = {},
                        onInspectHive = { _, _ -> },
                        onShowHiveQr = { _, _ -> },
                        onOpenHiveHistory = {},
                        onSaveBeeTrap = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
                        onDeleteBeeTrap = {},
                        onRecordTrapCheck = { _, _, _ -> },
                        onTransferTrapToHive = { _, _, _, _, _, _, _, _ -> },
                        onOpenGeneralMap = {}
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dark/02_apiarios_e_colmeias_dark.png")
    }

    @Test
    fun test_03_inspections_screen_dark() {
        val sampleInspections = listOf(
            InspectionEntity(
                id = 1L,
                apiaryId = 1L,
                hiveId = 1L,
                date = System.currentTimeMillis() - 86400000L,
                queenSeen = true,
                broodPattern = "Excelente uniforme",
                eggsSeen = true,
                larvaeSeen = true,
                cappedBroodSeen = true,
                honeyStores = "Abundante",
                pollenStores = "Suficiente",
                pestDetected = "Nenhuma",
                notesRaw = "Colmeia muito forte com postura regular em espiral.",
                inspectorName = "Christian Dahmer"
            )
        )
        composeTestRule.setContent {
            ApisSmartTheme(themeConfig = darkThemeConfig) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    InspectionsScreen(
                        inspections = sampleInspections,
                        apiaries = sampleApiaries,
                        hives = sampleHives,
                        onOpenNewInspection = {},
                        onOpenCombImageAnalysis = {},
                        onOpenHandsFreeMode = {},
                        onOpenQrScanner = {},
                        onDeleteInspection = {}
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dark/03_inspecoes_sanitarias_dark.png")
    }

    @Test
    fun test_04_harvest_screen_dark() {
        composeTestRule.setContent {
            ApisSmartTheme(themeConfig = darkThemeConfig) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HarvestScreen(
                        harvests = sampleHarvests,
                        apiaries = sampleApiaries,
                        hives = sampleHives,
                        totalHoneyKg = 84.5,
                        onRecordHarvest = { _, _, _, _, _, _, _, _, _, _, _ -> },
                        onDeleteHarvest = {}
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dark/04_colheita_e_producao_dark.png")
    }

    @Test
    fun test_05_inventory_screen_dark() {
        composeTestRule.setContent {
            ApisSmartTheme(themeConfig = darkThemeConfig) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    InventoryScreen(
                        inventory = sampleInventory,
                        lowStockItems = emptyList(),
                        onUpdateQuantity = { _, _ -> },
                        onSaveItem = { _, _, _, _, _, _, _ -> },
                        onDeleteItem = {}
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dark/05_estoque_e_insumos_dark.png")
    }

    @Test
    fun test_06_schedule_screen_dark() {
        composeTestRule.setContent {
            ApisSmartTheme(themeConfig = darkThemeConfig) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScheduleScreen(
                        tasks = sampleTasks,
                        apiaries = sampleApiaries,
                        onToggleTask = {},
                        onSaveTask = { _, _, _, _, _, _, _, _, _ -> },
                        onDeleteTask = {}
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dark/06_cronograma_manejos_dark.png")
    }

    @Test
    fun test_07_financial_screen_dark() {
        composeTestRule.setContent {
            ApisSmartTheme(themeConfig = darkThemeConfig) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FinancialScreen(
                        transactions = sampleTransactions,
                        apiaries = sampleApiaries,
                        totalHivesCount = 3,
                        onAddTransaction = { _, _, _, _, _, _, _, _, _ -> },
                        onUpdateTransaction = {},
                        onTogglePaymentStatus = {},
                        onDeleteTransaction = {}
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dark/07_gestao_financeira_dark.png")
    }

    @Test
    fun test_08_ai_assistant_screen_dark() {
        composeTestRule.setContent {
            ApisSmartTheme(themeConfig = darkThemeConfig) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AiConsultantScreen(
                        consultations = sampleConsultations,
                        apiaries = sampleApiaries,
                        hives = sampleHives,
                        isLoading = false,
                        onSendMessage = { _, _ -> },
                        onClearHistory = {}
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dark/08_assistente_tecnico_ia_dark.png")
    }

    @Test
    fun test_09_license_admin_dialog_dark() {
        composeTestRule.setContent {
            ApisSmartTheme(themeConfig = darkThemeConfig) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LicenseManagementCard(
                        licenseData = sampleLicense,
                        isVerifyingServer = false,
                        serverMessage = null,
                        onActivateKey = {},
                        onVerifyServer = {},
                        onUpdateServerUrl = {},
                        onGenerateAdminKey = { _, _, _, _, _ -> "APIS-PRO-2026-F89A-7B1C" },
                        onResetTrial = {},
                        onDeactivate = {},
                        onClose = {}
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dark/09_administracao_licenca_dark.png")
    }
}
