package com.subtrack.app.util

import com.subtrack.app.data.Subscription
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun exportToCsv(subscriptions: List<Subscription>): String {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    val sb = StringBuilder()
    sb.append("Название;Цена;Валюта;Категория;Цикл;Дата списания;Статус\n")

    subscriptions.forEach { sub ->
        sb.append(safe(sub.name)).append(";")
        sb.append(sub.price).append(";")
        sb.append(sub.currency).append(";")
        sb.append(safe(sub.category)).append(";")
        sb.append(cycleLabel(sub.cycle)).append(";")
        sb.append(dateFormat.format(Date(sub.nextPaymentDate))).append(";")
        sb.append(if (sub.isCancelled) "Отменена" else "Активна")
        sb.append("\n")
    }

    return sb.toString()
}

private fun safe(text: String): String = text.replace(";", ",")

private fun cycleLabel(cycle: String): String = when (cycle) {
    "MONTHLY" -> "Месяц"
    "QUARTERLY" -> "3 месяца"
    "HALF_YEARLY" -> "6 месяцев"
    "YEARLY" -> "Год"
    "CUSTOM" -> "Свой"
    else -> cycle
}
