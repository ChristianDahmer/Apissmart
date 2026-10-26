package com.example.receipt.model

import androidx.compose.ui.graphics.Color

enum class ReceiptTemplateStyle(val label: String, val description: String) {
    CLASSIC_TRADITIONAL(
        label = "Recibo Clássico Tradicional (Talão)",
        description = "Layout clássico estilo talão de papelão com valor por extenso destacado, guilhoché e discriminação detalhada dos produtos"
    ),
    CLASSIC_COMMERCIAL(
        label = "Clássico Comercial",
        description = "Bordas duplas tradicionais, canhoto destacável e linhas de assinatura formais"
    ),
    MODERN_CLEAN(
        label = "Moderno Clean",
        description = "Design contemporâneo, cabeçalho suave, badges de status e tipografia fluida"
    ),
    THERMAL_80MM(
        label = "Cupom Térmico 80mm",
        description = "Formato estreito monospaçado estilo bobina térmica de feiras e impressoras portáteis"
    ),
    ARTISANAL_CERTIFICATE(
        label = "Certificado Apícola Artesanal",
        description = "Moldura estilizada, brasão apícola e selo dourado de pureza e manejo sustentável"
    )
}

enum class ReceiptColorTheme(
    val label: String,
    val primaryColor: Long,
    val lightBgColor: Long,
    val darkAccentColor: Long
) {
    HONEY_GOLD(
        label = "🍯 Dourado Mel",
        primaryColor = 0xFFD97706,
        lightBgColor = 0xFFFFFBEB,
        darkAccentColor = 0xFF92400E
    ),
    FOREST_GREEN(
        label = "🌿 Verde Floresta",
        primaryColor = 0xFF059669,
        lightBgColor = 0xFFECFDF5,
        darkAccentColor = 0xFF065F46
    ),
    ROYAL_BLUE(
        label = "🌊 Azul Colonial",
        primaryColor = 0xFF0284C7,
        lightBgColor = 0xFFF0F9FF,
        darkAccentColor = 0xFF075985
    ),
    SLATE_DARK(
        label = "🖤 Grafite Nobre",
        primaryColor = 0xFF334155,
        lightBgColor = 0xFFF8FAFC,
        darkAccentColor = 0xFF0F172A
    ),
    PROPOLIS_AMBER(
        label = "🪵 Âmbar Própolis",
        primaryColor = 0xFFEA580C,
        lightBgColor = 0xFFFFF7ED,
        darkAccentColor = 0xFF9A3412
    ),
    BURGUNDY(
        label = "🍇 Vinho Nobre",
        primaryColor = 0xFF881337,
        lightBgColor = 0xFFFFF1F2,
        darkAccentColor = 0xFF4C0519
    );

    val composePrimary: Color get() = Color(primaryColor)
    val composeLightBg: Color get() = Color(lightBgColor)
    val composeDarkAccent: Color get() = Color(darkAccentColor)
}

enum class ReceiptIconType(val label: String, val iconEmoji: String) {
    QUEEN_BEE("Abelha Rainha", "🐝"),
    HONEY_POT("Pote de Mel & Favo", "🍯"),
    CERTIFIED_SEAL("Selo de Pureza", "⭐"),
    NATURE_LEAF("Mata & Florada", "🌿"),
    NONE("Sem Ícone", "—")
}

enum class ReceiptDensity(val label: String, val spacingMultiplier: Float) {
    COMPACT("Compacto (Econômico)", 0.75f),
    STANDARD("Padrão Equilibrado", 1.0f),
    EXPANDED("Espaçoso & Confortável", 1.25f)
}

data class ReceiptLayoutConfig(
    val templateStyle: ReceiptTemplateStyle = ReceiptTemplateStyle.CLASSIC_TRADITIONAL,
    val colorTheme: ReceiptColorTheme = ReceiptColorTheme.HONEY_GOLD,
    val iconType: ReceiptIconType = ReceiptIconType.QUEEN_BEE,
    val density: ReceiptDensity = ReceiptDensity.STANDARD,
    
    // Dados do Produtor / Emissor
    val businessName: String = "Apiário & Entreposto Vale das Abelhas",
    val ownerName: String = "Produtor Apícola",
    val document: String = "00.000.000/0001-00",
    val stateRegistration: String = "Produtor Rural / SIM-014",
    val phone: String = "(11) 98765-4321",
    val email: String = "contato@apissmart.com.br",
    val address: String = "Estrada das Flores, Km 12 - Zona Rural",
    val cityState: String = "Passa Quatro - MG",

    // Pagamento & Chave PIX
    val showPixKey: Boolean = true,
    val pixKey: String = "contato@apissmart.com.br",
    val pixKeyType: String = "E-mail",
    val pixReceiverName: String = "Apiário Vale das Abelhas",
    val pixBank: String = "Banco do Brasil / Cooperativa Sicredi",

    // Opções de Visibilidade dos Elementos
    val showReceiptNumber: Boolean = true,
    val showDueDateOrPaymentDate: Boolean = true,
    val showItemsTable: Boolean = true,
    val showIssuerSignature: Boolean = true,
    val showCustomerSignature: Boolean = true,
    val showTearOffStub: Boolean = true,
    val showQualityWatermark: Boolean = true,
    val showClientDocument: Boolean = true,
    val showClientAddress: Boolean = true,

    // Título e Textos Legais Personalizáveis
    val receiptTitle: String = "RECIBO DE VENDA E PAGAMENTO",
    val legalTerm: String = "Recebemos a importância supra descrita referente aos produtos apícolas e/ou serviços abaixo discriminados, dando plena, rasa e geral quitação de pago.",
    val footerGreeting: String = "Agradecemos pela preferência! Nosso mel é 100% puro e colhido de forma sustentável respeitando a natureza.",
    val qualitySealText: String = "Mel 100% Puro • Rastreabilidade Garantida • ApisSmart",
    val savedIssuerSignatureSvg: String? = null
)

data class ReceiptItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val description: String,
    val quantity: Double,
    val unit: String = "unid",
    val unitPrice: Double,
    val totalPrice: Double = quantity * unitPrice,
    val batchNumber: String = "",
    val productCategory: String = "",
    val packaging: String = ""
)

data class ReceiptData(
    val id: String = "REC-${System.currentTimeMillis() % 100000}",
    val receiptNumber: Int = 1,
    val transactionId: Long? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val clientName: String = "Cliente Balcão",
    val clientDocument: String = "",
    val clientPhone: String = "",
    val clientAddress: String = "",
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "PIX",
    val items: List<ReceiptItem> = emptyList(),
    val notes: String = "",
    val status: String = "EMITIDO", // "EMITIDO", "ASSINADO", "CANCELADO"
    val issuerSignatureSvg: String? = null,
    val customerSignatureSvg: String? = null
)
