package com.factory.slumberaisleepcoach.model

import androidx.compose.ui.graphics.Color

enum class SleepStage(
    val displayName: String,
    val shortName: String,
    val color: Color,
    val description: String
) {
    AWAKE(
        displayName = "Awake",
        shortName = "AW",
        color = Color(0xFFFF6B6B),
        description = "You are awake or in very light rest"
    ),
    LIGHT(
        displayName = "Light Sleep",
        shortName = "N2",
        color = Color(0xFF4FC3F7),
        description = "Light non-REM sleep — body relaxing"
    ),
    DEEP(
        displayName = "Deep Sleep",
        shortName = "N3",
        color = Color(0xFF1565C0),
        description = "Slow-wave sleep — most restorative stage"
    ),
    REM(
        displayName = "REM Sleep",
        shortName = "REM",
        color = Color(0xFF9C27B0),
        description = "Rapid Eye Movement — dreaming and memory consolidation"
    )
}

fun determineSleepStage(elapsedMinutes: Long): SleepStage {
    if (elapsedMinutes < 5) return SleepStage.AWAKE
    val cycleMinutes = (elapsedMinutes - 5) % 90
    return when {
        cycleMinutes < 25 -> SleepStage.LIGHT
        cycleMinutes < 55 -> SleepStage.DEEP
        else -> SleepStage.REM
    }
}
