package com.factory.slumberaisleepcoach.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import com.factory.slumberaisleepcoach.MainDispatcherRule
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BillingManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: Context
    private lateinit var premiumManager: PremiumManager
    private lateinit var billingClient: BillingClient
    private lateinit var builder: BillingClient.Builder
    private lateinit var billingManager: BillingManager
    private lateinit var purchasesUpdatedListenerSlot: CapturingSlot<PurchasesUpdatedListener>

    private var clientReady = false
    private var connectionResponseCode = BillingClient.BillingResponseCode.OK

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        every { context.applicationContext } returns context

        premiumManager = mockk(relaxed = true)

        billingClient = mockk(relaxed = true)
        builder = mockk(relaxed = true)
        purchasesUpdatedListenerSlot = slot()

        mockkStatic(BillingClient::class)
        every { BillingClient.newBuilder(any()) } returns builder
        every { builder.setListener(capture(purchasesUpdatedListenerSlot)) } returns builder
        every { builder.enablePendingPurchases() } returns builder
        every { builder.build() } returns billingClient

        every { billingClient.isReady } answers { clientReady }
        every { billingClient.startConnection(any()) } answers {
            val listener = firstArg<BillingClientStateListener>()
            if (connectionResponseCode == BillingClient.BillingResponseCode.OK) clientReady = true
            listener.onBillingSetupFinished(billingResult(connectionResponseCode))
        }
        every { billingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any<PurchasesResponseListener>()) } answers {
            val listener = secondArg<PurchasesResponseListener>()
            listener.onQueryPurchasesResponse(billingResult(BillingClient.BillingResponseCode.OK), emptyList())
        }

        billingManager = BillingManager(context, premiumManager)
    }

    @After
    fun tearDown() {
        unmockkStatic(BillingClient::class)
    }

    private fun billingResult(code: Int, message: String = ""): BillingResult =
        BillingResult.newBuilder().setResponseCode(code).setDebugMessage(message).build()

    private fun fakeProductDetails(productId: String, offerToken: String? = "token"): ProductDetails {
        val details = mockk<ProductDetails>(relaxed = true)
        every { details.productId } returns productId
        if (offerToken != null) {
            val offer = mockk<ProductDetails.SubscriptionOfferDetails>(relaxed = true)
            every { offer.offerToken } returns offerToken
            every { details.subscriptionOfferDetails } returns listOf(offer)
        } else {
            every { details.subscriptionOfferDetails } returns null
        }
        return details
    }

    private fun fakePurchase(
        productId: String,
        state: Int = Purchase.PurchaseState.PURCHASED,
        token: String = "token-$productId",
        acknowledged: Boolean = false
    ): Purchase {
        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.products } returns listOf(productId)
        every { purchase.purchaseState } returns state
        every { purchase.purchaseToken } returns token
        every { purchase.isAcknowledged } returns acknowledged
        return purchase
    }

    private fun stubQueryProductDetails(vararg productDetails: ProductDetails) {
        every { billingClient.queryProductDetailsAsync(any(), any()) } answers {
            val listener = secondArg<ProductDetailsResponseListener>()
            listener.onProductDetailsResponse(billingResult(BillingClient.BillingResponseCode.OK), productDetails.toList())
        }
    }

    /**
     * [syncPurchases] queries SUBS then INAPP, sequentially and in that order; since
     * [QueryPurchasesParams] doesn't publicly expose the product type it was built with,
     * route responses by call order instead.
     */
    private fun stubQueryPurchases(subs: List<Purchase> = emptyList(), inapp: List<Purchase> = emptyList()) {
        var callCount = 0
        every {
            billingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any<PurchasesResponseListener>())
        } answers {
            val listener = secondArg<PurchasesResponseListener>()
            val purchases = if (callCount == 0) subs else inapp
            callCount++
            listener.onQueryPurchasesResponse(billingResult(BillingClient.BillingResponseCode.OK), purchases)
        }
    }

    private fun stubAcknowledgePurchase() {
        every { billingClient.acknowledgePurchase(any(), any()) } answers {
            secondArg<AcknowledgePurchaseResponseListener>()
                .onAcknowledgePurchaseResponse(billingResult(BillingClient.BillingResponseCode.OK))
        }
    }

    private fun stubConsumePurchase() {
        every { billingClient.consumeAsync(any(), any()) } answers {
            secondArg<ConsumeResponseListener>()
                .onConsumeResponse(billingResult(BillingClient.BillingResponseCode.OK), "token")
        }
    }

    // --- connect() ---

    @Test
    fun `connect transitions to CONNECTED on successful setup`() = runTest(mainDispatcherRule.testDispatcher) {
        billingManager.connect()

        assertEquals(BillingConnectionState.CONNECTED, billingManager.connectionState.value)
    }

    @Test
    fun `connect transitions to UNAVAILABLE when billing setup fails`() = runTest(mainDispatcherRule.testDispatcher) {
        connectionResponseCode = BillingClient.BillingResponseCode.BILLING_UNAVAILABLE

        billingManager.connect()

        assertEquals(BillingConnectionState.UNAVAILABLE, billingManager.connectionState.value)
    }

    @Test
    fun `connect is a no-op when the client is already ready`() = runTest(mainDispatcherRule.testDispatcher) {
        clientReady = true

        billingManager.connect()

        assertEquals(BillingConnectionState.CONNECTED, billingManager.connectionState.value)
        verify(exactly = 0) { billingClient.startConnection(any()) }
    }

    // --- loadProductDetails() ---

    @Test
    fun `loadProductDetails populates productDetails for every configured product returned by Play`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val weekly = fakeProductDetails(PremiumProduct.WEEKLY.productId)
            val lifetime = fakeProductDetails(PremiumProduct.LIFETIME.productId, offerToken = null)
            stubQueryProductDetails(weekly, lifetime)

            billingManager.loadProductDetails()

            val details = billingManager.productDetails.value
            assertEquals(weekly, details[PremiumProduct.WEEKLY.productId])
            assertEquals(lifetime, details[PremiumProduct.LIFETIME.productId])
        }

    @Test
    fun `loadProductDetails leaves productDetails empty when the connection is unavailable`() =
        runTest(mainDispatcherRule.testDispatcher) {
            connectionResponseCode = BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE

            billingManager.loadProductDetails()

            assertTrue(billingManager.productDetails.value.isEmpty())
        }

    // --- launchPurchaseFlow() (purchase flow) ---

    @Test
    fun `launchPurchaseFlow emits an error when product details have not loaded`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val activity = mockk<Activity>(relaxed = true)
            val events = mutableListOf<PurchaseEvent>()
            val job = launch { billingManager.purchaseEvents.collect { events.add(it) } }

            billingManager.launchPurchaseFlow(activity, PremiumProduct.MONTHLY)
            advanceUntilIdle()

            assertTrue(events.single() is PurchaseEvent.Error)
            verify(exactly = 0) { billingClient.launchBillingFlow(any(), any()) }
            job.cancel()
        }

    @Test
    fun `launchPurchaseFlow emits an error when a subscription has no offer token`() =
        runTest(mainDispatcherRule.testDispatcher) {
            stubQueryProductDetails(fakeProductDetails(PremiumProduct.MONTHLY.productId, offerToken = null))
            billingManager.loadProductDetails()
            val activity = mockk<Activity>(relaxed = true)
            val events = mutableListOf<PurchaseEvent>()
            val job = launch { billingManager.purchaseEvents.collect { events.add(it) } }

            billingManager.launchPurchaseFlow(activity, PremiumProduct.MONTHLY)
            advanceUntilIdle()

            assertTrue(events.single() is PurchaseEvent.Error)
            job.cancel()
        }

    @Test
    fun `launchPurchaseFlow starts the Play purchase UI once product details are loaded`() =
        runTest(mainDispatcherRule.testDispatcher) {
            stubQueryProductDetails(fakeProductDetails(PremiumProduct.MONTHLY.productId))
            billingManager.loadProductDetails()
            every { billingClient.launchBillingFlow(any(), any()) } returns billingResult(BillingClient.BillingResponseCode.OK)
            val activity = mockk<Activity>(relaxed = true)

            billingManager.launchPurchaseFlow(activity, PremiumProduct.MONTHLY)

            verify { billingClient.launchBillingFlow(activity, any()) }
        }

    @Test
    fun `launchPurchaseFlow emits an error when Play fails to start the purchase UI`() =
        runTest(mainDispatcherRule.testDispatcher) {
            stubQueryProductDetails(fakeProductDetails(PremiumProduct.LIFETIME.productId, offerToken = null))
            billingManager.loadProductDetails()
            every { billingClient.launchBillingFlow(any(), any()) } returns
                billingResult(BillingClient.BillingResponseCode.ERROR, "boom")
            val activity = mockk<Activity>(relaxed = true)
            val events = mutableListOf<PurchaseEvent>()
            val job = launch { billingManager.purchaseEvents.collect { events.add(it) } }

            billingManager.launchPurchaseFlow(activity, PremiumProduct.LIFETIME)
            advanceUntilIdle()

            assertEquals("boom", (events.single() as PurchaseEvent.Error).message)
            job.cancel()
        }

    // --- purchasesUpdatedListener (result of the purchase flow) ---

    @Test
    fun `a completed subscription purchase grants premium, acknowledges it, and emits Success`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { premiumManager.grantPremium(any()) } just Runs
            stubAcknowledgePurchase()
            val purchase = fakePurchase(PremiumProduct.MONTHLY.productId, acknowledged = false)
            val events = mutableListOf<PurchaseEvent>()
            val job = launch { billingManager.purchaseEvents.collect { events.add(it) } }

            purchasesUpdatedListenerSlot.captured.onPurchasesUpdated(
                billingResult(BillingClient.BillingResponseCode.OK),
                listOf(purchase)
            )
            advanceUntilIdle()

            coVerify { premiumManager.grantPremium(PremiumProduct.MONTHLY.productId) }
            verify { billingClient.acknowledgePurchase(any(), any()) }
            assertEquals(PremiumProduct.MONTHLY, (events.single() as PurchaseEvent.Success).product)
            job.cancel()
        }

    @Test
    fun `a completed consumable purchase is consumed instead of granting premium`() =
        runTest(mainDispatcherRule.testDispatcher) {
            stubConsumePurchase()
            val purchase = fakePurchase(PremiumProduct.SMALL_IAP.productId)
            val events = mutableListOf<PurchaseEvent>()
            val job = launch { billingManager.purchaseEvents.collect { events.add(it) } }

            purchasesUpdatedListenerSlot.captured.onPurchasesUpdated(
                billingResult(BillingClient.BillingResponseCode.OK),
                listOf(purchase)
            )
            advanceUntilIdle()

            verify { billingClient.consumeAsync(any(), any()) }
            coVerify(exactly = 0) { premiumManager.grantPremium(any()) }
            assertEquals(PremiumProduct.SMALL_IAP, (events.single() as PurchaseEvent.Success).product)
            job.cancel()
        }

    @Test
    fun `an already-acknowledged purchase is not re-acknowledged`() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { premiumManager.grantPremium(any()) } just Runs
        val purchase = fakePurchase(PremiumProduct.YEARLY.productId, acknowledged = true)

        purchasesUpdatedListenerSlot.captured.onPurchasesUpdated(
            billingResult(BillingClient.BillingResponseCode.OK),
            listOf(purchase)
        )
        advanceUntilIdle()

        verify(exactly = 0) { billingClient.acknowledgePurchase(any(), any()) }
        coVerify { premiumManager.grantPremium(PremiumProduct.YEARLY.productId) }
    }

    @Test
    fun `a pending purchase emits Pending and does not grant premium`() = runTest(mainDispatcherRule.testDispatcher) {
        val purchase = fakePurchase(PremiumProduct.MONTHLY.productId, state = Purchase.PurchaseState.PENDING)
        val events = mutableListOf<PurchaseEvent>()
        val job = launch { billingManager.purchaseEvents.collect { events.add(it) } }

        purchasesUpdatedListenerSlot.captured.onPurchasesUpdated(
            billingResult(BillingClient.BillingResponseCode.OK),
            listOf(purchase)
        )
        advanceUntilIdle()

        assertTrue(events.single() is PurchaseEvent.Pending)
        coVerify(exactly = 0) { premiumManager.grantPremium(any()) }
        job.cancel()
    }

    @Test
    fun `user cancelling the purchase flow emits Cancelled`() = runTest(mainDispatcherRule.testDispatcher) {
        val events = mutableListOf<PurchaseEvent>()
        val job = launch { billingManager.purchaseEvents.collect { events.add(it) } }

        purchasesUpdatedListenerSlot.captured.onPurchasesUpdated(
            billingResult(BillingClient.BillingResponseCode.USER_CANCELED),
            null
        )
        advanceUntilIdle()

        assertTrue(events.single() is PurchaseEvent.Cancelled)
        job.cancel()
    }

    @Test
    fun `an already-owned response emits AlreadyOwned`() = runTest(mainDispatcherRule.testDispatcher) {
        val events = mutableListOf<PurchaseEvent>()
        val job = launch { billingManager.purchaseEvents.collect { events.add(it) } }

        purchasesUpdatedListenerSlot.captured.onPurchasesUpdated(
            billingResult(BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED),
            null
        )
        advanceUntilIdle()

        assertTrue(events.single() is PurchaseEvent.AlreadyOwned)
        job.cancel()
    }

    @Test
    fun `a network error response emits a network Error message`() = runTest(mainDispatcherRule.testDispatcher) {
        val events = mutableListOf<PurchaseEvent>()
        val job = launch { billingManager.purchaseEvents.collect { events.add(it) } }

        purchasesUpdatedListenerSlot.captured.onPurchasesUpdated(
            billingResult(BillingClient.BillingResponseCode.NETWORK_ERROR),
            null
        )
        advanceUntilIdle()

        assertTrue((events.single() as PurchaseEvent.Error).message.isNotBlank())
        job.cancel()
    }

    @Test
    fun `an OK response with no purchases emits an Error`() = runTest(mainDispatcherRule.testDispatcher) {
        val events = mutableListOf<PurchaseEvent>()
        val job = launch { billingManager.purchaseEvents.collect { events.add(it) } }

        purchasesUpdatedListenerSlot.captured.onPurchasesUpdated(
            billingResult(BillingClient.BillingResponseCode.OK),
            emptyList()
        )
        advanceUntilIdle()

        assertTrue(events.single() is PurchaseEvent.Error)
        job.cancel()
    }

    // --- syncPurchases() (restore purchases / subscription status checking) ---

    @Test
    fun `syncPurchases grants premium when Play reports an active subscription`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { premiumManager.grantPremium(any()) } just Runs
            coEvery { premiumManager.isCurrentlyPremium() } returns false
            stubAcknowledgePurchase()
            val purchase = fakePurchase(PremiumProduct.YEARLY.productId, acknowledged = true)
            stubQueryPurchases(subs = listOf(purchase))

            billingManager.syncPurchases()

            coVerify { premiumManager.grantPremium(PremiumProduct.YEARLY.productId) }
        }

    @Test
    fun `syncPurchases revokes premium when no active purchases remain but the user was premium`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { premiumManager.isCurrentlyPremium() } returns true
            coEvery { premiumManager.revokePremium() } just Runs
            stubQueryPurchases()

            billingManager.syncPurchases()

            coVerify { premiumManager.revokePremium() }
        }

    @Test
    fun `restore purchases emits NoPurchasesToRestore when nothing is found and the user was not premium`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { premiumManager.isCurrentlyPremium() } returns false
            stubQueryPurchases()
            val events = mutableListOf<PurchaseEvent>()
            val job = launch { billingManager.purchaseEvents.collect { events.add(it) } }

            billingManager.syncPurchases(isUserInitiatedRestore = true)
            advanceUntilIdle()

            assertTrue(events.single() is PurchaseEvent.NoPurchasesToRestore)
            coVerify(exactly = 0) { premiumManager.revokePremium() }
            job.cancel()
        }

    @Test
    fun `restore purchases emits an Error when there is no connection`() =
        runTest(mainDispatcherRule.testDispatcher) {
            connectionResponseCode = BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE
            val events = mutableListOf<PurchaseEvent>()
            val job = launch { billingManager.purchaseEvents.collect { events.add(it) } }

            billingManager.syncPurchases(isUserInitiatedRestore = true)
            advanceUntilIdle()

            assertTrue(events.single() is PurchaseEvent.Error)
            job.cancel()
        }

    @Test
    fun `syncPurchases sweeps unconsumed consumable purchases`() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { premiumManager.isCurrentlyPremium() } returns false
        stubConsumePurchase()
        val consumable = fakePurchase(PremiumProduct.SMALL_IAP.productId)
        stubQueryPurchases(inapp = listOf(consumable))

        billingManager.syncPurchases()

        verify { billingClient.consumeAsync(any(), any()) }
    }

    // --- endConnection() ---

    @Test
    fun `endConnection tears down the billing client and resets state`() = runTest(mainDispatcherRule.testDispatcher) {
        billingManager.connect()

        billingManager.endConnection()

        verify { billingClient.endConnection() }
        assertEquals(BillingConnectionState.DISCONNECTED, billingManager.connectionState.value)
    }
}
