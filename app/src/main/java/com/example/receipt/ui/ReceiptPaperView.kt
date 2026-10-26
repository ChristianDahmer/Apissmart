package com.example.receipt.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.receipt.model.ReceiptColorTheme
import com.example.receipt.model.ReceiptDensity
import com.example.receipt.model.ReceiptIconType
import com.example.receipt.model.ReceiptLayoutConfig
import com.example.receipt.model.ReceiptTemplateStyle
import com.example.receipt.model.ReceiptData
import com.example.receipt.util.CurrencyToWordsPtBr
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceiptPaperView(
    receipt: ReceiptData,
    config: ReceiptLayoutConfig,
    modifier: Modifier = Modifier
) {
    when (config.templateStyle) {
        ReceiptTemplateStyle.CLASSIC_TRADITIONAL -> ClassicTraditionalReceiptLayout(receipt, config, modifier)
        ReceiptTemplateStyle.CLASSIC_COMMERCIAL -> ClassicReceiptLayout(receipt, config, modifier)
        ReceiptTemplateStyle.MODERN_CLEAN -> ModernReceiptLayout(receipt, config, modifier)
        ReceiptTemplateStyle.THERMAL_80MM -> ThermalReceiptLayout(receipt, config, modifier)
        ReceiptTemplateStyle.ARTISANAL_CERTIFICATE -> ArtisanalReceiptLayout(receipt, config, modifier)
    }
}

// -------------------------------------------------------------
// 0. CLASSIC TRADITIONAL TEMPLATE (Estilo Talão de Recibo Oficial)
// -------------------------------------------------------------
@Composable
fun ClassicTraditionalReceiptLayout(
    receipt: ReceiptData,
    config: ReceiptLayoutConfig,
    modifier: Modifier = Modifier
) {
    val primaryColor = config.colorTheme.composePrimary
    val darkAccent = config.colorTheme.composeDarkAccent
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateStr = sdf.format(Date(receipt.dateMillis))
    val amountStr = "R$ %,.2f".format(Locale.getDefault(), receipt.totalAmount)
    val wordsAmount = CurrencyToWordsPtBr.convert(receipt.totalAmount)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(3.dp, primaryColor, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFF8)), // Papel creme clássico
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Header: Top bar with Title & Receipt Number & Currency Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (config.iconType != ReceiptIconType.NONE) {
                        Surface(
                            color = primaryColor.copy(alpha = 0.12f),
                            shape = CircleShape,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(config.iconType.iconEmoji, fontSize = 22.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Column {
                        Text(
                            text = config.businessName.uppercase(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = darkAccent
                        )
                        Text(
                            text = "CNPJ/CPF: ${config.document} • ${config.cityState}",
                            fontSize = 10.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }

                // Currency Badge Box (Valor R$)
                Surface(
                    color = primaryColor,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "VALOR (R$)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = amountStr,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = primaryColor.copy(alpha = 0.4f), thickness = 2.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Subheader: Recibo Nº & Data
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = primaryColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "RECIBO Nº ${receipt.id}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Data de Emissão: $dateStr",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body 1: Declaration & Written Value (Valor por Extenso)
            Surface(
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "DECLARAÇÃO DE RECEBIMENTO & VALOR POR EXTENSO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Recebi(emos) de ${receipt.clientName.ifBlank { "Consumidor Final" }}" +
                                (if (config.showClientDocument && receipt.clientDocument.isNotBlank()) ", inscrito(a) no CPF/CNPJ nº ${receipt.clientDocument}" else "") +
                                ", a quantia de:",
                        fontSize = 12.sp,
                        color = Color(0xFF1E293B),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Highlight box for "VALOR POR EXTENSO"
                    Surface(
                        color = primaryColor.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "✍️ VALOR PAGO DESCRITO POR EXTENSO:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkAccent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = wordsAmount,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                color = primaryColor,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Correspondente ao pagamento de produtos e/ou serviços apícolas descritos abaixo em detalhes:",
                        fontSize = 11.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body 2: Product Details (Detalhes sobre o Produto)
            if (config.showItemsTable && receipt.items.isNotEmpty()) {
                Surface(
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📦 DETALHES SOBRE OS PRODUTOS APÍCOLAS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Text(
                                text = "${receipt.items.size} produto(s)",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        receipt.items.forEachIndexed { index, item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (index % 2 == 0) Color(0xFFF8FAFC) else Color.White)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}. ${item.description}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "R$ %,.2f".format(Locale.getDefault(), item.totalPrice),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = primaryColor
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Qtd: ${item.quantity} ${item.unit} x R$ %,.2f".format(Locale.getDefault(), item.unitPrice),
                                        fontSize = 10.sp,
                                        color = Color(0xFF475569)
                                    )

                                    val extraDetails = mutableListOf<String>()
                                    if (item.productCategory.isNotBlank()) extraDetails.add("Tipo: ${item.productCategory}")
                                    if (item.packaging.isNotBlank()) extraDetails.add("Emb: ${item.packaging}")
                                    if (item.batchNumber.isNotBlank()) extraDetails.add("Lote: ${item.batchNumber}")

                                    if (extraDetails.isNotEmpty()) {
                                        Text(
                                            text = extraDetails.joinToString(" • "),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = darkAccent
                                        )
                                    }
                                }
                            }
                            if (index < receipt.items.size - 1) {
                                HorizontalDivider(color = Color(0xFFE2E8F0))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Payment Details Box
            Surface(
                color = Color(0xFFF0FDF4),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Forma de Pagamento: ${receipt.paymentMethod}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                        if (config.showPixKey && config.pixKey.isNotBlank()) {
                            Text(
                                text = "Chave PIX (${config.pixKeyType}): ${config.pixKey} • Favorecido: ${config.pixReceiverName}",
                                fontSize = 10.sp,
                                color = Color(0xFF166534)
                            )
                        }
                    }

                    Text(
                        text = "QUITADO ✅",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF166534)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legal Term & Quitação
            Text(
                text = config.legalTerm,
                fontSize = 10.sp,
                color = Color(0xFF475569),
                lineHeight = 14.sp,
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Signatures Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                if (config.showIssuerSignature) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(150.dp)
                    ) {
                        val issuerSvg = receipt.issuerSignatureSvg ?: config.savedIssuerSignatureSvg
                        if (!issuerSvg.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(45.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                com.example.receipt.util.SignaturePathView(
                                    svgPathD = issuerSvg,
                                    strokeColor = primaryColor
                                )
                            }
                        }
                        DottedLine(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = config.ownerName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Emissor / Apicultor",
                            fontSize = 9.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (config.showCustomerSignature) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(150.dp)
                    ) {
                        val customerSvg = receipt.customerSignatureSvg
                        if (!customerSvg.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(45.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                com.example.receipt.util.SignaturePathView(
                                    svgPathD = customerSvg,
                                    strokeColor = Color(0xFF0F172A)
                                )
                            }
                        }
                        DottedLine(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = receipt.clientName.ifBlank { "Cliente / Pagador" },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Assinatura do Recebedor",
                            fontSize = 9.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(6.dp))

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (config.showQualityWatermark) {
                    Text(
                        text = "⭐ ${config.qualitySealText}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryColor
                    )
                }
                Text(
                    text = config.footerGreeting,
                    fontSize = 9.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 1. CLASSIC COMMERCIAL TEMPLATE (Canhoto, Bordas, Estilo Formal)
// -------------------------------------------------------------
@Composable
fun ClassicReceiptLayout(
    receipt: ReceiptData,
    config: ReceiptLayoutConfig,
    modifier: Modifier = Modifier
) {
    val primaryColor = config.colorTheme.composePrimary
    val darkAccent = config.colorTheme.composeDarkAccent
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateStr = sdf.format(Date(receipt.dateMillis))
    val amountStr = "R$ %,.2f".format(Locale.getDefault(), receipt.totalAmount)
    val wordsAmount = CurrencyToWordsPtBr.convert(receipt.totalAmount)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, primaryColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Tear-off Stub (Canhoto destacável de controle)
            if (config.showTearOffStub) {
                Surface(
                    color = primaryColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✂️ CANHOTO DO EMISSOR (CONTROLE APÍCOLA)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkAccent
                            )
                            Text(
                                text = "Nº ${receipt.id}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cliente: ${receipt.clientName}", fontSize = 11.sp, color = Color(0xFF334155))
                            Text("Valor: $amountStr", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                        }
                        Text("Data: $dateStr • Pgto: ${receipt.paymentMethod}", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }

                // Dashed separator line for tear-off
                DashedLine(
                    color = primaryColor.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
            }

            // Main Receipt Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (config.iconType != ReceiptIconType.NONE) {
                    Surface(
                        color = primaryColor.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = config.iconType.iconEmoji,
                                fontSize = 24.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.businessName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkAccent
                    )
                    Text(
                        text = "${config.ownerName} • CNPJ/CPF: ${config.document}",
                        fontSize = 11.sp,
                        color = Color(0xFF475569)
                    )
                    if (config.stateRegistration.isNotBlank()) {
                        Text(
                            text = "Registro: ${config.stateRegistration}",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Text(
                        text = "${config.address} • ${config.cityState}",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title Banner
            Surface(
                color = primaryColor,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = config.receiptTitle.uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    if (config.showReceiptNumber) {
                        Text(
                            text = "Nº ${receipt.id}",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount Highlight Box
            Surface(
                color = primaryColor.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VALOR RECEBIDO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkAccent
                    )
                    Text(
                        text = amountStr,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                    Text(
                        text = "($wordsAmount)",
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF334155),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customer Info Box
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "DADOS DO CLIENTE / PAGADOR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Nome: ${receipt.clientName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                    if (config.showClientDocument && receipt.clientDocument.isNotBlank()) {
                        Text("CPF/CNPJ: ${receipt.clientDocument}", fontSize = 11.sp, color = Color(0xFF475569))
                    }
                    if (receipt.clientPhone.isNotBlank()) {
                        Text("Telefone: ${receipt.clientPhone}", fontSize = 11.sp, color = Color(0xFF475569))
                    }
                    if (config.showClientAddress && receipt.clientAddress.isNotBlank()) {
                        Text("Endereço: ${receipt.clientAddress}", fontSize = 11.sp, color = Color(0xFF475569))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Items Table
            if (config.showItemsTable && receipt.items.isNotEmpty()) {
                Text(
                    text = "DISCRIMINAÇÃO DOS ITENS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                ) {
                    // Header row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("Item / Descrição", modifier = Modifier.weight(2.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        Text("Qtd", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color(0xFF334155))
                        Text("Unitário", modifier = Modifier.weight(1.2f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, color = Color(0xFF334155))
                        Text("Total", modifier = Modifier.weight(1.3f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, color = Color(0xFF334155))
                    }

                    receipt.items.forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (idx % 2 == 0) Color.White else Color(0xFFF8FAFC))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(item.description, modifier = Modifier.weight(2.5f), fontSize = 11.sp, color = Color(0xFF1E293B))
                            Text("${item.quantity} ${item.unit}", modifier = Modifier.weight(1f), fontSize = 10.sp, textAlign = TextAlign.Center, color = Color(0xFF475569))
                            Text("R$ %,.2f".format(Locale.getDefault(), item.unitPrice), modifier = Modifier.weight(1.2f), fontSize = 10.sp, textAlign = TextAlign.Right, color = Color(0xFF475569))
                            Text("R$ %,.2f".format(Locale.getDefault(), item.totalPrice), modifier = Modifier.weight(1.3f), fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, color = primaryColor)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Payment Details & PIX
            Surface(
                color = Color(0xFFF0FDF4),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Forma de Pagamento: ${receipt.paymentMethod} • Data: $dateStr",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                        if (config.showPixKey && config.pixKey.isNotBlank()) {
                            Text(
                                text = "Chave PIX (${config.pixKeyType}): ${config.pixKey}",
                                fontSize = 10.sp,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "Titular: ${config.pixReceiverName} (${config.pixBank})",
                                fontSize = 9.sp,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legal Term Text
            Surface(
                color = Color(0xFFFAFAFA),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = config.legalTerm,
                    fontSize = 10.sp,
                    color = Color(0xFF475569),
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Signatures Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                if (config.showIssuerSignature) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(140.dp)
                    ) {
                        val issuerSvg = receipt.issuerSignatureSvg ?: config.savedIssuerSignatureSvg
                        if (!issuerSvg.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                com.example.receipt.util.SignaturePathView(
                                    svgPathD = issuerSvg,
                                    strokeColor = primaryColor
                                )
                            }
                        }
                        DottedLine(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = config.ownerName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Emissor / Produtor Apícola",
                            fontSize = 9.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (config.showCustomerSignature) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(140.dp)
                    ) {
                        val customerSvg = receipt.customerSignatureSvg
                        if (!customerSvg.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                com.example.receipt.util.SignaturePathView(
                                    svgPathD = customerSvg,
                                    strokeColor = Color(0xFF0F172A)
                                )
                            }
                        }
                        DottedLine(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = receipt.clientName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Assinatura do Pagador",
                            fontSize = 9.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (config.showQualityWatermark) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = config.qualitySealText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor
                        )
                    }
                }
                Text(
                    text = config.footerGreeting,
                    fontSize = 9.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 2. MODERN CLEAN TEMPLATE (Card moderno, cores suaves, elegante)
// -------------------------------------------------------------
@Composable
fun ModernReceiptLayout(
    receipt: ReceiptData,
    config: ReceiptLayoutConfig,
    modifier: Modifier = Modifier
) {
    val primaryColor = config.colorTheme.composePrimary
    val lightBg = config.colorTheme.composeLightBg
    val darkAccent = config.colorTheme.composeDarkAccent
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateStr = sdf.format(Date(receipt.dateMillis))
    val amountStr = "R$ %,.2f".format(Locale.getDefault(), receipt.totalAmount)
    val wordsAmount = CurrencyToWordsPtBr.convert(receipt.totalAmount)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            // Header Color Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(primaryColor)
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(config.iconType.iconEmoji, fontSize = 22.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = config.businessName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = config.receiptTitle,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = receipt.id,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Body
            Column(modifier = Modifier.padding(18.dp)) {
                // Value pill
                Surface(
                    color = lightBg,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL COMPROVADO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkAccent
                        )
                        Text(
                            text = amountStr,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = darkAccent
                        )
                        Text(
                            text = wordsAmount,
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Info Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("PAGO POR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Text(receipt.clientName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            if (receipt.clientDocument.isNotBlank()) {
                                Text("Doc: ${receipt.clientDocument}", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                    }

                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("DATA & PAGAMENTO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Text(dateStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text(receipt.paymentMethod, fontSize = 10.sp, color = primaryColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (config.showItemsTable && receipt.items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Itens e Produtos Apícolas",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    receipt.items.forEach { item ->
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(8.dp).background(primaryColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(item.description, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                                        Text("${item.quantity} ${item.unit} x R$ %,.2f".format(Locale.getDefault(), item.unitPrice), fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                }
                                Text("R$ %,.2f".format(Locale.getDefault(), item.totalPrice), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                            }
                        }
                    }
                }

                if (config.showPixKey && config.pixKey.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("PIX: ${config.pixKey} (${config.pixKeyType})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                Text("Favorecido: ${config.pixReceiverName}", fontSize = 10.sp, color = Color(0xFF166534))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = config.legalTerm,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Signatures Modern
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (config.showIssuerSignature) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).padding(horizontal = 6.dp)
                        ) {
                            val issuerSvg = receipt.issuerSignatureSvg ?: config.savedIssuerSignatureSvg
                            if (!issuerSvg.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    com.example.receipt.util.SignaturePathView(
                                        svgPathD = issuerSvg,
                                        strokeColor = primaryColor
                                    )
                                }
                            }
                            DottedLine(modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(config.ownerName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text("Produtor / Emissor", fontSize = 9.sp, color = Color(0xFF64748B))
                        }
                    }
                    if (config.showCustomerSignature) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).padding(horizontal = 6.dp)
                        ) {
                            val customerSvg = receipt.customerSignatureSvg
                            if (!customerSvg.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    com.example.receipt.util.SignaturePathView(
                                        svgPathD = customerSvg,
                                        strokeColor = Color(0xFF0F172A)
                                    )
                                }
                            }
                            DottedLine(modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(receipt.clientName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text("Cliente / Pagador", fontSize = 9.sp, color = Color(0xFF64748B))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${config.qualitySealText} • ${config.footerGreeting}",
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 3. THERMAL 80MM TEMPLATE (Monospaçado, Bobina de Feira/Cupom)
// -------------------------------------------------------------
@Composable
fun ThermalReceiptLayout(
    receipt: ReceiptData,
    config: ReceiptLayoutConfig,
    modifier: Modifier = Modifier
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(receipt.dateMillis))
    val amountStr = "R$ %,.2f".format(Locale.getDefault(), receipt.totalAmount)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "================================",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black
            )
            Text(
                text = config.businessName.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black
            )
            Text(
                text = "CNPJ/CPF: ${config.document}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.DarkGray
            )
            Text(
                text = "${config.cityState} • Tel: ${config.phone}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.DarkGray
            )
            Text(
                text = "================================",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black
            )

            Text(
                text = "CUPOM / RECIBO: ${receipt.id}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.Black
            )
            Text(
                text = "DATA/HORA: $dateStr",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.Black
            )
            Text(
                text = "CLIENTE: ${receipt.clientName}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.Black
            )
            if (receipt.clientDocument.isNotBlank()) {
                Text(
                    text = "CPF/CNPJ: ${receipt.clientDocument}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color.Black
                )
            }

            Text(
                text = "--------------------------------",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Gray
            )

            if (receipt.items.isNotEmpty()) {
                receipt.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.quantity.toInt()}x ${item.description.take(18)}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "R$ %,.2f".format(Locale.getDefault(), item.totalPrice),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
                Text(
                    text = "--------------------------------",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Gray
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TOTAL:",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = amountStr,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }
            Text(
                text = "FORMA PGTO: ${receipt.paymentMethod}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.DarkGray
            )

            if (config.showPixKey && config.pixKey.isNotBlank()) {
                Text(
                    text = "CHAVE PIX: ${config.pixKey}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color.DarkGray
                )
            }

            Text(
                text = "--------------------------------",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Gray
            )

            Text(
                text = config.footerGreeting,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.DarkGray
            )

            Text(
                text = "================================",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black
            )
        }
    }
}

// -------------------------------------------------------------
// 4. ARTISANAL CERTIFICATE TEMPLATE (Rústico apícola, selo dourado)
// -------------------------------------------------------------
@Composable
fun ArtisanalReceiptLayout(
    receipt: ReceiptData,
    config: ReceiptLayoutConfig,
    modifier: Modifier = Modifier
) {
    val primaryColor = config.colorTheme.composePrimary
    val darkAccent = config.colorTheme.composeDarkAccent
    val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
    val dateStr = sdf.format(Date(receipt.dateMillis))
    val amountStr = "R$ %,.2f".format(Locale.getDefault(), receipt.totalAmount)
    val wordsAmount = CurrencyToWordsPtBr.convert(receipt.totalAmount)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(3.dp, Color(0xFFD97706), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Artisanal Crest Header
            Text(
                text = "🐝 ✨ 🍯",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = config.businessName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF78350F),
                textAlign = TextAlign.Center
            )
            Text(
                text = "CERTIFICADO DE ORIGEM & RECIBO DE QUITAÇÃO",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFB45309),
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${config.cityState} • Registro: ${config.stateRegistration}",
                fontSize = 10.sp,
                color = Color(0xFF92400E)
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFFDE68A), thickness = 2.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Body
            Text(
                text = "Certificamos que recebemos de",
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = Color(0xFF78350F)
            )
            Text(
                text = receipt.clientName.uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF451A03),
                textAlign = TextAlign.Center
            )
            if (receipt.clientDocument.isNotBlank()) {
                Text(
                    text = "Inscrito no CPF/CNPJ nº ${receipt.clientDocument}",
                    fontSize = 11.sp,
                    color = Color(0xFF78350F)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "A quantia expressa de:",
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = Color(0xFF78350F)
            )
            Surface(
                color = Color(0xFFFEF3C7),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = amountStr,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF92400E),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
            Text(
                text = "($wordsAmount)",
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic,
                color = Color(0xFF78350F),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (receipt.items.isNotEmpty()) {
                Surface(
                    color = Color.White.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "PRODUTOS DA FLORADA:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        receipt.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("• ${item.description} (${item.quantity} ${item.unit})", fontSize = 11.sp, color = Color(0xFF451A03))
                                Text("R$ %,.2f".format(Locale.getDefault(), item.totalPrice), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = config.legalTerm,
                fontSize = 10.sp,
                color = Color(0xFF78350F),
                textAlign = TextAlign.Justify,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${config.cityState}, $dateStr.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF92400E)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Signature
            val issuerSvg = receipt.issuerSignatureSvg ?: config.savedIssuerSignatureSvg
            if (!issuerSvg.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    com.example.receipt.util.SignaturePathView(
                        svgPathD = issuerSvg,
                        strokeColor = Color(0xFF78350F)
                    )
                }
            }
            DottedLine(modifier = Modifier.width(220.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = config.ownerName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF451A03)
            )
            Text(
                text = "Manejo Apícola Sustentável",
                fontSize = 10.sp,
                color = Color(0xFF92400E)
            )

            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = Color(0xFFFDE68A),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "⭐ ${config.qualitySealText}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF78350F),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun DashedLine(
    color: Color = Color.Gray,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.height(2.dp)) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 2.dp.toPx(),
            pathEffect = pathEffect
        )
    }
}

@Composable
fun DottedLine(
    color: Color = Color(0xFF64748B),
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.height(1.dp)) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = pathEffect
        )
    }
}
