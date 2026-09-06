package com.factory.slumberaisleepcoach.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SleepStageTest {

    @Test
    fun `first 5 minutes are AWAKE`() {
        assertEquals(SleepStage.AWAKE, determineSleepStage(0L))
        assertEquals(SleepStage.AWAKE, determineSleepStage(4L))
    }

    @Test
    fun `first cycle progresses through LIGHT, DEEP, then REM`() {
        assertEquals(SleepStage.LIGHT, determineSleepStage(5L))
        assertEquals(SleepStage.LIGHT, determineSleepStage(29L))
        assertEquals(SleepStage.DEEP, determineSleepStage(30L))
        assertEquals(SleepStage.DEEP, determineSleepStage(59L))
        assertEquals(SleepStage.REM, determineSleepStage(60L))
        assertEquals(SleepStage.REM, determineSleepStage(94L))
    }

    @Test
    fun `stages repeat every 90-minute cycle after the initial 5-minute awake period`() {
        assertEquals(determineSleepStage(5L), determineSleepStage(5L + 90L))
        assertEquals(determineSleepStage(29L), determineSleepStage(29L + 90L))
        assertEquals(determineSleepStage(60L), determineSleepStage(60L + 90L * 3))
    }
}
