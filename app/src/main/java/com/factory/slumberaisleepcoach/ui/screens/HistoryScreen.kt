package com.factory.slumberaisleepcoach.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.factory.slumberaisleepcoach.data.entities.SleepSession
import com.factory.slumberaisleepcoach.ui.components.LockedFeatureCard
import com.factory.slumberaisleepcoach.ui.components.SessionListItem

private const val FREE_HISTORY_LIMIT = 3

@Composable
fun HistoryScreen(
    sessions: List<SleepSession>,
    isPremium: Boolean,
    onSessionClick: (Long) -> Unit,
    onUpgradeClick: () -> Unit
) {
    if (sessions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "No sleep sessions recorded yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Track a night's sleep to start building your history.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        return
    }

    val visibleSessions = if (isPremium) sessions else sessions.take(FREE_HISTORY_LIMIT)
    val lockedCount = sessions.size - visibleSessions.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Sleep History",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        items(visibleSessions, key = { it.id }) { session ->
            SessionListItem(session = session, onClick = { onSessionClick(session.id) })
        }
        if (lockedCount > 0) {
            item {
                LockedFeatureCard(
                    title = "Unlimited History",
                    description = "$lockedCount more night${if (lockedCount == 1) "" else "s"} available with Premium.",
                    onUnlockClick = onUpgradeClick
                )
            }
        }
    }
}
