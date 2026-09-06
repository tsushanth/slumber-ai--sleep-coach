package com.factory.slumberaisleepcoach.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.factory.slumberaisleepcoach.model.SleepStage
import com.factory.slumberaisleepcoach.ui.components.LockedFeatureCard
import com.factory.slumberaisleepcoach.ui.components.StageTimelineChart
import com.factory.slumberaisleepcoach.ui.components.WaveformView
import com.factory.slumberaisleepcoach.util.formatDurationHms
import com.factory.slumberaisleepcoach.viewmodel.StageDataPoint

@Composable
fun TrackingScreen(
    elapsedMs: Long,
    currentStage: SleepStage,
    stageHistory: List<StageDataPoint>,
    amplitudeHistory: List<Float>,
    snoringDetected: Boolean,
    snoringEventsCount: Int,
    isPremium: Boolean,
    onStopTracking: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Tracking your sleep",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = formatDurationHms(elapsedMs),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(currentStage.color.copy(alpha = 0.18f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = currentStage.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = currentStage.color
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = (if (snoringDetected) "Snoring detected" else "Listening") +
                            ", $snoringEventsCount events so far"
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (snoringDetected) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.surfaceVariant)
                )
                Text(
                    text = if (snoringDetected) "Snoring detected" else "Listening…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(16.dp)
                )
                Text(
                    text = " $snoringEventsCount events",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            WaveformView(amplitudes = amplitudeHistory, isSnoring = snoringDetected)

            Spacer(modifier = Modifier.height(24.dp))
            if (isPremium) {
                StageTimelineChart(
                    stageHistory = stageHistory,
                    totalElapsedMs = elapsedMs,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LockedFeatureCard(
                    title = "Live Sleep Stage Analysis",
                    description = "See your sleep stages update in real time.",
                    onUnlockClick = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onStopTracking()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Icon(imageVector = Icons.Filled.Stop, contentDescription = null)
            Text(
                text = "Stop Tracking",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
