package com.subtrack.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun MonthlySpendingChart(
    subscriptions: List<Subscription>,
    modifier: Modifier = Modifier
) {
    if (subscriptions.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("Нет данных для графика", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val now = Calendar.getInstance()
    val currentMonth = now.get(Calendar.MONTH)
    val currentYear = now.get(Calendar.YEAR)
    val monthName = MONTH_NAMES_RU[currentMonth]
    val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Начало и конец текущего месяца
    val monthStart = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentMonth)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
    }.timeInMillis

    val monthEnd = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentMonth)
        set(Calendar.DAY_OF_MONTH, daysInMonth)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
    }.timeInMillis

    // Собираем списания по дням
    val dailyMap = mutableMapOf<Int, MutableList<Pair<Double, String>>>()

    subscriptions.filter { !it.isCancelled }.forEach { sub ->
        var paymentTime = sub.firstPaymentDate
        var safety = 0
        while (paymentTime <= monthEnd && safety < 500) {
            if (paymentTime >= monthStart) {
                val cal = Calendar.getInstance().apply { timeInMillis = paymentTime }
                val day = cal.get(Calendar.DAY_OF_MONTH)
                dailyMap.getOrPut(day) { mutableListOf() }.add(sub.price to sub.name)
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
            Text("В этом месяце списаний нет", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val totalMonth = dailyMap.values.sumOf { list -> list.sumOf { it.first } }

    // Первый день месяца (день недели: 1=Пн, 7=Вс)
    val firstDayCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentMonth)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    // Приводим к стандарту: Пн=0, Вт=1, ..., Вс=6
    val firstDayOfWeek = (firstDayCal.get(Calendar.DAY_OF_WEEK) + 5) % 7

    val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    Column(modifier) {
        // Заголовок с суммой
        Text(
            "Траты в месяц: $monthName $currentYear — ${formatMoney(totalMonth)} ₽",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))

        // Дни недели
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        // Сетка календаря
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7 // округляем вверх

        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - firstDayOfWeek + 1

                    if (dayNumber in 1..daysInMonth) {
                        val hasSpending = dailyMap.containsKey(dayNumber)
                        val dayAmount = dailyMap[dayNumber]?.sumOf { it.first } ?: 0.0

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    color = if (hasSpending)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$dayNumber",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (hasSpending) FontWeight.Bold else FontWeight.Normal,
                                    color = if (hasSpending)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (hasSpending) {
                                    Text(
                                        formatMoney(dayAmount),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    } else {
                        // Пустая ячейка
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Список подписок с суммами
        Text(
            "Детализация:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        dailyMap.entries.sortedBy { it.key }.forEach { (day, list) ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "$day числа:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    list.joinToString(", ") { it.second } + " — ${formatMoney(list.sumOf { it.first })} ₽",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
