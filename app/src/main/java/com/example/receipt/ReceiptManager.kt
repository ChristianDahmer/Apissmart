package com.example.receipt

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.local.entities.FinancialTransactionEntity
import com.example.receipt.model.ReceiptColorTheme
import com.example.receipt.model.ReceiptDensity
import com.example.receipt.model.ReceiptIconType
import com.example.receipt.model.ReceiptItem
import com.example.receipt.model.ReceiptLayoutConfig
import com.example.receipt.model.ReceiptTemplateStyle
import com.example.receipt.model.ReceiptData
import com.example.receipt.util.CurrencyToWordsPtBr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReceiptManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("apis_smart_receipt_prefs", Context.MODE_PRIVATE)

    private val _layoutConfig = MutableStateFlow(loadLayoutConfig())
    val layoutConfig: StateFlow<ReceiptLayoutConfig> = _layoutConfig.asStateFlow()

    private val _receipts = MutableStateFlow(loadReceipts())
    val receipts: StateFlow<List<ReceiptData>> = _receipts.asStateFlow()

    init {
        if (_receipts.value.isEmpty()) {
            val sampleReceipts = createSampleReceipts()
            saveReceiptsList(sampleReceipts)
            _receipts.value = sampleReceipts
        }
    }

    // --- Layout Configuration Persistence ---

    fun loadLayoutConfig(): ReceiptLayoutConfig {
        val templateStr = prefs.getString(KEY_TEMPLATE, ReceiptTemplateStyle.CLASSIC_COMMERCIAL.name) ?: ReceiptTemplateStyle.CLASSIC_COMMERCIAL.name
        val colorThemeStr = prefs.getString(KEY_COLOR_THEME, ReceiptColorTheme.HONEY_GOLD.name) ?: ReceiptColorTheme.HONEY_GOLD.name
        val iconTypeStr = prefs.getString(KEY_ICON_TYPE, ReceiptIconType.QUEEN_BEE.name) ?: ReceiptIconType.QUEEN_BEE.name
        val densityStr = prefs.getString(KEY_DENSITY, ReceiptDensity.STANDARD.name) ?: ReceiptDensity.STANDARD.name

        return ReceiptLayoutConfig(
            templateStyle = try { ReceiptTemplateStyle.valueOf(templateStr) } catch (_: Exception) { ReceiptTemplateStyle.CLASSIC_COMMERCIAL },
            colorTheme = try { ReceiptColorTheme.valueOf(colorThemeStr) } catch (_: Exception) { ReceiptColorTheme.HONEY_GOLD },
            iconType = try { ReceiptIconType.valueOf(iconTypeStr) } catch (_: Exception) { ReceiptIconType.QUEEN_BEE },
            density = try { ReceiptDensity.valueOf(densityStr) } catch (_: Exception) { ReceiptDensity.STANDARD },

            businessName = prefs.getString(KEY_BIZ_NAME, "Apiário & Entreposto Vale das Abelhas") ?: "Apiário & Entreposto Vale das Abelhas",
            ownerName = prefs.getString(KEY_OWNER_NAME, "José Silveira (Apicultor Responsável)") ?: "José Silveira (Apicultor Responsável)",
            document = prefs.getString(KEY_DOC, "24.582.910/0001-45") ?: "24.582.910/0001-45",
            stateRegistration = prefs.getString(KEY_STATE_REG, "Produtor Rural MG 042.819-0") ?: "Produtor Rural MG 042.819-0",
            phone = prefs.getString(KEY_PHONE, "(35) 99876-5432") ?: "(35) 99876-5432",
            email = prefs.getString(KEY_EMAIL, "contato@valedasabelhas.com.br") ?: "contato@valedasabelhas.com.br",
            address = prefs.getString(KEY_ADDRESS, "Estrada Rural dos Mananciais, Km 4 - Sítio Mel Dourado") ?: "Estrada Rural dos Mananciais, Km 4 - Sítio Mel Dourado",
            cityState = prefs.getString(KEY_CITY_STATE, "Passa Quatro - MG") ?: "Passa Quatro - MG",

            showPixKey = prefs.getBoolean(KEY_SHOW_PIX, true),
            pixKey = prefs.getString(KEY_PIX_KEY, "contato@valedasabelhas.com.br") ?: "contato@valedasabelhas.com.br",
            pixKeyType = prefs.getString(KEY_PIX_TYPE, "E-mail") ?: "E-mail",
            pixReceiverName = prefs.getString(KEY_PIX_RECEIVER, "José Silveira / Apiário Vale das Abelhas") ?: "José Silveira / Apiário Vale das Abelhas",
            pixBank = prefs.getString(KEY_PIX_BANK, "Banco do Brasil / Sicredi") ?: "Banco do Brasil / Sicredi",

            showReceiptNumber = prefs.getBoolean(KEY_SHOW_NUM, true),
            showDueDateOrPaymentDate = prefs.getBoolean(KEY_SHOW_DATE, true),
            showItemsTable = prefs.getBoolean(KEY_SHOW_ITEMS, true),
            showIssuerSignature = prefs.getBoolean(KEY_SHOW_ISSUER_SIG, true),
            showCustomerSignature = prefs.getBoolean(KEY_SHOW_CUST_SIG, true),
            showTearOffStub = prefs.getBoolean(KEY_SHOW_STUB, true),
            showQualityWatermark = prefs.getBoolean(KEY_SHOW_SEAL, true),
            showClientDocument = prefs.getBoolean(KEY_SHOW_CLIENT_DOC, true),
            showClientAddress = prefs.getBoolean(KEY_SHOW_CLIENT_ADDR, true),

            receiptTitle = prefs.getString(KEY_RECEIPT_TITLE, "RECIBO DE VENDA & PAGAMENTO APÍCOLA") ?: "RECIBO DE VENDA & PAGAMENTO APÍCOLA",
            legalTerm = prefs.getString(KEY_LEGAL_TERM, "Declaramos haver recebido a quantia supra discriminada, referente à aquisição de produtos apícolas de manejo sustentável e/ou prestação de serviços apícolas, dando plena, rasa e geral quitação de pago.") ?: "Declaramos haver recebido a quantia supra discriminada, referente à aquisição de produtos apícolas de manejo sustentável e/ou prestação de serviços apícolas, dando plena, rasa e geral quitação de pago.",
            footerGreeting = prefs.getString(KEY_FOOTER_GREETING, "Agradecemos pela preferência! Mel 100% puro e natural, colhido com carinho e respeito às abelhas.") ?: "Agradecemos pela preferência! Mel 100% puro e natural, colhido com carinho e respeito às abelhas.",
            qualitySealText = prefs.getString(KEY_QUALITY_SEAL, "Selo ApisSmart de Pureza & Boas Práticas Apícolas") ?: "Selo ApisSmart de Pureza & Boas Práticas Apícolas",
            savedIssuerSignatureSvg = prefs.getString(KEY_SAVED_ISSUER_SIG, null)
        )
    }

    fun saveLayoutConfig(config: ReceiptLayoutConfig) {
        prefs.edit()
            .putString(KEY_TEMPLATE, config.templateStyle.name)
            .putString(KEY_COLOR_THEME, config.colorTheme.name)
            .putString(KEY_ICON_TYPE, config.iconType.name)
            .putString(KEY_DENSITY, config.density.name)
            .putString(KEY_BIZ_NAME, config.businessName)
            .putString(KEY_OWNER_NAME, config.ownerName)
            .putString(KEY_DOC, config.document)
            .putString(KEY_STATE_REG, config.stateRegistration)
            .putString(KEY_PHONE, config.phone)
            .putString(KEY_EMAIL, config.email)
            .putString(KEY_ADDRESS, config.address)
            .putString(KEY_CITY_STATE, config.cityState)
            .putBoolean(KEY_SHOW_PIX, config.showPixKey)
            .putString(KEY_PIX_KEY, config.pixKey)
            .putString(KEY_PIX_TYPE, config.pixKeyType)
            .putString(KEY_PIX_RECEIVER, config.pixReceiverName)
            .putString(KEY_PIX_BANK, config.pixBank)
            .putBoolean(KEY_SHOW_NUM, config.showReceiptNumber)
            .putBoolean(KEY_SHOW_DATE, config.showDueDateOrPaymentDate)
            .putBoolean(KEY_SHOW_ITEMS, config.showItemsTable)
            .putBoolean(KEY_SHOW_ISSUER_SIG, config.showIssuerSignature)
            .putBoolean(KEY_SHOW_CUST_SIG, config.showCustomerSignature)
            .putBoolean(KEY_SHOW_STUB, config.showTearOffStub)
            .putBoolean(KEY_SHOW_SEAL, config.showQualityWatermark)
            .putBoolean(KEY_SHOW_CLIENT_DOC, config.showClientDocument)
            .putBoolean(KEY_SHOW_CLIENT_ADDR, config.showClientAddress)
            .putString(KEY_RECEIPT_TITLE, config.receiptTitle)
            .putString(KEY_LEGAL_TERM, config.legalTerm)
            .putString(KEY_FOOTER_GREETING, config.footerGreeting)
            .putString(KEY_QUALITY_SEAL, config.qualitySealText)
            .putString(KEY_SAVED_ISSUER_SIG, config.savedIssuerSignatureSvg)
            .apply()

        _layoutConfig.value = config
    }

    fun resetLayoutConfigToDefault() {
        val defaultConfig = ReceiptLayoutConfig()
        saveLayoutConfig(defaultConfig)
    }

    // --- Receipts Persistence & Operations ---

    fun getNextReceiptNumber(): Int {
        val currentMax = _receipts.value.maxOfOrNull { it.receiptNumber } ?: 0
        return currentMax + 1
    }

    fun saveReceipt(receipt: ReceiptData) {
        val current = _receipts.value.toMutableList()
        val index = current.indexOfFirst { it.id == receipt.id }
        if (index >= 0) {
            current[index] = receipt
        } else {
            current.add(0, receipt)
        }
        saveReceiptsList(current)
        _receipts.value = current
    }

    fun deleteReceipt(receiptId: String) {
        val current = _receipts.value.filterNot { it.id == receiptId }
        saveReceiptsList(current)
        _receipts.value = current
    }

    fun getReceiptById(id: String): ReceiptData? {
        return _receipts.value.find { it.id == id }
    }

    fun createReceiptFromTransaction(
        transaction: FinancialTransactionEntity,
        clientName: String = "Cliente Balcão"
    ): ReceiptData {
        val number = getNextReceiptNumber()
        val formattedNum = "%04d".format(Locale.getDefault(), number)
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(transaction.dateMillis))

        val singleItem = ReceiptItem(
            description = transaction.category + if (transaction.description.isNotBlank()) " - ${transaction.description}" else "",
            quantity = transaction.quantityKgOrUnits ?: 1.0,
            unit = if (transaction.category.contains("Mel", ignoreCase = true)) "kg" else "unid",
            unitPrice = if ((transaction.quantityKgOrUnits ?: 1.0) > 0) {
                transaction.amount / (transaction.quantityKgOrUnits ?: 1.0)
            } else transaction.amount,
            totalPrice = transaction.amount
        )

        return ReceiptData(
            id = "REC-$year-$formattedNum",
            receiptNumber = number,
            transactionId = transaction.id,
            dateMillis = transaction.dateMillis,
            clientName = clientName,
            clientDocument = "",
            clientPhone = "",
            clientAddress = "",
            totalAmount = transaction.amount,
            paymentMethod = transaction.paymentMethod,
            items = listOf(singleItem),
            notes = "Lançamento vinculado ao Caixa do Apiário (Transação #${transaction.id})",
            status = "EMITIDO"
        )
    }

    private fun loadReceipts(): List<ReceiptData> {
        val jsonStr = prefs.getString(KEY_RECEIPTS_JSON, null) ?: return emptyList()
        return try {
            val list = mutableListOf<ReceiptData>()
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val itemsList = mutableListOf<ReceiptItem>()
                val itemsArray = obj.optJSONArray("items")
                if (itemsArray != null) {
                    for (j in 0 until itemsArray.length()) {
                        val itemObj = itemsArray.getJSONObject(j)
                        itemsList.add(
                            ReceiptItem(
                                id = itemObj.optString("id", java.util.UUID.randomUUID().toString()),
                                description = itemObj.optString("description", ""),
                                quantity = itemObj.optDouble("quantity", 1.0),
                                unit = itemObj.optString("unit", "unid"),
                                unitPrice = itemObj.optDouble("unitPrice", 0.0),
                                totalPrice = itemObj.optDouble("totalPrice", 0.0),
                                batchNumber = itemObj.optString("batchNumber", ""),
                                productCategory = itemObj.optString("productCategory", ""),
                                packaging = itemObj.optString("packaging", "")
                            )
                        )
                    }
                }
                list.add(
                    ReceiptData(
                        id = obj.getString("id"),
                        receiptNumber = obj.optInt("receiptNumber", i + 1),
                        transactionId = if (obj.has("transactionId") && !obj.isNull("transactionId")) obj.getLong("transactionId") else null,
                        dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                        clientName = obj.optString("clientName", ""),
                        clientDocument = obj.optString("clientDocument", ""),
                        clientPhone = obj.optString("clientPhone", ""),
                        clientAddress = obj.optString("clientAddress", ""),
                        totalAmount = obj.optDouble("totalAmount", 0.0),
                        paymentMethod = obj.optString("paymentMethod", "PIX"),
                        items = itemsList,
                        notes = obj.optString("notes", ""),
                        status = obj.optString("status", "EMITIDO"),
                        issuerSignatureSvg = obj.optString("issuerSignatureSvg", null),
                        customerSignatureSvg = obj.optString("customerSignatureSvg", null)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveReceiptsList(list: List<ReceiptData>) {
        val array = JSONArray()
        for (r in list) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("receiptNumber", r.receiptNumber)
            if (r.transactionId != null) obj.put("transactionId", r.transactionId)
            obj.put("dateMillis", r.dateMillis)
            obj.put("clientName", r.clientName)
            obj.put("clientDocument", r.clientDocument)
            obj.put("clientPhone", r.clientPhone)
            obj.put("clientAddress", r.clientAddress)
            obj.put("totalAmount", r.totalAmount)
            obj.put("paymentMethod", r.paymentMethod)
            obj.put("notes", r.notes)
            obj.put("status", r.status)
            if (r.issuerSignatureSvg != null) obj.put("issuerSignatureSvg", r.issuerSignatureSvg)
            if (r.customerSignatureSvg != null) obj.put("customerSignatureSvg", r.customerSignatureSvg)

            val itemsArray = JSONArray()
            for (item in r.items) {
                val itemObj = JSONObject()
                itemObj.put("id", item.id)
                itemObj.put("description", item.description)
                itemObj.put("quantity", item.quantity)
                itemObj.put("unit", item.unit)
                itemObj.put("unitPrice", item.unitPrice)
                itemObj.put("totalPrice", item.totalPrice)
                if (item.batchNumber.isNotBlank()) itemObj.put("batchNumber", item.batchNumber)
                if (item.productCategory.isNotBlank()) itemObj.put("productCategory", item.productCategory)
                if (item.packaging.isNotBlank()) itemObj.put("packaging", item.packaging)
                itemsArray.put(itemObj)
            }
            obj.put("items", itemsArray)
            array.put(obj)
        }
        prefs.edit().putString(KEY_RECEIPTS_JSON, array.toString()).apply()
    }

    private fun createSampleReceipts(): List<ReceiptData> {
        val now = System.currentTimeMillis()
        val oneDay = 86400000L
        return listOf(
            ReceiptData(
                id = "REC-2026-0001",
                receiptNumber = 1,
                dateMillis = now - (oneDay * 2),
                clientName = "Empório & Mercearia Serra Verde",
                clientDocument = "12.345.678/0001-90",
                clientPhone = "(35) 98811-2233",
                clientAddress = "Av. Central, 450 - Centro",
                totalAmount = 540.0,
                paymentMethod = "PIX",
                items = listOf(
                    ReceiptItem(description = "Mel Puro Silvestre 1kg (Pote de Vidro)", quantity = 10.0, unit = "unid", unitPrice = 45.0, totalPrice = 450.0),
                    ReceiptItem(description = "Extrato de Própolis Verde 30ml", quantity = 3.0, unit = "unid", unitPrice = 30.0, totalPrice = 90.0)
                ),
                notes = "Entrega no estabelecimento. Lote #2026-04",
                status = "ASSINADO"
            ),
            ReceiptData(
                id = "REC-2026-0002",
                receiptNumber = 2,
                dateMillis = now - oneDay,
                clientName = "Maria Helena Nogueira",
                clientDocument = "087.654.321-10",
                clientPhone = "(35) 99122-3344",
                clientAddress = "Rua das Flores, 88",
                totalAmount = 145.0,
                paymentMethod = "Dinheiro",
                items = listOf(
                    ReceiptItem(description = "Pólen Apícola Desidratado 150g", quantity = 2.0, unit = "pote", unitPrice = 35.0, totalPrice = 70.0),
                    ReceiptItem(description = "Mel Florada Laranjeira 500g (Bisnaga)", quantity = 3.0, unit = "unid", unitPrice = 25.0, totalPrice = 75.0)
                ),
                notes = "Venda direta no apiário / feira.",
                status = "EMITIDO"
            )
        )
    }

    // --- Export, Print & Sharing Formatters ---

    fun generateFormattedReceiptText(receipt: ReceiptData, config: ReceiptLayoutConfig): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateStr = sdf.format(Date(receipt.dateMillis))
        val amountStr = "R$ %,.2f".format(Locale.getDefault(), receipt.totalAmount)
        val amountInWords = CurrencyToWordsPtBr.convert(receipt.totalAmount)

        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("  ${config.businessName.uppercase()}")
        sb.appendLine("  ${config.receiptTitle}")
        sb.appendLine("==========================================")
        if (config.showReceiptNumber) {
            sb.appendLine("Recibo Nº: ${receipt.id}")
        }
        sb.appendLine("Data de Emissão: $dateStr")
        sb.appendLine("VALOR TOTAL: $amountStr")
        sb.appendLine("($amountInWords)")
        sb.appendLine("------------------------------------------")

        sb.appendLine("DADOS DO CLIENTE / PAGADOR:")
        sb.appendLine("Nome: ${receipt.clientName}")
        if (config.showClientDocument && receipt.clientDocument.isNotBlank()) {
            sb.appendLine("CPF/CNPJ: ${receipt.clientDocument}")
        }
        if (receipt.clientPhone.isNotBlank()) {
            sb.appendLine("Telefone: ${receipt.clientPhone}")
        }
        if (config.showClientAddress && receipt.clientAddress.isNotBlank()) {
            sb.appendLine("Endereço: ${receipt.clientAddress}")
        }
        sb.appendLine("------------------------------------------")

        if (config.showItemsTable && receipt.items.isNotEmpty()) {
            sb.appendLine("PRODUTOS / SERVIÇOS APÍCOLAS:")
            for (item in receipt.items) {
                val itemVal = "R$ %,.2f".format(Locale.getDefault(), item.totalPrice)
                val unitPrice = "R$ %,.2f".format(Locale.getDefault(), item.unitPrice)
                sb.appendLine("• ${item.description}")
                sb.appendLine("  Qtd: ${item.quantity} ${item.unit} x $unitPrice = $itemVal")
            }
            sb.appendLine("------------------------------------------")
        }

        sb.appendLine("Forma de Pagamento: ${receipt.paymentMethod}")
        if (config.showPixKey && config.pixKey.isNotBlank()) {
            sb.appendLine("Chave PIX: ${config.pixKey} (${config.pixKeyType})")
            sb.appendLine("Favorecido: ${config.pixReceiverName}")
        }

        sb.appendLine("------------------------------------------")
        sb.appendLine(config.legalTerm)
        sb.appendLine("------------------------------------------")

        sb.appendLine("EMISSOR:")
        sb.appendLine(config.ownerName)
        sb.appendLine("${config.businessName} - CNPJ/CPF: ${config.document}")
        if (config.stateRegistration.isNotBlank()) {
            sb.appendLine("Registro: ${config.stateRegistration}")
        }
        sb.appendLine("Contato: ${config.phone} • ${config.cityState}")

        if (config.showQualityWatermark) {
            sb.appendLine("🌱 ${config.qualitySealText}")
        }
        sb.appendLine("\n${config.footerGreeting}")
        sb.appendLine("==========================================")
        return sb.toString()
    }

    fun generateHtmlReceipt(receipt: ReceiptData, config: ReceiptLayoutConfig): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateStr = sdf.format(Date(receipt.dateMillis))
        val amountStr = "R$ %,.2f".format(Locale.getDefault(), receipt.totalAmount)
        val amountInWords = CurrencyToWordsPtBr.convert(receipt.totalAmount)
        val primaryHex = "#%06X".format(0xFFFFFF and config.colorTheme.primaryColor.toInt())

        val itemsHtml = StringBuilder()
        for (item in receipt.items) {
            val totalStr = "R$ %,.2f".format(Locale.getDefault(), item.totalPrice)
            val unitStr = "R$ %,.2f".format(Locale.getDefault(), item.unitPrice)
            itemsHtml.append("""
                <tr>
                    <td style="padding: 6px 8px; border-bottom: 1px solid #e2e8f0;">${item.description}</td>
                    <td style="padding: 6px 8px; border-bottom: 1px solid #e2e8f0; text-align: center;">${item.quantity} ${item.unit}</td>
                    <td style="padding: 6px 8px; border-bottom: 1px solid #e2e8f0; text-align: right;">$unitStr</td>
                    <td style="padding: 6px 8px; border-bottom: 1px solid #e2e8f0; text-align: right; font-weight: bold;">$totalStr</td>
                </tr>
            """.trimIndent())
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>${config.receiptTitle} - ${receipt.id}</title>
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif; margin: 0; padding: 20px; color: #1e293b; background: #fff; }
                .receipt-container { max-width: 650px; margin: 0 auto; border: 2px solid $primaryHex; border-radius: 12px; padding: 24px; box-sizing: border-box; }
                .header { text-align: center; border-bottom: 2px dashed $primaryHex; padding-bottom: 16px; margin-bottom: 16px; }
                .logo-emoji { font-size: 32px; margin-bottom: 4px; }
                .biz-name { font-size: 20px; font-weight: bold; color: $primaryHex; margin: 0; }
                .biz-sub { font-size: 12px; color: #64748b; margin-top: 4px; }
                .title-badge { display: inline-block; background: $primaryHex; color: #fff; padding: 4px 14px; border-radius: 20px; font-weight: bold; font-size: 13px; margin-top: 10px; }
                .meta-row { display: flex; justify-content: space-between; margin-bottom: 16px; font-size: 13px; }
                .amount-card { background: #f8fafc; border: 1px solid #cbd5e1; border-radius: 8px; padding: 12px; margin-bottom: 16px; text-align: center; }
                .amount-value { font-size: 24px; font-weight: 800; color: $primaryHex; }
                .amount-words { font-size: 12px; font-style: italic; color: #475569; margin-top: 4px; }
                .section-title { font-size: 13px; font-weight: bold; text-transform: uppercase; color: $primaryHex; border-bottom: 1px solid #e2e8f0; padding-bottom: 4px; margin-top: 14px; margin-bottom: 8px; }
                table { width: 100%; border-collapse: collapse; font-size: 12px; margin-bottom: 14px; }
                th { background: #f1f5f9; padding: 6px 8px; text-align: left; font-weight: bold; }
                .legal-text { font-size: 11px; color: #475569; line-height: 1.4; text-align: justify; margin: 14px 0; padding: 10px; background: #fafafa; border-radius: 6px; }
                .signatures { display: flex; justify-content: space-around; margin-top: 30px; text-align: center; font-size: 12px; }
                .sig-line { width: 220px; border-top: 1px solid #334155; padding-top: 4px; margin-top: 40px; }
                .footer { text-align: center; font-size: 11px; color: #94a3b8; margin-top: 20px; border-top: 1px solid #e2e8f0; padding-top: 12px; }
            </style>
        </head>
        <body>
            <div class="receipt-container">
                <div class="header">
                    <div class="logo-emoji">${config.iconType.iconEmoji}</div>
                    <h1 class="biz-name">${config.businessName}</h1>
                    <div class="biz-sub">${config.ownerName} • CNPJ/CPF: ${config.document}</div>
                    <div class="biz-sub">${config.address} • ${config.cityState} • Tel: ${config.phone}</div>
                    <div><span class="title-badge">${config.receiptTitle}</span></div>
                </div>

                <div class="meta-row">
                    <div><strong>Nº DO RECIBO:</strong> ${receipt.id}</div>
                    <div><strong>DATA:</strong> $dateStr</div>
                    <div><strong>FORMA:</strong> ${receipt.paymentMethod}</div>
                </div>

                <div class="amount-card">
                    <div class="amount-value">$amountStr</div>
                    <div class="amount-words">($amountInWords)</div>
                </div>

                <div class="section-title">Dados do Cliente / Pagador</div>
                <div style="font-size: 13px; line-height: 1.5; margin-bottom: 12px;">
                    <div><strong>Nome:</strong> ${receipt.clientName}</div>
                    ${if (config.showClientDocument && receipt.clientDocument.isNotBlank()) "<div><strong>CPF/CNPJ:</strong> ${receipt.clientDocument}</div>" else ""}
                    ${if (receipt.clientPhone.isNotBlank()) "<div><strong>Telefone:</strong> ${receipt.clientPhone}</div>" else ""}
                    ${if (config.showClientAddress && receipt.clientAddress.isNotBlank()) "<div><strong>Endereço:</strong> ${receipt.clientAddress}</div>" else ""}
                </div>

                ${if (config.showItemsTable && receipt.items.isNotEmpty()) """
                    <div class="section-title">Discriminação dos Produtos / Serviços</div>
                    <table>
                        <thead>
                            <tr>
                                <th>Item / Descrição</th>
                                <th style="text-align: center;">Qtd</th>
                                <th style="text-align: right;">Unitário</th>
                                <th style="text-align: right;">Total</th>
                            </tr>
                        </thead>
                        <tbody>
                            $itemsHtml
                        </tbody>
                    </table>
                """ else ""}

                ${if (config.showPixKey && config.pixKey.isNotBlank()) """
                    <div style="background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 6px; padding: 8px 12px; font-size: 12px; margin-bottom: 12px;">
                        <strong>🔑 Chave PIX (${config.pixKeyType}):</strong> ${config.pixKey} | <strong>Favorecido:</strong> ${config.pixReceiverName} (${config.pixBank})
                    </div>
                """ else ""}

                <div class="legal-text">
                    ${config.legalTerm}
                </div>

                <div class="signatures">
                    ${if (config.showIssuerSignature) """
                        <div style="text-align: center;">
                            ${let {
                                val issuerSvg = receipt.issuerSignatureSvg ?: config.savedIssuerSignatureSvg
                                if (!issuerSvg.isNullOrBlank()) {
                                    com.example.receipt.util.ReceiptSignatureHelper.toHtmlSvgString(issuerSvg, primaryHex)
                                } else ""
                            }}
                            <div class="sig-line">
                                <strong>${config.ownerName}</strong><br>
                                <span style="font-size: 10px; color: #64748b;">Emissor / Apicultor</span>
                            </div>
                        </div>
                    """ else ""}
                    ${if (config.showCustomerSignature) """
                        <div style="text-align: center;">
                            ${let {
                                val customerSvg = receipt.customerSignatureSvg
                                if (!customerSvg.isNullOrBlank()) {
                                    com.example.receipt.util.ReceiptSignatureHelper.toHtmlSvgString(customerSvg, "#0F172A")
                                } else ""
                            }}
                            <div class="sig-line">
                                <strong>${receipt.clientName}</strong><br>
                                <span style="font-size: 10px; color: #64748b;">Cliente / Pagador</span>
                            </div>
                        </div>
                    """ else ""}
                </div>

                <div class="footer">
                    <div>🌱 ${config.qualitySealText}</div>
                    <div style="margin-top: 4px;">${config.footerGreeting}</div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun printOrSavePdf(receipt: ReceiptData, config: ReceiptLayoutConfig) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager != null) {
                    val webView = WebView(context)
                    webView.settings.javaScriptEnabled = true
                    webView.settings.domStorageEnabled = true
                    val htmlContent = generateHtmlReceipt(receipt, config)

                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            val printAdapter = webView.createPrintDocumentAdapter("Recibo_${receipt.id}")
                            val jobName = "ApisSmart_Recibo_${receipt.id}"
                            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
                        }
                    }
                    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                } else {
                    com.example.receipt.util.ReceiptPdfGenerator.savePdfToDownloads(context, receipt, config)
                }
            } catch (_: Exception) {
                com.example.receipt.util.ReceiptPdfGenerator.savePdfToDownloads(context, receipt, config)
            }
        }
    }

    fun savePdfToDownloads(receipt: ReceiptData, config: ReceiptLayoutConfig) {
        com.example.receipt.util.ReceiptPdfGenerator.savePdfToDownloads(context, receipt, config)
    }

    fun sharePdf(receipt: ReceiptData, config: ReceiptLayoutConfig, targetWhatsApp: Boolean = false) {
        com.example.receipt.util.ReceiptPdfGenerator.sharePdf(context, receipt, config, targetWhatsApp)
    }

    fun createShareIntent(receipt: ReceiptData, config: ReceiptLayoutConfig): Intent {
        val text = generateFormattedReceiptText(receipt, config)
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Recibo de Pagamento - ${receipt.id} (${config.businessName})")
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    companion object {
        private const val KEY_TEMPLATE = "layout_template"
        private const val KEY_COLOR_THEME = "layout_color_theme"
        private const val KEY_ICON_TYPE = "layout_icon_type"
        private const val KEY_DENSITY = "layout_density"
        private const val KEY_BIZ_NAME = "layout_biz_name"
        private const val KEY_OWNER_NAME = "layout_owner_name"
        private const val KEY_DOC = "layout_doc"
        private const val KEY_STATE_REG = "layout_state_reg"
        private const val KEY_PHONE = "layout_phone"
        private const val KEY_EMAIL = "layout_email"
        private const val KEY_ADDRESS = "layout_address"
        private const val KEY_CITY_STATE = "layout_city_state"
        private const val KEY_SHOW_PIX = "layout_show_pix"
        private const val KEY_PIX_KEY = "layout_pix_key"
        private const val KEY_PIX_TYPE = "layout_pix_type"
        private const val KEY_PIX_RECEIVER = "layout_pix_receiver"
        private const val KEY_PIX_BANK = "layout_pix_bank"
        private const val KEY_SHOW_NUM = "layout_show_num"
        private const val KEY_SHOW_DATE = "layout_show_date"
        private const val KEY_SHOW_ITEMS = "layout_show_items"
        private const val KEY_SHOW_ISSUER_SIG = "layout_show_issuer_sig"
        private const val KEY_SHOW_CUST_SIG = "layout_show_cust_sig"
        private const val KEY_SHOW_STUB = "layout_show_stub"
        private const val KEY_SHOW_SEAL = "layout_show_seal"
        private const val KEY_SHOW_CLIENT_DOC = "layout_show_client_doc"
        private const val KEY_SHOW_CLIENT_ADDR = "layout_show_client_addr"
        private const val KEY_RECEIPT_TITLE = "layout_receipt_title"
        private const val KEY_LEGAL_TERM = "layout_legal_term"
        private const val KEY_FOOTER_GREETING = "layout_footer_greeting"
        private const val KEY_QUALITY_SEAL = "layout_quality_seal"
        private const val KEY_SAVED_ISSUER_SIG = "layout_saved_issuer_sig"
        private const val KEY_RECEIPTS_JSON = "receipts_storage_json"
    }
}
