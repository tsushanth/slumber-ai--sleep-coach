package com.factory.slumberaisleepcoach.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class TimeFormatTest {

    @Test
    fun `formatDurationHms formats zero as 00-00-00`() {
        assertEquals("00:00:00", formatDurationHms(0L))
    }

    @Test
    fun `formatDurationHms pads hours, minutes, and seconds to two digits`() {
        val durationMs = TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(2) + TimeUnit.SECONDS.toMillis(3)

        assertEquals("01:02:03", formatDurationHms(durationMs))
    }

    @Test
    fun `formatDurationHms wraps minutes and seconds past their unit, not the total`() {
        val durationMs = TimeUnit.HOURS.toMillis(2) + TimeUnit.MINUTES.toMillis(90)

        assertEquals("03:30:00", formatDurationHms(durationMs))
    }

    @Test
    fun `formatDurationHm omits the hour component when under an hour`() {
        val durationMs = TimeUnit.MINUTES.toMillis(45)

        assertEquals("45m", formatDurationHm(durationMs))
    }

    @Test
    fun `formatDurationHm includes hours and minutes when an hour or more`() {
        val durationMs = TimeUnit.HOURS.toMillis(7) + TimeUnit.MINUTES.toMillis(15)

        assertEquals("7h 15m", formatDurationHm(durationMs))
    }

    @Test
    fun `formatDurationHm rounds down to zero minutes for a sub-minute duration`() {
        assertEquals("0m", formatDurationHm(500L))
    }
}
