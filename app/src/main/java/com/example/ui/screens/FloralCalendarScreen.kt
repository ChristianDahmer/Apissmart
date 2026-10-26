package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.FloralObservationEntity
import com.example.ui.viewmodel.ApisSmartViewModel
import com.example.util.BotanicalBiome
import com.example.util.FloralCalendarEngine
import com.example.util.FloralSpecies
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloralCalendarScreen(viewModel: ApisSmartViewModel) {
    val apiaries by viewModel.apiaries.collectAsState()
    val selectedApiary by viewModel.selectedApiaryForFloralCalendar.collectAsState()
    val observations by viewModel.floralObservations.collectAsState()

    // Default to first apiary if none selected
    val currentApiary = selectedApiary ?: apiaries.firstOrNull()

    // Current calendar month as default (1-12)
    val calendarInstance = Calendar.getInstance()
    var selectedMonth by remember { mutableStateOf(calendarInstance.get(Calendar.MONTH) + 1) }

    // Manual biome override if desired
    var manualBiomeOverride by remember { mutableStateOf<BotanicalBiome?>(null) }

    // Determine biome from apiary lat/long or override
    val activeBiome = manualBiomeOverride ?: run {
        if (currentApiary != null) {
            FloralCalendarEngine.detectBiome(currentApiary.latitude, currentApiary.longitude)
        } else {
            BotanicalBiome.CERRADO
        }
    }

    val seasonalAnalysis = remember(activeBiome, selectedMonth) {
        FloralCalendarEngine.analyzeSeasonalConditions(activeBiome, selectedMonth)
    }

    var showObservationDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(42.dp),
                                    shape = CircleShape,
                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🌸", fontSize = 22.sp)
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Calendário de Floradas",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Inteligência Botânica & Georeferenciada",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Apiary Selector
                        if (apiaries.isNotEmpty()) {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "📍 Apiário de Referência:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        var expandedApiaryMenu by remember { mutableStateOf(false) }
                                        Box(modifier = Modifier.weight(1f)) {
                                            OutlinedButton(
                                                onClick = { expandedApiaryMenu = true },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = currentApiary?.name ?: "Selecionar Apiário",
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = expandedApiaryMenu,
                                                onDismissRequest = { expandedApiaryMenu = false }
                                            ) {
                                                apiaries.forEach { ap ->
                                                    DropdownMenuItem(
                                                        text = { Text("${ap.name} (${ap.location})") },
                                                        onClick = {
                                                            viewModel.selectApiaryForFloralCalendar(ap)
                                                            manualBiomeOverride = null
                                                            expandedApiaryMenu = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Biome Display & Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${activeBiome.emoji} Bioma: ${activeBiome.title}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = activeBiome.subtitle,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            var expandedBiomeMenu by remember { mutableStateOf(false) }
                            Box {
                                TextButton(onClick = { expandedBiomeMenu = true }) {
                                    Text("Alterar Bioma", fontSize = 12.sp)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = expandedBiomeMenu,
                                    onDismissRequest = { expandedBiomeMenu = false }
                                ) {
                                    BotanicalBiome.values().forEach { bio ->
                                        DropdownMenuItem(
                                            text = { Text("${bio.emoji} ${bio.title}") },
                                            onClick = {
                                                manualBiomeOverride = bio
                                                expandedBiomeMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Month Selector Horizontal Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "📅 Selecione o Mês de Análise:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FloralCalendarEngine.MONTH_NAMES.forEachIndexed { index, monthName ->
                            val monthNumber = index + 1
                            val isSelected = monthNumber == selectedMonth
                            val activeSpeciesCount = FloralCalendarEngine.getSpeciesActiveInMonth(activeBiome, monthNumber).size

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedMonth = monthNumber },
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                tonalElevation = if (isSelected) 4.dp else 1.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = FloralCalendarEngine.MONTH_ABBR[index],
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Badge(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "$activeSpeciesCount esp.",
                                            fontSize = 9.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Seasonal Analysis & Recommendations Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            seasonalAnalysis.nectarFlowIndex.contains("Intenso") -> Color(0xFFE8F5E9)
                            seasonalAnalysis.nectarFlowIndex.contains("Escassez") -> Color(0xFFFFEBEE)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        when {
                            seasonalAnalysis.nectarFlowIndex.contains("Intenso") -> Color(0xFF4CAF50)
                            seasonalAnalysis.nectarFlowIndex.contains("Escassez") -> Color(0xFFEF5350)
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📊 Panorama de ${seasonalAnalysis.monthName} (${seasonalAnalysis.seasonName})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Badge(
                                containerColor = when {
                                    seasonalAnalysis.nectarFlowIndex.contains("Intenso") -> Color(0xFF2E7D32)
                                    seasonalAnalysis.nectarFlowIndex.contains("Escassez") -> Color(0xFFC62828)
                                    else -> Color(0xFFF57C00)
                                }
                            ) {
                                Text(
                                    text = seasonalAnalysis.nectarFlowIndex,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Harvest advice
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("🍯", fontSize = 18.sp)
                                Text(
                                    text = "Recomendação de Colheita:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = seasonalAnalysis.harvestAdvice,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Nutrition advice
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("🥣", fontSize = 18.sp)
                                Text(
                                    text = "Orientação de Nutrição & Alimentação:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = seasonalAnalysis.nutritionAdvice,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Recommended Actions
                        if (seasonalAnalysis.recommendedActions.isNotEmpty()) {
                            Text(
                                text = "✅ Ações Recomendadas para o Período:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            seasonalAnalysis.recommendedActions.forEach { action ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Text(text = action, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.createMaintenanceTaskFromTip(
                                        title = "Manejo Sazonal (${seasonalAnalysis.monthName}): ${seasonalAnalysis.managementPhase}",
                                        description = seasonalAnalysis.harvestAdvice,
                                        taskType = "Manejo Sazonal",
                                        daysAhead = 2
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_schedule_seasonal_task"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Agendar Manejo", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Active Species List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🌸 Espécies em Floração (${seasonalAnalysis.activeSpecies.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { showObservationDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Registrar Florada", fontSize = 12.sp)
                    }
                }
            }

            // Species Cards
            if (seasonalAnalysis.activeSpecies.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🍂", fontSize = 32.sp)
                            Text(
                                text = "Nenhuma espécie catalogada em floração principal neste mês para este bioma.",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(seasonalAnalysis.activeSpecies) { species ->
                    FloralSpeciesCard(species = species, currentMonth = selectedMonth)
                }
            }

            // Local Field Observations Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🌿 Observações de Campo Registradas",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Badge { Text("${observations.size}") }
                }
            }

            if (observations.isEmpty()) {
                item {
                    Text(
                        text = "Nenhuma observação botânica local registrada ainda. Clique em 'Registrar Florada' para adicionar plantas observadas no seu apiário.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(observations) { obs ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🌱 ${obs.plantName} (${obs.scientificName})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Recurso: ${obs.resourceType} • Mês: ${obs.startMonth}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (obs.notes.isNotBlank()) {
                                    Text(
                                        text = "Obs: ${obs.notes}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.deleteFloralObservation(obs) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        // Add Observation Dialog
        if (showObservationDialog) {
            var plantName by remember { mutableStateOf("") }
            var scientificName by remember { mutableStateOf("") }
            var resourceType by remember { mutableStateOf("Néctar") }
            var notes by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showObservationDialog = false },
                title = { Text("Registrar Florada no Apiário") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = plantName,
                            onValueChange = { plantName = it },
                            label = { Text("Nome Popular da Planta") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = scientificName,
                            onValueChange = { scientificName = it },
                            label = { Text("Nome Científico (Opcional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = resourceType,
                            onValueChange = { resourceType = it },
                            label = { Text("Recurso (Néctar, Pólen, Resina)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Observações de Campo") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (plantName.isNotBlank()) {
                                viewModel.saveFloralObservation(
                                    apiaryId = currentApiary?.id ?: 0L,
                                    plantName = plantName,
                                    scientificName = scientificName,
                                    resourceType = resourceType,
                                    startMonth = selectedMonth,
                                    notes = notes
                                )
                                showObservationDialog = false
                            }
                        }
                    ) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showObservationDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun FloralSpeciesCard(species: FloralSpecies, currentMonth: Int) {
    val isPeak = currentMonth in species.peakMonths
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isPeak) "🌸" else "🌼",
                        fontSize = 20.sp
                    )
                    Column {
                        Text(
                            text = species.popularName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = species.scientificName,
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Badge(
                    containerColor = if (isPeak) Color(0xFFE91E63) else Color(0xFF2E7D32)
                ) {
                    Text(
                        text = if (isPeak) "Pico" else "Floração",
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }

            // Quick badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Recurso: ${species.resourceType.title}",
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Família: ${species.family}",
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "🍯 Cor do Mel: ${species.honeyColor}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "👅 Sabor / Aroma: ${species.honeyFlavor}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "⚡ Valor Apícola: ${species.apiculturalValue}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "💡 Recomendação: ${species.harvestRecommendation}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
