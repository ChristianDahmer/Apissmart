package com.example.receipt.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalPrintshop
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.license.BrasilApiCnpjService
import com.example.receipt.ReceiptManager
import com.example.receipt.model.ReceiptItem
import com.example.receipt.model.ReceiptLayoutConfig
import com.example.receipt.model.ReceiptData
import com.example.receipt.util.CurrencyToWordsPtBr
import com.example.ui.theme.HoneyGoldPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueReceiptDialog(
    initialReceipt: ReceiptData,
    layoutConfig: ReceiptLayoutConfig,
    receiptManager: ReceiptManager,
    onDismiss: () -> Unit,
    onSaveReceipt: (ReceiptData) -> Unit,
    onOpenLayoutEditor: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isViewMode by remember { mutableStateOf(false) }

    var clientName by remember { mutableStateOf(initialReceipt.clientName) }
    var clientDocument by remember { mutableStateOf(initialReceipt.clientDocument) }
    var clientPhone by remember { mutableStateOf(initialReceipt.clientPhone) }
    var clientAddress by remember { mutableStateOf(initialReceipt.clientAddress) }
    var paymentMethod by remember { mutableStateOf(initialReceipt.paymentMethod) }
    var notes by remember { mutableStateOf(initialReceipt.notes) }

    var isConsultingDoc by remember { mutableStateOf(false) }
    var docConsultationMessage by remember { mutableStateOf<String?>(null) }

    fun performDocumentLookup(docInput: String) {
        val clean = docInput.filter { it.isDigit() }
        if (clean.length == 14) {
            isConsultingDoc = true
            docConsultationMessage = "Consultando CNPJ na Receita Federal / Brasil API..."
            coroutineScope.launch {
                val res = BrasilApiCnpjService.consultAndValidateAssociation(clean)
                isConsultingDoc = false
                if (res.isSuccess) {
                    if (res.preferredName.isNotBlank()) {
                        clientName = res.preferredName
                    }
                    if (res.fullAddress.isNotBlank()) {
                        clientAddress = res.fullAddress
                    }
                    if (res.telefone.isNotBlank()) {
                        clientPhone = res.telefone
                    }
                    docConsultationMessage = "${res.statusBadgeText}\n${res.statusMessage}"
                } else {
                    docConsultationMessage = "⚠️ " + (res.errorMessage ?: "CNPJ não encontrado.")
                }
            }
        } else if (clean.length == 11) {
            val isValidCpf = BrasilApiCnpjService.isValidCpfDigits(clean)
            if (isValidCpf) {
                val match = receiptManager.receipts.value.firstOrNull { r ->
                    r.clientDocument.filter { c -> c.isDigit() } == clean && r.clientName.isNotBlank()
                }
                if (match != null) {
                    if (clientName.isBlank()) clientName = match.clientName
                    if (clientPhone.isBlank()) clientPhone = match.clientPhone
                    if (clientAddress.isBlank()) clientAddress = match.clientAddress
                    docConsultationMessage = "👤 CPF VÁLIDO • Cliente encontrado no histórico: ${match.clientName}"
                } else {
                    docConsultationMessage = "✅ CPF VÁLIDO (Dígitos verificadores válidos - Módulo 11)"
                }
            } else {
                docConsultationMessage = "❌ CPF INVÁLIDO (Dígitos verificadores incorretos)"
            }
        }
    }

    val itemsList = remember {
        mutableStateListOf<ReceiptItem>().apply {
            addAll(initialReceipt.items.ifEmpty {
                listOf(
                    ReceiptItem(
                        description = "Mel Puro Silvestre 1kg (Vidro)",
                        quantity = 1.0,
                        unit = "unid",
                        unitPrice = 45.0,
                        totalPrice = 45.0
                    )
                )
            })
        }
    }

    // New item inputs
    var newItemDesc by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("1") }
    var newItemUnit by remember { mutableStateOf("unid") }
    var newItemPrice by remember { mutableStateOf("40,00") }
    var newItemBatch by remember { mutableStateOf("") }
    var newItemCategory by remember { mutableStateOf("Mel") }
    var newItemPackaging by remember { mutableStateOf("Vidro 1kg") }

    // Digital signature drawing points
    val signaturePaths = remember { mutableStateListOf<List<Offset>>() }
    var currentPathPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var isSignaturePanelVisible by remember { mutableStateOf(false) }

    val calculatedTotal = itemsList.sumOf { it.totalPrice }
    val wordsTotal = CurrencyToWordsPtBr.convert(calculatedTotal)

    val currentSignatureSvg = remember(signaturePaths.size, currentPathPoints.size) {
        if (signaturePaths.isNotEmpty()) {
            com.example.receipt.util.ReceiptSignatureHelper.serializePathsToSvgPath(signaturePaths)
        } else null
    }

    val currentReceiptData = initialReceipt.copy(
        clientName = clientName.ifBlank { "Consumidor Final" },
        clientDocument = clientDocument,
        clientPhone = clientPhone,
        clientAddress = clientAddress,
        paymentMethod = paymentMethod,
        items = itemsList.toList(),
        totalAmount = calculatedTotal,
        notes = notes,
        issuerSignatureSvg = currentSignatureSvg ?: initialReceipt.issuerSignatureSvg ?: layoutConfig.savedIssuerSignatureSvg
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("issue_receipt_dialog"),
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
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = layoutConfig.colorTheme.composePrimary.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = layoutConfig.colorTheme.composePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isViewMode) "Visualização do Recibo" else "Emissão de Recibo Apícola",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Nº ${currentReceiptData.id} • ${layoutConfig.templateStyle.label}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onOpenLayoutEditor,
                                modifier = Modifier.testTag("open_layout_editor_shortcut_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Brush,
                                    contentDescription = "Editar Layout",
                                    tint = layoutConfig.colorTheme.composePrimary
                                )
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("close_issue_receipt_btn")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar")
                            }
                        }
                    }
                }

                // Mode Selector Bar (Editar Dados vs Prévia do Recibo)
                TabRow(
                    selectedTabIndex = if (isViewMode) 1 else 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = !isViewMode,
                        onClick = { isViewMode = false },
                        text = { Text("Preencher Dados") },
                        icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = isViewMode,
                        onClick = { isViewMode = true },
                        text = { Text("Ver Recibo Formatado") },
                        icon = { Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                // Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (isViewMode) {
                        // View mode: shows paper receipt with action buttons
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            ReceiptPaperView(
                                receipt = currentReceiptData,
                                config = layoutConfig,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    } else {
                        // Edit Mode Form
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Total Banner
                            Surface(
                                color = layoutConfig.colorTheme.composePrimary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "VALOR TOTAL DO RECIBO",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = layoutConfig.colorTheme.composeDarkAccent
                                        )
                                        Text(
                                            text = "R$ %,.2f".format(Locale.getDefault(), calculatedTotal),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = layoutConfig.colorTheme.composePrimary
                                        )
                                        Text(
                                            text = wordsTotal,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        color = layoutConfig.colorTheme.composePrimary,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = paymentMethod,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            // Section 1: Customer Data
                            Text(
                                text = "1. Dados do Cliente / Pagador",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = clientName,
                                onValueChange = { clientName = it },
                                label = { Text("Nome do Cliente / Comprador") },
                                placeholder = { Text("ex: Maria Silva ou Supermercado Central") },
                                modifier = Modifier.fillMaxWidth().testTag("client_name_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.3f)) {
                                    OutlinedTextField(
                                        value = clientDocument,
                                        onValueChange = { input ->
                                            val formatted = BrasilApiCnpjService.formatDocument(input)
                                            clientDocument = formatted
                                            val clean = formatted.filter { c -> c.isDigit() }
                                            if (clean.length == 14 || clean.length == 11) {
                                                performDocumentLookup(clean)
                                            }
                                        },
                                        label = { Text("CPF ou CNPJ") },
                                        placeholder = { Text("00.000.000/0000-00") },
                                        trailingIcon = {
                                            if (isConsultingDoc) {
                                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                            } else {
                                                IconButton(
                                                    onClick = {
                                                        performDocumentLookup(clientDocument)
                                                    },
                                                    enabled = clientDocument.filter { c -> c.isDigit() }.length in listOf(11, 14)
                                                ) {
                                                    Icon(Icons.Default.Search, contentDescription = "Consultar Documento", modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("client_document_input")
                                    )
                                }

                                OutlinedTextField(
                                    value = clientPhone,
                                    onValueChange = { clientPhone = it },
                                    label = { Text("WhatsApp") },
                                    placeholder = { Text("(00) 00000-0000") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (docConsultationMessage != null) {
                                Surface(
                                    color = if (docConsultationMessage!!.contains("✅") || docConsultationMessage!!.contains("👤")) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (docConsultationMessage!!.contains("✅") || docConsultationMessage!!.contains("👤")) Color(0xFFBBF7D0) else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = docConsultationMessage!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = if (docConsultationMessage!!.contains("✅") || docConsultationMessage!!.contains("👤")) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = clientAddress,
                                onValueChange = { clientAddress = it },
                                label = { Text("Endereço do Cliente / Entrega (Opcional)") },
                                placeholder = { Text("Rua, Número, Bairro, Cidade") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Section 2: Items & Products
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "2. Produtos & Serviços Apícolas",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${itemsList.size} item(ns)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Quick Add Chips
                            Text(
                                text = "Toque para adicionar rapidamente produtos apícolas comuns:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val presets = listOf(
                                    Triple("Mel Silvestre 1kg (Vidro)", 1.0, 45.0),
                                    Triple("Mel Laranjeira 500g (Bisnaga)", 1.0, 25.0),
                                    Triple("Extrato Própolis Verde 30ml", 1.0, 30.0),
                                    Triple("Pólen Apícola 150g", 1.0, 35.0),
                                    Triple("Cera de Abelha 1kg", 1.0, 50.0),
                                    Triple("Favo de Mel 400g", 1.0, 40.0),
                                    Triple("Enxame Langstroth", 1.0, 450.0),
                                    Triple("Rainha Fecundada", 1.0, 90.0),
                                    Triple("Serviço de Polinização", 1.0, 250.0)
                                )

                                presets.forEach { (name, qty, price) ->
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            itemsList.add(
                                                ReceiptItem(
                                                    description = name,
                                                    quantity = qty,
                                                    unit = if (name.contains("1kg")) "kg" else "unid",
                                                    unitPrice = price,
                                                    totalPrice = qty * price
                                                )
                                            )
                                        },
                                        label = { Text("+ $name (R$ %.0f)".format(price)) },
                                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    )
                                }
                            }

                            // Items List Card
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            ) {
                                if (itemsList.isEmpty()) {
                                    Text(
                                        text = "Nenhum item adicionado. Adicione produtos acima.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                } else {
                                    itemsList.forEachIndexed { index, item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.description, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                Text(
                                                    text = "${item.quantity} ${item.unit} x R$ %,.2f = R$ %,.2f".format(
                                                        Locale.getDefault(),
                                                        item.unitPrice,
                                                        item.totalPrice
                                                    ),
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                val details = listOfNotNull(
                                                    item.productCategory.ifBlank { null }?.let { "Tipo: $it" },
                                                    item.packaging.ifBlank { null }?.let { "Embalagem: $it" },
                                                    item.batchNumber.ifBlank { null }?.let { "Lote: $it" }
                                                )
                                                if (details.isNotEmpty()) {
                                                    Text(
                                                        text = details.joinToString(" • "),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = layoutConfig.colorTheme.composeDarkAccent
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = { itemsList.removeAt(index) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Remover",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        if (index < itemsList.size - 1) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        }
                                    }
                                }
                            }

                            // Manual Add Item Form
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Adicionar Item Avulso com Detalhes do Produto",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = newItemDesc,
                                        onValueChange = { newItemDesc = it },
                                        label = { Text("Descrição do Produto / Serviço") },
                                        placeholder = { Text("ex: Mel Silvestre de Florada Nativa") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = newItemQty,
                                            onValueChange = { newItemQty = it },
                                            label = { Text("Qtd") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )

                                        OutlinedTextField(
                                            value = newItemUnit,
                                            onValueChange = { newItemUnit = it },
                                            label = { Text("Unidade") },
                                            modifier = Modifier.weight(1f)
                                        )

                                        OutlinedTextField(
                                            value = newItemPrice,
                                            onValueChange = { newItemPrice = it },
                                            label = { Text("Valor Unit. (R$)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(1.5f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = newItemCategory,
                                            onValueChange = { newItemCategory = it },
                                            label = { Text("Categoria/Tipo") },
                                            placeholder = { Text("ex: Mel, Própolis, Cera") },
                                            modifier = Modifier.weight(1f)
                                        )

                                        OutlinedTextField(
                                            value = newItemPackaging,
                                            onValueChange = { newItemPackaging = it },
                                            label = { Text("Embalagem") },
                                            placeholder = { Text("ex: Vidro 1kg, Bisnaga") },
                                            modifier = Modifier.weight(1f)
                                        )

                                        OutlinedTextField(
                                            value = newItemBatch,
                                            onValueChange = { newItemBatch = it },
                                            label = { Text("Nº do Lote") },
                                            placeholder = { Text("ex: #2026-A") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (newItemDesc.isNotBlank()) {
                                                val qty = newItemQty.replace(",", ".").toDoubleOrNull() ?: 1.0
                                                val price = newItemPrice.replace(",", ".").toDoubleOrNull() ?: 0.0
                                                itemsList.add(
                                                    ReceiptItem(
                                                        description = newItemDesc,
                                                        quantity = qty,
                                                        unit = newItemUnit.ifBlank { "unid" },
                                                        unitPrice = price,
                                                        totalPrice = qty * price,
                                                        productCategory = newItemCategory,
                                                        packaging = newItemPackaging,
                                                        batchNumber = newItemBatch
                                                    )
                                                )
                                                newItemDesc = ""
                                                newItemQty = "1"
                                                newItemPrice = "0,00"
                                                newItemBatch = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = layoutConfig.colorTheme.composePrimary),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Adicionar Item")
                                    }
                                }
                            }

                            // Section 3: Payment Method
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "3. Forma de Pagamento",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("PIX", "Dinheiro", "Cartão de Crédito", "Cartão de Débito", "Transferência TED", "Boleto").forEach { method ->
                                    FilterChip(
                                        selected = paymentMethod == method,
                                        onClick = { paymentMethod = method },
                                        label = { Text(method) }
                                    )
                                }
                            }

                            // Section 4: Digital Signature Canvas Panel
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Brush, contentDescription = null, tint = layoutConfig.colorTheme.composePrimary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Assinatura Digital no Recibo",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        TextButton(onClick = { isSignaturePanelVisible = !isSignaturePanelVisible }) {
                                            Text(if (isSignaturePanelVisible) "Recolher" else "Assinar com o Dedo")
                                        }
                                    }

                                    if (isSignaturePanelVisible) {
                                        Text(
                                            text = "Desenhe a assinatura ou rubrica diretamente no espaço abaixo:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .background(Color.White, RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                                .pointerInput(Unit) {
                                                    detectDragGestures(
                                                        onDragStart = { offset ->
                                                            currentPathPoints = listOf(offset)
                                                        },
                                                        onDrag = { change, _ ->
                                                            currentPathPoints = currentPathPoints + change.position
                                                        },
                                                        onDragEnd = {
                                                            if (currentPathPoints.isNotEmpty()) {
                                                                signaturePaths.add(currentPathPoints)
                                                                currentPathPoints = emptyList()
                                                            }
                                                        }
                                                    )
                                                }
                                        ) {
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                // Draw existing paths
                                                for (pathPoints in signaturePaths) {
                                                    if (pathPoints.size > 1) {
                                                        val path = Path().apply {
                                                            moveTo(pathPoints.first().x, pathPoints.first().y)
                                                            for (i in 1 until pathPoints.size) {
                                                                lineTo(pathPoints[i].x, pathPoints[i].y)
                                                            }
                                                        }
                                                        drawPath(
                                                            path = path,
                                                            color = Color(0xFF0F172A),
                                                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                                        )
                                                    }
                                                }

                                                // Draw active path
                                                if (currentPathPoints.size > 1) {
                                                    val path = Path().apply {
                                                        moveTo(currentPathPoints.first().x, currentPathPoints.first().y)
                                                        for (i in 1 until currentPathPoints.size) {
                                                            lineTo(currentPathPoints[i].x, currentPathPoints[i].y)
                                                        }
                                                    }
                                                    drawPath(
                                                        path = path,
                                                        color = Color(0xFF0F172A),
                                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                                    )
                                                }
                                            }

                                            // Placeholder hint when empty
                                            if (signaturePaths.isEmpty() && currentPathPoints.isEmpty()) {
                                                Text(
                                                    text = "✍️ Assine aqui com o dedo...",
                                                    color = Color.LightGray,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    signaturePaths.clear()
                                                    currentPathPoints = emptyList()
                                                }
                                            ) {
                                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Limpar Assinatura")
                                            }
                                        }
                                    }
                                }
                            }

                            // Section 5: Notes
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Observações do Recibo (Opcional)") },
                                placeholder = { Text("ex: Entregue em mãos / Lote colhido em Outubro") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Bottom Action Buttons
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Quick actions row (WhatsApp PDF, Compartilhar PDF, Salvar PDF, Imprimir, Copiar)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    receiptManager.sharePdf(currentReceiptData, layoutConfig, targetWhatsApp = true)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("share_whatsapp_pdf_btn")
                            ) {
                                Text("💬 Enviar WhatsApp PDF", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    receiptManager.sharePdf(currentReceiptData, layoutConfig, targetWhatsApp = false)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("share_pdf_btn")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Compartilhar PDF", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    receiptManager.savePdfToDownloads(currentReceiptData, layoutConfig)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("save_pdf_downloads_btn")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salvar em PDF", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    receiptManager.printOrSavePdf(currentReceiptData, layoutConfig)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("print_pdf_receipt_btn")
                            ) {
                                Icon(Icons.Default.LocalPrintshop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Imprimir", fontSize = 12.sp)
                            }

                            IconButton(
                                onClick = {
                                    val text = receiptManager.generateFormattedReceiptText(currentReceiptData, layoutConfig)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clip = ClipData.newPlainText("Recibo ApisSmart", text)
                                    clipboard?.setPrimaryClip(clip)
                                    Toast.makeText(context, "📋 Recibo copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Texto", modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Save Receipt Button
                        Button(
                            onClick = {
                                onSaveReceipt(currentReceiptData)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = layoutConfig.colorTheme.composePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_issued_receipt_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salvar e Emitir Recibo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
