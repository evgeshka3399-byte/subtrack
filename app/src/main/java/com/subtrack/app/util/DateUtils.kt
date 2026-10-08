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

    while (calendar.timeInMillis <= now) {
        when (cycle) {
            "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
            "QUARTERLY" -> calendar.add(Calendar.MONTH, 3)
            "HALF_YEARLY" -> calendar.add(Calendar.MONTH, 6)
            "YEARLY" -> calendar.add(Calendar.YEAR, 1)
            "CUSTOM" -> calendar.add(Calendar.DAY_OF_YEAR, customDays.coerceAtLeast(1))
            else -> calendar.add(Calendar.MONTH, 1)
        }
    }
    return calendar.timeInMillis
}

fun monthlyEquivalent(price: Double, cycle: String, customDays: Int): Double {
    return when (cycle) {
        "MONTHLY" -> price
        "QUARTERLY" -> price / 3
        "HALF_YEARLY" -> price / 6
        "YEARLY" -> price / 12
        "CUSTOM" -> if (customDays > 0) price * 30.0 / customDays else price
        else -> price
    }
}

fun calculateMonthlyTotal(subs: List<Subscription>): Double =
    subs.sumOf { monthlyEquivalent(it.price, it.cycle, it.customCycleDays) }

fun calculateYearlyTotal(subs: List<Subscription>): Double =
    calculateMonthlyTotal(subs) * 12

fun daysUntil(timestamp: Long): Long {
    val diff = timestamp - System.currentTimeMillis()
    return TimeUnit.MILLISECONDS.toDays(diff)
}
