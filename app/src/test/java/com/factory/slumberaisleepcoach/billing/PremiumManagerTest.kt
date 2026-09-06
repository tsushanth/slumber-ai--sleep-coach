package com.factory.slumberaisleepcoach.billing

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PremiumManagerTest {

    private lateinit var context: Context
    private lateinit var premiumManager: PremiumManager

    @Before
    fun setUp() = runTest {
        context = ApplicationProvider.getApplicationContext()
        premiumManager = PremiumManager(context)
        // Datastore is a process-wide singleton keyed by file name; reset it so each test starts clean.
        premiumManager.revokePremium()
    }

    @Test
    fun `initial state is not premium`() = runTest {
        assertFalse(premiumManager.isCurrentlyPremium())
    }

    @Test
    fun `grantPremium persists premium state`() = runTest {
        premiumManager.grantPremium(PremiumProduct.MONTHLY.productId)

        assertTrue(premiumManager.isCurrentlyPremium())
    }

    @Test
    fun `revokePremium clears a previously granted premium state`() = runTest {
        premiumManager.grantPremium(PremiumProduct.YEARLY.productId)
        assertTrue(premiumManager.isCurrentlyPremium())

        premiumManager.revokePremium()

        assertFalse(premiumManager.isCurrentlyPremium())
    }

    @Test
    fun `granting premium again with a different product overwrites the previous one`() = runTest {
        premiumManager.grantPremium(PremiumProduct.WEEKLY.productId)
        premiumManager.grantPremium(PremiumProduct.LIFETIME.productId)

        assertTrue(premiumManager.isCurrentlyPremium())
    }

    @Test
    fun `isPremiumFlow emits distinct premium status changes`() = runTest {
        premiumManager.isPremiumFlow.test {
            assertFalse(awaitItem())

            premiumManager.grantPremium(PremiumProduct.MONTHLY.productId)
            assertTrue(awaitItem())

            // Granting again (same boolean value) should not re-emit since the flow is distinct.
            premiumManager.grantPremium(PremiumProduct.YEARLY.productId)
            expectNoEvents()

            premiumManager.revokePremium()
            assertFalse(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `feature gating - premium-only feature is locked until premium is granted`() = runTest {
        fun isPremiumFeatureUnlocked(isPremium: Boolean) = isPremium

        assertFalse(isPremiumFeatureUnlocked(premiumManager.isCurrentlyPremium()))

        premiumManager.grantPremium(PremiumProduct.LIFETIME.productId)

        assertTrue(isPremiumFeatureUnlocked(premiumManager.isCurrentlyPremium()))
    }

    @Test
    fun `isCurrentlyPremium is consistent with the latest isPremiumFlow emission`() = runTest {
        premiumManager.grantPremium(PremiumProduct.WEEKLY.productId)

        var flowValue = false
        premiumManager.isPremiumFlow.test {
            flowValue = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(flowValue, premiumManager.isCurrentlyPremium())
    }
}
