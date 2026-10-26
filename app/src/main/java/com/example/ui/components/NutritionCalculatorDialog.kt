package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Science
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.ApiaryEntity
import java.util.Locale
import kotlin.math.roundToInt

enum class NutritionType {
    SYRUP_1_1,      // Incentivo (1 açúcar : 1 água)
    SYRUP_2_1,      // Manutenção (2 açúcar : 1 água)
    PROTEIN_PATTY,  // Bife Proteico
    OXALIC_ACID     // Tratamento Varroa
}

private data class RecipeCalculation(
    val sugarKg: Int,
    val waterLiters: Int,
    val yeastKg: Int,
    val soyKg: Int,
    val oxalicGrams: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionCalculatorDialog(
    apiaries: List<ApiaryEntity>,
    onDismiss: () -> Unit,
    onDeductStock: (sugarKg: Int, yeastKg: Int, soyKg: Int) -> Unit,
    onScheduleTask: (title: String, desc: String, apiaryId: Long?, apiaryName: String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(NutritionType.SYRUP_1_1) }
    var hiveCount by remember { mutableFloatStateOf(10f) }
    var doseMlPerHive by remember { mutableFloatStateOf(500f) }
    var selectedApiary by remember { mutableStateOf(apiaries.firstOrNull()) }

    val hives = hiveCount.roundToInt()
    val doseMl = doseMlPerHive.roundToInt()
    val totalVolumeLiters = (hives * doseMl) / 1000.0

    // Ingredient calculations
    val (sugarKg, waterLiters, yeastKg, soyKg, oxalicGrams) = when (selectedType) {
        NutritionType.SYRUP_1_1 -> {
            val sugar = (totalVolumeLiters * 0.65).roundToInt().coerceAtLeast(1)
            val water = (totalVolumeLiters * 0.65).roundToInt().coerceAtLeast(1)
            RecipeCalculation(sugar, water, 0, 0, 0)
        }
        NutritionType.SYRUP_2_1 -> {
            val sugar = (totalVolumeLiters * 0.90).roundToInt().coerceAtLeast(1)
            val water = (totalVolumeLiters * 0.45).roundToInt().coerceAtLeast(1)
            RecipeCalculation(sugar, water, 0, 0, 0)
        }
        NutritionType.PROTEIN_PATTY -> {
            val totalPattyGrams = hives * 150
            val yeast = (totalPattyGrams * 0.35 / 1000.0).roundToInt().coerceAtLeast(1)
            val soy = (totalPattyGrams * 0.35 / 1000.0).roundToInt().coerceAtLeast(1)
            val sugar = (totalPattyGrams * 0.20 / 1000.0).roundToInt().coerceAtLeast(1)
            RecipeCalculation(sugar, 1, yeast, soy, 0)
        }
        NutritionType.OXALIC_ACID -> {
            val sugar = (totalVolumeLiters * 0.65).roundToInt().coerceAtLeast(1)
            val water = (totalVolumeLiters * 0.65).roundToInt().coerceAtLeast(1)
            val oxalic = (totalVolumeLiters * 35).roundToInt()
            RecipeCalculation(sugar, water, 0, 0, oxalic)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().testTag("nutrition_calculator_dialog"),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Calculadora de Nutrição",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), androidx.compose.foundation.shape.CircleShape)
                                .testTag("nutrition_dialog_close_btn")
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
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                Spacer(modifier = Modifier.height(14.dp))

                // Recipe Type Selector
                Text(
                    text = "Tipo de Formulação:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedType == NutritionType.SYRUP_1_1,
                        onClick = { selectedType = NutritionType.SYRUP_1_1 },
                        label = { Text("Incentivo 1:1") }
                    )
                    FilterChip(
                        selected = selectedType == NutritionType.SYRUP_2_1,
                        onClick = { selectedType = NutritionType.SYRUP_2_1 },
                        label = { Text("Inverno 2:1") }
                    )
                    FilterChip(
                        selected = selectedType == NutritionType.PROTEIN_PATTY,
                        onClick = { selectedType = NutritionType.PROTEIN_PATTY },
                        label = { Text("Bife Proteico") }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hive Count Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Número de Colmeias:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "$hives colmeias",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = hiveCount,
                    onValueChange = { hiveCount = it },
                    valueRange = 1f..100f,
                    steps = 98,
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedType != NutritionType.PROTEIN_PATTY) {
                    // Dosage Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Dose por Colmeia:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${doseMl} mL",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = doseMlPerHive,
                        onValueChange = { doseMlPerHive = it },
                        valueRange = 200f..2000f,
                        steps = 17,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Recipe Result Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "📋 Proporções Calculadas:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (selectedType == NutritionType.SYRUP_1_1 || selectedType == NutritionType.SYRUP_2_1) {
                            Text(text = "• Açúcar Cristal: $sugarKg kg", fontWeight = FontWeight.SemiBold)
                            Text(text = "• Água Filtrada / Morna: $waterLiters Litros", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "• Rendimento Total: ${String.format(Locale.US, "%.1f", totalVolumeLiters)} Litros de Xarope",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (selectedType == NutritionType.PROTEIN_PATTY) {
                            Text(text = "• Levedura de Cerveja Inativa: $yeastKg kg", fontWeight = FontWeight.SemiBold)
                            Text(text = "• Farinha de Soja Desengordurada: $soyKg kg", fontWeight = FontWeight.SemiBold)
                            Text(text = "• Açúcar / Xarope ligante: $sugarKg kg", fontWeight = FontWeight.SemiBold)
                            Text(text = "• Rendimento: $hives bifes de 150g sobre os quadros", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text(text = "• Açúcar: $sugarKg kg e Água: $waterLiters L")
                            Text(text = "• Ácido Oxálico Di-hidratado: $oxalicGrams gramas", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (selectedType) {
                                NutritionType.SYRUP_1_1 -> "💡 Dica: Fornecer no final da tarde em alimentadores para estimular postura sem provocar pilhagem."
                                NutritionType.SYRUP_2_1 -> "💡 Dica: Xarope denso para reserva rápida de inverno/entressafra. Não ferver o açúcar para evitar HMF."
                                NutritionType.PROTEIN_PATTY -> "💡 Dica: Envolver a massa em papel manteiga com fenda para manter a umidade sobre o ninho."
                                NutritionType.OXALIC_ACID -> "⚠️ Dica: Uso estritamente no gotejamento outonal na ausência de melgueiras."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Button(
                    onClick = {
                        onDeductStock(sugarKg, yeastKg, soyKg)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Inventory2, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Deduzir Insumos do Estoque")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val title = when (selectedType) {
                            NutritionType.SYRUP_1_1 -> "Fornecer Xarope de Incentivo (1:1)"
                            NutritionType.SYRUP_2_1 -> "Alimentação de Manutenção (2:1)"
                            NutritionType.PROTEIN_PATTY -> "Aplicação de Bife Proteico"
                            NutritionType.OXALIC_ACID -> "Tratamento com Ácido Oxálico"
                        }
                        val desc = "Dose calculada para $hives colmeias ($sugarKg kg açúcar, etc.)"
                        onScheduleTask(title, desc, selectedApiary?.id, selectedApiary?.name)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agendar Manejo na Agenda")
                }
            }
        }
    }
}
}
