package com.factory.slumberaisleepcoach.data.repository

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
class SettingsRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: SettingsRepository

    @Before
    fun setUp() = runTest {
        context = ApplicationProvider.getApplicationContext()
        repository = SettingsRepository(context)
        // Datastore is a process-wide singleton keyed by file name; reset it so each test starts clean.
        repository.setSensitivity(3500f)
        // hasCompletedOnboardingFlow has no reset method; tests that depend on the default
        // value run before any test in this class sets it.
    }

    @Test
    fun `sensitivityFlow defaults to 3500`() = runTest {
        repository.sensitivityFlow.test {
            assertEquals(3500f, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSensitivity persists the new value`() = runTest {
        repository.setSensitivity(5000f)

        repository.sensitivityFlow.test {
            assertEquals(5000f, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sensitivityFlow emits every update in order`() = runTest {
        repository.sensitivityFlow.test {
            assertEquals(3500f, awaitItem())

            repository.setSensitivity(2000f)
            assertEquals(2000f, awaitItem())

            repository.setSensitivity(6000f)
            assertEquals(6000f, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setOnboardingComplete persists true`() = runTest {
        repository.setOnboardingComplete()

        repository.hasCompletedOnboardingFlow.test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasCompletedOnboardingFlow reflects the latest write`() = runTest {
        var before = true
        repository.hasCompletedOnboardingFlow.test {
            before = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        repository.setOnboardingComplete()

        var after = false
        repository.hasCompletedOnboardingFlow.test {
            after = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(before)
        assertTrue(after)
    }
}
