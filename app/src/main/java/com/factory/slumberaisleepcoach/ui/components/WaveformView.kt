package com.factory.slumberaisleepcoach.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private const val MAX_AMPLITUDE = 15000f

@Composable
fun WaveformView(
    amplitudes: List<Float>,
    isSnoring: Boolean,
    modifier: Modifier = Modifier
) {
    val activeColor by animateFloatAsState(
        targetValue = if (isSnoring) 1f else 0f,
        animationSpec = tween(300),
        label = "waveformColor"
    )
    val normalColor = MaterialTheme.colorScheme.primary
    val alertColor = Color(0xFFFF6B6B)
    val description = if (isSnoring) "Live audio waveform, snoring detected" else "Live audio waveform, room is quiet"

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .semantics { contentDescription = description }
    ) {
        if (amplitudes.isEmpty()) return@Canvas
        val barColor = lerpColor(normalColor, alertColor, activeColor)
        val barCount = amplitudes.size
        val gap = 4.dp.toPx()
        val barWidth = (size.width - gap * (barCount - 1)) / barCount
        val midY = size.height / 2

        amplitudes.forEachIndexed { index, amplitude ->
            val normalized = (amplitude / MAX_AMPLITUDE).coerceIn(0.04f, 1f)
            val barHeight = normalized * size.height
            val x = index * (barWidth + gap)
            drawLine(
                color = barColor,
                start = Offset(x + barWidth / 2, midY - barHeight / 2),
                end = Offset(x + barWidth / 2, midY + barHeight / 2),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color = Color(
    red = start.red + (end.red - start.red) * fraction,
    green = start.green + (end.green - start.green) * fraction,
    blue = start.blue + (end.blue - start.blue) * fraction,
    alpha = 1f
)
