package com.subtrack.app.util

import com.subtrack.app.data.Subscription
import java.util.Calendar
import java.util.concurrent.TimeUnit

fun calculateNextPaymentDate(
    firstPaymentDate: Long,
    cycle: String,
    customDays: Int
): Long {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance().apply { timeInMillis = firstPaymentDate }

    var safety = 0
    while (calendar.timeInMillis <= now && safety < 1000) {
        when (cycle) {
            "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
            "QUARTERLY" -> calendar.add(Calendar.MONTH, 3)
            "HALF_YEARLY" -> calendar.add(Calendar.MONTH, 6)
            "YEARLY" -> calendar.add(Calendar.YEAR, 1)
            "CUSTOM" -> calendar.add(Calendar.DAY_OF_YEAR, customDays.coerceAtLeast(1))
            else -> calendar.add(Calendar.MONTH, 1)
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
