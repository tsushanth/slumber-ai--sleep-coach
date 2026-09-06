package com.factory.slumberaisleepcoach.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_sessions")
data class SleepSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val durationMs: Long = 0L,
    val qualityScore: Int = 0,
    val deepSleepMs: Long = 0L,
    val lightSleepMs: Long = 0L,
    val remSleepMs: Long = 0L,
    val awakeMs: Long = 0L,
    val snoringEvents: Int = 0,
    val snoringDurationMs: Long = 0L,
    val notes: String = ""
)
