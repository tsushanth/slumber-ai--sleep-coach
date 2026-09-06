package com.factory.slumberaisleepcoach.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.factory.slumberaisleepcoach.data.entities.SleepSession
import com.factory.slumberaisleepcoach.data.entities.SnoringRecord
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SnoringRecordDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var sessionDao: SleepSessionDao
    private lateinit var snoringDao: SnoringRecordDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionDao = db.sleepSessionDao()
        snoringDao = db.snoringRecordDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    private suspend fun insertSession(): Long = sessionDao.insertSession(SleepSession(startTime = 0L))

    @Test
    fun `insertRecord and getRecordsForSession returns records ordered by timestamp ascending`() = runTest {
        val sessionId = insertSession()
        snoringDao.insertRecord(SnoringRecord(sessionId = sessionId, timestamp = 300L))
        snoringDao.insertRecord(SnoringRecord(sessionId = sessionId, timestamp = 100L))
        snoringDao.insertRecord(SnoringRecord(sessionId = sessionId, timestamp = 200L))

        snoringDao.getRecordsForSession(sessionId).test {
            val records = awaitItem()
            assertEquals(listOf(100L, 200L, 300L), records.map { it.timestamp })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getRecordsForSession only returns records for the requested session`() = runTest {
        val sessionA = insertSession()
        val sessionB = insertSession()
        snoringDao.insertRecord(SnoringRecord(sessionId = sessionA, timestamp = 1L))
        snoringDao.insertRecord(SnoringRecord(sessionId = sessionB, timestamp = 2L))

        snoringDao.getRecordsForSession(sessionA).test {
            val records = awaitItem()
            assertEquals(1, records.size)
            assertEquals(sessionA, records.first().sessionId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCountForSession reflects the number of inserted records`() = runTest {
        val sessionId = insertSession()
        assertEquals(0, snoringDao.getCountForSession(sessionId))

        snoringDao.insertRecord(SnoringRecord(sessionId = sessionId, timestamp = 1L))
        snoringDao.insertRecord(SnoringRecord(sessionId = sessionId, timestamp = 2L))

        assertEquals(2, snoringDao.getCountForSession(sessionId))
    }

    @Test
    fun `deleteRecordsForSession removes only that session's records`() = runTest {
        val sessionA = insertSession()
        val sessionB = insertSession()
        snoringDao.insertRecord(SnoringRecord(sessionId = sessionA, timestamp = 1L))
        snoringDao.insertRecord(SnoringRecord(sessionId = sessionB, timestamp = 2L))

        snoringDao.deleteRecordsForSession(sessionA)

        assertEquals(0, snoringDao.getCountForSession(sessionA))
        assertEquals(1, snoringDao.getCountForSession(sessionB))
    }

    @Test
    fun `deleting the parent session cascades to its snoring records`() = runTest {
        val sessionId = insertSession()
        snoringDao.insertRecord(SnoringRecord(sessionId = sessionId, timestamp = 1L))
        snoringDao.insertRecord(SnoringRecord(sessionId = sessionId, timestamp = 2L))
        assertTrue(snoringDao.getCountForSession(sessionId) > 0)

        val session = sessionDao.getSessionById(sessionId)!!
        sessionDao.deleteSession(session)

        assertEquals(0, snoringDao.getCountForSession(sessionId))
    }
}
