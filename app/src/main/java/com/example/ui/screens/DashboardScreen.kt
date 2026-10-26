package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.BeeTrapEntity
import com.example.data.local.entities.HarvestEntity
import com.example.data.local.entities.HiveEntity
import com.example.data.local.entities.TaskScheduleEntity
import com.example.license.LicenseData
import com.example.ui.components.DashboardCustomizationDialog
import com.example.ui.components.HealthBadge
import com.example.ui.components.HoneyProductionGrowthChart
import com.example.ui.components.LicenseBanner
import com.example.ui.components.PriorityBadge
import com.example.ui.components.WeatherForecastModalDialog
import com.example.ui.components.WeatherManejoCard
import com.example.ui.theme.HoneyGoldContainer
import com.example.ui.theme.HoneyGoldDark
import com.example.ui.theme.HoneyGoldLight
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.WarningAlertRed
import com.example.ui.viewmodel.ApiaryWeatherInfo
import com.example.ui.viewmodel.DashboardCardType
import com.example.ui.viewmodel.ScreenTab
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    apiaries: List<ApiaryEntity>,
    hives: List<HiveEntity>,
    beeTraps: List<BeeTrapEntity> = emptyList(),
    tasks: List<TaskScheduleEntity>,
    harvests: List<HarvestEntity>,
    totalHoneyKg: Double,
    sanitaryAlertCount: Int,
    weatherInfo: ApiaryWeatherInfo,
    selectedWeatherApiaryId: Long? = null,
    dashboardCards: List<DashboardCardType> = DashboardCardType.entries.toList(),
    onUpdateDashboardCards: (List<DashboardCardType>) -> Unit = {},
    onResetDashboardCards: () -> Unit = {},
    onSelectWeatherApiary: (Long) -> Unit = {},
    onRefreshWeather: () -> Unit = {},
    onNavigateTab: (ScreenTab) -> Unit,
    onOpenQuickInspection: () -> Unit,
    onOpenNutritionCalculator: () -> Unit,
    onOpenCombImageAnalysis: () -> Unit,
    onOpenHandsFreeMode: () -> Unit,
    onOpenQrScanner: () -> Unit,
    onOpenExportReports: () -> Unit,
    onOpenGeneralMap: () -> Unit = {},
    onOpenThemeCustomization: () -> Unit = {},
    onOpenLicense: () -> Unit = {},
    onOpenMaintenanceTips: () -> Unit = {},
    onOpenQualitySeals: () -> Unit = {},
    onOpenMarketingMaterial: () -> Unit = {},
    licenseData: LicenseData? = null,
    onToggleTask: (TaskScheduleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomizationDialog by remember { mutableStateOf(false) }
    var showWeatherModal by remember { mutableStateOf(false) }

    val pendingTasks = tasks.filter { !it.isCompleted }.take(3)
    val alertHives = hives.filter { it.healthStatus == "Alerta Sanitário" }

    val capturedTraps = beeTraps.filter { it.status == "Capturada" }
    val activeTraps = beeTraps.filter { it.status == "Armada" }

    val apisHivesCount = hives.count { !it.speciesCategory.contains("Melipon") }
    val asfHivesCount = hives.count { it.speciesCategory.contains("Melipon") }

    val avgYieldPerHive = if (hives.isNotEmpty()) totalHoneyKg / hives.size else 0.0

    if (showCustomizationDialog) {
        DashboardCustomizationDialog(
            currentCards = dashboardCards,
            onSaveCards = { newOrder ->
                onUpdateDashboardCards(newOrder)
            },
            onResetDefaults = onResetDashboardCards,
            onDismiss = { showCustomizationDialog = false }
        )
    }

    if (showWeatherModal) {
        WeatherForecastModalDialog(
            weatherInfo = weatherInfo,
            apiaries = apiaries,
            selectedApiaryId = selectedWeatherApiaryId,
            onSelectApiary = onSelectWeatherApiary,
            onDismiss = { showWeatherModal = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top Customization Header Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "PAINEL DE CONTROLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = HoneyGoldContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "v${BuildConfig.VERSION_NAME}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = HoneyGoldDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.clickable { showCustomizationDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Personalizar Disposição",
                            tint = HoneyGoldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Personalizar Cards",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HoneyGoldPrimary
                        )
                    }
                }
            }
        }

        // License status banner (independent license outside Google Play)
        licenseData?.let { lic ->
            item {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)) {
                    LicenseBanner(
                        licenseData = lic,
                        onOpenLicenseDialog = onOpenLicense
                    )
                }
            }
        }

        // Render Cards in User-Defined Custom Order
        dashboardCards.forEach { cardType ->
            when (cardType) {
                DashboardCardType.ALERTS -> {
                    item(key = "card_alerts") {
                        AlertsCardSection(
                            alertHives = alertHives,
                            weatherInfo = weatherInfo,
                            pendingTasks = tasks.filter { !it.isCompleted && it.priority == "Alta" },
                            onNavigateToInspections = { onNavigateTab(ScreenTab.INSPECTIONS) },
                            onNavigateToSchedule = { onNavigateTab(ScreenTab.SCHEDULE) }
                        )
                    }
                }

                DashboardCardType.OVERVIEW -> {
                    item(key = "card_overview") {
                        OverviewCardSection(
                            apiariesCount = apiaries.size,
                            hivesCount = hives.size,
                            totalHoneyKg = totalHoneyKg,
                            avgYieldPerHive = avgYieldPerHive,
                            activeTrapsCount = activeTraps.size,
                            capturedTrapsCount = capturedTraps.size,
                            apisHivesCount = apisHivesCount,
                            asfHivesCount = asfHivesCount,
                            hivesNotEmpty = hives.isNotEmpty(),
                            hives = hives,
                            harvests = harvests,
                            onNavigateTab = onNavigateTab
                        )
                    }
                }

                DashboardCardType.BLUETOOTH_RADAR -> {
                    item(key = "card_ble_radar") {
                        BluetoothRadarCardSection(
                            onNavigateToBleTab = { onNavigateTab(ScreenTab.BLUETOOTH_TAGS) }
                        )
                    }
                }

                DashboardCardType.VOICE_INSPECTION -> {
                    item(key = "card_voice") {
                        VoiceInspectionCardSection(
                            onOpenHandsFreeMode = onOpenHandsFreeMode,
                            onOpenQuickInspection = onOpenQuickInspection
                        )
                    }
                }

                DashboardCardType.APIARY_MAP -> {
                    item(key = "card_apiary_map") {
                        ApiaryMapCardSection(
                            apiaries = apiaries,
                            hives = hives,
                            beeTraps = beeTraps,
                            onOpenGeneralMap = onOpenGeneralMap
                        )
                    }
                }

                DashboardCardType.WEATHER_MANEJO -> {
                    item(key = "card_weather") {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            WeatherManejoCard(
                                weatherInfo = weatherInfo,
                                apiaryName = apiaries.firstOrNull()?.name ?: "",
                                apiaries = apiaries,
                                selectedApiaryId = selectedWeatherApiaryId,
                                onSelectApiary = onSelectWeatherApiary,
                                onOpenForecastModal = { showWeatherModal = true },
                                onRefresh = onRefreshWeather
                            )
                        }
                    }
                }

                DashboardCardType.QUICK_TOOLS -> {
                    item(key = "card_tools") {
                        QuickToolsCardSection(
                            onNavigateTab = onNavigateTab,
                            onOpenThemeCustomization = onOpenThemeCustomization,
                            onOpenNutritionCalculator = onOpenNutritionCalculator,
                            onOpenCombImageAnalysis = onOpenCombImageAnalysis,
                            onOpenHandsFreeMode = onOpenHandsFreeMode,
                            onOpenQrScanner = onOpenQrScanner,
                            onOpenExportReports = onOpenExportReports,
                            onOpenGeneralMap = onOpenGeneralMap,
                            onOpenMaintenanceTips = onOpenMaintenanceTips,
                            onOpenQualitySeals = onOpenQualitySeals,
                            onOpenMarketingMaterial = onOpenMarketingMaterial,
                            onOpenCustomization = { showCustomizationDialog = true }
                        )
                    }
                }

                DashboardCardType.SCHEDULED_TASKS -> {
                    item(key = "card_tasks") {
                        ScheduledTasksCardSection(
                            pendingTasks = pendingTasks,
                            onNavigateTab = onNavigateTab,
                            onToggleTask = onToggleTask
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// INDIVIDUAL CARD SECTION COMPOSABLES
// ----------------------------------------------------------------------------

@Composable
private fun AlertsCardSection(
    alertHives: List<HiveEntity>,
    weatherInfo: ApiaryWeatherInfo,
    pendingTasks: List<TaskScheduleEntity>,
    onNavigateToInspections: () -> Unit,
    onNavigateToSchedule: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        val hasAlerts = alertHives.isNotEmpty() || pendingTasks.isNotEmpty()

        if (hasAlerts) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_alerts_card"),
                shape = RectangleShape,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color(0xFFE11D48) // Vermelho goiaba
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp) // Aumentado em 100%
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "CENTRAL DE ALERTAS ATIVOS",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFDE047), // Amarelo de alto contraste
                                letterSpacing = 0.5.sp
                            )
                        }

                        Text(
                            text = "${alertHives.size + pendingTasks.size} pendências",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (alertHives.isNotEmpty()) {
                        Text(
                            text = "🚨 Colmeias sob atenção sanitária:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        alertHives.take(2).forEach { hive ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clickable { onNavigateToInspections() },
                                shape = RectangleShape,
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Colmeia ${hive.hiveNumber} • ${hive.beeSpecies}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Rainha ${hive.queenColor} • Toque para revisar",
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                    HealthBadge(status = hive.healthStatus)
                                }
                            }
                        }
                    }

                    if (pendingTasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⏰ Manejos de alta prioridade:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        pendingTasks.take(2).forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToSchedule() }
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${task.title} (${task.apiaryName ?: "Apiário"})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                PriorityBadge(priority = task.priority)
                            }
                        }
                    }
                }
            }
        } else {
            // All Clear / Healthy Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFECFDF5)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MeadowGreenSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Apiários em Condição Saudável",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = "Nenhum alerta sanitário ativo. Manter calendário preventivo.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = Color(0xFF047857)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewCardSection(
    apiariesCount: Int,
    hivesCount: Int,
    totalHoneyKg: Double,
    avgYieldPerHive: Double,
    activeTrapsCount: Int,
    capturedTrapsCount: Int,
    apisHivesCount: Int,
    asfHivesCount: Int,
    hivesNotEmpty: Boolean,
    hives: List<HiveEntity> = emptyList(),
    harvests: List<HarvestEntity> = emptyList(),
    onNavigateTab: (ScreenTab) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(
            text = "VISÃO GERAL & PRODUÇÃO",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Apiários",
                value = "$apiariesCount",
                iconComposable = {
                    Icon(
                        painter = painterResource(R.drawable.ic_beehive),
                        contentDescription = "Colmeia",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab(ScreenTab.APIARIES) }
            )
            KpiCard(
                title = "Colmeias Ativas",
                value = "$hivesCount",
                icon = "🐝",
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab(ScreenTab.APIARIES) }
            )
            KpiCard(
                title = "Produção Total",
                value = "${String.format(Locale.US, "%.1f", totalHoneyKg)} kg",
                icon = "🍯",
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab(ScreenTab.HARVESTS) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Média / Colmeia",
                value = "${String.format(Locale.US, "%.1f", avgYieldPerHive)} kg",
                icon = "📈",
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab(ScreenTab.HARVESTS) }
            )
            KpiCard(
                title = "Iscas Armadas",
                value = "$activeTrapsCount",
                icon = "🪤",
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab(ScreenTab.APIARIES) }
            )
            KpiCard(
                title = "Capturas 🎉",
                value = "$capturedTrapsCount",
                icon = "🎯",
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab(ScreenTab.APIARIES) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Gráfico de Produção de Mel Integrado com Linhas e Barras
        HoneyProductionGrowthChart(
            harvests = harvests,
            hives = hives
        )

        if (hivesNotEmpty) {
            Spacer(modifier = Modifier.height(8.dp))
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🐝 Plantel por Categoria de Abelha",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Apis: $apisHivesCount colmeias • ASF (Sem Ferrão): $asfHivesCount colônias",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { onNavigateTab(ScreenTab.APIARIES) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Ver Todas", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewProductionChartCard(
    harvests: List<HarvestEntity>,
    totalHoneyKg: Double,
    avgYieldPerHive: Double,
    onNavigateTab: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val honeyHarvests = remember(harvests) {
        harvests.filter { it.productType.contains("Mel", ignoreCase = true) || it.quantityKg > 0 }
    }

    val chartData = remember(honeyHarvests) {
        val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.US)
        val monthLabelFormat = SimpleDateFormat("MMM", Locale.forLanguageTag("pt-BR"))

        val recentMonths = (5 downTo 0).map { monthsAgo ->
            val cal = Calendar.getInstance().apply {
                add(Calendar.MONTH, -monthsAgo)
            }
            val key = monthKeyFormat.format(cal.time)
            val rawLabel = monthLabelFormat.format(cal.time).replace(".", "")
            val label = rawLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            key to label
        }

        val grouped = honeyHarvests.groupBy { monthKeyFormat.format(Date(it.date)) }
        val hasRealData = honeyHarvests.isNotEmpty()
        val baselineKg = listOf(45.0, 75.0, 110.0, 150.0, 90.0, 135.0)

        recentMonths.mapIndexed { index, (key, label) ->
            val realSum = grouped[key]?.sumOf { it.quantityKg } ?: 0.0
            val displayValue = if (hasRealData) realSum else baselineKg.getOrElse(index) { 80.0 }
            Triple(label, displayValue, hasRealData)
        }
    }

    val maxVal = remember(chartData) {
        val maxInSeries = chartData.maxOfOrNull { it.second } ?: 100.0
        if (maxInSeries <= 0.0) 100.0 else maxInSeries * 1.2
    }

    val hasRealHarvests = honeyHarvests.isNotEmpty()
    val monthlyAvg = if (chartData.isNotEmpty()) chartData.map { it.second }.average() else 0.0

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_production_chart_card"),
        shape = RectangleShape,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(HoneyGoldPrimary.copy(alpha = 0.18f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = HoneyGoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PRODUÇÃO DE MEL",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (hasRealHarvests) MeadowGreenSecondary.copy(alpha = 0.15f) else HoneyGoldPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (hasRealHarvests) "SAFRA ATIVA" else "PROJEÇÃO & SAFRA",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasRealHarvests) MeadowGreenSecondary else HoneyGoldDark,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Histórico dos últimos 6 meses (kg)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Ver Safras ➔",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateTab(ScreenTab.HARVESTS) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 18.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    chartData.forEach { (monthLabel, valueKg, _) ->
                        val heightFraction = (valueKg / maxVal).coerceIn(0.08, 1.0).toFloat()
                        val isPeak = valueKg == chartData.maxOfOrNull { it.second } && valueKg > 0.0

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = if (valueKg >= 100) "${valueKg.toInt()}k" else "${valueKg.toInt()}",
                                fontSize = 10.sp,
                                fontWeight = if (isPeak) FontWeight.Black else FontWeight.SemiBold,
                                color = if (isPeak) HoneyGoldDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.55f)
                                    .fillMaxHeight(heightFraction)
                                    .background(
                                        Brush.verticalGradient(
                                            if (isPeak) listOf(Color(0xFFFBBF24), Color(0xFFD97706))
                                            else listOf(Color(0xFFFDE68A), Color(0xFFF59E0B))
                                        ),
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = monthLabel,
                                fontSize = 10.sp,
                                fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Normal,
                                color = if (isPeak) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Safra", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${String.format(Locale.US, "%.1f", totalHoneyKg)} kg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Média Mensal", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${String.format(Locale.US, "%.1f", monthlyAvg)} kg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Média/Colmeia", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${String.format(Locale.US, "%.1f", avgYieldPerHive)} kg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceInspectionCardSection(
    onOpenHandsFreeMode: () -> Unit,
    onOpenQuickInspection: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("dashboard_voice_card"),
        shape = RectangleShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(HoneyGoldLight, HoneyGoldPrimary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Inspeção por Voz & IA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HoneyGoldPrimary
                        ) {
                            Text(
                                text = "MÃOS LIVRES",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Escuta de voz ativa e mãos livres no apiário. Diga \"Iniciar ditado\", alterne colmeias e salve sem tirar as luvas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenHandsFreeMode,
                    colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Iniciar Modo Luvas & Voz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenQuickInspection,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(0.8f)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = HoneyGoldPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Diagnóstico IA", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ApiaryMapCardSection(
    apiaries: List<ApiaryEntity>,
    hives: List<HiveEntity>,
    beeTraps: List<BeeTrapEntity>,
    onOpenGeneralMap: () -> Unit
) {
    val mainApiary = apiaries.firstOrNull()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onOpenGeneralMap() }
            .testTag("dashboard_map_card"),
        shape = RectangleShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MeadowGreenSecondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = MeadowGreenSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Mapa do Apiário & Satélite",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${apiaries.size} apiários • ${hives.size} colmeias • ${beeTraps.size} iscas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onOpenGeneralMap,
                    colors = ButtonDefaults.buttonColors(containerColor = MeadowGreenSecondary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Abrir Mapa", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MINI MAP VIEWPORT (Pequeno mapa interativo no card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RectangleShape)
                    .border(1.dp, MeadowGreenSecondary.copy(alpha = 0.35f), RectangleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF1E3A2F),
                                Color(0xFF142820),
                                Color(0xFF0D1B15)
                            )
                        )
                    )
            ) {
                // Topographic & Foraging Radius Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)

                    // Draw topographic terrain simulation lines
                    val gridSpacing = 35f
                    var y = 0f
                    while (y <= size.height) {
                        drawLine(
                            color = Color(0xFF2D5A47).copy(alpha = 0.25f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += gridSpacing
                    }
                    var x = 0f
                    while (x <= size.width) {
                        drawLine(
                            color = Color(0xFF2D5A47).copy(alpha = 0.25f),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f
                        )
                        x += gridSpacing
                    }

                    // Outer Foraging Circle (3.0 km radius)
                    drawCircle(
                        color = Color(0xFF4ADE80).copy(alpha = 0.15f),
                        radius = size.height * 0.45f,
                        center = centerOffset
                    )
                    drawCircle(
                        color = Color(0xFF4ADE80).copy(alpha = 0.5f),
                        radius = size.height * 0.45f,
                        center = centerOffset,
                        style = Stroke(
                            width = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    )

                    // Intermediate Foraging Circle (1.5 km radius)
                    drawCircle(
                        color = Color(0xFFFBBF24).copy(alpha = 0.12f),
                        radius = size.height * 0.28f,
                        center = centerOffset
                    )
                    drawCircle(
                        color = Color(0xFFFBBF24).copy(alpha = 0.6f),
                        radius = size.height * 0.28f,
                        center = centerOffset,
                        style = Stroke(
                            width = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    )

                    // Inner Core Apiary Circle (500m radius)
                    drawCircle(
                        color = HoneyGoldPrimary.copy(alpha = 0.25f),
                        radius = size.height * 0.14f,
                        center = centerOffset
                    )
                    drawCircle(
                        color = HoneyGoldPrimary,
                        radius = size.height * 0.14f,
                        center = centerOffset,
                        style = Stroke(width = 2f)
                    )

                    // Draw Hive Points (Colmeias distribuídas)
                    val hiveOffsets = listOf(
                        Offset(-25f, -15f),
                        Offset(20f, -22f),
                        Offset(-18f, 25f),
                        Offset(26f, 18f),
                        Offset(-35f, 5f),
                        Offset(32f, -5f),
                        Offset(5f, 30f),
                        Offset(-10f, -32f)
                    )
                    val totalHivesToDraw = hives.size.coerceAtLeast(4).coerceAtMost(hiveOffsets.size)
                    for (i in 0 until totalHivesToDraw) {
                        val pt = centerOffset + hiveOffsets[i]
                        drawCircle(
                            color = MeadowGreenSecondary.copy(alpha = 0.4f),
                            radius = 7f,
                            center = pt
                        )
                        drawCircle(
                            color = Color(0xFF4ADE80),
                            radius = 4f,
                            center = pt
                        )
                    }

                    // Draw Bee Trap Points (Iscas armadas no raio)
                    val trapOffsets = listOf(
                        Offset(-60f, -40f),
                        Offset(55f, 45f),
                        Offset(65f, -35f),
                        Offset(-50f, 50f)
                    )
                    val totalTrapsToDraw = beeTraps.size.coerceAtLeast(2).coerceAtMost(trapOffsets.size)
                    for (i in 0 until totalTrapsToDraw) {
                        val pt = centerOffset + trapOffsets[i]
                        drawCircle(
                            color = HoneyGoldLight.copy(alpha = 0.4f),
                            radius = 8f,
                            center = pt
                        )
                        drawCircle(
                            color = HoneyGoldLight,
                            radius = 4.5f,
                            center = pt
                        )
                    }

                    // Central Apiary Core Dot
                    drawCircle(
                        color = Color.White,
                        radius = 6f,
                        center = centerOffset
                    )
                    drawCircle(
                        color = HoneyGoldPrimary,
                        radius = 4f,
                        center = centerOffset
                    )
                }

                // Top Left Overlay: Compass & Radius Info
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🧭 N", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("•", fontSize = 10.sp, color = Color.LightGray)
                        Text("Raio 3km", fontSize = 10.sp, color = Color(0xFF4ADE80), fontWeight = FontWeight.Medium)
                    }
                }

                // Top Right Overlay: Satellite Layer Badge
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🛰️ Satélite HD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HoneyGoldLight)
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Expandir",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Center Apiary Name Tag
                Surface(
                    color = Color(0xFF0F172A).copy(alpha = 0.85f),
                    shape = RectangleShape,
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🏡", fontSize = 10.sp)
                        Text(
                            text = mainApiary?.name ?: "Apiário Sol Nascente",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Bottom Overlay Bar (Tap to view full map)
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RectangleShape,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Toque no card para abrir o mapa interativo em tela cheia",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "Explorar 🔍",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = HoneyGoldLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Coordinates preview badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val coordsText = if (mainApiary?.latitude != null && mainApiary.longitude != null) {
                        "GPS: ${String.format(Locale.US, "%.4f", mainApiary.latitude)}, ${String.format(Locale.US, "%.4f", mainApiary.longitude)}"
                    } else {
                        "GPS: -22.7394, -45.5913"
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = HoneyGoldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = coordsText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "🐝 ${hives.size} colmeias",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MeadowGreenSecondary
                        )
                        Text(
                            text = "🪤 ${beeTraps.size} iscas",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HoneyGoldPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickToolsCardSection(
    onNavigateTab: (ScreenTab) -> Unit,
    onOpenThemeCustomization: () -> Unit,
    onOpenNutritionCalculator: () -> Unit,
    onOpenCombImageAnalysis: () -> Unit,
    onOpenHandsFreeMode: () -> Unit,
    onOpenQrScanner: () -> Unit,
    onOpenExportReports: () -> Unit,
    onOpenGeneralMap: () -> Unit,
    onOpenMaintenanceTips: () -> Unit,
    onOpenQualitySeals: () -> Unit,
    onOpenMarketingMaterial: () -> Unit,
    onOpenCustomization: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(
            text = "FERRAMENTAS TÉCNICAS & MANEJO",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ToolQuickChip(
                    title = "Material Marketing",
                    subtitle = "Folders, Copys & ZIPs",
                    icon = Icons.Default.Campaign,
                    containerColor = Color(0xFFFEF3C7),
                    iconColor = Color(0xFFD97706),
                    onClick = onOpenMarketingMaterial
                )
            }
            item {
                ToolQuickChip(
                    title = "Dicas de Manejo",
                    subtitle = "Manutenção & boas práticas",
                    icon = Icons.Default.TipsAndUpdates,
                    containerColor = Color(0xFFFEF3C7),
                    iconColor = Color(0xFFD97706),
                    onClick = onOpenMaintenanceTips
                )
            }
            item {
                ToolQuickChip(
                    title = "Selos de Qualidade",
                    subtitle = "ARTE, Orgânico & IG",
                    icon = Icons.Default.Verified,
                    containerColor = Color(0xFFDCFCE7),
                    iconColor = Color(0xFF15803D),
                    onClick = onOpenQualitySeals
                )
            }
            item {
                ToolQuickChip(
                    title = "Radar Tags BLE",
                    subtitle = "Rastrear colmeias",
                    icon = Icons.Default.BluetoothSearching,
                    containerColor = Color(0xFFE0F2FE),
                    iconColor = Color(0xFF0284C7),
                    onClick = { onNavigateTab(ScreenTab.BLUETOOTH_TAGS) }
                )
            }
            item {
                ToolQuickChip(
                    title = "Personalizar Iniciar",
                    subtitle = "Organizar cards do painel",
                    icon = Icons.Default.DashboardCustomize,
                    containerColor = Color(0xFFFEF3C7),
                    iconColor = HoneyGoldPrimary,
                    onClick = onOpenCustomization
                )
            }
            item {
                ToolQuickChip(
                    title = "Gestão Financeira",
                    subtitle = "Caixa, Vendas & Custos",
                    icon = Icons.Default.PointOfSale,
                    containerColor = Color(0xFFDCFCE7),
                    iconColor = Color(0xFF15803D),
                    onClick = { onNavigateTab(ScreenTab.FINANCE) }
                )
            }
            item {
                ToolQuickChip(
                    title = "Tema & Visual",
                    subtitle = "Claro, Escuro e Fontes",
                    icon = Icons.Default.Palette,
                    containerColor = Color(0xFFFDE68A),
                    iconColor = Color(0xFFB45309),
                    onClick = onOpenThemeCustomization
                )
            }
            item {
                ToolQuickChip(
                    title = "Nutrição & Xarope",
                    subtitle = "Calculadora 1:1 e 2:1",
                    icon = Icons.Default.Calculate,
                    containerColor = Color(0xFFFEF3C7),
                    iconColor = Color(0xFFD97706),
                    onClick = onOpenNutritionCalculator
                )
            }
            item {
                ToolQuickChip(
                    title = "Foto do Quadro",
                    subtitle = "Visão IA de postura",
                    icon = Icons.Default.CameraAlt,
                    containerColor = Color(0xFFDCFCE7),
                    iconColor = Color(0xFF16A34A),
                    onClick = onOpenCombImageAnalysis
                )
            }
            item {
                ToolQuickChip(
                    title = "Modo Luvas & Voz",
                    subtitle = "Ditado no apiário",
                    icon = Icons.Default.Mic,
                    containerColor = Color(0xFFF3E8FF),
                    iconColor = Color(0xFF9333EA),
                    onClick = onOpenHandsFreeMode
                )
            }
            item {
                ToolQuickChip(
                    title = "Scanner QR",
                    subtitle = "Ficha da colmeia",
                    icon = Icons.Default.QrCodeScanner,
                    containerColor = Color(0xFFE0F2FE),
                    iconColor = Color(0xFF0284C7),
                    onClick = onOpenQrScanner
                )
            }
            item {
                ToolQuickChip(
                    title = "Relatórios & Laudos",
                    subtitle = "CSV & Rastreabilidade",
                    icon = Icons.Default.Description,
                    containerColor = Color(0xFFFFEDD5),
                    iconColor = Color(0xFFEA580C),
                    onClick = onOpenExportReports
                )
            }
            item {
                ToolQuickChip(
                    title = "Mapa Geral",
                    subtitle = "Apiários, colmeias & iscas",
                    icon = Icons.Default.Explore,
                    containerColor = Color(0xFFFEF3C7),
                    iconColor = Color(0xFFD97706),
                    onClick = onOpenGeneralMap
                )
            }
        }
    }
}

@Composable
private fun ScheduledTasksCardSection(
    pendingTasks: List<TaskScheduleEntity>,
    onNavigateTab: (ScreenTab) -> Unit,
    onToggleTask: (TaskScheduleEntity) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MANEJOS AGENDADOS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Ver todos",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onNavigateTab(ScreenTab.SCHEDULE) }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (pendingTasks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "Nenhum manejo pendente para os próximos dias.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            pendingTasks.forEach { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RectangleShape
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Concluir",
                                tint = if (task.isCompleted) MeadowGreenSecondary else Color.LightGray,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { onToggleTask(task) }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = task.title,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${task.apiaryName ?: "Apiário"} • ${task.taskType}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        PriorityBadge(priority = task.priority)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolQuickChip(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF475569))
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    icon: String? = null,
    iconComposable: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (iconComposable != null) {
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.CenterStart) {
                    iconComposable()
                }
            } else if (icon != null) {
                Text(text = icon, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BluetoothRadarCardSection(
    onNavigateToBleTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("dashboard_ble_radar_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
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
                            .background(HoneyGoldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BluetoothSearching,
                            contentDescription = null,
                            tint = HoneyGoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Rastreamento BLE & Radar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Identificação e proximidade de colmeias",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onNavigateToBleTab,
                    colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Abrir Radar", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎯 Quente/Frio", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Medidor RSSI", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🌡️ Telemetria", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Temp. & Umidade", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🛡️ Anti-Furto", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Alerta fora de raio", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
