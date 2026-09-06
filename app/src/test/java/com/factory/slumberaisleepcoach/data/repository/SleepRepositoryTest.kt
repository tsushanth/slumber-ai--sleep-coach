package com.factory.slumberaisleepcoach.data.repository

import com.factory.slumberaisleepcoach.data.database.SleepSessionDao
import com.factory.slumberaisleepcoach.data.database.SnoringRecordDao
import com.factory.slumberaisleepcoach.data.entities.SleepSession
import com.factory.slumberaisleepcoach.data.entities.SnoringRecord
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class SleepRepositoryTest {

    private val sessionDao: SleepSessionDao = mockk()
    private val snoringDao: SnoringRecordDao = mockk()
    private lateinit var repository: SleepRepository

    @Before
    fun setUp() {
        repository = SleepRepository(sessionDao, snoringDao)
    }

    @Test
    fun `startSession inserts a new session and returns its id`() = runTest {
        val slot = slot<SleepSession>()
        coEvery { sessionDao.insertSession(capture(slot)) } returns 42L

        val id = repository.startSession()

        assertEquals(42L, id)
        assertEquals(0L, slot.captured.id)
        assertNull(slot.captured.endTime)
    }

    @Test
    fun `endSession updates the existing session with the given stats`() = runTest {
        val existing = SleepSession(id = 1L, startTime = 100L)
        coEvery { sessionDao.getSessionById(1L) } returns existing
        val updatedSlot = slot<SleepSession>()
        coEvery { sessionDao.updateSession(capture(updatedSlot)) } just Runs

        val stats = SleepStats(
            totalDurationMs = 500L,
            qualityScore = 90,
            deepSleepMs = 100L,
            lightSleepMs = 200L,
            remSleepMs = 100L,
            awakeMs = 100L,
            snoringEvents = 2,
            snoringDurationMs = 50L
        )

        repository.endSession(1L, stats)

        with(updatedSlot.captured) {
            assertEquals(500L, durationMs)
            assertEquals(90, qualityScore)
            assertEquals(100L, deepSleepMs)
            assertEquals(200L, lightSleepMs)
            assertEquals(100L, remSleepMs)
            assertEquals(100L, awakeMs)
            assertEquals(2, snoringEvents)
            assertEquals(50L, snoringDurationMs)
        }
        coVerify(exactly = 1) { sessionDao.updateSession(any()) }
    }

    @Test
    fun `endSession does nothing when the session no longer exists`() = runTest {
        coEvery { sessionDao.getSessionById(99L) } returns null
        val stats = SleepStats(0L, 0, 0L, 0L, 0L, 0L, 0, 0L)

        repository.endSession(99L, stats)

        coVerify(exactly = 0) { sessionDao.updateSession(any()) }
    }

    @Test
    fun `addSnoringRecord delegates to the snoring dao`() = runTest {
        val record = SnoringRecord(sessionId = 1L, timestamp = 123L)
        coEvery { snoringDao.insertRecord(record) } returns 7L

        val id = repository.addSnoringRecord(record)

        assertEquals(7L, id)
        coVerify { snoringDao.insertRecord(record) }
    }

    @Test
    fun `deleteSession delegates to the session dao`() = runTest {
        val session = SleepSession(id = 5L)
        coEvery { sessionDao.deleteSession(session) } just Runs

        repository.deleteSession(session)

        coVerify { sessionDao.deleteSession(session) }
    }

    @Test
    fun `getAllSessions delegates the flow from the dao`() = runTest {
        val sessions = listOf(SleepSession(id = 1L))
        every { sessionDao.getAllSessions() } returns flowOf(sessions)

        assertEquals(sessions, repository.getAllSessions().let { flow ->
            var result: List<SleepSession> = emptyList()
            flow.collect { result = it }
            result
        })
    }

    @Test
    fun `getSnoringRecords delegates the flow from the snoring dao`() = runTest {
        val records = listOf(SnoringRecord(sessionId = 1L))
        every { snoringDao.getRecordsForSession(1L) } returns flowOf(records)

        var result: List<SnoringRecord> = emptyList()
        repository.getSnoringRecords(1L).collect { result = it }

        assertEquals(records, result)
    }

    // --- error handling ---

    @Test
    fun `startSession propagates an exception from the dao`() = runTest {
        coEvery { sessionDao.insertSession(any()) } throws IllegalStateException("db closed")

        assertThrows(IllegalStateException::class.java) {
            runBlocking { repository.startSession() }
        }
    }

    @Test
    fun `endSession propagates an exception raised while updating`() = runTest {
        val existing = SleepSession(id = 1L, startTime = 100L)
        coEvery { sessionDao.getSessionById(1L) } returns existing
        coEvery { sessionDao.updateSession(any()) } throws IllegalStateException("db closed")
        val stats = SleepStats(0L, 0, 0L, 0L, 0L, 0L, 0, 0L)

        assertThrows(IllegalStateException::class.java) {
            runBlocking { repository.endSession(1L, stats) }
        }
    }
}
