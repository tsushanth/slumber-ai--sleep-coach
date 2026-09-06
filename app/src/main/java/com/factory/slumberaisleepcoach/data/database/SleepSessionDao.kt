package com.factory.slumberaisleepcoach.data.database

import androidx.room.*
import com.factory.slumberaisleepcoach.data.entities.SleepSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepSessionDao {

    @Query("SELECT * FROM sleep_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SleepSession>>

    @Query("SELECT * FROM sleep_sessions WHERE endTime IS NOT NULL ORDER BY startTime DESC LIMIT 7")
    fun getRecentSessions(): Flow<List<SleepSession>>

    @Query("SELECT * FROM sleep_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SleepSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SleepSession): Long

    @Update
    suspend fun updateSession(session: SleepSession)

    @Delete
    suspend fun deleteSession(session: SleepSession)

    @Query("SELECT AVG(durationMs) FROM sleep_sessions WHERE endTime IS NOT NULL AND durationMs > 0")
    fun getAverageSleepDuration(): Flow<Long?>

    @Query("SELECT AVG(qualityScore) FROM sleep_sessions WHERE endTime IS NOT NULL")
    fun getAverageQuality(): Flow<Float?>

    @Query("SELECT COUNT(*) FROM sleep_sessions WHERE endTime IS NOT NULL")
    fun getTotalSessionCount(): Flow<Int>

    @Query("SELECT SUM(snoringEvents) FROM sleep_sessions WHERE endTime IS NOT NULL")
    fun getTotalSnoringEvents(): Flow<Int?>
}
