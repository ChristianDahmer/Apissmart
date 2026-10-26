package com.example.receipt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.license.BrasilApiCnpjService
import com.example.receipt.model.ReceiptColorTheme
import com.example.receipt.model.ReceiptDensity
import com.example.receipt.model.ReceiptIconType
import com.example.receipt.model.ReceiptItem
import com.example.receipt.model.ReceiptLayoutConfig
import com.example.receipt.model.ReceiptTemplateStyle
import com.example.receipt.model.ReceiptData
import com.example.ui.theme.HoneyGoldPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptLayoutEditorDialog(
    initialConfig: ReceiptLayoutConfig,
    onDismiss: () -> Unit,
    onSaveConfig: (ReceiptLayoutConfig) -> Unit,
    onResetToDefaults: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Estilo & Cores, 1: Dados do Emissor, 2: PIX & Pagamento, 3: Campos & Termos, 4: Prévia ao Vivo

    // Working copy states
    var templateStyle by remember { mutableStateOf(initialConfig.templateStyle) }
    var colorTheme by remember { mutableStateOf(initialConfig.colorTheme) }
    var iconType by remember { mutableStateOf(initialConfig.iconType) }
    var density by remember { mutableStateOf(initialConfig.density) }

    var businessName by remember { mutableStateOf(initialConfig.businessName) }
    var ownerName by remember { mutableStateOf(initialConfig.ownerName) }
    var document by remember { mutableStateOf(initialConfig.document) }
    var stateRegistration by remember { mutableStateOf(initialConfig.stateRegistration) }
    var phone by remember { mutableStateOf(initialConfig.phone) }
    var email by remember { mutableStateOf(initialConfig.email) }
    var address by remember { mutableStateOf(initialConfig.address) }
    var cityState by remember { mutableStateOf(initialConfig.cityState) }

    var showPixKey by remember { mutableStateOf(initialConfig.showPixKey) }
    var pixKey by remember { mutableStateOf(initialConfig.pixKey) }
    var pixKeyType by remember { mutableStateOf(initialConfig.pixKeyType) }
    var pixReceiverName by remember { mutableStateOf(initialConfig.pixReceiverName) }
    var pixBank by remember { mutableStateOf(initialConfig.pixBank) }

    var showReceiptNumber by remember { mutableStateOf(initialConfig.showReceiptNumber) }
    var showDueDateOrPaymentDate by remember { mutableStateOf(initialConfig.showDueDateOrPaymentDate) }
    var showItemsTable by remember { mutableStateOf(initialConfig.showItemsTable) }
    var showIssuerSignature by remember { mutableStateOf(initialConfig.showIssuerSignature) }
    var showCustomerSignature by remember { mutableStateOf(initialConfig.showCustomerSignature) }
    var showTearOffStub by remember { mutableStateOf(initialConfig.showTearOffStub) }
    var showQualityWatermark by remember { mutableStateOf(initialConfig.showQualityWatermark) }
    var showClientDocument by remember { mutableStateOf(initialConfig.showClientDocument) }
    var showClientAddress by remember { mutableStateOf(initialConfig.showClientAddress) }

    var receiptTitle by remember { mutableStateOf(initialConfig.receiptTitle) }
    var legalTerm by remember { mutableStateOf(initialConfig.legalTerm) }
    var footerGreeting by remember { mutableStateOf(initialConfig.footerGreeting) }
    var qualitySealText by remember { mutableStateOf(initialConfig.qualitySealText) }
    var savedIssuerSignatureSvg by remember { mutableStateOf(initialConfig.savedIssuerSignatureSvg) }

    // Live composed config for real-time preview
    val currentWorkingConfig = ReceiptLayoutConfig(
        templateStyle = templateStyle,
        colorTheme = colorTheme,
        iconType = iconType,
        density = density,
        businessName = businessName,
        ownerName = ownerName,
        document = document,
        stateRegistration = stateRegistration,
        phone = phone,
        email = email,
        address = address,
        cityState = cityState,
        showPixKey = showPixKey,
        pixKey = pixKey,
        pixKeyType = pixKeyType,
        pixReceiverName = pixReceiverName,
        pixBank = pixBank,
        showReceiptNumber = showReceiptNumber,
        showDueDateOrPaymentDate = showDueDateOrPaymentDate,
        showItemsTable = showItemsTable,
        showIssuerSignature = showIssuerSignature,
        showCustomerSignature = showCustomerSignature,
        showTearOffStub = showTearOffStub,
        showQualityWatermark = showQualityWatermark,
        showClientDocument = showClientDocument,
        showClientAddress = showClientAddress,
        receiptTitle = receiptTitle,
        legalTerm = legalTerm,
        footerGreeting = footerGreeting,
        qualitySealText = qualitySealText,
        savedIssuerSignatureSvg = savedIssuerSignatureSvg
    )

    // Sample receipt for real-time interactive preview
    val previewReceipt = remember {
        ReceiptData(
            id = "REC-2026-0042",
            receiptNumber = 42,
            dateMillis = System.currentTimeMillis(),
            clientName = "Cooperativa Agropecuária Mantiqueira",
            clientDocument = "33.444.555/0001-99",
            clientPhone = "(35) 99123-4567",
            clientAddress = "Av. dos Eucaliptos, 1200",
            totalAmount = 385.00,
            paymentMethod = "PIX",
            items = listOf(
                ReceiptItem(description = "Mel Puro Silvestre 1kg (Vidro)", quantity = 5.0, unit = "unid", unitPrice = 45.0, totalPrice = 225.0),
                ReceiptItem(description = "Extrato de Própolis Verde 30ml", quantity = 3.0, unit = "frasco", unitPrice = 30.0, totalPrice = 90.0),
                ReceiptItem(description = "Favo de Mel Fresco em Caixa", quantity = 2.0, unit = "unid", unitPrice = 35.0, totalPrice = 70.0)
            ),
            notes = "Lote de Colheita de Primavera #2026-P",
            status = "EMITIDO"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("receipt_layout_editor_dialog"),
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
                                color = colorTheme.composePrimary.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Brush,
                                        contentDescription = null,
                                        tint = colorTheme.composePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Editor de Layout do Recibo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Personalize o recibo oficial de vendas do apiário",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_layout_editor_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }
                }

                // Tabs Navigation
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Estilo & Visual") },
                        icon = { Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Dados do Emissor") },
                        icon = { Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Chave PIX") },
                        icon = { Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Campos & Textos") },
                        icon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = { Text("Prévia ao Vivo") },
                        icon = { Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                // Tab Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> StyleAndColorTab(
                            currentTemplate = templateStyle,
                            onTemplateSelected = { templateStyle = it },
                            currentColor = colorTheme,
                            onColorSelected = { colorTheme = it },
                            currentIcon = iconType,
                            onIconSelected = { iconType = it },
                            currentDensity = density,
                            onDensitySelected = { density = it }
                        )

                        1 -> ProducerDataTab(
                            businessName = businessName,
                            onBusinessNameChange = { businessName = it },
                            ownerName = ownerName,
                            onOwnerNameChange = { ownerName = it },
                            document = document,
                            onDocumentChange = { document = it },
                            stateRegistration = stateRegistration,
                            onStateRegistrationChange = { stateRegistration = it },
                            phone = phone,
                            onPhoneChange = { phone = it },
                            email = email,
                            onEmailChange = { email = it },
                            address = address,
                            onAddressChange = { address = it },
                            cityState = cityState,
                            onCityStateChange = { cityState = it },
                            savedIssuerSignatureSvg = savedIssuerSignatureSvg,
                            onSavedIssuerSignatureChange = { savedIssuerSignatureSvg = it },
                            colorTheme = colorTheme
                        )

                        2 -> PixAndPaymentsTab(
                            showPixKey = showPixKey,
                            onToggleShowPix = { showPixKey = it },
                            pixKey = pixKey,
                            onPixKeyChange = { pixKey = it },
                            pixKeyType = pixKeyType,
                            onPixKeyTypeChange = { pixKeyType = it },
                            pixReceiverName = pixReceiverName,
                            onPixReceiverNameChange = { pixReceiverName = it },
                            pixBank = pixBank,
                            onPixBankChange = { pixBank = it }
                        )

                        3 -> FieldsAndTextsTab(
                            showReceiptNumber = showReceiptNumber,
                            onToggleReceiptNumber = { showReceiptNumber = it },
                            showTearOffStub = showTearOffStub,
                            onToggleTearOffStub = { showTearOffStub = it },
                            showItemsTable = showItemsTable,
                            onToggleItemsTable = { showItemsTable = it },
                            showIssuerSignature = showIssuerSignature,
                            onToggleIssuerSignature = { showIssuerSignature = it },
                            showCustomerSignature = showCustomerSignature,
                            onToggleCustomerSignature = { showCustomerSignature = it },
                            showQualityWatermark = showQualityWatermark,
                            onToggleQualityWatermark = { showQualityWatermark = it },
                            showClientDocument = showClientDocument,
                            onToggleClientDocument = { showClientDocument = it },
                            showClientAddress = showClientAddress,
                            onToggleClientAddress = { showClientAddress = it },
                            receiptTitle = receiptTitle,
                            onReceiptTitleChange = { receiptTitle = it },
                            legalTerm = legalTerm,
                            onLegalTermChange = { legalTerm = it },
                            footerGreeting = footerGreeting,
                            onFooterGreetingChange = { footerGreeting = it },
                            qualitySealText = qualitySealText,
                            onQualitySealTextChange = { qualitySealText = it }
                        )

                        4 -> LivePreviewTab(
                            receipt = previewReceipt,
                            config = currentWorkingConfig
                        )
                    }
                }

                // Bottom Actions Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                onResetToDefaults()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("reset_layout_btn")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restaurar Padrão")
                        }

                        Button(
                            onClick = {
                                onSaveConfig(currentWorkingConfig)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colorTheme.composePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_receipt_layout_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salvar Layout")
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// TAB 0: Estilo, Modelos & Cores
// ------------------------------------------------------------------
@Composable
private fun StyleAndColorTab(
    currentTemplate: ReceiptTemplateStyle,
    onTemplateSelected: (ReceiptTemplateStyle) -> Unit,
    currentColor: ReceiptColorTheme,
    onColorSelected: (ReceiptColorTheme) -> Unit,
    currentIcon: ReceiptIconType,
    onIconSelected: (ReceiptIconType) -> Unit,
    currentDensity: ReceiptDensity,
    onDensitySelected: (ReceiptDensity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "1. Escolha o Modelo / Layout Base",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        ReceiptTemplateStyle.values().forEach { template ->
            val isSelected = currentTemplate == template
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onTemplateSelected(template) }
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) currentColor.composePrimary else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) currentColor.composePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(2.dp, if (isSelected) currentColor.composePrimary else Color.Gray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(currentColor.composePrimary, CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = template.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) currentColor.composePrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = template.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "2. Paleta de Cores do Recibo",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ReceiptColorTheme.values().forEach { colorOpt ->
                val isSelected = currentColor == colorOpt
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colorOpt.composePrimary.copy(alpha = 0.15f),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, colorOpt.composePrimary) else null,
                    modifier = Modifier
                        .clickable { onColorSelected(colorOpt) }
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(colorOpt.composePrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = colorOpt.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = colorOpt.composeDarkAccent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "3. Ícone / Logotipo de Cabeçalho",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReceiptIconType.values().forEach { iconOpt ->
                FilterChip(
                    selected = currentIcon == iconOpt,
                    onClick = { onIconSelected(iconOpt) },
                    label = { Text("${iconOpt.iconEmoji} ${iconOpt.label}") }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "4. Densidade & Espaçamento",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReceiptDensity.values().forEach { d ->
                FilterChip(
                    selected = currentDensity == d,
                    onClick = { onDensitySelected(d) },
                    label = { Text(d.label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ------------------------------------------------------------------
// TAB 1: Dados do Emissor / Produtor Apícola
// ------------------------------------------------------------------
@Composable
private fun ProducerDataTab(
    businessName: String,
    onBusinessNameChange: (String) -> Unit,
    ownerName: String,
    onOwnerNameChange: (String) -> Unit,
    document: String,
    onDocumentChange: (String) -> Unit,
    stateRegistration: String,
    onStateRegistrationChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    cityState: String,
    onCityStateChange: (String) -> Unit,
    savedIssuerSignatureSvg: String? = null,
    onSavedIssuerSignatureChange: (String?) -> Unit = {},
    colorTheme: ReceiptColorTheme = ReceiptColorTheme.HONEY_GOLD
) {
    val coroutineScope = rememberCoroutineScope()
    var isConsulting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun doLookup(docInput: String) {
        val clean = docInput.filter { it.isDigit() }
        if (clean.length == 14) {
            isConsulting = true
            statusMessage = "Consultando CNPJ da empresa/associação..."
            coroutineScope.launch {
                val res = BrasilApiCnpjService.consultAndValidateAssociation(clean)
                isConsulting = false
                if (res.isSuccess) {
                    if (res.nomeFantasia.isNotBlank()) onBusinessNameChange(res.nomeFantasia)
                    else if (res.razaoSocial.isNotBlank()) onBusinessNameChange(res.razaoSocial)
                    if (res.razaoSocial.isNotBlank()) onOwnerNameChange(res.razaoSocial)
                    if (res.fullAddress.isNotBlank()) onAddressChange(res.fullAddress)
                    if (res.municipio.isNotBlank()) onCityStateChange("${res.municipio} - ${res.uf}")
                    if (res.telefone.isNotBlank()) onPhoneChange(res.telefone)
                    if (res.email.isNotBlank()) onEmailChange(res.email)
                    statusMessage = "${res.statusBadgeText}\n${res.statusMessage}"
                } else {
                    statusMessage = "⚠️ " + (res.errorMessage ?: "Erro ao consultar CNPJ.")
                }
            }
        } else if (clean.length == 11) {
            val valid = BrasilApiCnpjService.isValidCpfDigits(clean)
            if (valid) {
                statusMessage = "✅ CPF VÁLIDO (Dígitos verificadores corretos - Módulo 11)"
            } else {
                statusMessage = "❌ CPF INVÁLIDO (Dígitos verificadores incorretos)"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Identificação do Emissor no Recibo",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Essas informações serão impressas no cabeçalho do recibo de venda. Digite o CNPJ para busca automática via Receita Federal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = document,
                onValueChange = { input ->
                    val formatted = BrasilApiCnpjService.formatDocument(input)
                    onDocumentChange(formatted)
                    val clean = formatted.filter { c -> c.isDigit() }
                    if (clean.length == 14 || clean.length == 11) {
                        doLookup(clean)
                    }
                },
                label = { Text("CPF ou CNPJ do Emissor") },
                placeholder = { Text("00.000.000/0000-00") },
                trailingIcon = {
                    if (isConsulting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(
                            onClick = { doLookup(document) },
                            enabled = document.filter { c -> c.isDigit() }.length in listOf(11, 14)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Consultar CNPJ", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier.weight(1.2f).testTag("producer_document_input")
            )

            OutlinedTextField(
                value = stateRegistration,
                onValueChange = onStateRegistrationChange,
                label = { Text("Inscr. Estadual / SIM / SIF") },
                placeholder = { Text("SIM-021") },
                modifier = Modifier.weight(1f)
            )
        }

        if (statusMessage != null) {
            Surface(
                color = if (statusMessage!!.contains("✅") || statusMessage!!.contains("CONFIRMADA")) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (statusMessage!!.contains("✅") || statusMessage!!.contains("CONFIRMADA")) Color(0xFFBBF7D0) else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = statusMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (statusMessage!!.contains("✅") || statusMessage!!.contains("CONFIRMADA")) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        OutlinedTextField(
            value = businessName,
            onValueChange = onBusinessNameChange,
            label = { Text("Nome Fantasia / Apiário / Associação") },
            placeholder = { Text("ex: Associação Apícola Mantiqueira") },
            modifier = Modifier.fillMaxWidth().testTag("producer_biz_name_input")
        )

        OutlinedTextField(
            value = ownerName,
            onValueChange = onOwnerNameChange,
            label = { Text("Razão Social / Nome do Apicultor") },
            placeholder = { Text("ex: João Carlos da Silva") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("WhatsApp / Telefone") },
                placeholder = { Text("(00) 00000-0000") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("E-mail de Contato") },
                placeholder = { Text("contato@apiario.com.br") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Endereço / Logradouro") },
            placeholder = { Text("Estrada das Flores, Km 4 - Sítio do Mel") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cityState,
            onValueChange = onCityStateChange,
            label = { Text("Cidade e Estado") },
            placeholder = { Text("Passa Quatro - MG") },
            modifier = Modifier.fillMaxWidth()
        )

        // Signature Registration Section
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val sigPaths = remember { mutableStateListOf<List<Offset>>() }
            var currentPathPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Brush, contentDescription = null, tint = colorTheme.composePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Assinatura Cadastrada do Apicultor",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!savedIssuerSignatureSvg.isNullOrBlank()) {
                        Surface(
                            color = Color(0xFF16A34A).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "✅ Cadastrada",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A assinatura desenhada aqui será colocada automaticamente nos recibos emitidos pelo sistema.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, colorTheme.composePrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
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
                                        sigPaths.add(currentPathPoints)
                                        currentPathPoints = emptyList()
                                        val svg = com.example.receipt.util.ReceiptSignatureHelper.serializePathsToSvgPath(sigPaths)
                                        onSavedIssuerSignatureChange(svg)
                                    }
                                }
                            )
                        }
                ) {
                    if (!savedIssuerSignatureSvg.isNullOrBlank() && sigPaths.isEmpty() && currentPathPoints.isEmpty()) {
                        com.example.receipt.util.SignaturePathView(
                            svgPathD = savedIssuerSignatureSvg,
                            strokeColor = colorTheme.composePrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        for (pathPoints in sigPaths) {
                            if (pathPoints.size > 1) {
                                val path = Path().apply {
                                    moveTo(pathPoints.first().x, pathPoints.first().y)
                                    for (i in 1 until pathPoints.size) {
                                        lineTo(pathPoints[i].x, pathPoints[i].y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = colorTheme.composePrimary,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }

                        if (currentPathPoints.size > 1) {
                            val path = Path().apply {
                                moveTo(currentPathPoints.first().x, currentPathPoints.first().y)
                                for (i in 1 until currentPathPoints.size) {
                                    lineTo(currentPathPoints[i].x, currentPathPoints[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = colorTheme.composePrimary,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    if (sigPaths.isEmpty() && currentPathPoints.isEmpty() && savedIssuerSignatureSvg.isNullOrBlank()) {
                        Text(
                            text = "✍️ Desenhe sua assinatura cadastrada aqui...",
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
                    if (!savedIssuerSignatureSvg.isNullOrBlank() || sigPaths.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                sigPaths.clear()
                                currentPathPoints = emptyList()
                                onSavedIssuerSignatureChange(null)
                            }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Limpar Assinatura Cadastrada")
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// TAB 2: Chave PIX & Pagamentos
// ------------------------------------------------------------------
@Composable
private fun PixAndPaymentsTab(
    showPixKey: Boolean,
    onToggleShowPix: (Boolean) -> Unit,
    pixKey: String,
    onPixKeyChange: (String) -> Unit,
    pixKeyType: String,
    onPixKeyTypeChange: (String) -> Unit,
    pixReceiverName: String,
    onPixReceiverNameChange: (String) -> Unit,
    pixBank: String,
    onPixBankChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Exibir Chave PIX no Recibo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Facilita o recebimento e pagamento imediato por parte do cliente ou cooperativa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = showPixKey,
                    onCheckedChange = onToggleShowPix
                )
            }
        }

        if (showPixKey) {
            Text(
                text = "Tipo de Chave PIX",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("E-mail", "CPF/CNPJ", "Celular", "Chave Aleatória").forEach { type ->
                    FilterChip(
                        selected = pixKeyType == type,
                        onClick = { onPixKeyTypeChange(type) },
                        label = { Text(type) }
                    )
                }
            }

            OutlinedTextField(
                value = pixKey,
                onValueChange = onPixKeyChange,
                label = { Text("Chave PIX") },
                placeholder = { Text("Digite sua chave...") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pixReceiverName,
                onValueChange = onPixReceiverNameChange,
                label = { Text("Nome do Titular da Conta PIX") },
                placeholder = { Text("Nome que aparece na tela de confirmação do PIX") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pixBank,
                onValueChange = onPixBankChange,
                label = { Text("Banco / Cooperativa (Opcional)") },
                placeholder = { Text("ex: Sicredi / Banco do Brasil / Nubank") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ------------------------------------------------------------------
// TAB 3: Campos Visíveis & Textos Legais
// ------------------------------------------------------------------
@Composable
private fun FieldsAndTextsTab(
    showReceiptNumber: Boolean,
    onToggleReceiptNumber: (Boolean) -> Unit,
    showTearOffStub: Boolean,
    onToggleTearOffStub: (Boolean) -> Unit,
    showItemsTable: Boolean,
    onToggleItemsTable: (Boolean) -> Unit,
    showIssuerSignature: Boolean,
    onToggleIssuerSignature: (Boolean) -> Unit,
    showCustomerSignature: Boolean,
    onToggleCustomerSignature: (Boolean) -> Unit,
    showQualityWatermark: Boolean,
    onToggleQualityWatermark: (Boolean) -> Unit,
    showClientDocument: Boolean,
    onToggleClientDocument: (Boolean) -> Unit,
    showClientAddress: Boolean,
    onToggleClientAddress: (Boolean) -> Unit,
    receiptTitle: String,
    onReceiptTitleChange: (String) -> Unit,
    legalTerm: String,
    onLegalTermChange: (String) -> Unit,
    footerGreeting: String,
    onFooterGreetingChange: (String) -> Unit,
    qualitySealText: String,
    onQualitySealTextChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Campos e Elementos a Exibir",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        ToggleOptionRow(
            title = "Número Sequencial do Recibo",
            subtitle = "Ex: Nº REC-2026-0042 no cabeçalho",
            checked = showReceiptNumber,
            onCheckedChange = onToggleReceiptNumber
        )

        ToggleOptionRow(
            title = "Canhoto Destacável de Controle",
            subtitle = "Parte superior picotada para controle do apicultor",
            checked = showTearOffStub,
            onCheckedChange = onToggleTearOffStub
        )

        ToggleOptionRow(
            title = "Tabela Detalhada de Produtos",
            subtitle = "Discrimina itens, quantidade, preço unitário e valor total",
            checked = showItemsTable,
            onCheckedChange = onToggleItemsTable
        )

        ToggleOptionRow(
            title = "Linha de Assinatura do Emissor",
            subtitle = "Espaço para assinatura e carimbo do apicultor",
            checked = showIssuerSignature,
            onCheckedChange = onToggleIssuerSignature
        )

        ToggleOptionRow(
            title = "Linha de Assinatura do Cliente",
            subtitle = "Espaço formal para confirmação de recebimento do comprador",
            checked = showCustomerSignature,
            onCheckedChange = onToggleCustomerSignature
        )

        ToggleOptionRow(
            title = "Selo de Garantia Apícola",
            subtitle = "Exibe selo de pureza e sustentabilidade ambiental",
            checked = showQualityWatermark,
            onCheckedChange = onToggleQualityWatermark
        )

        ToggleOptionRow(
            title = "Documento do Cliente (CPF/CNPJ)",
            subtitle = "Exibe campo para o documento do comprador",
            checked = showClientDocument,
            onCheckedChange = onToggleClientDocument
        )

        ToggleOptionRow(
            title = "Endereço do Cliente",
            subtitle = "Exibe endereço completo de entrega do cliente",
            checked = showClientAddress,
            onCheckedChange = onToggleClientAddress
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Textos e Mensagens Personalizadas",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = receiptTitle,
            onValueChange = onReceiptTitleChange,
            label = { Text("Título do Recibo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = legalTerm,
            onValueChange = onLegalTermChange,
            label = { Text("Termo de Quitação Legal") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        OutlinedTextField(
            value = footerGreeting,
            onValueChange = onFooterGreetingChange,
            label = { Text("Mensagem de Agradecimento / Rodapé") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = qualitySealText,
            onValueChange = onQualitySealTextChange,
            label = { Text("Texto do Selo de Qualidade Apícola") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ToggleOptionRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// ------------------------------------------------------------------
// TAB 4: Prévia ao Vivo (Live Preview em Tempo Real)
// ------------------------------------------------------------------
@Composable
private fun LivePreviewTab(
    receipt: ReceiptData,
    config: ReceiptLayoutConfig
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = config.colorTheme.composePrimary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = config.colorTheme.composePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Prévia em tempo real: veja como o recibo impresso ou compartilhado via WhatsApp será renderizado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = config.colorTheme.composeDarkAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Render actual live receipt paper
        ReceiptPaperView(
            receipt = receipt,
            config = config,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
