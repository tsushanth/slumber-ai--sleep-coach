package com.factory.slumberaisleepcoach.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.factory.slumberaisleepcoach.data.entities.SleepSession
import com.factory.slumberaisleepcoach.data.entities.SnoringRecord
import com.factory.slumberaisleepcoach.model.SleepStage
import com.factory.slumberaisleepcoach.ui.components.LockedFeatureCard
import com.factory.slumberaisleepcoach.ui.components.QualityRing
import com.factory.slumberaisleepcoach.util.formatDurationHm
import com.factory.slumberaisleepcoach.util.formatSessionDate
import com.factory.slumberaisleepcoach.util.formatSessionTime

@Composable
fun SessionDetailScreen(
    session: SleepSession?,
    snoringRecords: List<SnoringRecord>,
    isPremium: Boolean,
    onBack: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onBack()
            }) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Session Details",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (session == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = "Loading session details" },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${formatSessionDate(session.startTime)} · ${formatSessionTime(session.startTime)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    QualityRing(quality = session.qualityScore, modifier = Modifier.padding(top = 16.dp))
                    Text(
                        text = formatDurationHm(session.durationMs),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            if (isPremium) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Sleep Stages",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StageRow("Deep Sleep", session.deepSleepMs, SleepStage.DEEP.color)
                            StageRow("Light Sleep", session.lightSleepMs, SleepStage.LIGHT.color)
                            StageRow("REM Sleep", session.remSleepMs, SleepStage.REM.color)
                            StageRow("Awake", session.awakeMs, SleepStage.AWAKE.color)
                        }
                    }
                }

                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Snoring Summary",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${session.snoringEvents}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Events",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column {
                                    Text(
                                        text = formatDurationHm(session.snoringDurationMs),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Total Duration",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                if (snoringRecords.isNotEmpty()) {
                    item {
                        Text(
                            text = "Snoring Events Timeline",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    items(snoringRecords, key = { it.id }) { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "Snoring event at ${formatSessionTime(record.timestamp)}, " +
                                        "peak amplitude ${record.maxAmplitude.toInt()}"
                                },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatSessionTime(record.timestamp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Peak ${record.maxAmplitude.toInt()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                item {
                    LockedFeatureCard(
                        title = "Sleep Stage Analysis",
                        description = "See your deep, light, REM and awake breakdown for this night.",
                        onUnlockClick = onUpgradeClick
                    )
                }
                item {
                    LockedFeatureCard(
                        title = "Snoring Insights",
                        description = "See snoring event count, duration and a full timeline.",
                        onUnlockClick = onUpgradeClick
                    )
                }
            }
        }
    }
}

@Composable
private fun StageRow(label: String, durationMs: Long, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
                drawCircle(color = color)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Text(
            text = formatDurationHm(durationMs),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
