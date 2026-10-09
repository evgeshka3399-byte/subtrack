package com.subtrack.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChartSlice(
    val label: String,
    val value: Double,
    val color: Color
)

@Composable
fun PieChart(
    slices: List<ChartSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 60f
) {
    val total = slices.sumOf { it.value }
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800),
        label = "pie_animation"
    )

    if (total <= 0.0) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                "Нет данных для диаграммы",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val size = minOf(this.size.width, this.size.height) - strokeWidth
            val topLeft = Offset(
                (this.size.width - size) / 2f,
                (this.size.height - size) / 2f
            )
            val arcSize = Size(size, size)

            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = (slice.value / total * 360f * animatedProgress).toFloat()
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweep
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Всего",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${total.toInt()} ₽",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
