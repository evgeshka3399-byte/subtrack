package com.subtrack.app.ui.theme

import androidx.compose.ui.graphics.Color

object CategoryColors {
    val Мультиподписки = Color(0xFF2962FF)
    val Кино = Color(0xFFE53935)
    val Музыка = Color(0xFF8E24AA)
    val Книги = Color(0xFFFB8C00)
    val Софт = Color(0xFF00ACC1)
    val Здоровье = Color(0xFF43A047)
    val Связь = Color(0xFF00897B)
    val Другое = Color(0xFF757575)

    val categories = listOf(
        "Мультиподписки",
        "Кино и ТВ",
        "Музыка",
        "Книги и обучение",
        "Софт и сервисы",
        "Здоровье и спорт",
        "Связь",
        "Другое"
    )

    fun colorFor(category: String): Color = when (category) {
        "Мультиподписки" -> Мультиподписки
        "Кино и ТВ" -> Кино
        "Музыка" -> Музыка
        "Книги и обучение" -> Книги
        "Софт и сервисы" -> Софт
        "Здоровье и спорт" -> Здоровье
        "Связь" -> Связь
        else -> Другое
    }
}
