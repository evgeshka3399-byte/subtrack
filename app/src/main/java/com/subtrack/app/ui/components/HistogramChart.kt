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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subtrack.app.data.Subscription

@Composable
fun HistogramChart(
    subscriptions: List<Subscription>,
    modifier: Modifier = Modifier
) {
    if (subscriptions.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                "Нет данных",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // Диапазоны цен
    val ranges = listOf(
        "0–300",
        "300–600",
        "600–1000",
        "1000–2000",
        "2000+"
    )

    val counts = IntArray(ranges.size)
    subscriptions.forEach { sub ->
        when {
            sub.price < 300 -> counts[0]++
            sub.price < 600 -> counts[1]++
            sub.price < 1000 -> counts[2]++
            sub.price < 2000 -> counts[3]++
            else -> counts[4]++
        }
    }

    val maxCount = counts.max().coerceAtLeast(1)
    val barColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(900),
        label = "hist_animation"
    )

    Column(modifier) {
        Row(Modifier.weight(1f).fillMaxWidth()) {
            Column(
                Modifier
                    .width(32.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text("$maxCount", style = MaterialTheme.typography.labelSmall, color = labelColor)
                Text("${maxCount / 2}", style = MaterialTheme.typography.labelSmall, color = labelColor)
                Text("0", style = MaterialTheme.typography.labelSmall, color = labelColor)
            }

            Spacer(Modifier.width(8.dp))

            Canvas(Modifier.weight(1f).fillMaxHeight()) {
                val w = size.width
                val h = size.height
                val barWidth = w / ranges.size * 0.7f
                val gap = w / ranges.size * 0.3f

                // Сетка
                for (i in 0..2) {
                    val y = h * i / 2f
                    drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 2f)
                }

                // Столбцы
                counts.forEachIndexed { index, count ->
                    val barHeight = (count.toFloat() / maxCount * h * animatedProgress)
                    val x = gap / 2 + index * (barWidth + gap)
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, h - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth().padding(start = 40.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ranges.forEach { range ->
                Text(
                    range,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontSize = 9.sp
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "Распределение цен подписок (₽)",
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
