package com.subtrack.app.util

import com.subtrack.app.data.Subscription
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Вычисляет дату следующего списания от firstPaymentDate.
 *
 * Если firstPaymentDate в будущем — возвращает её как есть.
 * Если в прошлом — прибавляет цикл, пока дата не станет > сегодня.
 */
fun calculateNextPaymentDate(
    firstPaymentDate: Long,
    cycle: String,
    customDays: Int
): Long {
    val now = System.currentTimeMillis()

    // Если дата в будущем или сегодня — оставляем как есть
    if (firstPaymentDate >= now - 24L * 60 * 60 * 1000) return firstPaymentDate

    val calendar = Calendar.getInstance().apply {
        timeInMillis = firstPaymentDate
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    var safety = 0

    while (calendar.timeInMillis <= now && safety < 1000) {
        val beforeDay = calendar.get(Calendar.DAY_OF_MONTH)
        when (cycle) {
            "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
            "QUARTERLY" -> calendar.add(Calendar.MONTH, 3)
            "HALF_YEARLY" -> calendar.add(Calendar.MONTH, 6)
            "YEARLY" -> calendar.add(Calendar.YEAR, 1)
            "CUSTOM" -> calendar.add(Calendar.DAY_OF_YEAR, customDays.coerceAtLeast(1))
            else -> calendar.add(Calendar.MONTH, 1)
        }
        // Защита от сдвига дня (например, 31 января → 28 февраля)
        val afterDay = calendar.get(Calendar.DAY_OF_MONTH)
        if (afterDay < beforeDay && cycle in listOf("MONTHLY", "QUARTERLY", "HALF_YEARLY")) {
            calendar.set(Calendar.DAY_OF_MONTH, beforeDay.coerceAtMost(
                calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            ))
        }
        safety++
    }
    return calendar.timeInMillis
}

fun monthlyEquivalent(price: Double, cycle: String, customDays: Int): Double {
    if (price.isNaN() || price.isInfinite()) return 0.0
    val result = when (cycle) {
        "MONTHLY" -> price
        "QUARTERLY" -> price / 3
        "HALF_YEARLY" -> price / 6
        "YEARLY" -> price / 12
        "CUSTOM" -> if (customDays > 0) price * 30.0 / customDays else price
        else -> price
    }
    return if (result.isNaN() || result.isInfinite()) 0.0 else result
}

fun calculateMonthlyTotal(subs: List<Subscription>): Double {
    val total = subs.sumOf { monthlyEquivalent(it.price, it.cycle, it.customCycleDays) }
    return if (total.isNaN() || total.isInfinite()) 0.0 else total
}

fun calculateYearlyTotal(subs: List<Subscription>): Double {
    val total = calculateMonthlyTotal(subs) * 12
    return if (total.isNaN() || total.isInfinite()) 0.0 else total
}

fun daysUntil(timestamp: Long): Long {
    val diff = timestamp - System.currentTimeMillis()
    return TimeUnit.MILLISECONDS.toDays(diff)
}

fun formatMoney(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "0"
    return "%.0f".format(value)
}
