package com.factory.slumberaisleepcoach.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

enum class BillingConnectionState { DISCONNECTED, CONNECTING, CONNECTED, UNAVAILABLE }

sealed class PurchaseEvent {
    data class Success(val product: PremiumProduct) : PurchaseEvent()
    data object Cancelled : PurchaseEvent()
    data object Pending : PurchaseEvent()
    data object AlreadyOwned : PurchaseEvent()
    data class Error(val message: String) : PurchaseEvent()
    data object NoPurchasesToRestore : PurchaseEvent()
}

/**
 * Thin coroutine/Flow wrapper around [BillingClient] (Google Play Billing Library v6).
 *
 * Entitlement is only ever persisted through [PremiumManager]; this class is purely
 * responsible for talking to Play and reconciling that store with what Play reports.
 */
class BillingManager(
    context: Context,
    private val premiumManager: PremiumManager
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _connectionState = MutableStateFlow(BillingConnectionState.DISCONNECTED)
    val connectionState: StateFlow<BillingConnectionState> = _connectionState.asStateFlow()

    private val _productDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetails: StateFlow<Map<String, ProductDetails>> = _productDetails.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseEvent>(extraBufferCapacity = 4)
    val purchaseEvents: SharedFlow<PurchaseEvent> = _purchaseEvents.asSharedFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases.isNullOrEmpty()) {
                    scope.launch { _purchaseEvents.emit(PurchaseEvent.Error("No purchase was returned. Please try again.")) }
                } else {
                    purchases.forEach { purchase -> scope.launch { handlePurchase(purchase) } }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                scope.launch { _purchaseEvents.emit(PurchaseEvent.Cancelled) }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
                scope.launch { _purchaseEvents.emit(PurchaseEvent.AlreadyOwned) }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.NETWORK_ERROR,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE ->
                scope.launch { _purchaseEvents.emit(PurchaseEvent.Error("No network connection. Check your connection and try again.")) }
            else ->
                scope.launch {
                    _purchaseEvents.emit(PurchaseEvent.Error(billingResult.debugMessage.ifBlank { "Purchase failed. Please try again." }))
                }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    suspend fun connect() {
        if (billingClient.isReady) {
            _connectionState.value = BillingConnectionState.CONNECTED
            return
        }
        _connectionState.value = BillingConnectionState.CONNECTING
        val connected = suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (cont.isActive) cont.resume(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                }

                override fun onBillingServiceDisconnected() {
                    _connectionState.value = BillingConnectionState.DISCONNECTED
                }
            })
        }
        _connectionState.value = if (connected) BillingConnectionState.CONNECTED else BillingConnectionState.UNAVAILABLE
    }

    suspend fun loadProductDetails() {
        if (!ensureConnected()) return

        val subsProducts = PremiumProduct.entries.filter { it.isSubscription }.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it.productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val inAppProducts = PremiumProduct.entries.filter { !it.isSubscription }.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it.productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val results = mutableMapOf<String, ProductDetails>()
        queryProductDetails(subsProducts).forEach { results[it.productId] = it }
        queryProductDetails(inAppProducts).forEach { results[it.productId] = it }
        _productDetails.value = results
    }

    private suspend fun queryProductDetails(products: List<QueryProductDetailsParams.Product>): List<ProductDetails> {
        if (products.isEmpty()) return emptyList()
        val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()
        return suspendCancellableCoroutine { cont ->
            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                val result = if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) productDetailsList else emptyList()
                if (cont.isActive) cont.resume(result)
            }
        }
    }

    /** Launches the Play purchase UI for [product]. Requires [loadProductDetails] to have completed. */
    fun launchPurchaseFlow(activity: Activity, product: PremiumProduct) {
        val details = _productDetails.value[product.productId]
        if (details == null) {
            scope.launch {
                _purchaseEvents.emit(PurchaseEvent.Error("This item isn't available right now. Check your connection and try again."))
            }
            return
        }

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)

        if (product.isSubscription) {
            val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (offerToken == null) {
                scope.launch { _purchaseEvents.emit(PurchaseEvent.Error("No subscription offer is currently available for this plan.")) }
                return
            }
            productDetailsParamsBuilder.setOfferToken(offerToken)
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            scope.launch {
                _purchaseEvents.emit(PurchaseEvent.Error(result.debugMessage.ifBlank { "Unable to start the purchase. Please try again." }))
            }
        }
    }

    /**
     * Re-syncs local entitlement with what Play actually has on record. Used on app start and
     * for the "Restore purchases" action. Play's active-purchase query naturally drops
     * cancelled/expired subscriptions, so an active premium purchase disappearing here is how
     * subscription expiry is detected client-side.
     */
    suspend fun syncPurchases(isUserInitiatedRestore: Boolean = false) {
        if (!ensureConnected()) {
            if (isUserInitiatedRestore) {
                _purchaseEvents.emit(PurchaseEvent.Error("No network connection. Check your connection and try again."))
            }
            return
        }

        val allPurchases = queryPurchases(BillingClient.ProductType.SUBS) + queryPurchases(BillingClient.ProductType.INAPP)

        val activeEntitlementPurchase = allPurchases.firstOrNull { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.any { id -> PremiumProduct.fromProductId(id)?.let { it != PremiumProduct.SMALL_IAP } == true }
        }

        when {
            activeEntitlementPurchase != null -> handlePurchase(activeEntitlementPurchase, emitEvents = isUserInitiatedRestore)
            premiumManager.isCurrentlyPremium() -> premiumManager.revokePremium()
            isUserInitiatedRestore -> _purchaseEvents.emit(PurchaseEvent.NoPurchasesToRestore)
        }

        // Sweep any consumable purchase that wasn't consumed yet (e.g. app was killed mid-flow).
        allPurchases
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && it.products.contains(PremiumProduct.SMALL_IAP.productId) }
            .forEach { consumePurchase(it) }
    }

    private suspend fun queryPurchases(productType: String): List<Purchase> {
        val params = QueryPurchasesParams.newBuilder().setProductType(productType).build()
        return suspendCancellableCoroutine { cont ->
            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                val result = if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) purchases else emptyList()
                if (cont.isActive) cont.resume(result)
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase, emitEvents: Boolean = true) {
        val productId = purchase.products.firstOrNull() ?: return
        val product = PremiumProduct.fromProductId(productId)

        when (purchase.purchaseState) {
            Purchase.PurchaseState.PENDING -> {
                if (emitEvents) _purchaseEvents.emit(PurchaseEvent.Pending)
            }
            Purchase.PurchaseState.PURCHASED -> {
                if (product == PremiumProduct.SMALL_IAP) {
                    consumePurchase(purchase)
                } else {
                    if (!purchase.isAcknowledged) acknowledgePurchase(purchase)
                    // No backend to resolve a precise renewal date; entitlement is kept live by
                    // re-running syncPurchases() on app start/resume against Play's own records.
                    premiumManager.grantPremium(productId)
                }
                if (emitEvents && product != null) _purchaseEvents.emit(PurchaseEvent.Success(product))
            }
            else -> Unit
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        suspendCancellableCoroutine { cont ->
            billingClient.acknowledgePurchase(params) { if (cont.isActive) cont.resume(Unit) }
        }
    }

    private suspend fun consumePurchase(purchase: Purchase) {
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        suspendCancellableCoroutine { cont ->
            billingClient.consumeAsync(params) { _, _ -> if (cont.isActive) cont.resume(Unit) }
        }
    }

    private suspend fun ensureConnected(): Boolean {
        if (billingClient.isReady) return true
        connect()
        return billingClient.isReady
    }

    fun endConnection() {
        billingClient.endConnection()
        _connectionState.value = BillingConnectionState.DISCONNECTED
    }
}
