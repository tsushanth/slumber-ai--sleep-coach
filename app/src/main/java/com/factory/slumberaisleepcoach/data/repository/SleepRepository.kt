package com.factory.slumberaisleepcoach.data.repository

import com.factory.slumberaisleepcoach.data.database.SleepSessionDao
import com.factory.slumberaisleepcoach.data.database.SnoringRecordDao
import com.factory.slumberaisleepcoach.data.entities.SleepSession
import com.factory.slumberaisleepcoach.data.entities.SnoringRecord
import kotlinx.coroutines.flow.Flow

data class SleepStats(
    val totalDurationMs: Long,
    val qualityScore: Int,
    val deepSleepMs: Long,
    val lightSleepMs: Long,
    val remSleepMs: Long,
    val awakeMs: Long,
    val snoringEvents: Int,
    val snoringDurationMs: Long
)

class SleepRepository(
    private val sessionDao: SleepSessionDao,
    private val snoringDao: SnoringRecordDao
) {

    fun getAllSessions(): Flow<List<SleepSession>> = sessionDao.getAllSessions()

    suspend fun getSessionById(id: Long): SleepSession? = sessionDao.getSessionById(id)

    fun getRecentSessions(): Flow<List<SleepSession>> = sessionDao.getRecentSessions()

    fun getAverageSleepDuration(): Flow<Long?> = sessionDao.getAverageSleepDuration()

    fun getAverageQuality(): Flow<Float?> = sessionDao.getAverageQuality()

    fun getTotalSessionCount(): Flow<Int> = sessionDao.getTotalSessionCount()

    fun getTotalSnoringEvents(): Flow<Int?> = sessionDao.getTotalSnoringEvents()

    suspend fun startSession(): Long {
        val session = SleepSession(startTime = System.currentTimeMillis())
        return sessionDao.insertSession(session)
    }

    suspend fun endSession(sessionId: Long, stats: SleepStats) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        val updatedSession = session.copy(
            endTime = System.currentTimeMillis(),
            durationMs = stats.totalDurationMs,
            qualityScore = stats.qualityScore,
            deepSleepMs = stats.deepSleepMs,
            lightSleepMs = stats.lightSleepMs,
            remSleepMs = stats.remSleepMs,
            awakeMs = stats.awakeMs,
            snoringEvents = stats.snoringEvents,
            snoringDurationMs = stats.snoringDurationMs
        )
        sessionDao.updateSession(updatedSession)
    }

    suspend fun addSnoringRecord(record: SnoringRecord): Long = snoringDao.insertRecord(record)

    fun getSnoringRecords(sessionId: Long): Flow<List<SnoringRecord>> =
        snoringDao.getRecordsForSession(sessionId)

    suspend fun deleteSession(session: SleepSession) = sessionDao.deleteSession(session)
}
