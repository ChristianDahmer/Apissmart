package com.example.receipt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalPrintshop
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.receipt.ReceiptManager
import com.example.receipt.model.ReceiptLayoutConfig
import com.example.receipt.model.ReceiptData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsHubDialog(
    receipts: List<ReceiptData>,
    layoutConfig: ReceiptLayoutConfig,
    receiptManager: ReceiptManager,
    onDismiss: () -> Unit,
    onOpenIssueReceipt: (ReceiptData?) -> Unit,
    onOpenLayoutEditor: () -> Unit,
    onDeleteReceipt: (String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("TODOS") }

    val filteredReceipts = receipts.filter { r ->
        val matchesQuery = searchQuery.isBlank() ||
                r.clientName.contains(searchQuery, ignoreCase = true) ||
                r.id.contains(searchQuery, ignoreCase = true) ||
                r.notes.contains(searchQuery, ignoreCase = true)

        val matchesStatus = when (statusFilter) {
            "EMITIDO" -> r.status == "EMITIDO"
            "ASSINADO" -> r.status == "ASSINADO"
            "CANCELADO" -> r.status == "CANCELADO"
            else -> true
        }

        matchesQuery && matchesStatus
    }

    val totalAmountAll = receipts.sumOf { it.totalAmount }
    val totalCount = receipts.size
    val averageReceipt = if (totalCount > 0) totalAmountAll / totalCount else 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("receipts_hub_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                // Top App Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = layoutConfig.colorTheme.composePrimary.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = layoutConfig.colorTheme.composePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Módulo de Recibos Apícolas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Emissão, gerenciamento e layout oficial",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_receipts_hub_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // KPI Overview Card
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "TOTAL FATURADO EM RECIBOS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "R$ %,.2f".format(Locale.getDefault(), totalAmountAll),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = layoutConfig.colorTheme.composePrimary
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Recibos emitidos: $totalCount",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Ticket Médio: R$ %,.2f".format(Locale.getDefault(), averageReceipt),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Quick Action Buttons (+ Novo Recibo & Configurar Layout)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onOpenIssueReceipt(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = layoutConfig.colorTheme.composePrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("issue_new_receipt_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Emitir Novo Recibo")
                            }

                            OutlinedButton(
                                onClick = onOpenLayoutEditor,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_layout_editor_btn")
                            ) {
                                Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Editar Layout")
                            }
                        }
                    }

                    // Layout Banner info
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = layoutConfig.colorTheme.composePrimary.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(layoutConfig.iconType.iconEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Modelo Ativo: ${layoutConfig.templateStyle.label}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = layoutConfig.colorTheme.composeDarkAccent
                                        )
                                        Text(
                                            text = "${layoutConfig.businessName} • Cor: ${layoutConfig.colorTheme.label}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                TextButton(onClick = onOpenLayoutEditor) {
                                    Text("Alterar", color = layoutConfig.colorTheme.composePrimary)
                                }
                            }
                        }
                    }

                    // Search & Filters
                    item {
                        Column {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Buscar por cliente ou nº do recibo") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("TODOS", "EMITIDO", "ASSINADO", "CANCELADO").forEach { status ->
                                    FilterChip(
                                        selected = statusFilter == status,
                                        onClick = { statusFilter = status },
                                        label = { Text(status) }
                                    )
                                }
                            }
                        }
                    }

                    // Receipts List
                    if (filteredReceipts.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Nenhum recibo encontrado",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Clique em 'Emitir Novo Recibo' ou emita diretamente de um lançamento financeiro.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredReceipts, key = { it.id }) { receipt ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val dateStr = sdf.format(Date(receipt.dateMillis))

                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenIssueReceipt(receipt) }
                                    .testTag("receipt_item_${receipt.id}"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = layoutConfig.colorTheme.composePrimary.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = receipt.id,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = layoutConfig.colorTheme.composePrimary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = dateStr,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Text(
                                            text = "R$ %,.2f".format(Locale.getDefault(), receipt.totalAmount),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = layoutConfig.colorTheme.composePrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = receipt.clientName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (receipt.items.isNotEmpty()) {
                                        val itemsSummary = receipt.items.joinToString(", ") { "${it.quantity.toInt()}x ${it.description}" }
                                        Text(
                                            text = itemsSummary,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = when (receipt.status) {
                                                "ASSINADO" -> Color(0xFF16A34A).copy(alpha = 0.15f)
                                                "CANCELADO" -> Color.Red.copy(alpha = 0.15f)
                                                else -> layoutConfig.colorTheme.composePrimary.copy(alpha = 0.15f)
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "${receipt.status} • ${receipt.paymentMethod}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (receipt.status) {
                                                    "ASSINADO" -> Color(0xFF16A34A)
                                                    "CANCELADO" -> Color.Red
                                                    else -> layoutConfig.colorTheme.composePrimary
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }

                                        // Action buttons on card
                                        Row {
                                            IconButton(
                                                onClick = { onOpenIssueReceipt(receipt) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Visibility, contentDescription = "Visualizar", modifier = Modifier.size(18.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    receiptManager.sharePdf(receipt, layoutConfig, targetWhatsApp = true)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Text("💬", fontSize = 14.sp)
                                            }

                                            IconButton(
                                                onClick = {
                                                    receiptManager.sharePdf(receipt, layoutConfig, targetWhatsApp = false)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = "Compartilhar PDF", modifier = Modifier.size(18.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    receiptManager.savePdfToDownloads(receipt, layoutConfig)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.LocalPrintshop, contentDescription = "Salvar PDF / Imprimir", modifier = Modifier.size(18.dp))
                                            }

                                            IconButton(
                                                onClick = { onDeleteReceipt(receipt.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Red, modifier = Modifier.size(18.dp))
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
}
