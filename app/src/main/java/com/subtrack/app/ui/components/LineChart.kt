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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LinePoint(
    val label: String,
    val value: Double
)

@Composable
fun LineChart(
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
    val textColor = MaterialTheme.colorScheme.onBackground

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(900),
        label = "line_animation"
    )

    Column(modifier) {
        Row(
            Modifier.weight(1f).fillMaxWidth()
        ) {
            // Ось Y с подписями
            Column(
                Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "${maxValue.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
                Text(
                    "${(maxValue / 2).toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
                Text(
                    "0",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
            }

            Spacer(Modifier.width(8.dp))

            // Сам график
            Box(Modifier.weight(1f).fillMaxHeight()) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val stepX = if (points.size > 1) w / (points.size - 1) else w

                    // Горизонтальные линии сетки (3 штуки)
                    for (i in 0..2) {
                        val y = h * i / 2f
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 2f
                        )
                    }

                    // Точки графика
                    val offsets = points.mapIndexed { index, point ->
                        val x = stepX * index
                        val y = h - (point.value / maxValue * h * animatedProgress).toFloat()
                        Offset(x, y)
                    }

                    // Линия
                    if (offsets.size > 1) {
                        val path = Path().apply {
                            moveTo(offsets.first().x, offsets.first().y)
                            offsets.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 6f)
                        )
                    }

                    // Точки на линии
                    offsets.forEach { offset ->
                        drawCircle(
                            color = lineColor,
                            radius = 10f,
                            center = offset
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 5f,
                            center = offset
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Ось X с месяцами
        Row(
            Modifier.fillMaxWidth().padding(start = 56.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { point ->
                Text(
                    point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontSize = 10.sp
                )
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

/**
 * Строит точки прогноза на N месяцев вперёд.
 * Для каждой активной подписки смотрим, в какие месяцы произойдёт списание.
 */
fun buildForecast(
    subscriptions: List<com.subtrack.app.data.Subscription>,
    months: Int = 12
): List<LinePoint> {
    val calendar = java.util.Calendar.getInstance()
    val monthNames = listOf("Янв", "Фев", "Мар", "Апр", "Май", "Июн",
                            "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек")

    val result = mutableListOf<LinePoint>()
    val now = System.currentTimeMillis()

    for (i in 0 until months) {
        val monthCal = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            add(java.util.Calendar.MONTH, i)
        }
        val monthLabel = monthNames[monthCal.get(java.util.Calendar.MONTH)]

        var total = 0.0
        subscriptions.forEach { sub ->
            if (!sub.isCancelled) {
                var paymentTime = sub.nextPaymentDate
                // Считаем все списания внутри целевого месяца
                val monthStart = monthCal.clone() as java.util.Calendar
                monthStart.set(java.util.Calendar.DAY_OF_MONTH, 1)
                monthStart.set(java.util.Calendar.HOUR_OF_DAY, 0)
                val monthEnd = monthStart.clone() as java.util.Calendar
                monthEnd.add(java.util.Calendar.MONTH, 1)

                // Симулируем списания
                var safety = 0
                while (paymentTime < monthEnd.timeInMillis && safety < 100) {
                    if (paymentTime >= monthStart.timeInMillis) {
                        total += sub.price
                    }
                    val c = java.util.Calendar.getInstance().apply {
                        timeInMillis = paymentTime
                    }
                    when (sub.cycle) {
                        "MONTHLY" -> c.add(java.util.Calendar.MONTH, 1)
                        "QUARTERLY" -> c.add(java.util.Calendar.MONTH, 3)
                        "HALF_YEARLY" -> c.add(java.util.Calendar.MONTH, 6)
                        "YEARLY" -> c.add(java.util.Calendar.YEAR, 1)
                        "CUSTOM" -> c.add(java.util.Calendar.DAY_OF_YEAR,
                            sub.customCycleDays.coerceAtLeast(1))
                        else -> c.add(java.util.Calendar.MONTH, 1)
                    }
                    paymentTime = c.timeInMillis
                    safety++
                }
            }
        }

        result.add(LinePoint(monthLabel, total))
    }
    return result
}
