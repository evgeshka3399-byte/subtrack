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

enum class ForecastMode { AVERAGE, ACTUAL }

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

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(900),
        label = "line_animation"
    )

    Column(modifier) {
        Row(
            Modifier.weight(1f).fillMaxWidth()
        ) {
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

                    if (offsets.size > 1) {
                        val path = Path().apply {
                            moveTo(offsets.first().x, offsets.first().y)
                            offsets.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(path, lineColor, style = Stroke(width = 6f))
                    }

                    offsets.forEach { offset ->
                        drawCircle(lineColor, radius = 10f, center = offset)
                        drawCircle(Color.White, radius = 5f, center = offset)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

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
 * Строит прогноз на N месяцев.
 * @param mode AVERAGE — среднемесячная нагрузка (все годовые траты усреднены).
 *             ACTUAL  — реальные списания по датам.
 */
fun buildForecast(
    subscriptions: List<com.subtrack.app.data.Subscription>,
    months: Int = 12,
    mode: ForecastMode = ForecastMode.AVERAGE
): List<LinePoint> {
    val monthNames = listOf("Янв", "Фев", "Мар", "Апр", "Май", "Июн",
                            "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек")
    val now = System.currentTimeMillis()

    // Границы месяцев
    val monthBounds = (0 until months).map { i ->
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            add(java.util.Calendar.MONTH, i)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val monthIndex = cal.get(java.util.Calendar.MONTH)
        cal.add(java.util.Calendar.MONTH, 1)
        val end = cal.timeInMillis
        Triple(monthNames[monthIndex], start, end)
    }

    val activeSubs = subscriptions.filter { !it.isCancelled }

    return when (mode) {
        ForecastMode.AVERAGE -> {
            // Месячная нагрузка каждой подписки
            val monthlyTotal = activeSubs.sumOf { sub ->
                com.subtrack.app.util.monthlyEquivalent(sub.price, sub.cycle, sub.customCycleDays)
            }
            monthBounds.map { (label, _, _) -> LinePoint(label, monthlyTotal) }
        }

                ForecastMode.ACTUAL -> {
            monthBounds.map { (label, start, end) ->
                var total = 0.0
                activeSubs.forEach { sub ->
                    // Стартуем с firstPaymentDate, но пропускаем всё, что уже в прошлом
                    var paymentTime = sub.firstPaymentDate
                    val nowTime = System.currentTimeMillis()

                    // Прокручиваем цикл, пока не попадём в будущее
                    var skipSafety = 0
                    while (paymentTime <= nowTime && skipSafety < 500) {
                        val cal = java.util.Calendar.getInstance().apply {
                            timeInMillis = paymentTime
                        }
                        when (sub.cycle) {
                            "MONTHLY" -> cal.add(java.util.Calendar.MONTH, 1)
                            "QUARTERLY" -> cal.add(java.util.Calendar.MONTH, 3)
                            "HALF_YEARLY" -> cal.add(java.util.Calendar.MONTH, 6)
                            "YEARLY" -> cal.add(java.util.Calendar.YEAR, 1)
                            "CUSTOM" -> cal.add(java.util.Calendar.DAY_OF_YEAR,
                                sub.customCycleDays.coerceAtLeast(1))
                            else -> cal.add(java.util.Calendar.MONTH, 1)
                        }
                        paymentTime = cal.timeInMillis
                        skipSafety++
                    }

                    // Теперь считаем списания внутри целевого месяца
                    var safety = 0
                    while (paymentTime < end && safety < 500) {
                        if (paymentTime >= start && paymentTime < end) {
                            total += sub.price
                        }
                        val cal = java.util.Calendar.getInstance().apply {
                            timeInMillis = paymentTime
                        }
                        when (sub.cycle) {
                            "MONTHLY" -> cal.add(java.util.Calendar.MONTH, 1)
                            "QUARTERLY" -> cal.add(java.util.Calendar.MONTH, 3)
                            "HALF_YEARLY" -> cal.add(java.util.Calendar.MONTH, 6)
                            "YEARLY" -> cal.add(java.util.Calendar.YEAR, 1)
                            "CUSTOM" -> cal.add(java.util.Calendar.DAY_OF_YEAR,
                                sub.customCycleDays.coerceAtLeast(1))
                            else -> cal.add(java.util.Calendar.MONTH, 1)
                        }
                        paymentTime = cal.timeInMillis
                        safety++
                    }
                }
                LinePoint(label, total)
            }
        }
