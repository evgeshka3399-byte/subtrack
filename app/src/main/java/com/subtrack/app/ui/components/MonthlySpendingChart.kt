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
import com.subtrack.app.data.Subscription
import com.subtrack.app.util.formatMoney
import java.util.Calendar
import java.util.Locale

private val MONTH_NAMES_RU = listOf(
    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
)

data class DailySpending(
    val day: Int,
    val amount: Double,
    val subscriptions: List<String>
)

@Composable
fun MonthlySpendingChart(
    subscriptions: List<Subscription>,
    modifier: Modifier = Modifier
) {
    if (subscriptions.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                "Нет данных для графика",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // Текущий месяц
    val now = Calendar.getInstance()
    val currentMonth = now.get(Calendar.MONTH)
    val currentYear = now.get(Calendar.YEAR)
    val monthName = MONTH_NAMES_RU[currentMonth]
    val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Собираем списания текущего месяца
    val monthStart = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentMonth)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val monthEnd = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentMonth)
        set(Calendar.DAY_OF_MONTH, daysInMonth)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    // Список списаний: день → (сумма, список подписок)
    val dailyMap = mutableMapOf<Int, MutableList<Pair<Double, String>>>()

    subscriptions.filter { !it.isCancelled }.forEach { sub ->
        var paymentTime = sub.firstPaymentDate
        var safety = 0
        while (paymentTime <= monthEnd && safety < 500) {
            if (paymentTime >= monthStart) {
                val cal = Calendar.getInstance().apply { timeInMillis = paymentTime }
                val day = cal.get(Calendar.DAY_OF_MONTH)
                dailyMap.getOrPut(day) { mutableListOf() }
                    .add(sub.price to sub.name)
            }
            val cal = Calendar.getInstance().apply { timeInMillis = paymentTime }
            when (sub.cycle) {
                "MONTHLY" -> cal.add(Calendar.MONTH, 1)
                "QUARTERLY" -> cal.add(Calendar.MONTH, 3)
                "HALF_YEARLY" -> cal.add(Calendar.MONTH, 6)
                "YEARLY" -> cal.add(Calendar.YEAR, 1)
                "CUSTOM" -> cal.add(Calendar.DAY_OF_YEAR, sub.customCycleDays.coerceAtLeast(1))
                else -> cal.add(Calendar.MONTH, 1)
            }
            paymentTime = cal.timeInMillis
            safety++
        }
    }

    if (dailyMap.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                "В этом месяце списаний нет",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxAmount = dailyMap.maxOf { it.value.sumOf { p -> p.first } }.coerceAtLeast(1.0)
    val totalMonth = dailyMap.values.sumOf { list -> list.sumOf { it.first } }

    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = MaterialTheme.colorScheme.primary

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(900),
        label = "monthly_animation"
    )

    Column(modifier) {
        // Заголовок
        Text(
            "Траты в месяц: $monthName $currentYear",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))

        Row(Modifier.weight(1f).fillMaxWidth()) {
            // Ось Y
            Column(
                Modifier
                    .width(56.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "${maxAmount.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
                Text(
                    "${(maxAmount / 2).toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
                Text("0", style = MaterialTheme.typography.labelSmall, color = labelColor)
            }

            Spacer(Modifier.width(8.dp))

            Box(Modifier.weight(1f).fillMaxHeight()) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Сетка по Y
                    for (i in 0..2) {
                        val y = h * i / 2f
                        drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 2f)
                    }

                    // Позиции дней по X
                    val points = dailyMap.entries
                        .sortedBy { it.key }
                        .map { (day, list) ->
                            val x = (day - 1).toFloat() / (daysInMonth - 1) * w
                            val amount = list.sumOf { it.first }
                            val y = h - (amount / maxAmount * h * animatedProgress).toFloat()
                            Offset(x, y) to (amount to list.map { it.second })
                        }

                    // Линия
                    if (points.size > 1) {
                        val path = Path().apply {
                            moveTo(points.first().first.x, points.first().first.y)
                            points.drop(1).forEach { lineTo(it.first.x, it.first.y) }
                        }
                        drawPath(path, lineColor, style = Stroke(width = 6f))
                    }

                    // Точки
                    points.forEach { (offset, _) ->
                        drawCircle(lineColor, radius = 12f, center = offset)
                        drawCircle(Color.White, radius = 6f, center = offset)
                    }
                }

                // Сумма за месяц — по центру
                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Всего за месяц",
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                    Text(
                        "${formatMoney(totalMonth)} ₽",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = lineColor
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Ось X — дни
        Row(
            Modifier.fillMaxWidth().padding(start = 64.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(1, 5, 10, 15, 20, 25, daysInMonth).forEach { day ->
                Text(
                    "$day",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "Дни списаний",
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(8.dp))

        // Список подписок
        dailyMap.entries.sortedBy { it.key }.forEach { (day, list) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "$day числа:",
                    style = MaterialTheme.typography.bodySmall,
                    color = labelColor
                )
                Text(
                    list.joinToString(", ") { it.second } +
                            " — ${formatMoney(list.sumOf { it.first })} ₽",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
