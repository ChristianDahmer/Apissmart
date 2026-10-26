package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.HiveEntity
import com.example.data.local.entities.InventoryEntity
import com.example.data.local.entities.TaskScheduleEntity
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.viewmodel.ScreenTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SearchResultItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val categoryLabel: String,
    val icon: ImageVector,
    val targetTab: ScreenTab
)

@Composable
fun SystemSearchDialog(
    apiaries: List<ApiaryEntity>,
    hives: List<HiveEntity>,
    inventory: List<InventoryEntity>,
    tasks: List<TaskScheduleEntity>,
    onDismiss: () -> Unit,
    onNavigateToTab: (ScreenTab) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    val allResults = remember(searchQuery, apiaries, hives, inventory, tasks) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val query = searchQuery.trim().lowercase()
            val list = mutableListOf<SearchResultItem>()

            // Apiários
            apiaries.filter { it.name.lowercase().contains(query) || it.location.lowercase().contains(query) }
                .forEach {
                    list.add(
                        SearchResultItem(
                            id = "apiary_${it.id}",
                            title = "🌿 Apiário: ${it.name}",
                            subtitle = "Local: ${it.location} • Flora: ${it.predominantFlora}",
                            categoryLabel = "Apiário",
                            icon = Icons.Default.Agriculture,
                            targetTab = ScreenTab.APIARIES
                        )
                    )
                }

            // Colmeias
            hives.filter { it.hiveNumber.lowercase().contains(query) || it.beeSpecies.lowercase().contains(query) || it.hiveType.lowercase().contains(query) }
                .forEach {
                    list.add(
                        SearchResultItem(
                            id = "hive_${it.id}",
                            title = "🐝 Colmeia ${it.hiveNumber}",
                            subtitle = "Espécie: ${it.beeSpecies} • Tipo: ${it.hiveType}",
                            categoryLabel = "Colmeia",
                            icon = Icons.Default.Agriculture,
                            targetTab = ScreenTab.APIARIES
                        )
                    )
                }

            // Estoque / Insumos
            inventory.filter { it.name.lowercase().contains(query) || it.category.lowercase().contains(query) }
                .forEach {
                    list.add(
                        SearchResultItem(
                            id = "inv_${it.id}",
                            title = "📦 Estoque: ${it.name}",
                            subtitle = "Categoria: ${it.category} • Quantidade: ${it.quantity} ${it.unit}",
                            categoryLabel = "Estoque",
                            icon = Icons.Default.Inventory2,
                            targetTab = ScreenTab.INVENTORY
                        )
                    )
                }

            // Tarefas / Manejos
            tasks.filter { it.title.lowercase().contains(query) || it.description.lowercase().contains(query) }
                .forEach {
                    val dateStr = dateFormat.format(Date(it.dueDate))
                    list.add(
                        SearchResultItem(
                            id = "task_${it.id}",
                            title = "📅 Manejo: ${it.title}",
                            subtitle = "Data: $dateStr • Prioridade: ${it.priority}",
                            categoryLabel = "Manejos",
                            icon = Icons.Default.CalendarMonth,
                            targetTab = ScreenTab.SCHEDULE
                        )
                    )
                }

            list
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().testTag("system_search_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = HoneyGoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Busca Geral ApisSmart",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_search_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Campo de Texto de Busca
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Digite para buscar apiários, colmeias, estoque...", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("search_query_input"),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = HoneyGoldPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpar")
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Lista de Resultados
                if (searchQuery.isBlank()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔍 Digite o nome de um apiário, número de colmeia, produto de estoque ou tarefa para buscar em todo o sistema.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else if (allResults.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum resultado encontrado para \"$searchQuery\"",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allResults, key = { it.id }) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDismiss()
                                        onNavigateToTab(item.targetTab)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(HoneyGoldPrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(item.icon, contentDescription = null, tint = HoneyGoldPrimary, modifier = Modifier.size(20.dp))
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(item.subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
