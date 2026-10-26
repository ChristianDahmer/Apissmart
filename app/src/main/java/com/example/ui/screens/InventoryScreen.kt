package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import android.content.Context
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.local.entities.InventoryEntity
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.WarningAlertRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    inventory: List<InventoryEntity>,
    lowStockItems: List<InventoryEntity>,
    onUpdateQuantity: (InventoryEntity, Int) -> Unit,
    onSaveItem: (name: String, category: String, quantity: Int, unit: String, minThreshold: Int, notes: String, id: Long) -> Unit,
    onDeleteItem: (InventoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("Todos") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<InventoryEntity?>(null) }

    val categories = listOf("Todos", "Madeiramento", "Quadros e Cera", "Alimentação", "Sanidade", "Equipamentos/EPI", "Ferramentas")

    val filteredItems = if (selectedCategory == "Todos") {
        inventory
    } else {
        inventory.filter { it.category.contains(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        modifier = modifier.testTag("inventory_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = HoneyGoldPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("add_inventory_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Item")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "ESTOQUE DE EQUIPAMENTOS E INSUMOS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Low Stock Alert Banner
            if (lowStockItems.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAlertRed, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Atenção: ${lowStockItems.size} itens com estoque baixo!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = lowStockItems.joinToString(", ") { "${it.name} (${it.quantity} ${it.unit})" },
                                    fontSize = 11.sp,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }
            }

            if (filteredItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "Nenhum item encontrado nesta categoria.",
                            modifier = Modifier.padding(20.dp),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    InventoryItemCard(
                        item = item,
                        onIncrement = { onUpdateQuantity(item, 1) },
                        onDecrement = { onUpdateQuantity(item, -1) },
                        onEdit = { editingItem = item },
                        onDelete = { onDeleteItem(item) }
                    )
                }
            }
        }
    }

    if (showAddDialog || editingItem != null) {
        val target = editingItem
        InventoryFormDialog(
            initialItem = target,
            existingItems = inventory,
            onDismiss = {
                showAddDialog = false
                editingItem = null
            },
            onConfirm = { name, cat, qty, unit, min, notes ->
                onSaveItem(name, cat, qty, unit, min, notes, target?.id ?: 0L)
                showAddDialog = false
                editingItem = null
            }
        )
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryEntity,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLowStock = item.quantity <= item.minThreshold

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("inventory_item_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isLowStock) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${item.category} • Mínimo: ${item.minThreshold} ${item.unit}",
                    fontSize = 12.sp,
                    color = if (isLowStock) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Stepper Buttons
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onDecrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Diminuir", modifier = Modifier.size(16.dp))
                }

                Text(
                    text = "${item.quantity} ${item.unit}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(HoneyGoldPrimary)
                        .clickable { onIncrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar", tint = Color.White, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

data class InventoryItemSuggestion(
    val name: String,
    val category: String,
    val defaultUnit: String,
    val defaultMinThreshold: Int
)

val DEFAULT_INVENTORY_SUGGESTIONS = listOf(
    // Madeiramento
    InventoryItemSuggestion("Colmeia Completa Langstroth", "Madeiramento", "un", 3),
    InventoryItemSuggestion("Ninho Padrão Langstroth", "Madeiramento", "un", 5),
    InventoryItemSuggestion("Melgueira Padrão Langstroth", "Madeiramento", "un", 10),
    InventoryItemSuggestion("Fundo com Ventilação", "Madeiramento", "un", 5),
    InventoryItemSuggestion("Tampa Revestida Galvanizada", "Madeiramento", "un", 5),
    InventoryItemSuggestion("Tela Excluidora de Rainha", "Madeiramento", "un", 10),
    InventoryItemSuggestion("Alvado Móvel / Redutor", "Madeiramento", "un", 8),
    InventoryItemSuggestion("Escape Invertido para Melgueira", "Madeiramento", "un", 5),

    // Quadros e Cera
    InventoryItemSuggestion("Quadros de Ninho Aramados", "Quadros e Cera", "un", 20),
    InventoryItemSuggestion("Quadros de Melgueira com Ilhós", "Quadros e Cera", "un", 30),
    InventoryItemSuggestion("Cera Alveolada 100% Pura", "Quadros e Cera", "kg", 5),
    InventoryItemSuggestion("Carretel Arame Inox 0.45mm", "Quadros e Cera", "un", 2),
    InventoryItemSuggestion("Ilhoses de Latão para Quadros", "Quadros e Cera", "cento", 5),
    InventoryItemSuggestion("Espaçador Metálico de Quadros", "Quadros e Cera", "un", 15),

    // Alimentação
    InventoryItemSuggestion("Açúcar Cristal para Xarope", "Alimentação", "kg", 25),
    InventoryItemSuggestion("Pasta / Bife Proteico com Pólen", "Alimentação", "kg", 5),
    InventoryItemSuggestion("Promotor de Crescimento Líquido", "Alimentação", "L", 2),
    InventoryItemSuggestion("Xarope Energético Invertido 2:1", "Alimentação", "L", 10),
    InventoryItemSuggestion("Alimentador de Cocho Interno", "Alimentação", "un", 6),
    InventoryItemSuggestion("Alimentador tipo Boardman Externo", "Alimentação", "un", 6),

    // Sanidade
    InventoryItemSuggestion("Ácido Oxálico Dihidratado 99%", "Sanidade", "kg", 2),
    InventoryItemSuggestion("Tiras de Ácido Oxálico e Glicerina", "Sanidade", "un", 20),
    InventoryItemSuggestion("Timol em Cristais Puro", "Sanidade", "g", 250),
    InventoryItemSuggestion("Tiras Apivar contra Varroa", "Sanidade", "un", 10),
    InventoryItemSuggestion("Álcool 70% Desinfetante", "Sanidade", "L", 5),
    InventoryItemSuggestion("Glicerina Vegetal Pura", "Sanidade", "L", 2),

    // Equipamentos/EPI
    InventoryItemSuggestion("Macacão Apícola Antiferroada", "Equipamentos/EPI", "un", 2),
    InventoryItemSuggestion("Luvas de Couro Punho Longo", "Equipamentos/EPI", "par", 3),
    InventoryItemSuggestion("Fumigador Inox Grande c/ Fole", "Equipamentos/EPI", "un", 2),
    InventoryItemSuggestion("Espátula Inox Ponta Reta e Curva", "Equipamentos/EPI", "un", 3),
    InventoryItemSuggestion("Formão de Apicultor", "Equipamentos/EPI", "un", 2),
    InventoryItemSuggestion("Escovão Apícola Cerdas Macias", "Equipamentos/EPI", "un", 3),

    // Ferramentas
    InventoryItemSuggestion("Incrustador de Cera Elétrico", "Ferramentas", "un", 1),
    InventoryItemSuggestion("Garfo Desoperculador Inox", "Ferramentas", "un", 2),
    InventoryItemSuggestion("Gaiola para Introdução de Rainha", "Ferramentas", "un", 5),
    InventoryItemSuggestion("Marcador de Rainha (Caneta Posca)", "Ferramentas", "un", 2),
    InventoryItemSuggestion("Faca Desoperculadora Serrilhada", "Ferramentas", "un", 2),

    // Embalagens & Envase
    InventoryItemSuggestion("Pote de Vidro Sextavado 500g", "Embalagens & Envase", "un", 50),
    InventoryItemSuggestion("Pote de Vidro Cilíndrico 1kg", "Embalagens & Envase", "un", 50),
    InventoryItemSuggestion("Bisnaga Plástica Invertida 280g", "Embalagens & Envase", "un", 40),
    InventoryItemSuggestion("Balde Apícola Atóxico 20L", "Embalagens & Envase", "un", 5),
    InventoryItemSuggestion("Lacre Termoencolhível de Segurança", "Embalagens & Envase", "cento", 3)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryFormDialog(
    initialItem: InventoryEntity?,
    existingItems: List<InventoryEntity> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, quantity: Int, unit: String, minThreshold: Int, notes: String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("apis_inventory_categories", Context.MODE_PRIVATE) }
    var customCategories by remember {
        mutableStateOf(
            prefs.getStringSet("custom_categories", emptySet())?.toList() ?: emptyList()
        )
    }

    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: "Madeiramento") }
    var quantityStr by remember { mutableStateOf(initialItem?.quantity?.toString() ?: "10") }
    var unit by remember { mutableStateOf(initialItem?.unit ?: "un") }
    var minStr by remember { mutableStateOf(initialItem?.minThreshold?.toString() ?: "4") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }

    var expandedCategoryMenu by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }

    val defaultCategories = listOf(
        "Madeiramento",
        "Quadros e Cera",
        "Alimentação",
        "Sanidade",
        "Equipamentos/EPI",
        "Ferramentas",
        "Embalagens & Envase"
    )
    val allCategories = remember(customCategories, existingItems) {
        val fromItems = existingItems.map { it.category }.filter { it.isNotBlank() }
        (defaultCategories + customCategories + fromItems).distinct()
    }

    // Suggestions matching what's typed or currently selected category
    val availableSuggestions = remember(name, category, existingItems) {
        val itemsFromExisting = existingItems.map {
            InventoryItemSuggestion(it.name, it.category, it.unit, it.minThreshold)
        }
        val combined = (DEFAULT_INVENTORY_SUGGESTIONS + itemsFromExisting).distinctBy { it.name.lowercase().trim() }
        if (name.isBlank()) {
            val forCategory = combined.filter { it.category.equals(category, ignoreCase = true) }
            if (forCategory.isNotEmpty()) forCategory else combined.take(8)
        } else {
            combined.filter { it.name.contains(name, ignoreCase = true) }
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
                            text = if (initialItem == null) "Novo Item de Estoque" else "Editar Item",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Button(
                            onClick = {
                                val q = quantityStr.toIntOrNull() ?: 0
                                val min = minStr.toIntOrNull() ?: 0
                                if (name.isNotBlank()) {
                                    onConfirm(name.trim(), category.trim(), q, unit.trim(), min, notes.trim())
                                }
                            },
                            enabled = name.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                        ) {
                            Text("Salvar")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), CircleShape)
                                .testTag("inventory_dialog_close_btn")
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
                    // Categoria como Combo Dropdown
                    Text("Categoria do Insumo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(
                        expanded = expandedCategoryMenu,
                        onExpandedChange = { expandedCategoryMenu = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inventory_category_combo_box")
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecione a Categoria") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryMenu)
                            },
                            leadingIcon = {
                                Text(
                                    text = when {
                                        category.contains("Madeiramento", ignoreCase = true) -> "🪵"
                                        category.contains("Quadros", ignoreCase = true) || category.contains("Cera", ignoreCase = true) -> "🕯️"
                                        category.contains("Alimentação", ignoreCase = true) || category.contains("Nutrição", ignoreCase = true) -> "🍯"
                                        category.contains("Sanidade", ignoreCase = true) || category.contains("Varroa", ignoreCase = true) -> "💊"
                                        category.contains("EPI", ignoreCase = true) || category.contains("Equipamento", ignoreCase = true) -> "🦺"
                                        category.contains("Ferramenta", ignoreCase = true) -> "🔧"
                                        category.contains("Embalagem", ignoreCase = true) || category.contains("Envase", ignoreCase = true) -> "📦"
                                        else -> "📦"
                                    },
                                    fontSize = 16.sp
                                )
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("inventory_category_combo")
                        )

                        ExposedDropdownMenu(
                            expanded = expandedCategoryMenu,
                            onDismissRequest = { expandedCategoryMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = HoneyGoldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "➕ Cadastrar nova categoria...",
                                            fontWeight = FontWeight.Bold,
                                            color = HoneyGoldPrimary
                                        )
                                    }
                                },
                                onClick = {
                                    expandedCategoryMenu = false
                                    showNewCategoryDialog = true
                                },
                                modifier = Modifier.testTag("combo_add_new_category_item")
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            allCategories.forEach { cat ->
                                val isCustom = customCategories.contains(cat)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = cat,
                                                fontWeight = if (cat == category) FontWeight.Bold else FontWeight.Normal,
                                                color = if (cat == category) HoneyGoldPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isCustom) {
                                                Text(
                                                    text = "Personalizado",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    leadingIcon = {
                                        Text(
                                            text = when {
                                                cat.contains("Madeiramento", ignoreCase = true) -> "🪵"
                                                cat.contains("Quadros", ignoreCase = true) || cat.contains("Cera", ignoreCase = true) -> "🕯️"
                                                cat.contains("Alimentação", ignoreCase = true) || cat.contains("Nutrição", ignoreCase = true) -> "🍯"
                                                cat.contains("Sanidade", ignoreCase = true) || cat.contains("Varroa", ignoreCase = true) -> "💊"
                                                cat.contains("EPI", ignoreCase = true) || cat.contains("Equipamento", ignoreCase = true) -> "🦺"
                                                cat.contains("Ferramenta", ignoreCase = true) -> "🔧"
                                                cat.contains("Embalagem", ignoreCase = true) || cat.contains("Envase", ignoreCase = true) -> "📦"
                                                else -> "📦"
                                            },
                                            fontSize = 16.sp
                                        )
                                    },
                                    onClick = {
                                        category = cat
                                        expandedCategoryMenu = false
                                    },
                                    modifier = Modifier.testTag("category_item_${cat.replace(" ", "_")}")
                                )
                            }
                        }
                    }

                    // Nome do Item com Sugestões de Preenchimento
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Item / Insumo") },
                        placeholder = { Text("Ex: Melgueira Padrão, Tiras Ácido Oxálico") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inventory_item_name_input"),
                        trailingIcon = {
                            if (name.isNotBlank()) {
                                IconButton(onClick = { name = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpar nome",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    )

                    // Barra de Sugestões de Preenchimento Rápidas
                    if (availableSuggestions.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💡 Sugestões de Insumos ($category):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HoneyGoldPrimary
                                )
                                Text(
                                    text = "Toque para aplicar",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(availableSuggestions.take(10)) { suggestion ->
                                    SuggestionChip(
                                        onClick = {
                                            name = suggestion.name
                                            unit = suggestion.defaultUnit
                                            minStr = suggestion.defaultMinThreshold.toString()
                                            if (category != suggestion.category) {
                                                category = suggestion.category
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = suggestion.name,
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        ),
                                        modifier = Modifier.testTag("inventory_suggestion_${suggestion.name.hashCode()}")
                                    )
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("Quantidade") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unidade (un, kg, L)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = minStr,
                        onValueChange = { minStr = it },
                        label = { Text("Estoque Mínimo de Alerta") },
                        modifier = Modifier.fillMaxWidth()
                    )
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

    // Modal para cadastrar nova categoria
    if (showNewCategoryDialog) {
        AlertDialog(
            onDismissRequest = {
                showNewCategoryDialog = false
                newCategoryInput = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("➕", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nova Categoria", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Digite o nome da nova categoria para incluir no estoque:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = newCategoryInput,
                        onValueChange = { newCategoryInput = it },
                        label = { Text("Nome da Categoria") },
                        placeholder = { Text("Ex: Medicamentos Veterinários") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_custom_category_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newCategoryInput.trim()
                        if (trimmed.isNotBlank()) {
                            val updated = (customCategories + trimmed).distinct()
                            customCategories = updated
                            prefs.edit().putStringSet("custom_categories", updated.toSet()).apply()
                            category = trimmed
                            newCategoryInput = ""
                            showNewCategoryDialog = false
                        }
                    },
                    enabled = newCategoryInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                    modifier = Modifier.testTag("confirm_new_category_btn")
                ) {
                    Text("Cadastrar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNewCategoryDialog = false
                        newCategoryInput = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
