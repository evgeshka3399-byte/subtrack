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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AreaChart(
    points: List<LinePoint>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                "Нет данных для графика",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxValue = points.maxOf { it.value }.coerceAtLeast(1.0)
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(900),
        label = "area_animation"
    )

    Column(modifier) {
        Row(Modifier.weight(1f).fillMaxWidth()) {
            Column(
                Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text("${maxValue.toInt()}", style = MaterialTheme.typography.labelSmall, color = labelColor)
                Text("${(maxValue / 2).toInt()}", style = MaterialTheme.typography.labelSmall, color = labelColor)
                Text("0", style = MaterialTheme.typography.labelSmall, color = labelColor)
            }

            Spacer(Modifier.width(8.dp))

            Box(Modifier.weight(1f).fillMaxHeight()) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val stepX = if (points.size > 1) w / (points.size - 1) else w

                    for (i in 0..2) {
                        val y = h * i / 2f
                        drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 2f)
                    }

                    val offsets = points.mapIndexed { index, point ->
                        val x = stepX * index
                        val y = h - (point.value / maxValue * h * animatedProgress).toFloat()
                        Offset(x, y)
                    }

                    // Заливка области
                    if (offsets.size > 1) {
                        val fillPath = Path().apply {
                            moveTo(offsets.first().x, h)
                            offsets.forEach { lineTo(it.x, it.y) }
                            lineTo(offsets.last().x, h)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            color = lineColor.copy(alpha = 0.3f)
                        )

                        // Линия сверху
                        val linePath = Path().apply {
                            moveTo(offsets.first().x, offsets.first().y)
                            offsets.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(width = 6f)
                        )
                    }

                    // Точки
                    offsets.forEach { offset ->
                        drawCircle(lineColor, radius = 8f, center = offset)
                        drawCircle(Color.White, radius = 4f, center = offset)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth().padding(start = 56.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEachIndexed { index, point ->
                if (index % 3 == 0 || index == points.size - 1) {
                    Text(
                        point.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "Прогноз трат по месяцам",
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
