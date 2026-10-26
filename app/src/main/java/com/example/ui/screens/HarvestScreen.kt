package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.HarvestEntity
import com.example.data.local.entities.HiveEntity
import com.example.ui.components.HoneyProductionGrowthChart
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestScreen(
    harvests: List<HarvestEntity>,
    apiaries: List<ApiaryEntity>,
    hives: List<HiveEntity>,
    totalHoneyKg: Double,
    onRecordHarvest: (
        apiaryId: Long,
        hiveId: Long?,
        productType: String,
        quantityKg: Double,
        moisturePercent: Double?,
        floralSource: String,
        supersHarvestedCount: Int?,
        framesHarvestedCount: Int?,
        framesPerSuper: Int?,
        avgYieldPerFrameKg: Double?,
        notes: String
    ) -> Unit,
    onDeleteHarvest: (HarvestEntity) -> Unit,
    onOpenQualitySeals: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddHarvestDialog by remember { mutableStateOf(false) }
    var showCalculatorSection by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Analytics calculations
    val honeyHarvests = harvests.filter { it.productType.contains("Mel", ignoreCase = true) }
    val totalHoney = honeyHarvests.sumOf { it.quantityKg }
    val totalPropolis = harvests.filter { it.productType.contains("Própolis", ignoreCase = true) }.sumOf { it.quantityKg }
    val totalCera = harvests.filter { it.productType.contains("Cera", ignoreCase = true) }.sumOf { it.quantityKg }
    val totalPolen = harvests.filter { it.productType.contains("Pólen", ignoreCase = true) }.sumOf { it.quantityKg }

    val activeHivesCount = hives.size.coerceAtLeast(1)
    val avgPerHive = if (hives.isNotEmpty()) totalHoney / activeHivesCount else 0.0

    val totalSupersHarvested = honeyHarvests.mapNotNull { it.supersHarvestedCount }.sum().coerceAtLeast(1)
    val totalFramesHarvested = honeyHarvests.mapNotNull { it.framesHarvestedCount }.sum().coerceAtLeast(1)

    val avgPerSuper = if (totalSupersHarvested > 0) totalHoney / totalSupersHarvested else 0.0
    val avgPerFrame = if (totalFramesHarvested > 0) totalHoney / totalFramesHarvested else 0.0

    // Calculator prefill values
    var prefillQuantity by remember { mutableStateOf<Double?>(null) }
    var prefillSupers by remember { mutableStateOf(1) }
    var prefillFramesPerSuper by remember { mutableStateOf(10) }
    var prefillYieldPerFrame by remember { mutableStateOf(2.2) }

    Scaffold(
        modifier = modifier.testTag("harvest_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddHarvestDialog = true },
                containerColor = HoneyGoldPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("add_harvest_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Registrar Colheita")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selos de Qualidade & Agregação de Valor (ARTE, Orgânico, IG)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenQualitySeals() }
                        .testTag("harvest_quality_seals_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MeadowGreenSecondary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = MeadowGreenSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Selos de Qualidade & Certificação",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MeadowGreenSecondary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "+ VALOR",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MeadowGreenSecondary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Selo ARTE • Orgânico Brasil (MAPA) • Indicação Geográfica (IG). Agregue até 100% no valor do seu mel.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onOpenQualitySeals,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MeadowGreenSecondary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Metas", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Header: Production Analytics & Averages (Requisito: Média de produção por colmeia e produção total)
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("production_kpis_card"),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "📊 Balanço de Produção & Médias",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Métricas por colmeia, melgueira e quadro",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FilledTonalButton(
                                onClick = { showCalculatorSection = !showCalculatorSection },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (showCalculatorSection) "Fechar Calc." else "Calculadora", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Big Total & Averages Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1.2f),
                                colors = CardDefaults.cardColors(containerColor = HoneyGoldPrimary.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Produção Total (Mel)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", totalHoney)} kg",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = HoneyGoldPrimary
                                    )
                                    Text(
                                        text = "${honeyHarvests.size} extrações registradas",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MeadowGreenSecondary.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Média / Colmeia", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", avgPerHive)} kg",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MeadowGreenSecondary
                                    )
                                    Text(
                                        text = "Base: ${hives.size} colmeias",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Average per Super & per Frame
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Média / Melgueira", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", avgPerSuper)} kg/melg.",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Média / Quadro", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${String.format(Locale.US, "%.2f", avgPerFrame)} kg/quadro",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Própolis / Cera", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", totalPropolis)}kg / ${String.format(Locale.US, "%.1f", totalCera)}kg",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Super & Frame Yield Calculator (Requisito: permitir definir número de quadros por melgueira e produção de quadros por melgueira)
            item {
                AnimatedVisibility(visible = showCalculatorSection) {
                    SuperYieldCalculatorCard(
                        onPrefillHarvest = { qty, supers, framesPerSup, yieldPerFrame ->
                            prefillQuantity = qty
                            prefillSupers = supers
                            prefillFramesPerSuper = framesPerSup
                            prefillYieldPerFrame = yieldPerFrame
                            showAddHarvestDialog = true
                        }
                    )
                }
            }

            // Gráfico Recharts de Crescimento da Produção de Mel ao Longo dos Meses por Colmeia
            item {
                HoneyProductionGrowthChart(
                    harvests = harvests,
                    hives = hives
                )
            }

            // Harvest Records Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Histórico de Colheitas (${harvests.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total: ${String.format(Locale.US, "%.1f", totalHoney)} kg",
                        style = MaterialTheme.typography.labelMedium,
                        color = HoneyGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (harvests.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🍯", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Nenhuma colheita registrada ainda.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Toque no botão '+' para registrar a primeira colheita com detalhes de melgueiras.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(harvests, key = { it.id }) { harvest ->
                    val apiary = apiaries.find { it.id == harvest.apiaryId }
                    val hive = hives.find { it.id == harvest.hiveId }
                    HarvestItemCard(
                        harvest = harvest,
                        apiaryName = apiary?.name ?: "Apiário Não Localizado",
                        hiveNumber = hive?.hiveNumber ?: "Múltiplas Colmeias / Geral",
                        dateFormat = dateFormat,
                        onDelete = { onDeleteHarvest(harvest) }
                    )
                }
            }
        }
    }

    // Add Harvest Dialog
    if (showAddHarvestDialog) {
        AddHarvestDialog(
            apiaries = apiaries,
            hives = hives,
            initialQuantity = prefillQuantity,
            initialSupers = prefillSupers,
            initialFramesPerSuper = prefillFramesPerSuper,
            initialYieldPerFrame = prefillYieldPerFrame,
            onDismiss = {
                showAddHarvestDialog = false
                prefillQuantity = null
            },
            onSave = { apiaryId, hiveId, prod, qty, moist, flora, supers, frames, framesPerSup, avgFrame, notes ->
                onRecordHarvest(apiaryId, hiveId, prod, qty, moist, flora, supers, frames, framesPerSup, avgFrame, notes)
                showAddHarvestDialog = false
                prefillQuantity = null
            }
        )
    }
}

// -------------------------------------------------------------
// CALCULADORA DE PRODUÇÃO POR MELGUEIRA E QUADROS
// -------------------------------------------------------------
@Composable
fun SuperYieldCalculatorCard(
    onPrefillHarvest: (quantityKg: Double, supersCount: Int, framesPerSuper: Int, yieldPerFrame: Double) -> Unit
) {
    var supersCount by remember { mutableIntStateOf(2) }
    var framesPerSuper by remember { mutableIntStateOf(9) } // 8, 9 ou 10
    var yieldPerFrameKg by remember { mutableDoubleStateOf(2.4) }
    var operculationPercent by remember { mutableIntStateOf(90) }
    var pricePerKg by remember { mutableDoubleStateOf(45.0) }

    val totalFrames = supersCount * framesPerSuper
    val rawYieldKg = totalFrames * yieldPerFrameKg * (operculationPercent / 100.0)
    val honeyVolumeLiters = rawYieldKg / 1.42 // Densidade média do mel de abelha
    val estimatedRevenue = rawYieldKg * pricePerKg

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("super_yield_calculator"),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MeadowGreenSecondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧮", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Calculadora & Estimativa de Melgueiras",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Projeção por número de quadros e produtividade",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Inputs Row: Melgueiras & Quadros/Melgueira
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Melgueiras a colher: $supersCount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Slider(
                        value = supersCount.toFloat(),
                        onValueChange = { supersCount = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Quadros / Melgueira: $framesPerSuper q", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(8, 9, 10).forEach { count ->
                            AssistChip(
                                onClick = { framesPerSuper = count },
                                label = { Text("${count}q", fontSize = 11.sp, fontWeight = if (framesPerSuper == count) FontWeight.Bold else FontWeight.Normal) },
                                colors = if (framesPerSuper == count) AssistChipDefaults.assistChipColors(containerColor = HoneyGoldPrimary.copy(alpha = 0.2f)) else AssistChipDefaults.assistChipColors()
                            )
                        }
                    }
                }
            }

            // Yield per frame slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Produção estimada por quadro:", style = MaterialTheme.typography.labelSmall)
                    Text("${String.format(Locale.US, "%.2f", yieldPerFrameKg)} kg / quadro", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = HoneyGoldPrimary)
                }
                Slider(
                    value = yieldPerFrameKg.toFloat(),
                    onValueChange = { yieldPerFrameKg = it.toDouble() },
                    valueRange = 0.2f..3.5f,
                    steps = 32
                )
            }

            // Operculation slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Nível de Operculação dos Favos:", style = MaterialTheme.typography.labelSmall)
                    Text("$operculationPercent%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MeadowGreenSecondary)
                }
                Slider(
                    value = operculationPercent.toFloat(),
                    onValueChange = { operculationPercent = it.toInt() },
                    valueRange = 50f..100f,
                    steps = 9
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Result Display Box
            Card(
                colors = CardDefaults.cardColors(containerColor = HoneyGoldPrimary.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PROJEÇÃO DE COLHEITA:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${String.format(Locale.US, "%.1f", rawYieldKg)} kg de Mel",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = HoneyGoldPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Volume Líquido:", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "≈ ${String.format(Locale.US, "%.1f", honeyVolumeLiters)} Litros",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$totalFrames quadros totais (${supersCount} melg. x ${framesPerSuper}q)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Receita Est: R$ ${String.format(Locale.US, "%.2f", estimatedRevenue)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MeadowGreenSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onPrefillHarvest(rawYieldKg, supersCount, framesPerSuper, yieldPerFrameKg) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Preencher Colheita com Estes Dados")
            }
        }
    }
}

// -------------------------------------------------------------
// HARVEST ITEM CARD
// -------------------------------------------------------------
@Composable
fun HarvestItemCard(
    harvest: HarvestEntity,
    apiaryName: String,
    hiveNumber: String,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("harvest_item_${harvest.id}"),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(HoneyGoldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (harvest.productType == "Mel") "🍯" else "🌿", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${harvest.quantityKg} kg de ${harvest.productType}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$apiaryName • $hiveNumber",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Supers & frames info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (harvest.supersHarvestedCount != null) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Melgueiras: ${harvest.supersHarvestedCount} (${harvest.framesPerSuper ?: 10}q/melg)", fontSize = 11.sp) }
                    )
                }
                if (harvest.avgYieldPerFrameKg != null) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Média: ${String.format(Locale.US, "%.2f", harvest.avgYieldPerFrameKg)} kg/quadro", fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Batch, Floral Source and Moisture Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌸 Florada: ${harvest.floralSource}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "📅 ${dateFormat.format(Date(harvest.date))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (harvest.moisturePercent != null) {
                Spacer(modifier = Modifier.height(4.dp))
                val isMoistHigh = harvest.moisturePercent > 19.5
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isMoistHigh) {
                        Icon(Icons.Default.Warning, contentDescription = "Alerta", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Umidade: ${harvest.moisturePercent}% (🚨 Risco de Fermentação >19.5%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "💧 Umidade: ${harvest.moisturePercent}% (✅ Padrão Conforme MAPA <20%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MeadowGreenSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (harvest.batchNumber.isNotBlank()) {
                Text(
                    text = "🔖 Lote: ${harvest.batchNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOG: ADICIONAR REGISTRO DE COLHEITA
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHarvestDialog(
    apiaries: List<ApiaryEntity>,
    hives: List<HiveEntity>,
    initialQuantity: Double? = null,
    initialSupers: Int = 1,
    initialFramesPerSuper: Int = 10,
    initialYieldPerFrame: Double = 2.2,
    onDismiss: () -> Unit,
    onSave: (
        apiaryId: Long,
        hiveId: Long?,
        productType: String,
        quantityKg: Double,
        moisturePercent: Double?,
        floralSource: String,
        supersHarvestedCount: Int?,
        framesHarvestedCount: Int?,
        framesPerSuper: Int?,
        avgYieldPerFrameKg: Double?,
        notes: String
    ) -> Unit
) {
    var selectedApiaryId by remember { mutableStateOf(apiaries.firstOrNull()?.id ?: 0L) }
    var selectedHiveId by remember { mutableStateOf<Long?>(null) }
    var productType by remember { mutableStateOf("Mel") }
    var quantityText by remember { mutableStateOf(initialQuantity?.let { String.format(Locale.US, "%.1f", it) } ?: "") }
    var moistureText by remember { mutableStateOf("") }
    var floralSource by remember { mutableStateOf("") }

    var supersText by remember { mutableStateOf(initialSupers.toString()) }
    var framesPerSuperText by remember { mutableStateOf(initialFramesPerSuper.toString()) }
    var notes by remember { mutableStateOf("") }

    var expApiary by remember { mutableStateOf(false) }
    var expHive by remember { mutableStateOf(false) }
    var expProduct by remember { mutableStateOf(false) }

    val availableHives = hives.filter { it.apiaryId == selectedApiaryId }

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
                            text = "🍯 Nova Colheita",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Button(
                            onClick = {
                                val qty = quantityText.toDoubleOrNull() ?: 0.0
                                val moist = moistureText.toDoubleOrNull()
                                val supers = supersText.toIntOrNull() ?: 1
                                val framesPerSup = framesPerSuperText.toIntOrNull() ?: 10
                                val totalFrames = supers * framesPerSup
                                val avgPerFrame = if (totalFrames > 0) qty / totalFrames else null

                                if (selectedApiaryId > 0 && qty > 0) {
                                    onSave(selectedApiaryId, selectedHiveId, productType, qty, moist, floralSource, supers, totalFrames, framesPerSup, avgPerFrame, notes)
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
                                .testTag("harvest_dialog_close_btn")
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
                    // Apiary Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expApiary,
                        onExpandedChange = { expApiary = it }
                    ) {
                        val currentApiaryName = apiaries.find { it.id == selectedApiaryId }?.name ?: "Selecione"
                        OutlinedTextField(
                            value = currentApiaryName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Apiário de Origem") },
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
                                        selectedHiveId = null
                                        expApiary = false
                                    }
                                )
                            }
                        }
                    }

                    // Hive Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expHive,
                        onExpandedChange = { expHive = it }
                    ) {
                        val currentHiveText = hives.find { it.id == selectedHiveId }?.let { "${it.hiveNumber} (${it.beeSpecies})" } ?: "Múltiplas Colmeias / Lote Geral"
                        OutlinedTextField(
                            value = currentHiveText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Colmeia Específica") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expHive) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expHive,
                            onDismissRequest = { expHive = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Múltiplas Colmeias / Lote Geral") },
                                onClick = {
                                    selectedHiveId = null
                                    expHive = false
                                }
                            )
                            availableHives.forEach { h ->
                                DropdownMenuItem(
                                    text = { Text("${h.hiveNumber} - ${h.beeSpecies} (${h.hiveType})") },
                                    onClick = {
                                        selectedHiveId = h.id
                                        framesPerSuperText = h.framesPerSuper.toString()
                                        expHive = false
                                    }
                                )
                            }
                        }
                    }

                    // Product Type
                    ExposedDropdownMenuBox(
                        expanded = expProduct,
                        onExpandedChange = { expProduct = it }
                    ) {
                        OutlinedTextField(
                            value = productType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Produto Colhido") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expProduct) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expProduct,
                            onDismissRequest = { expProduct = false }
                        ) {
                            listOf("Mel", "Própolis Verde", "Própolis Marrom", "Cera Alveolada", "Pólen Apícola", "Geleia Real", "Apitoxina").forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p) },
                                    onClick = {
                                        productType = p
                                        expProduct = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantidade Colhida (kg)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Melgueiras & Quadros por melgueira
                    Text("📦 Detalhes das Melgueiras Colhidas:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = HoneyGoldPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = supersText,
                            onValueChange = { supersText = it },
                            label = { Text("Melgueiras") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = framesPerSuperText,
                            onValueChange = { framesPerSuperText = it },
                            label = { Text("Quadros/Melg.") },
                            modifier = Modifier.weight(1f),
                            supportingText = { Text("Ex: 8, 9 ou 10") }
                        )
                    }

                    OutlinedTextField(
                        value = moistureText,
                        onValueChange = { moistureText = it },
                        label = { Text("% Umidade Refratômetro (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("Padrão ideal para Mel: abaixo de 19.5%") }
                    )

                    OutlinedTextField(
                        value = floralSource,
                        onValueChange = { floralSource = it },
                        label = { Text("Origem Floral Predominante") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas de Colheita / Lote") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        }
    }
}
