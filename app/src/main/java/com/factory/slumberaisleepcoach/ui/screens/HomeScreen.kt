package com.factory.slumberaisleepcoach.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.factory.slumberaisleepcoach.data.entities.SleepSession
import com.factory.slumberaisleepcoach.ui.components.LockedFeatureCard
import com.factory.slumberaisleepcoach.ui.components.SessionListItem
import com.factory.slumberaisleepcoach.ui.components.StatCard
import com.factory.slumberaisleepcoach.util.formatDurationHm

@Composable
fun HomeScreen(
    recentSessions: List<SleepSession>,
    averageSleepDuration: Long?,
    averageQuality: Float?,
    totalSnoringEvents: Int?,
    totalSessionCount: Int,
    isPremium: Boolean,
    onStartTracking: () -> Unit,
    onSeeAllHistory: () -> Unit,
    onSessionClick: (Long) -> Unit,
    onUpgradeClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Good evening",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Ready for restful sleep?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        item {
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onStartTracking()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(imageVector = Icons.Filled.Nightlight, contentDescription = null)
                Spacer(modifier = Modifier.height(0.dp).padding(horizontal = 4.dp))
                Text(
                    text = "Start Sleep Tracking",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        if (!isPremium) {
            item {
                LockedFeatureCard(
                    title = "Go Premium",
                    description = "Unlock sleep stage analysis, snoring insights and unlimited history.",
                    onUnlockClick = onUpgradeClick
                )
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    StatCard(
                        icon = Icons.Filled.Bedtime,
                        label = "Avg. Sleep",
                        value = if (averageSleepDuration != null) formatDurationHm(averageSleepDuration) else "--"
                    )
                }
                item {
                    StatCard(
                        icon = Icons.Filled.Star,
                        label = "Avg. Quality",
                        value = if (averageQuality != null) "${averageQuality.toInt()}%" else "--",
                        locked = !isPremium,
                        onLockedClick = onUpgradeClick
                    )
                }
                item {
                    StatCard(
                        icon = Icons.Filled.GraphicEq,
                        label = "Snoring Events",
                        value = "${totalSnoringEvents ?: 0}",
                        locked = !isPremium,
                        onLockedClick = onUpgradeClick
                    )
                }
                item {
                    StatCard(
                        icon = Icons.Filled.Nightlight,
                        label = "Sessions Logged",
                        value = "$totalSessionCount"
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (recentSessions.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSeeAllHistory()
                        }
                    ) {
                        Text("See All")
                    }
                }
            }
        }

        if (recentSessions.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bedtime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.height(36.dp)
                    )
                    Text(
                        text = "No sleep sessions yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        text = "Start tracking tonight to see your history here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            items(recentSessions, key = { it.id }) { session ->
                SessionListItem(session = session, onClick = { onSessionClick(session.id) })
            }
        }
    }
}
