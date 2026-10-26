package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.HarvestEntity
import com.example.data.local.entities.HiveEntity
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.WaxYellowTertiary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

enum class RechartsChartMode(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    AREA_SPLINE("Linhas & Área", Icons.Default.ShowChart),
    BARS("Barras", Icons.Default.BarChart),
    CUMULATIVE("Acumulado", Icons.Default.Layers)
}

data class MonthlyHiveHarvestData(
    val monthKey: String, // e.g. "2026-05"
    val monthLabel: String, // e.g. "Mai/26"
    val timestamp: Long,
    val hiveId: Long,
    val hiveNumber: String,
    val quantityKg: Double,
    val moisturePercent: Double?,
    val floralSource: String,
    val harvestCount: Int
)

data class HiveSeriesInfo(
    val hiveId: Long,
    val hiveNumber: String,
    val color: Color,
    val totalHoneyKg: Double,
    val monthlyData: Map<String, Double> // monthKey -> quantityKg
)

private val SERIES_COLORS = listOf(
    Color(0xFFE5A00D), // Honey Amber Gold
    Color(0xFF2E7D32), // Meadow Green
    Color(0xFF1E88E5), // Cerulean Blue
    Color(0xFFD81B60), // Berry Magenta
    Color(0xFF8E24AA), // Royal Purple
    Color(0xFF00897B), // Teal
    Color(0xFFFB8C00), // Tangerine
    Color(0xFF5D4037)  // Wood Brown
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HoneyProductionGrowthChart(
    harvests: List<HarvestEntity>,
    hives: List<HiveEntity>,
    modifier: Modifier = Modifier
) {
    var chartMode by remember { mutableStateOf(RechartsChartMode.AREA_SPLINE) }
    var selectedHiveId by remember { mutableStateOf<Long?>(null) } // null = All Hives
    var activeTooltipPoint by remember { mutableStateOf<TooltipData?>(null) }
    var showDetailedTable by remember { mutableStateOf(false) }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(chartMode, selectedHiveId) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    // Filter honey harvests only
    val honeyHarvests = remember(harvests) {
        harvests.filter { it.productType.contains("Mel", ignoreCase = true) && it.quantityKg > 0 }
    }

    // Prepare chronological list of months
    val monthFormatKey = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()) }
    val monthFormatLabel = remember { SimpleDateFormat("MMM/yy", Locale.forLanguageTag("pt-BR")) }

    // Map all unique months in order
    val sortedMonths = remember(honeyHarvests) {
        val monthsMap = mutableMapOf<String, Pair<String, Long>>()
        honeyHarvests.forEach { h ->
            val key = monthFormatKey.format(Date(h.date))
            val label = monthFormatLabel.format(Date(h.date)).replaceFirstChar { it.uppercase() }
            if (!monthsMap.containsKey(key) || h.date < monthsMap[key]!!.second) {
                monthsMap[key] = Pair(label, h.date)
            }
        }
        monthsMap.entries
            .sortedBy { it.key }
            .map { Pair(it.key, it.value.first) }
    }

    // Map hive series
    val hiveSeriesList = remember(honeyHarvests, hives) {
        val hiveMap = hives.associateBy { it.id }
        val groupedByHive = honeyHarvests.groupBy { it.hiveId ?: -1L }

        groupedByHive.entries
            .filter { it.key != -1L }
            .mapIndexed { index, entry ->
                val hive = hiveMap[entry.key]
                val hiveName = hive?.hiveNumber ?: "Colmeia ${entry.key}"
                val monthlyMap = mutableMapOf<String, Double>()
                entry.value.forEach { h ->
                    val mKey = monthFormatKey.format(Date(h.date))
                    monthlyMap[mKey] = (monthlyMap[mKey] ?: 0.0) + h.quantityKg
                }
                val totalKg = entry.value.sumOf { it.quantityKg }
                val color = SERIES_COLORS[index % SERIES_COLORS.size]
                HiveSeriesInfo(
                    hiveId = entry.key,
                    hiveNumber = hiveName,
                    color = color,
                    totalHoneyKg = totalKg,
                    monthlyData = monthlyMap
                )
            }
            .sortedByDescending { it.totalHoneyKg }
    }

    // Monthly totals for aggregation
    val monthlyTotals = remember(sortedMonths, hiveSeriesList) {
        sortedMonths.associate { (mKey, _) ->
            mKey to hiveSeriesList.sumOf { it.monthlyData[mKey] ?: 0.0 }
        }
    }

    // Calculate Month-over-Month (MoM) Growth
    val momGrowthList = remember(sortedMonths, monthlyTotals, selectedHiveId, hiveSeriesList) {
        val list = mutableListOf<Pair<String, Double>>() // Month label -> Growth %
        val dataSeries = if (selectedHiveId == null) {
            sortedMonths.map { (mKey, label) -> label to (monthlyTotals[mKey] ?: 0.0) }
        } else {
            val series = hiveSeriesList.find { it.hiveId == selectedHiveId }
            sortedMonths.map { (mKey, label) -> label to (series?.monthlyData?.get(mKey) ?: 0.0) }
        }

        for (i in dataSeries.indices) {
            val (label, currentVal) = dataSeries[i]
            if (i == 0 || dataSeries[i - 1].second == 0.0) {
                list.add(label to 0.0)
            } else {
                val prevVal = dataSeries[i - 1].second
                val growth = ((currentVal - prevVal) / prevVal) * 100.0
                list.add(label to growth)
            }
        }
        list
    }

    // Identify top performer & peak month
    val topHive = remember(hiveSeriesList) { hiveSeriesList.maxByOrNull { it.totalHoneyKg } }
    val peakMonth = remember(sortedMonths, monthlyTotals) {
        sortedMonths.maxByOrNull { (mKey, _) -> monthlyTotals[mKey] ?: 0.0 }
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("honey_production_growth_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Recharts Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(HoneyGoldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Gráfico de Crescimento",
                            tint = HoneyGoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "📈 Crescimento da Produção (Recharts)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Evolução do rendimento de mel por colmeia",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Mode Selector (Segmented buttons)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                RechartsChartMode.values().forEachIndexed { index, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = RechartsChartMode.values().size),
                        onClick = {
                            chartMode = mode
                            activeTooltipPoint = null
                        },
                        selected = chartMode == mode,
                        icon = {
                            Icon(
                                imageVector = mode.icon,
                                contentDescription = mode.label,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    ) {
                        Text(text = mode.label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hive Filter Chips (Multi-series / Single Hive Selector)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Todas as Colmeias" chip
                FilterChip(
                    selected = selectedHiveId == null,
                    onClick = {
                        selectedHiveId = null
                        activeTooltipPoint = null
                    },
                    label = { Text("🐝 Todas as Colmeias") },
                    leadingIcon = if (selectedHiveId == null) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HoneyGoldPrimary,
                        selectedLabelColor = Color.White
                    )
                )

                // Individual Hives
                hiveSeriesList.forEach { series ->
                    val isSelected = selectedHiveId == series.hiveId
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedHiveId = if (isSelected) null else series.hiveId
                            activeTooltipPoint = null
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(series.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${series.hiveNumber} (${String.format(Locale.US, "%.1f", series.totalHoneyKg)}kg)")
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = series.color.copy(alpha = 0.2f),
                            selectedLabelColor = series.color
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Tooltip Display (Glassmorphic style Recharts card)
            AnimatedVisibility(visible = activeTooltipPoint != null) {
                activeTooltipPoint?.let { tip ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(tip.seriesColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${tip.hiveName} • ${tip.monthLabel}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (tip.growthPercent != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val isPositive = tip.growthPercent >= 0
                                        Icon(
                                            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                            contentDescription = null,
                                            tint = if (isPositive) MeadowGreenSecondary else Color(0xFFD32F2F),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%.1f", tip.growthPercent)}% vs mês ant.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isPositive) MeadowGreenSecondary else Color(0xFFD32F2F),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", tip.quantityKg)} kg",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = tip.seriesColor
                                )
                                Text(
                                    text = "${tip.extractions} extração(ões)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Main Interactive Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                if (sortedMonths.isEmpty() || hiveSeriesList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Sem dados suficientes para renderizar o gráfico.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    RechartsInteractiveCanvas(
                        chartMode = chartMode,
                        sortedMonths = sortedMonths,
                        hiveSeriesList = hiveSeriesList,
                        selectedHiveId = selectedHiveId,
                        animationProgress = animationProgress.value,
                        onPointSelected = { activeTooltipPoint = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recharts Analytics Insights Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Hive Card
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "🏆 Colmeia Campeã",
                            style = MaterialTheme.typography.labelSmall,
                            color = HoneyGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = topHive?.hiveNumber ?: "N/A",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.1f", topHive?.totalHoneyKg ?: 0.0)} kg colhidos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Peak Month Card
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "📅 Mês de Pico",
                            style = MaterialTheme.typography.labelSmall,
                            color = MeadowGreenSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = peakMonth?.second ?: "N/A",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.1f", peakMonth?.let { monthlyTotals[it.first] } ?: 0.0)} kg totais",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Toggle detailed table button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showDetailedTable = !showDetailedTable },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (showDetailedTable) "Ocultar Tabela de Dados" else "Ver Tabela Detalhada por Mês & Colmeia",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Detailed Data Table
            AnimatedVisibility(visible = showDetailedTable) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    sortedMonths.forEach { (mKey, mLabel) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Mês: $mLabel",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    val totalMonth = monthlyTotals[mKey] ?: 0.0
                                    Text(
                                        text = "Total: ${String.format(Locale.US, "%.1f", totalMonth)} kg",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = HoneyGoldPrimary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                hiveSeriesList.forEach { series ->
                                    val qty = series.monthlyData[mKey] ?: 0.0
                                    if (qty > 0 || selectedHiveId == series.hiveId) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(series.color)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = series.hiveNumber,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            Text(
                                                text = "${String.format(Locale.US, "%.1f", qty)} kg",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
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

data class TooltipData(
    val hiveName: String,
    val monthLabel: String,
    val quantityKg: Double,
    val growthPercent: Double?,
    val extractions: Int,
    val seriesColor: Color
)

@Composable
private fun RechartsInteractiveCanvas(
    chartMode: RechartsChartMode,
    sortedMonths: List<Pair<String, String>>,
    hiveSeriesList: List<HiveSeriesInfo>,
    selectedHiveId: Long?,
    animationProgress: Float,
    onPointSelected: (TooltipData) -> Unit
) {
    val density = LocalDensity.current

    // Active series to display
    val activeSeries = remember(hiveSeriesList, selectedHiveId) {
        if (selectedHiveId == null) hiveSeriesList else hiveSeriesList.filter { it.hiveId == selectedHiveId }
    }

    // Determine max value for Y-axis scaling
    val maxVal = remember(chartMode, sortedMonths, activeSeries, hiveSeriesList) {
        when (chartMode) {
            RechartsChartMode.CUMULATIVE -> {
                activeSeries.maxOfOrNull { series ->
                    sortedMonths.sumOf { (mKey, _) -> series.monthlyData[mKey] ?: 0.0 }
                }?.coerceAtLeast(10.0) ?: 10.0
            }
            RechartsChartMode.BARS, RechartsChartMode.AREA_SPLINE -> {
                activeSeries.flatMap { series ->
                    sortedMonths.map { (mKey, _) -> series.monthlyData[mKey] ?: 0.0 }
                }.maxOrNull()?.coerceAtLeast(10.0) ?: 10.0
            }
        } * 1.15 // 15% headroom
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(chartMode, sortedMonths, activeSeries) {
                detectTapGestures { offset ->
                    val paddingLeft = with(density) { 36.dp.toPx() }
                    val paddingRight = with(density) { 16.dp.toPx() }
                    val plotWidth = size.width - paddingLeft - paddingRight

                    if (sortedMonths.isNotEmpty() && offset.x >= paddingLeft && offset.x <= size.width - paddingRight) {
                        val stepX = plotWidth / (sortedMonths.size - 1).coerceAtLeast(1)
                        val nearestMonthIndex = ((offset.x - paddingLeft + (stepX / 2f)) / stepX).toInt()
                            .coerceIn(0, sortedMonths.size - 1)

                        val (mKey, mLabel) = sortedMonths[nearestMonthIndex]

                        // Find closest series to touch or active
                        val selectedSeries = if (activeSeries.size == 1) {
                            activeSeries.first()
                        } else {
                            activeSeries.maxByOrNull { it.monthlyData[mKey] ?: 0.0 } ?: activeSeries.first()
                        }

                        val qty = selectedSeries.monthlyData[mKey] ?: 0.0
                        val prevQty = if (nearestMonthIndex > 0) {
                            selectedSeries.monthlyData[sortedMonths[nearestMonthIndex - 1].first] ?: 0.0
                        } else 0.0

                        val growth = if (nearestMonthIndex > 0 && prevQty > 0.0) {
                            ((qty - prevQty) / prevQty) * 100.0
                        } else null

                        onPointSelected(
                            TooltipData(
                                hiveName = selectedSeries.hiveNumber,
                                monthLabel = mLabel,
                                quantityKg = qty,
                                growthPercent = growth,
                                extractions = if (qty > 0) 1 else 0,
                                seriesColor = selectedSeries.color
                            )
                        )
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height

        val paddingLeft = 36.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 26.dp.toPx()

        val plotWidth = width - paddingLeft - paddingRight
        val plotHeight = height - paddingTop - paddingBottom

        // 1. Draw Dashed Gridlines & Y-Axis Labels
        val ySteps = 4
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        val gridColor = Color(0x33888888)

        for (i in 0..ySteps) {
            val ratio = i / ySteps.toFloat()
            val yPos = height - paddingBottom - (ratio * plotHeight)
            val valueAtY = ratio * maxVal

            // Gridline
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, yPos),
                end = Offset(width - paddingRight, yPos),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )

            // Label
            drawContext.canvas.nativeCanvas.drawText(
                "${valueAtY.toInt()}k",
                paddingLeft - 8.dp.toPx(),
                yPos + 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                    isAntiAlias = true
                }
            )
        }

        // 2. Draw X-Axis Month Labels
        val stepX = if (sortedMonths.size > 1) plotWidth / (sortedMonths.size - 1) else plotWidth
        sortedMonths.forEachIndexed { index, (_, label) ->
            val xPos = if (sortedMonths.size > 1) paddingLeft + (index * stepX) else paddingLeft + (plotWidth / 2f)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                xPos,
                height - 6.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
            )
        }

        // 3. Render Chart Data based on mode
        when (chartMode) {
            RechartsChartMode.AREA_SPLINE -> {
                activeSeries.forEach { series ->
                    val points = mutableListOf<Offset>()
                    sortedMonths.forEachIndexed { index, (mKey, _) ->
                        val qty = series.monthlyData[mKey] ?: 0.0
                        val xPos = if (sortedMonths.size > 1) paddingLeft + (index * stepX) else paddingLeft + (plotWidth / 2f)
                        val yPos = height - paddingBottom - ((qty / maxVal).toFloat() * plotHeight * animationProgress)
                        points.add(Offset(xPos, yPos))
                    }

                    if (points.size > 1) {
                        // Create smooth cubic path
                        val linePath = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                                val controlY1 = p0.y
                                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                                val controlY2 = p1.y
                                cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                            }
                        }

                        // Gradient Area Fill Under Curve (Recharts style)
                        val areaPath = Path().apply {
                            addPath(linePath)
                            lineTo(points.last().x, height - paddingBottom)
                            lineTo(points.first().x, height - paddingBottom)
                            close()
                        }

                        val fillBrush = Brush.verticalGradient(
                            colors = listOf(
                                series.color.copy(alpha = 0.35f * animationProgress),
                                series.color.copy(alpha = 0.02f)
                            ),
                            startY = paddingTop,
                            endY = height - paddingBottom
                        )

                        drawPath(path = areaPath, brush = fillBrush)

                        // Draw Curve Line
                        drawPath(
                            path = linePath,
                            color = series.color,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Draw Data Point Circles (Dots)
                        points.forEach { pt ->
                            // Glowing Halo
                            drawCircle(
                                color = series.color.copy(alpha = 0.25f),
                                radius = 6.dp.toPx(),
                                center = pt
                            )
                            // Solid point
                            drawCircle(
                                color = series.color,
                                radius = 3.5.dp.toPx(),
                                center = pt
                            )
                            // Inner white core
                            drawCircle(
                                color = Color.White,
                                radius = 1.8.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }

            RechartsChartMode.BARS -> {
                val groupWidth = stepX * 0.7f
                val barWidth = (groupWidth / activeSeries.size.coerceAtLeast(1)).coerceAtLeast(4.dp.toPx())

                sortedMonths.forEachIndexed { monthIndex, (mKey, _) ->
                    val groupCenterX = if (sortedMonths.size > 1) paddingLeft + (monthIndex * stepX) else paddingLeft + (plotWidth / 2f)
                    val groupStartX = groupCenterX - (groupWidth / 2f)

                    activeSeries.forEachIndexed { seriesIndex, series ->
                        val qty = series.monthlyData[mKey] ?: 0.0
                        val barHeight = ((qty / maxVal).toFloat() * plotHeight * animationProgress).coerceAtLeast(0f)
                        val barLeft = groupStartX + (seriesIndex * barWidth)
                        val barTop = height - paddingBottom - barHeight

                        drawRoundRect(
                            color = series.color,
                            topLeft = Offset(barLeft, barTop),
                            size = Size(barWidth - 2.dp.toPx(), barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }

            RechartsChartMode.CUMULATIVE -> {
                activeSeries.forEach { series ->
                    val points = mutableListOf<Offset>()
                    var accumulated = 0.0

                    sortedMonths.forEachIndexed { index, (mKey, _) ->
                        accumulated += (series.monthlyData[mKey] ?: 0.0)
                        val xPos = if (sortedMonths.size > 1) paddingLeft + (index * stepX) else paddingLeft + (plotWidth / 2f)
                        val yPos = height - paddingBottom - ((accumulated / maxVal).toFloat() * plotHeight * animationProgress)
                        points.add(Offset(xPos, yPos))
                    }

                    if (points.size > 1) {
                        val path = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = series.color,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        points.forEach { pt ->
                            drawCircle(
                                color = series.color,
                                radius = 4.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }
        }
    }
}
