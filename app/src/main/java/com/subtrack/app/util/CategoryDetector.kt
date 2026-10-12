package com.subtrack.app.util

/**
 * Автоподбор категории по названию подписки.
 * Возвращает список возможных категорий (от самой вероятной к менее вероятной).
 */
object CategoryDetector {

    private val keywords: Map<String, List<String>> = mapOf(
        "Мультиподписки" to listOf(
            "яндекс плюс", "яндексплюс", "yandex plus", "плюс",
            "сберпрайм", "sberprime", "сбер прайм", "сбер",
            "мтс premium", "мтс премиум", "mts premium",
            "ozon premium", "озон премиум", "ozon",
            "тинькофф pro", "tinkoff pro", "т-банк pro", "tbank pro", "тинькофф",
            "альфа-смарт", "alfa smart", "альфа смарт",
            "втб плюс", "vtb plus",
            "wb клуб", "wildberries клуб", "вайлдберриз клуб",
            "газпром бонус", "gazprom bonus"
        ),
        "Кино и ТВ" to listOf(
            "нетфликс", "netflix",
            "кинопоиск", "kinopoisk",
            "иви", "ivi",
            "окко", "okko",
            "винк", "wink",
            "премьер", "premier",
            "старт", "start",
            "море", "more.tv",
            "амедиатека", "amediateka",
            "disney", "дисней"
        ),
        "Музыка" to listOf(
            "spotify", "спотифай", "спотифаи",
            "vk музыка", "вк музыка", "vk music",
            "яндекс музыка", "yandex music",
            "звук", "zvuk",
            "apple music", "эпл мьюзик",
            "youtube music", "ютуб музыка"
        ),
        "Книги и обучение" to listOf(
            "литрес", "litres",
            "mybook", "майбук",
            "яндекс книги", "yandex books",
            "skyeng", "скинг",
            "учи.ру", "uchi.ru",
            "duolingo", "дуолинго"
        ),
        "Софт и сервисы" to listOf(
            "касперский", "kaspersky",
            "облако", "cloud", "icloud",
            "яндекс облако", "yandex cloud",
            "chatgpt", "чатгпт", "openai",
            "nordvpn", "nord vpn",
            "expressvpn", "express vpn",
            "vpn", "впн",
            "microsoft 365", "office 365",
            "adobe", "адоб"
        ),
        "Здоровье и спорт" to listOf(
            "fitmost", "фитмост",
            "gymondo", "джимондо",
            "meditopia", "медитопия",
            "фитнес", "fitness",
            "спортзал", "gym"
        )
    )

    /**
     * Возвращает список категорий, подходящих под название.
     * Первая — самая вероятная.
     */
    fun detect(name: String): List<String> {
        if (name.isBlank()) return emptyList()
        val normalized = name.lowercase().replace("-", " ").trim()

        val matches = mutableListOf<Pair<String, Int>>()

        keywords.forEach { (category, words) ->
            var score = 0
            words.forEach { keyword ->
                if (normalized.contains(keyword)) {
                    // Чем длиннее совпадение — тем выше приоритет
                    score = maxOf(score, keyword.length)
                }
            }
            if (score > 0) matches.add(category to score)
        }

        return matches.sortedByDescending { it.second }.map { it.first }
    }

    /**
     * Возвращает одну наиболее вероятную категорию или null.
     */
    fun detectSingle(name: String): String? = detect(name).firstOrNull()
}
