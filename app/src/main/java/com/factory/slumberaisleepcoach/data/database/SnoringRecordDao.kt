package com.factory.slumberaisleepcoach.data.database

import androidx.room.*
import com.factory.slumberaisleepcoach.data.entities.SnoringRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SnoringRecordDao {

    @Query("SELECT * FROM snoring_records WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getRecordsForSession(sessionId: Long): Flow<List<SnoringRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SnoringRecord): Long

    @Query("DELETE FROM snoring_records WHERE sessionId = :sessionId")
    suspend fun deleteRecordsForSession(sessionId: Long)

    @Query("SELECT COUNT(*) FROM snoring_records WHERE sessionId = :sessionId")
    suspend fun getCountForSession(sessionId: Long): Int
}
