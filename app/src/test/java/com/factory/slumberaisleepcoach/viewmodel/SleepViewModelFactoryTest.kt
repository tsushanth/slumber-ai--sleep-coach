package com.factory.slumberaisleepcoach.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import com.factory.slumberaisleepcoach.billing.BillingManager
import com.factory.slumberaisleepcoach.billing.PremiumManager
import com.factory.slumberaisleepcoach.data.repository.SettingsRepository
import com.factory.slumberaisleepcoach.data.repository.SleepRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class SleepViewModelFactoryTest {

    private lateinit var application: Application
    private lateinit var repository: SleepRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var premiumManager: PremiumManager
    private lateinit var billingManager: BillingManager
    private lateinit var factory: SleepViewModelFactory

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

        settingsRepository = mockk(relaxed = true)
        every { settingsRepository.hasCompletedOnboardingFlow } returns MutableStateFlow(false)
        every { settingsRepository.sensitivityFlow } returns MutableStateFlow(3500f)

        premiumManager = mockk(relaxed = true)
        every { premiumManager.isPremiumFlow } returns MutableStateFlow(false)

        billingManager = mockk(relaxed = true)
        every { billingManager.productDetails } returns MutableStateFlow(emptyMap())
        every { billingManager.connectionState } returns MutableStateFlow(com.factory.slumberaisleepcoach.billing.BillingConnectionState.DISCONNECTED)
        every { billingManager.purchaseEvents } returns MutableSharedFlow()

        factory = SleepViewModelFactory(application, repository, settingsRepository, premiumManager, billingManager)
    }

    @Test
    fun `create returns a SleepViewModel wired with the provided dependencies`() {
        val viewModel = factory.create(SleepViewModel::class.java)

        assertNotNull(viewModel)
    }

    @Test
    fun `create throws for an unrelated ViewModel class`() {
        assertThrows(IllegalArgumentException::class.java) {
            factory.create(UnrelatedViewModel::class.java)
        }
    }

    private class UnrelatedViewModel : ViewModel()
}
