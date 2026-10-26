package com.example.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.FinancialTransactionEntity
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.WarningAlertRed
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.ui.platform.LocalContext
import com.example.receipt.ReceiptManager
import com.example.receipt.model.ReceiptData
import com.example.receipt.model.ReceiptLayoutConfig
import com.example.receipt.ui.IssueReceiptDialog
import com.example.receipt.ui.ReceiptLayoutEditorDialog
import com.example.receipt.ui.ReceiptsHubDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialScreen(
    transactions: List<FinancialTransactionEntity>,
    apiaries: List<ApiaryEntity>,
    totalHivesCount: Int,
    onAddTransaction: (
        type: String,
        category: String,
        amount: Double,
        dateMillis: Long,
        description: String,
        apiaryId: Long?,
        paymentStatus: String,
        paymentMethod: String,
        quantityKgOrUnits: Double?
    ) -> Unit,
    onUpdateTransaction: (FinancialTransactionEntity) -> Unit,
    onTogglePaymentStatus: (FinancialTransactionEntity) -> Unit,
    onDeleteTransaction: (FinancialTransactionEntity) -> Unit,
    receiptManager: ReceiptManager? = null,
    receiptLayoutConfig: ReceiptLayoutConfig = ReceiptLayoutConfig(),
    receiptsList: List<ReceiptData> = emptyList(),
    onSaveReceiptLayout: (ReceiptLayoutConfig) -> Unit = {},
    onResetReceiptLayout: () -> Unit = {},
    onSaveReceipt: (ReceiptData) -> Unit = {},
    onDeleteReceipt: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTypeFilter by remember { mutableStateOf("TODOS") } // "TODOS", "RECEITA", "DESPESA"
    var selectedApiaryFilterId by remember { mutableStateOf<Long?>(null) }
    var selectedStatusFilter by remember { mutableStateOf("TODOS") } // "TODOS", "PAGO", "PENDENTE"

    var showAddDialog by remember { mutableStateOf(false) }
    var initialDialogType by remember { mutableStateOf("RECEITA") }
    var transactionToEdit by remember { mutableStateOf<FinancialTransactionEntity?>(null) }

    val context = LocalContext.current
    val localReceiptManager = remember(context, receiptManager) { receiptManager ?: ReceiptManager(context) }

    var showReceiptLayoutEditor by remember { mutableStateOf(false) }
    var showReceiptsHub by remember { mutableStateOf(false) }
    var showIssueReceiptDialog by remember { mutableStateOf(false) }
    var selectedReceiptToIssue by remember { mutableStateOf<ReceiptData?>(null) }

    // Computations
    val filteredTransactions = transactions.filter { t ->
        val matchType = when (selectedTypeFilter) {
            "RECEITA" -> t.type == "RECEITA"
            "DESPESA" -> t.type == "DESPESA"
            else -> true
        }
        val matchApiary = selectedApiaryFilterId == null || t.apiaryId == selectedApiaryFilterId
        val matchStatus = when (selectedStatusFilter) {
            "PAGO" -> t.paymentStatus == "PAGO"
            "PENDENTE" -> t.paymentStatus == "PENDENTE"
            else -> true
        }
        matchType && matchApiary && matchStatus
    }

    val totalPaidIncomes = transactions.filter { it.type == "RECEITA" && it.paymentStatus == "PAGO" }.sumOf { it.amount }
    val totalPendingIncomes = transactions.filter { it.type == "RECEITA" && it.paymentStatus == "PENDENTE" }.sumOf { it.amount }
    
    val totalPaidExpenses = transactions.filter { it.type == "DESPESA" && it.paymentStatus == "PAGO" }.sumOf { it.amount }
    val totalPendingExpenses = transactions.filter { it.type == "DESPESA" && it.paymentStatus == "PENDENTE" }.sumOf { it.amount }

    val netCashflow = totalPaidIncomes - totalPaidExpenses
    val totalIncomesAll = totalPaidIncomes + totalPendingIncomes
    val totalExpensesAll = totalPaidExpenses + totalPendingExpenses
    val profitMarginPercent = if (totalIncomesAll > 0) ((totalIncomesAll - totalExpensesAll) / totalIncomesAll) * 100 else 0.0
    val costPerHive = if (totalHivesCount > 0) totalExpensesAll / totalHivesCount else 0.0

    Box(modifier = modifier.fillMaxSize().testTag("financial_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header Title Card
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = HoneyGoldPrimary.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.PointOfSale,
                                            contentDescription = null,
                                            tint = HoneyGoldPrimary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Gestão Financeira do Apiário",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Fluxo de Caixa & Custos da Atividade",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Saldo Atual Highlight
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (netCashflow >= 0) MeadowGreenSecondary.copy(alpha = 0.15f) else WarningAlertRed.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "SALDO EM CAIXA (RECEBIDO - PAGO)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "R$ %,.2f".format(Locale.getDefault(), netCashflow),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netCashflow >= 0) MeadowGreenSecondary else WarningAlertRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Cards Receitas vs Despesas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Receitas Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.TrendingUp,
                                            contentDescription = null,
                                            tint = MeadowGreenSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Receitas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "R$ %,.2f".format(Locale.getDefault(), totalPaidIncomes),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MeadowGreenSecondary
                                    )
                                    if (totalPendingIncomes > 0) {
                                        Text(
                                            text = "+ R$ %,.2f pendente".format(Locale.getDefault(), totalPendingIncomes),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Despesas Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.TrendingDown,
                                            contentDescription = null,
                                            tint = WarningAlertRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Despesas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "R$ %,.2f".format(Locale.getDefault(), totalPaidExpenses),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningAlertRed
                                    )
                                    if (totalPendingExpenses > 0) {
                                        Text(
                                            text = "+ R$ %,.2f a pagar".format(Locale.getDefault(), totalPendingExpenses),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Indicator & Key Metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Margem de Lucro: %.1f%%".format(Locale.US, profitMarginPercent),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (totalHivesCount > 0) {
                                Text(
                                    text = "Custo/Enxame: R$ %,.2f".format(Locale.getDefault(), costPerHive),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Banner Módulo de Recibos com Edição de Layout
                        Surface(
                            color = HoneyGoldPrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HoneyGoldPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showReceiptsHub = true }
                                .testTag("receipts_module_header_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = HoneyGoldPrimary.copy(alpha = 0.2f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(receiptLayoutConfig.iconType.iconEmoji, fontSize = 20.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Módulo de Recibos & Layout",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = HoneyGoldPrimary,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = receiptLayoutConfig.templateStyle.label,
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${receiptsList.size} recibo(s) • Toque p/ gerenciar ou editar layout",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { showReceiptLayoutEditor = true },
                                        modifier = Modifier.size(34.dp).testTag("edit_receipt_layout_top_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Brush,
                                            contentDescription = "Editar Layout do Recibo",
                                            tint = HoneyGoldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Action Buttons (+ Receita / + Despesa / Recibos)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            initialDialogType = "RECEITA"
                            transactionToEdit = null
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MeadowGreenSecondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("add_income_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Receita", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            initialDialogType = "DESPESA"
                            transactionToEdit = null
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarningAlertRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("add_expense_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Despesa", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showReceiptsHub = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("receipts_hub_quick_btn")
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp), tint = HoneyGoldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Recibos", fontSize = 12.sp, color = HoneyGoldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Filters Section
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Filtros & Lançamentos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Type Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedTypeFilter == "TODOS",
                                onClick = { selectedTypeFilter = "TODOS" },
                                label = { Text("Todos") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedTypeFilter == "RECEITA",
                                onClick = { selectedTypeFilter = "RECEITA" },
                                label = { Text("Receitas (+)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MeadowGreenSecondary.copy(alpha = 0.2f),
                                    selectedLabelColor = MeadowGreenSecondary
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedTypeFilter == "DESPESA",
                                onClick = { selectedTypeFilter = "DESPESA" },
                                label = { Text("Despesas (-)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WarningAlertRed.copy(alpha = 0.2f),
                                    selectedLabelColor = WarningAlertRed
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedStatusFilter == "PENDENTE",
                                onClick = {
                                    selectedStatusFilter = if (selectedStatusFilter == "PENDENTE") "TODOS" else "PENDENTE"
                                },
                                label = { Text("Pendentes ⏳") }
                            )
                        }
                    }

                    // Apiary Filter if multiple apiaries exist
                    if (apiaries.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedApiaryFilterId == null,
                                    onClick = { selectedApiaryFilterId = null },
                                    label = { Text("Todos Apiários") }
                                )
                            }
                            items(apiaries) { apiary ->
                                FilterChip(
                                    selected = selectedApiaryFilterId == apiary.id,
                                    onClick = { selectedApiaryFilterId = apiary.id },
                                    label = { Text(apiary.name) }
                                )
                            }
                        }
                    }
                }
            }

            // Transaction Items List
            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Nenhum lançamento financeiro encontrado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Registre receitas de vendas de mel e despesas de insumos para acompanhar o caixa.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { item ->
                    val apiaryName = apiaries.find { it.id == item.apiaryId }?.name ?: "Geral / Todos"
                    val isIncome = item.type == "RECEITA"
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(item.dateMillis))

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("financial_item_${item.id}"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (isIncome) MeadowGreenSecondary.copy(alpha = 0.15f) else WarningAlertRed.copy(alpha = 0.15f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isIncome) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                                contentDescription = null,
                                                tint = if (isIncome) MeadowGreenSecondary else WarningAlertRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = item.category,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "$dateStr • $apiaryName",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = "${if (isIncome) "+" else "-"} R$ %,.2f".format(Locale.getDefault(), item.amount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isIncome) MeadowGreenSecondary else WarningAlertRed
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (item.quantityKgOrUnits != null && item.quantityKgOrUnits > 0) {
                                Text(
                                    text = "Qtd: ${item.quantityKgOrUnits} kg / un",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Status badge with toggle button
                                Surface(
                                    color = if (item.paymentStatus == "PAGO") MeadowGreenSecondary.copy(alpha = 0.12f) else HoneyGoldPrimary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { onTogglePaymentStatus(item) }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (item.paymentStatus == "PAGO") Icons.Filled.CheckCircle else Icons.Filled.PendingActions,
                                            contentDescription = null,
                                            tint = if (item.paymentStatus == "PAGO") MeadowGreenSecondary else HoneyGoldPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (item.paymentStatus == "PAGO") "PAGO (${item.paymentMethod})" else "PENDENTE (Clique p/ alterar)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.paymentStatus == "PAGO") MeadowGreenSecondary else HoneyGoldPrimary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val newReceipt = localReceiptManager.createReceiptFromTransaction(
                                                transaction = item,
                                                clientName = if (item.description.isNotBlank() && item.description != item.category) item.description else "Cliente"
                                            )
                                            selectedReceiptToIssue = newReceipt
                                            showIssueReceiptDialog = true
                                        },
                                        modifier = Modifier.size(32.dp).testTag("issue_receipt_transaction_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = "Emitir Recibo",
                                            tint = HoneyGoldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            transactionToEdit = item
                                            initialDialogType = item.type
                                            showAddDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { onDeleteTransaction(item) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = WarningAlertRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for Adding or Editing Transaction
    if (showAddDialog) {
        FinancialTransactionDialog(
            initialType = initialDialogType,
            transactionToEdit = transactionToEdit,
            apiaries = apiaries,
            onDismiss = {
                showAddDialog = false
                transactionToEdit = null
            },
            onSave = { type, category, amount, dateMillis, description, apiaryId, paymentStatus, paymentMethod, quantity ->
                if (transactionToEdit != null) {
                    onUpdateTransaction(
                        transactionToEdit!!.copy(
                            type = type,
                            category = category,
                            amount = amount,
                            dateMillis = dateMillis,
                            description = description,
                            apiaryId = apiaryId,
                            paymentStatus = paymentStatus,
                            paymentMethod = paymentMethod,
                            quantityKgOrUnits = quantity
                        )
                    )
                } else {
                    onAddTransaction(type, category, amount, dateMillis, description, apiaryId, paymentStatus, paymentMethod, quantity)
                }
                showAddDialog = false
                transactionToEdit = null
            }
        )
    }

    // Dialog for Editing Receipt Layout
    if (showReceiptLayoutEditor) {
        ReceiptLayoutEditorDialog(
            initialConfig = receiptLayoutConfig,
            onDismiss = { showReceiptLayoutEditor = false },
            onSaveConfig = { newConfig ->
                onSaveReceiptLayout(newConfig)
                showReceiptLayoutEditor = false
            },
            onResetToDefaults = {
                onResetReceiptLayout()
            }
        )
    }

    // Central / Hub Dialog for Receipts
    if (showReceiptsHub) {
        ReceiptsHubDialog(
            receipts = receiptsList,
            layoutConfig = receiptLayoutConfig,
            receiptManager = localReceiptManager,
            onDismiss = { showReceiptsHub = false },
            onOpenIssueReceipt = { r ->
                selectedReceiptToIssue = r ?: localReceiptManager.createReceiptFromTransaction(
                    transaction = FinancialTransactionEntity(
                        type = "RECEITA",
                        category = "Venda de Mel",
                        amount = 150.0,
                        dateMillis = System.currentTimeMillis(),
                        description = "Venda direta no apiário"
                    )
                )
                showIssueReceiptDialog = true
            },
            onOpenLayoutEditor = {
                showReceiptLayoutEditor = true
            },
            onDeleteReceipt = onDeleteReceipt
        )
    }

    // Dialog for Issuing / Viewing Receipt
    if (showIssueReceiptDialog && selectedReceiptToIssue != null) {
        IssueReceiptDialog(
            initialReceipt = selectedReceiptToIssue!!,
            layoutConfig = receiptLayoutConfig,
            receiptManager = localReceiptManager,
            onDismiss = {
                showIssueReceiptDialog = false
                selectedReceiptToIssue = null
            },
            onSaveReceipt = { r ->
                onSaveReceipt(r)
                showIssueReceiptDialog = false
                selectedReceiptToIssue = null
            },
            onOpenLayoutEditor = {
                showReceiptLayoutEditor = true
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialTransactionDialog(
    initialType: String,
    transactionToEdit: FinancialTransactionEntity?,
    apiaries: List<ApiaryEntity>,
    onDismiss: () -> Unit,
    onSave: (
        type: String,
        category: String,
        amount: Double,
        dateMillis: Long,
        description: String,
        apiaryId: Long?,
        paymentStatus: String,
        paymentMethod: String,
        quantityKgOrUnits: Double?
    ) -> Unit
) {
    var type by remember { mutableStateOf(transactionToEdit?.type ?: initialType) }
    
    val incomeCategories = listOf(
        "Venda de Mel",
        "Venda de Própolis",
        "Venda de Cera",
        "Venda de Pólen",
        "Venda de Enxames/Rainhas",
        "Polinização Agrícola",
        "Outras Receitas"
    )

    val expenseCategories = listOf(
        "Insumos/Alimentação",
        "Equipamentos/Caixas",
        "Tratamento Sanitário",
        "Combustível/Transporte",
        "Embalagens/Rótulos",
        "Análise de Laboratório",
        "Mão de Obra",
        "Outras Despesas"
    )

    val currentCategories = if (type == "RECEITA") incomeCategories else expenseCategories
    var category by remember { mutableStateOf(transactionToEdit?.category ?: currentCategories.first()) }

    var amountText by remember { mutableStateOf(transactionToEdit?.amount?.toString() ?: "") }
    var description by remember { mutableStateOf(transactionToEdit?.description ?: "") }
    var selectedApiaryId by remember { mutableStateOf<Long?>(transactionToEdit?.apiaryId) }
    var paymentStatus by remember { mutableStateOf(transactionToEdit?.paymentStatus ?: "PAGO") }
    var paymentMethod by remember { mutableStateOf(transactionToEdit?.paymentMethod ?: "PIX") }
    var quantityText by remember { mutableStateOf(transactionToEdit?.quantityKgOrUnits?.toString() ?: "") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var apiaryExpanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }

    val paymentMethods = listOf("PIX", "Dinheiro", "Cartão de Crédito", "Transferência", "Boleto")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
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
                            text = if (transactionToEdit != null) "Editar Lançamento" else "Novo Lançamento",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Button(
                            onClick = {
                                val amt = amountText.toDoubleOrNull() ?: 0.0
                                val qty = quantityText.toDoubleOrNull()
                                val desc = description.ifBlank { category }
                                onSave(type, category, amt, System.currentTimeMillis(), desc, selectedApiaryId, paymentStatus, paymentMethod, qty)
                            },
                            enabled = amountText.toDoubleOrNull() != null && amountText.toDoubleOrNull()!! > 0.0,
                            modifier = Modifier.testTag("save_transaction_button")
                        ) {
                            Text("Salvar")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), CircleShape)
                                .testTag("financial_dialog_close_btn")
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

                // Scrollable Form Content
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Type Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = type == "RECEITA",
                            onClick = {
                                type = "RECEITA"
                                category = incomeCategories.first()
                            },
                            label = { Text("Receita (+)") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MeadowGreenSecondary.copy(alpha = 0.2f),
                                selectedLabelColor = MeadowGreenSecondary
                            )
                        )
                        FilterChip(
                            selected = type == "DESPESA",
                            onClick = {
                                type = "DESPESA"
                                category = expenseCategories.first()
                            },
                            label = { Text("Despesa (-)") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WarningAlertRed.copy(alpha = 0.2f),
                                selectedLabelColor = WarningAlertRed
                            )
                        )
                    }

                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoria") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            currentCategories.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        category = item
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Amount R$
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Valor Total (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("transaction_amount_input")
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição / Detalhes") },
                        modifier = Modifier.fillMaxWidth().testTag("transaction_desc_input")
                    )

                    // Quantity (optional)
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantidade (kg / un) - Opcional") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Apiary Selection Dropdown
                    ExposedDropdownMenuBox(
                        expanded = apiaryExpanded,
                        onExpandedChange = { apiaryExpanded = !apiaryExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val currentApiaryName = apiaries.find { it.id == selectedApiaryId }?.name ?: "Geral / Todos os Apiários"
                        OutlinedTextField(
                            value = currentApiaryName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Apiário") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = apiaryExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = apiaryExpanded,
                            onDismissRequest = { apiaryExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Geral / Todos os Apiários") },
                                onClick = {
                                    selectedApiaryId = null
                                    apiaryExpanded = false
                                }
                            )
                            apiaries.forEach { ap ->
                                DropdownMenuItem(
                                    text = { Text(ap.name) },
                                    onClick = {
                                        selectedApiaryId = ap.id
                                        apiaryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Payment Method & Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Method Dropdown
                        ExposedDropdownMenuBox(
                            expanded = methodExpanded,
                            onExpandedChange = { methodExpanded = !methodExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = paymentMethod,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Forma Pagto") },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = methodExpanded,
                                onDismissRequest = { methodExpanded = false }
                            ) {
                                paymentMethods.forEach { pm ->
                                    DropdownMenuItem(
                                        text = { Text(pm) },
                                        onClick = {
                                            paymentMethod = pm
                                            methodExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Status Switch
                        FilterChip(
                            selected = paymentStatus == "PAGO",
                            onClick = { paymentStatus = if (paymentStatus == "PAGO") "PENDENTE" else "PAGO" },
                            label = { Text(if (paymentStatus == "PAGO") "Pago" else "Pendente") },
                            modifier = Modifier.weight(1f).align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }
}
