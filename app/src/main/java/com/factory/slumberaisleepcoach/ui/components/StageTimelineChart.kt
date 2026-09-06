package com.factory.slumberaisleepcoach.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.factory.slumberaisleepcoach.model.SleepStage
import com.factory.slumberaisleepcoach.viewmodel.StageDataPoint

@Composable
fun StageTimelineChart(
    stageHistory: List<StageDataPoint>,
    totalElapsedMs: Long,
    modifier: Modifier = Modifier
) {
    val description = if (stageHistory.isEmpty()) {
        "Sleep stage timeline, no data yet"
    } else {
        "Sleep stage timeline, currently ${stageHistory.last().stage.displayName}"
    }

    Column(modifier = modifier.semantics { contentDescription = description }) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (stageHistory.isEmpty() || totalElapsedMs <= 0L) return@Canvas
            val total = totalElapsedMs.toFloat()
            var x = 0f
            for (index in stageHistory.indices) {
                val point = stageHistory[index]
                val nextStart = if (index + 1 < stageHistory.size) stageHistory[index + 1].elapsedMs else totalElapsedMs
                val segmentWidth = ((nextStart - point.elapsedMs).toFloat() / total) * size.width
                drawRect(
                    color = point.stage.color,
                    topLeft = Offset(x, 0f),
                    size = Size(segmentWidth, size.height)
                )
                x += segmentWidth
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SleepStage.entries.forEach { stage ->
                StageLegendItem(stage)
            }
        }
    }
}

@Composable
private fun StageLegendItem(stage: SleepStage) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = stage.color)
        }
        Text(
            text = stage.shortName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}
