package com.factory.slumberaisleepcoach.viewmodel

import android.app.Activity
import android.app.Application
import app.cash.turbine.test
import com.android.billingclient.api.ProductDetails
import com.factory.slumberaisleepcoach.MainDispatcherRule
import com.factory.slumberaisleepcoach.billing.BillingConnectionState
import com.factory.slumberaisleepcoach.billing.BillingManager
import com.factory.slumberaisleepcoach.billing.PremiumManager
import com.factory.slumberaisleepcoach.billing.PremiumProduct
import com.factory.slumberaisleepcoach.billing.PurchaseEvent
import com.factory.slumberaisleepcoach.data.entities.SleepSession
import com.factory.slumberaisleepcoach.data.repository.SettingsRepository
import com.factory.slumberaisleepcoach.data.repository.SleepRepository
import com.factory.slumberaisleepcoach.model.SleepStage
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SleepViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var application: Application
    private lateinit var repository: SleepRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var premiumManager: PremiumManager
    private lateinit var billingManager: BillingManager

    private lateinit var isPremiumFlow: MutableStateFlow<Boolean>
    private lateinit var hasCompletedOnboardingFlow: MutableStateFlow<Boolean>
    private lateinit var sensitivityFlow: MutableStateFlow<Float>
    private lateinit var productDetailsFlow: MutableStateFlow<Map<String, ProductDetails>>
    private lateinit var connectionStateFlow: MutableStateFlow<BillingConnectionState>
    private lateinit var purchaseEventsFlow: MutableSharedFlow<PurchaseEvent>

    @Before
    fun setUp() {
        application = mockk(relaxed = true)

        repository = mockk(relaxed = true)
        every { repository.getAllSessions() } returns flowOf(emptyList())
        every { repository.getRecentSessions() } returns flowOf(emptyList())
        every { repository.getAverageSleepDuration() } returns flowOf(null)
        every { repository.getAverageQuality() } returns flowOf(null)
        every { repository.getTotalSessionCount() } returns flowOf(0)
        every { repository.getTotalSnoringEvents() } returns flowOf(null)

        isPremiumFlow = MutableStateFlow(false)
        premiumManager = mockk(relaxed = true)
        every { premiumManager.isPremiumFlow } returns isPremiumFlow

        hasCompletedOnboardingFlow = MutableStateFlow(false)
        sensitivityFlow = MutableStateFlow(3500f)
        settingsRepository = mockk(relaxed = true)
        every { settingsRepository.hasCompletedOnboardingFlow } returns hasCompletedOnboardingFlow
        every { settingsRepository.sensitivityFlow } returns sensitivityFlow

        productDetailsFlow = MutableStateFlow(emptyMap())
        connectionStateFlow = MutableStateFlow(BillingConnectionState.DISCONNECTED)
        purchaseEventsFlow = MutableSharedFlow(extraBufferCapacity = 4)
        billingManager = mockk(relaxed = true)
        every { billingManager.productDetails } returns productDetailsFlow
        every { billingManager.connectionState } returns connectionStateFlow
        every { billingManager.purchaseEvents } returns purchaseEventsFlow
        coEvery { billingManager.connect() } just Runs
        coEvery { billingManager.loadProductDetails() } just Runs
        coEvery { billingManager.syncPurchases(any()) } just Runs
    }

    private fun createViewModel(): SleepViewModel =
        SleepViewModel(application, repository, settingsRepository, premiumManager, billingManager)

    // --- initial state ---

    @Test
    fun `initial state reflects defaults before any action is taken`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()

        assertEquals(TrackingState.IDLE, viewModel.trackingState.value)
        assertNull(viewModel.currentSessionId.value)
        assertEquals(0L, viewModel.elapsedMs.value)
        assertEquals(SleepStage.AWAKE, viewModel.currentStage.value)
        assertFalse(viewModel.snoringDetected.value)
        assertEquals(0, viewModel.snoringEventsCount.value)
        assertTrue(viewModel.stageHistory.value.isEmpty())
        assertTrue(viewModel.amplitudeHistory.value.isEmpty())
        assertNull(viewModel.selectedSession.value)
        assertNull(viewModel.trackingError.value)
        assertNull(viewModel.purchaseMessage.value)
        assertNull(viewModel.hasCompletedOnboarding.value)
    }

    @Test
    fun `init connects to billing, loads product details, and syncs purchases`() =
        runTest(mainDispatcherRule.testDispatcher) {
            createViewModel()
            advanceUntilIdle()

            coVerify { billingManager.connect() }
            coVerify { billingManager.loadProductDetails() }
            coVerify { billingManager.syncPurchases() }
        }

    // --- StateFlow mirroring from underlying sources ---

    @Test
    fun `isPremium mirrors premium manager updates`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.isPremium.value)

        isPremiumFlow.value = true
        advanceUntilIdle()

        assertTrue(viewModel.isPremium.value)
    }

    @Test
    fun `hasCompletedOnboarding mirrors settings repository updates`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(false, viewModel.hasCompletedOnboarding.value)

        hasCompletedOnboardingFlow.value = true
        advanceUntilIdle()

        assertEquals(true, viewModel.hasCompletedOnboarding.value)
    }

    @Test
    fun `snoringSensitivity mirrors settings repository updates via Turbine`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.snoringSensitivity.test {
                assertEquals(3500f, awaitItem())

                sensitivityFlow.value = 6000f
                assertEquals(6000f, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `productDetails and billingConnectionState expose the billing manager's live state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            connectionStateFlow.value = BillingConnectionState.CONNECTED
            assertEquals(BillingConnectionState.CONNECTED, viewModel.billingConnectionState.value)

            val fakeDetails = mapOf(PremiumProduct.MONTHLY.productId to mockk<ProductDetails>())
            productDetailsFlow.value = fakeDetails
            assertEquals(fakeDetails, viewModel.productDetails.value)
        }

    // --- billing actions ---

    @Test
    fun `purchase delegates to the billing manager`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        val activity = mockk<Activity>(relaxed = true)

        viewModel.purchase(activity, PremiumProduct.YEARLY)

        verify { billingManager.launchPurchaseFlow(activity, PremiumProduct.YEARLY) }
    }

    @Test
    fun `restorePurchases triggers a user-initiated sync`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()

        viewModel.restorePurchases()
        advanceUntilIdle()

        coVerify { billingManager.syncPurchases(isUserInitiatedRestore = true) }
    }

    @Test
    fun `purchaseMessage reflects each purchase event with a friendly message, via Turbine`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.purchaseMessage.test {
                assertNull(awaitItem())

                purchaseEventsFlow.emit(PurchaseEvent.Success(PremiumProduct.LIFETIME))
                assertEquals("You're now Premium! Enjoy full access to all features.", awaitItem())

                purchaseEventsFlow.emit(PurchaseEvent.Success(PremiumProduct.SMALL_IAP))
                assertEquals("Thank you for your support!", awaitItem())

                purchaseEventsFlow.emit(PurchaseEvent.Cancelled)
                assertEquals("Purchase cancelled.", awaitItem())

                purchaseEventsFlow.emit(PurchaseEvent.NoPurchasesToRestore)
                assertEquals("No previous purchases were found to restore.", awaitItem())

                purchaseEventsFlow.emit(PurchaseEvent.Error("network down"))
                assertEquals("network down", awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `dismissPurchaseMessage clears the current message`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        purchaseEventsFlow.emit(PurchaseEvent.Cancelled)
        advanceUntilIdle()
        assertEquals("Purchase cancelled.", viewModel.purchaseMessage.value)

        viewModel.dismissPurchaseMessage()

        assertNull(viewModel.purchaseMessage.value)
    }

    // --- onboarding / settings actions ---

    @Test
    fun `completeOnboarding persists onboarding completion`() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { settingsRepository.setOnboardingComplete() } just Runs
        val viewModel = createViewModel()

        viewModel.completeOnboarding()
        advanceUntilIdle()

        coVerify { settingsRepository.setOnboardingComplete() }
    }

    @Test
    fun `setSensitivity persists the new value`() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { settingsRepository.setSensitivity(any()) } just Runs
        val viewModel = createViewModel()

        viewModel.setSensitivity(4200f)
        advanceUntilIdle()

        coVerify { settingsRepository.setSensitivity(4200f) }
    }

    // --- tracking error state ---

    @Test
    fun `onTrackingPermissionDenied sets a trackingError message`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onTrackingPermissionDenied()

        assertEquals("Microphone permission is required to track your sleep.", viewModel.trackingError.value)
    }

    @Test
    fun `dismissTrackingError clears the trackingError`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        viewModel.onTrackingPermissionDenied()

        viewModel.dismissTrackingError()

        assertNull(viewModel.trackingError.value)
    }

    // --- session detail ---

    @Test
    fun `loadSession populates selectedSession from the repository`() = runTest(mainDispatcherRule.testDispatcher) {
        val session = SleepSession(id = 7L, startTime = 111L)
        coEvery { repository.getSessionById(7L) } returns session
        val viewModel = createViewModel()

        viewModel.loadSession(7L)
        advanceUntilIdle()

        assertEquals(session, viewModel.selectedSession.value)
    }

    @Test
    fun `loadSession leaves selectedSession null when the session is missing`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getSessionById(404L) } returns null
            val viewModel = createViewModel()

            viewModel.loadSession(404L)
            advanceUntilIdle()

            assertNull(viewModel.selectedSession.value)
        }

    @Test
    fun `clearSelectedSession resets selectedSession to null`() = runTest(mainDispatcherRule.testDispatcher) {
        val session = SleepSession(id = 7L)
        coEvery { repository.getSessionById(7L) } returns session
        val viewModel = createViewModel()
        viewModel.loadSession(7L)
        advanceUntilIdle()

        viewModel.clearSelectedSession()

        assertNull(viewModel.selectedSession.value)
    }

    // --- DB-backed flow delegation ---

    @Test
    fun `allSessions exposes sessions from the repository, via Turbine`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val sessions = listOf(SleepSession(id = 1L))
            every { repository.getAllSessions() } returns flowOf(sessions)
            val viewModel = createViewModel()

            viewModel.allSessions.test {
                assertEquals(sessions, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `totalSessionCount exposes the count from the repository`() = runTest(mainDispatcherRule.testDispatcher) {
        every { repository.getTotalSessionCount() } returns flowOf(12)
        val viewModel = createViewModel()

        viewModel.totalSessionCount.test {
            assertEquals(12, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
