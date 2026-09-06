package com.factory.slumberaisleepcoach.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.factory.slumberaisleepcoach.data.entities.SleepSession
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SleepSessionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SleepSessionDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.sleepSessionDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `insert and get session by id`() = runTest {
        val id = dao.insertSession(SleepSession(startTime = 1_000L))

        val loaded = dao.getSessionById(id)

        assertEquals(1_000L, loaded?.startTime)
    }

    @Test
    fun `getSessionById returns null for missing id`() = runTest {
        assertNull(dao.getSessionById(999L))
    }

    @Test
    fun `getAllSessions emits inserted sessions ordered by startTime descending`() = runTest {
        dao.insertSession(SleepSession(startTime = 100L))
        dao.insertSession(SleepSession(startTime = 300L))
        dao.insertSession(SleepSession(startTime = 200L))

        dao.getAllSessions().test {
            val sessions = awaitItem()
            assertEquals(listOf(300L, 200L, 100L), sessions.map { it.startTime })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getRecentSessions only includes completed sessions`() = runTest {
        dao.insertSession(SleepSession(startTime = 100L, endTime = null))
        dao.insertSession(SleepSession(startTime = 200L, endTime = 250L))

        dao.getRecentSessions().test {
            val sessions = awaitItem()
            assertEquals(1, sessions.size)
            assertEquals(200L, sessions.first().startTime)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getRecentSessions limits to the 7 most recent completed sessions`() = runTest {
        repeat(10) { i ->
            dao.insertSession(SleepSession(startTime = i.toLong(), endTime = i.toLong() + 1))
        }

        dao.getRecentSessions().test {
            val sessions = awaitItem()
            assertEquals(7, sessions.size)
            assertEquals(listOf(9L, 8L, 7L, 6L, 5L, 4L, 3L), sessions.map { it.startTime })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateSession persists changes`() = runTest {
        val id = dao.insertSession(SleepSession(startTime = 100L, qualityScore = 0))
        val inserted = dao.getSessionById(id)!!

        dao.updateSession(inserted.copy(endTime = 500L, qualityScore = 85))

        val updated = dao.getSessionById(id)
        assertEquals(500L, updated?.endTime)
        assertEquals(85, updated?.qualityScore)
    }

    @Test
    fun `deleteSession removes the row`() = runTest {
        val id = dao.insertSession(SleepSession(startTime = 100L))
        val inserted = dao.getSessionById(id)!!

        dao.deleteSession(inserted)

        assertNull(dao.getSessionById(id))
    }

    @Test
    fun `aggregate queries only consider completed sessions`() = runTest {
        // Not completed - should be excluded from every aggregate below.
        dao.insertSession(SleepSession(startTime = 1L, endTime = null, durationMs = 999_999L, qualityScore = 1, snoringEvents = 100))

        dao.insertSession(
            SleepSession(startTime = 2L, endTime = 3L, durationMs = 4_000_000L, qualityScore = 80, snoringEvents = 2)
        )
        dao.insertSession(
            SleepSession(startTime = 4L, endTime = 5L, durationMs = 6_000_000L, qualityScore = 60, snoringEvents = 4)
        )

        dao.getAverageSleepDuration().test {
            assertEquals(5_000_000L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        dao.getAverageQuality().test {
            assertEquals(70f, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        dao.getTotalSessionCount().test {
            assertEquals(2, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        dao.getTotalSnoringEvents().test {
            assertEquals(6, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `aggregate queries return null-safe defaults when there is no data`() = runTest {
        dao.getAverageSleepDuration().test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        dao.getTotalSessionCount().test {
            assertEquals(0, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        dao.getTotalSnoringEvents().test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertSession with OnConflict REPLACE overwrites an existing row with the same id`() = runTest {
        val id = dao.insertSession(SleepSession(id = 42L, startTime = 100L))

        dao.insertSession(SleepSession(id = 42L, startTime = 200L))

        val session = dao.getSessionById(id)
        assertEquals(200L, session?.startTime)
        dao.getAllSessions().test {
            assertTrue(awaitItem().size == 1)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
