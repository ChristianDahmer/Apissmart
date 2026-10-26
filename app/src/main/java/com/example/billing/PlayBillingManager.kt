package com.example.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Connection states for Google Play Billing.
 */
enum class BillingConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

/**
 * Representation of a subscription or in-app product available on Google Play.
 */
data class PlayProductItem(
    val productId: String,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val periodLabel: String,
    val isSubscription: Boolean,
    val productDetails: ProductDetails? = null,
    val offerToken: String? = null,
    val isBestValue: Boolean = false,
    val savingsBadge: String? = null
)

/**
 * Google Play Billing Manager (Play Billing Library 7.x).
 *
 * Handles:
 * - BillingClient connection lifecycle & auto-reconnect
 * - Querying subscription & in-app products
 * - Launching Google Play purchase flow
 * - Verifying & acknowledging purchases
 * - Restoring purchases across installs/devices
 * - Managing Google Play subscriptions
 */
class PlayBillingManager(
    private val context: Context,
    private val onPurchaseConfirmed: (purchase: Purchase, productId: String) -> Unit = { _, _ -> }
) : PurchasesUpdatedListener, BillingClientStateListener {

    companion object {
        private const val TAG = "PlayBillingManager"

        // Standard Google Play Console Product IDs
        const val PRODUCT_PRO_MONTHLY = "apissmart_pro_monthly"
        const val PRODUCT_PRO_ANNUAL = "apissmart_pro_annual"
        const val PRODUCT_PRO_LIFETIME = "apissmart_pro_lifetime"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connectionState = MutableStateFlow(BillingConnectionState.DISCONNECTED)
    val connectionState: StateFlow<BillingConnectionState> = _connectionState.asStateFlow()

    private val _products = MutableStateFlow<List<PlayProductItem>>(emptyList())
    val products: StateFlow<List<PlayProductItem>> = _products.asStateFlow()

    private val _activePurchases = MutableStateFlow<List<Purchase>>(emptyList())
    val activePurchases: StateFlow<List<Purchase>> = _activePurchases.asStateFlow()

    private val _isProUnlocked = MutableStateFlow(false)
    val isProUnlocked: StateFlow<Boolean> = _isProUnlocked.asStateFlow()

    private val _billingStatusMessage = MutableStateFlow<String?>(null)
    val billingStatusMessage: StateFlow<String?> = _billingStatusMessage.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private var billingClient: BillingClient? = null

    init {
        initDefaultProductCatalog()
        initBillingClient()
    }

    /**
     * Initializes default catalog for instant UI rendering and fallback.
     */
    private fun initDefaultProductCatalog() {
        _products.value = listOf(
            PlayProductItem(
                productId = PRODUCT_PRO_MONTHLY,
                title = "Plano Mensal Pro",
                description = "Acesso completo a todas as ferramentas inteligentes de manejo, sanidade e IA.",
                formattedPrice = "R$ 14,90/mês",
                periodLabel = "mês",
                isSubscription = true,
                isBestValue = false
            ),
            PlayProductItem(
                productId = PRODUCT_PRO_ANNUAL,
                title = "Plano Anual Pro",
                description = "Economize mais de 16% com pagamento anual. Renovação anual automática na Google Play.",
                formattedPrice = "R$ 149,90/ano",
                periodLabel = "ano",
                isSubscription = true,
                isBestValue = true,
                savingsBadge = "ECONOMIA DE 17%"
            ),
            PlayProductItem(
                productId = PRODUCT_PRO_LIFETIME,
                title = "Licença Vitalícia Permanente",
                description = "Pagamento único na Google Play. Sem mensalidades ou renovações, para sempre.",
                formattedPrice = "R$ 349,90 taxa única",
                periodLabel = "permanente",
                isSubscription = false,
                isBestValue = false,
                savingsBadge = "PAGAMENTO ÚNICO"
            )
        )
    }

    private fun initBillingClient() {
        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .enablePrepaidPlans()
            .build()

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(pendingPurchasesParams)
            .build()

        startConnection()
    }

    /**
     * Connects to Google Play Billing Service.
     */
    fun startConnection() {
        val client = billingClient ?: return
        if (client.isReady) {
            _connectionState.value = BillingConnectionState.CONNECTED
            queryAllProducts()
            restorePurchases()
            return
        }

        _connectionState.value = BillingConnectionState.CONNECTING
        try {
            client.startConnection(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start BillingClient connection", e)
            _connectionState.value = BillingConnectionState.ERROR
            _billingStatusMessage.value = "Não foi possível conectar ao Google Play: ${e.message}"
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "Google Play Billing setup successful")
            _connectionState.value = BillingConnectionState.CONNECTED
            _billingStatusMessage.value = null
            queryAllProducts()
            restorePurchases()
        } else {
            Log.w(TAG, "Google Play Billing setup finished with code: ${billingResult.responseCode} - ${billingResult.debugMessage}")
            _connectionState.value = BillingConnectionState.ERROR
            _billingStatusMessage.value = "Google Play Billing indisponível (${billingResult.responseCode}): ${billingResult.debugMessage}"
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.w(TAG, "Google Play Billing service disconnected. Reconnecting...")
        _connectionState.value = BillingConnectionState.DISCONNECTED
    }

    /**
     * Queries subscriptions and in-app products from Google Play Console.
     */
    fun queryAllProducts() {
        val client = billingClient
        if (client == null || !client.isReady) {
            Log.w(TAG, "BillingClient not ready to query products")
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_PRO_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_PRO_ANNUAL)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_PRO_LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (productDetailsList.isNotEmpty()) {
                    updateProductsFromGooglePlay(productDetailsList)
                } else {
                    Log.d(TAG, "No product details returned from Google Play. Retaining default catalog.")
                }
            } else {
                Log.w(TAG, "queryProductDetailsAsync failed: ${billingResult.responseCode} - ${billingResult.debugMessage}")
            }
        }
    }

    private fun updateProductsFromGooglePlay(detailsList: List<ProductDetails>) {
        val current = _products.value.toMutableList()
        val updated = current.map { item ->
            val playDetails = detailsList.find { it.productId == item.productId }
            if (playDetails != null) {
                if (playDetails.productType == BillingClient.ProductType.SUBS) {
                    val offer = playDetails.subscriptionOfferDetails?.firstOrNull()
                    val price = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    val token = offer?.offerToken
                    item.copy(
                        formattedPrice = price ?: item.formattedPrice,
                        productDetails = playDetails,
                        offerToken = token
                    )
                } else {
                    val price = playDetails.oneTimePurchaseOfferDetails?.formattedPrice
                    item.copy(
                        formattedPrice = price ?: item.formattedPrice,
                        productDetails = playDetails
                    )
                }
            } else {
                item
            }
        }
        _products.value = updated
    }

    /**
     * Launches the official Google Play purchase flow.
     */
    fun launchPurchaseFlow(activity: Activity, productId: String) {
        val client = billingClient
        if (client == null || !client.isReady) {
            _billingStatusMessage.value = "Google Play Store não está pronto. Tentando reconectar..."
            startConnection()
            return
        }

        val item = _products.value.find { it.productId == productId }
        val productDetails = item?.productDetails

        if (productDetails == null) {
            // In testing or when product not yet registered in Play Console
            _billingStatusMessage.value = "Produto '$productId' aguarda sincronização na Play Store. Verifique sua conexão com o Google Play."
            return
        }

        _isProcessing.value = true

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val offerToken = item.offerToken
                ?: productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (offerToken != null) {
                productDetailsParamsBuilder.setOfferToken(offerToken)
            }
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()

        val result = client.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _isProcessing.value = false
            _billingStatusMessage.value = "Erro ao iniciar compra na Google Play (${result.responseCode}): ${result.debugMessage}"
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        _isProcessing.value = false
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null && purchases.isNotEmpty()) {
                    handlePurchases(purchases)
                    _billingStatusMessage.value = "Compra confirmada com sucesso via Google Play!"
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingStatusMessage.value = "Compra cancelada pelo usuário."
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _billingStatusMessage.value = "Você já possui este item ou assinatura ativa. Restaurando..."
                restorePurchases()
            }
            else -> {
                _billingStatusMessage.value = "Aviso Google Play (${billingResult.responseCode}): ${billingResult.debugMessage}"
                Log.w(TAG, "PurchasesUpdated error code: ${billingResult.responseCode} msg: ${billingResult.debugMessage}")
            }
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        val client = billingClient ?: return
        var hasValidProPurchase = false

        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                hasValidProPurchase = true
                val productId = purchase.products.firstOrNull() ?: ""

                // Acknowledge if required by Google Play policy (must acknowledge within 3 days)
                if (!purchase.isAcknowledged) {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                    client.acknowledgePurchase(ackParams) { ackResult ->
                        if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d(TAG, "Purchase acknowledged successfully: ${purchase.orderId}")
                            scope.launch {
                                onPurchaseConfirmed(purchase, productId)
                            }
                        } else {
                            Log.w(TAG, "Failed to acknowledge purchase: ${ackResult.debugMessage}")
                        }
                    }
                } else {
                    scope.launch {
                        onPurchaseConfirmed(purchase, productId)
                    }
                }
            }
        }

        if (hasValidProPurchase) {
            _activePurchases.value = purchases
            _isProUnlocked.value = true
        }
    }

    /**
     * Restores existing purchases for the logged-in Google Account.
     * Complies with Google Play Developer Policy.
     */
    fun restorePurchases(onFinished: ((Boolean, String) -> Unit)? = null) {
        val client = billingClient
        if (client == null || !client.isReady) {
            onFinished?.invoke(false, "Serviço da Google Play não disponível no momento.")
            return
        }

        _isProcessing.value = true

        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryPurchasesAsync(subsParams) { subsResult, subsPurchases ->
            val inAppParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            client.queryPurchasesAsync(inAppParams) { inAppResult, inAppPurchases ->
                _isProcessing.value = false
                val allPurchases = (subsPurchases + inAppPurchases).filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                if (allPurchases.isNotEmpty()) {
                    handlePurchases(allPurchases)
                    val msg = "Encontrada(s) ${allPurchases.size} compra(s) ativa(s) na sua conta Google Play."
                    _billingStatusMessage.value = msg
                    onFinished?.invoke(true, msg)
                } else {
                    val msg = "Nenhuma compra ou assinatura ativa encontrada para esta conta Google."
                    _billingStatusMessage.value = msg
                    onFinished?.invoke(false, msg)
                }
            }
        }
    }

    /**
     * Clears the current transient status message.
     */
    fun clearStatusMessage() {
        _billingStatusMessage.value = null
    }

    /**
     * Opens the Google Play Store subscription management page.
     * Mandatory for Google Play compliance.
     */
    fun openGooglePlaySubscriptions(activityContext: Context, productId: String? = null) {
        val packageName = activityContext.packageName
        val url = if (!productId.isNullOrBlank()) {
            "https://play.google.com/store/account/subscriptions?package=$packageName&sku=$productId"
        } else {
            "https://play.google.com/store/account/subscriptions?package=$packageName"
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activityContext.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to generic Play Store subscriptions link
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activityContext.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to open Play Store subscriptions page", ex)
            }
        }
    }

    /**
     * Releases BillingClient connection resources.
     */
    fun destroy() {
        try {
            billingClient?.endConnection()
        } catch (_: Exception) {}
        billingClient = null
    }
}
