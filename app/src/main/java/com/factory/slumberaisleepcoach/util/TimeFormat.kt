package com.factory.slumberaisleepcoach.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun formatDurationHms(durationMs: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}

fun formatDurationHm(durationMs: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

fun formatSessionDate(timestampMs: Long): String {
    val formatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    return formatter.format(Date(timestampMs))
}

fun formatSessionTime(timestampMs: Long): String {
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(Date(timestampMs))
}
