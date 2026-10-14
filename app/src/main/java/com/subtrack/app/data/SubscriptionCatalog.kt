package com.subtrack.app.data

object SubscriptionCatalog {

    data class CatalogItem(
        val name: String,
        val category: String
    )

    val items: List<CatalogItem> = listOf(
        // === МУЛЬТИПОДПИСКИ ===
        CatalogItem("Яндекс Плюс", "Мультиподписки"),
        CatalogItem("СберПрайм", "Мультиподписки"),
        CatalogItem("МТС Premium", "Мультиподписки"),
        CatalogItem("OZON Premium", "Мультиподписки"),
        CatalogItem("Т-Банк Pro", "Мультиподписки"),
        CatalogItem("Альфа-Смарт", "Мультиподписки"),
        CatalogItem("ВТБ Плюс", "Мультиподписки"),
        CatalogItem("WB Клуб", "Мультиподписки"),
        CatalogItem("Газпром Бонус", "Мультиподписки"),

        // === КИНО И ТВ ===
        CatalogItem("Кинопоиск", "Кино и ТВ"),
        CatalogItem("Иви", "Кино и ТВ"),
        CatalogItem("Okko", "Кино и ТВ"),
        CatalogItem("Wink", "Кино и ТВ"),
        CatalogItem("Premier", "Кино и ТВ"),
        CatalogItem("Start", "Кино и ТВ"),
        CatalogItem("More.tv", "Кино и ТВ"),
        CatalogItem("Amediateka", "Кино и ТВ"),
        CatalogItem("Netflix", "Кино и ТВ"),
        CatalogItem("Disney+", "Кино и ТВ"),

        // === МУЗЫКА ===
        CatalogItem("Яндекс Музыка", "Музыка"),
        CatalogItem("VK Музыка", "Музыка"),
        CatalogItem("Звук", "Музыка"),
        CatalogItem("Spotify", "Музыка"),
        CatalogItem("Apple Music", "Музыка"),
        CatalogItem("YouTube Music", "Музыка"),

        // === КНИГИ И ОБУЧЕНИЕ ===
        CatalogItem("Яндекс Книги", "Книги и обучение"),
        CatalogItem("Литрес", "Книги и обучение"),
        CatalogItem("MyBook", "Книги и обучение"),
        CatalogItem("Skyeng", "Книги и обучение"),
        CatalogItem("Учи.ру", "Книги и обучение"),
        CatalogItem("Duolingo", "Книги и обучение"),

        // === СОФТ И СЕРВИСЫ ===
        CatalogItem("Яндекс Облако", "Софт и сервисы"),
        CatalogItem("Облако Mail.ru", "Софт и сервисы"),
        CatalogItem("iCloud", "Софт и сервисы"),
        CatalogItem("Kaspersky", "Софт и сервисы"),
        CatalogItem("ChatGPT Plus", "Софт и сервисы"),
        CatalogItem("NordVPN", "Софт и сервисы"),
        CatalogItem("ExpressVPN", "Софт и сервисы"),
        CatalogItem("Microsoft 365", "Софт и сервисы"),
        CatalogItem("Adobe Creative Cloud", "Софт и сервисы"),

        // === ЗДОРОВЬЕ И СПОРТ ===
        CatalogItem("Fitmost", "Здоровье и спорт"),
        CatalogItem("Gymondo", "Здоровье и спорт"),
        CatalogItem("Meditopia", "Здоровье и спорт"),
        CatalogItem("World Class", "Здоровье и спорт"),

        // === СВЯЗЬ ===
        CatalogItem("МТС", "Связь"),
        CatalogItem("МегаФон", "Связь"),
        CatalogItem("Билайн", "Связь"),
        CatalogItem("Т2 (Tele2)", "Связь"),
        CatalogItem("Yota", "Связь"),
        CatalogItem("СберМобайл", "Связь"),
        CatalogItem("Тинькофф Мобайл", "Связь"),

        // === ПРОЧЕЕ ===
        CatalogItem("YouTube Premium", "Кино и ТВ"),
        CatalogItem("Apple TV+", "Кино и ТВ")
    )

    fun search(query: String): List<CatalogItem> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase().trim()

        val startsWith = items.filter { it.name.lowercase().startsWith(q) }
        val contains = items.filter {
            !it.name.lowercase().startsWith(q) && it.name.lowercase().contains(q)
        }
        return (startsWith + contains).take(8)
    }

    fun findByName(name: String): CatalogItem? =
        items.firstOrNull { it.name.equals(name, ignoreCase = true) }
}
