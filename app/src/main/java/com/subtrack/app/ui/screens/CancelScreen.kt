package com.subtrack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.subtrack.app.ui.SubTrackViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelScreen(viewModel: SubTrackViewModel, id: Long?, onDone: () -> Unit) {
    val subscriptions by viewModel.activeSubscriptions.collectAsState()
    val sub = subscriptions.find { it.id == id }
    val scope = rememberCoroutineScope()
    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Отмена подписки") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (sub == null) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Подписка не найдена")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(sub.name, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "${"%.0f".format(sub.price)} ${sub.currency}",
                    style = MaterialTheme.typography.titleMedium
                )

                Divider()
                Text("Как отменить:", style = MaterialTheme.typography.titleMedium)

                val instructions = getCancelInstructions(sub.name)
                instructions.forEachIndexed { index, step ->
                    Text(
                        "${index + 1}. $step",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.cancelSubscription(sub)
                            showSuccessDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Я отменил — добавить в сэкономленное")
                }
            }
        }
    }

    if (showSuccessDialog && sub != null) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onDone()
            },
            icon = {
                Text("🎉", style = MaterialTheme.typography.headlineLarge)
            },
            title = {
                Text(
                    "Отличный выбор!",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    "Ты только что сохранил ${"%.0f".format(sub.price)} ₽ в месяц. " +
                    "Это ${"%.0f".format(sub.price * 12)} ₽ в год! Так держать 💪"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onDone()
                    }
                ) {
                    Text("Круто!")
                }
            }
        )
    }
}

fun getCancelInstructions(name: String): List<String> {
    val lower = name.lowercase().replace(" ", "").replace("-", "")

    fun has(vararg keys: String) = keys.any { it in lower }

    return when {
        // === МУЛЬТИПОДПИСКИ ===
        has("яндекс", "yandex", "плюс", "plus") -> listOf(
            "Открой plus.yandex.ru в браузере",
            "Войди в Яндекс ID",
            "Управление подпиской → Отменить",
            "Подтверди отмену"
        )
        has("сбер", "sber", "сберпрайм", "sberprime") -> listOf(
            "Открой СберБанк Онлайн",
            "Раздел «СберПрайм» в главном меню",
            "Нажми «Управление подпиской» → «Отключить»",
            "Подтверди отмену"
        )
        has("мтс", "mts", "premium") -> listOf(
            "Открой приложение «МТС» или личный кабинет mts.ru",
            "Раздел «Услуги» → «МТС Premium»",
            "Нажми «Отключить»",
            "Подтверди"
        )
        has("ozon", "озон") -> listOf(
            "Открой OZON.ru или приложение",
            "Профиль → Ozon Premium",
            "Нажми «Отменить подписку»",
            "Подтверди"
        )
        has("тинькофф", "tinkoff", "тбанк", "tbank", "pro") -> listOf(
            "Открой приложение Т-Банк",
            "Раздел «Т-Банк Pro»",
            "Настройки подписки → Отключить",
            "Подтверди"
        )
        has("альфа", "alfa", "смарт") -> listOf(
            "Открой приложение Альфа-Банк",
            "Раздел «Альфа-Смарт»",
            "Управление → Отключить",
            "Подтверди"
        )
        has("втб", "vtb") -> listOf(
            "Открой приложение ВТБ",
            "Раздел «ВТБ Плюс»",
            "Отключить подписку",
            "Подтверди"
        )
        has("wb", "вайлдберриз", "wildberries", "клуб") -> listOf(
            "Открой приложение Wildberries",
            "Профиль → WB Клуб",
            "Отменить подписку",
            "Подтверди"
        )
        has("газпром", "gazprom", "бонус") -> listOf(
            "Открой приложение «Газпром Бонус»",
            "Профиль → Управление подпиской",
            "Отключить",
            "Подтверди"
        )

        // === КИНО И ТВ ===
        has("netflix", "нетфликс") -> listOf(
            "Открой netflix.com в браузере",
            "Войди в аккаунт",
            "Профиль → Аккаунт → Отменить подписку",
            "Подтверди отмену"
        )
        has("кинопоиск", "kinopoisk") -> listOf(
            "Открой kinopoisk.ru",
            "Войди в аккаунт",
            "Профиль → Плюс → Отменить",
            "Подтверди"
        )
        has("иви", "ivi") -> listOf(
            "Открой ivi.ru",
            "Войди в аккаунт",
            "Профиль → Подписка → Отменить",
            "Подтверди"
        )
        has("okko", "окко") -> listOf(
            "Открой okko.tv",
            "Войди в аккаунт",
            "Профиль → Мои подписки → Отменить",
            "Подтверди"
        )
        has("wink", "винк") -> listOf(
            "Открой wink.ru",
            "Войди в аккаунт",
            "Профиль → Подписка → Отменить",
            "Подтверди"
        )
        has("premier", "премьер") -> listOf(
            "Открой premier.one",
            "Войди в аккаунт",
            "Профиль → Подписка → Отменить",
            "Подтверди"
        )
        has("start", "старт") -> listOf(
            "Открой start.ru",
            "Войди в аккаунт",
            "Профиль → Подписка → Отменить",
            "Подтверди"
        )
        has("more", "море") -> listOf(
            "Открой more.tv",
            "Войди в аккаунт",
            "Профиль → Отменить подписку",
            "Подтверди"
        )
        has("amediateka", "амедиатека") -> listOf(
            "Открой amediateka.ru",
            "Войди в аккаунт",
            "Профиль → Подписка → Отменить",
            "Подтверди"
        )

        // === МУЗЫКА ===
        has("vk", "вк", "вконтакте") -> listOf(
            "Открой vk.com или приложение VK",
            "Настройки → Платежи и подписки",
            "Найди VK Музыка → Отменить",
            "Подтверди"
        )
        has("spotify", "спотифай") -> listOf(
            "Открой spotify.com/account",
            "Войди в аккаунт",
            "План → Отменить Premium",
            "Подтверди"
        )
        has("звук", "zvuk") -> listOf(
            "Открой zvuk.com",
            "Войди в аккаунт",
            "Профиль → Подписка → Отменить",
            "Подтверди"
        )
        has("youtube", "ютуб") -> listOf(
            "Открой youtube.com/premium",
            "Войди в аккаунт",
            "Управление подпиской → Отменить",
            "Подтверди"
        )

        // === КНИГИ И ОБУЧЕНИЕ ===
        has("литрес", "litres") -> listOf(
            "Открой litres.ru",
            "Войди в аккаунт",
            "Профиль → Мои подписки → Отменить",
            "Подтверди"
        )
        has("mybook", "майбук") -> listOf(
            "Открой mybook.ru",
            "Войди в аккаунт",
            "Профиль → Подписка → Отменить",
            "Подтверди"
        )
        has("skyeng", "скинг") -> listOf(
            "Открой skyeng.ru или приложение",
            "Профиль → Подписка",
            "Отменить автопродление",
            "Подтверди"
        )
        has("учи", "uchi") -> listOf(
            "Открой uchi.ru",
            "Войди в аккаунт",
            "Профиль → Подписка → Отменить",
            "Подтверди"
        )

        // === СОФТ И СЕРВИСЫ ===
        has("kaspersky", "касперский") -> listOf(
            "Открой my.kaspersky.com",
            "Войди в аккаунт",
            "Подписки → Отменить автопродление",
            "Подтверди"
        )
        has("облако", "cloud", "mail") -> listOf(
            "Открой cloud.mail.ru",
            "Войди в аккаунт",
            "Настройки → Подписка → Отменить",
            "Подтверди"
        )
        has("chatgpt", "openai", "чатгпт") -> listOf(
            "Открой chat.openai.com",
            "Войди в аккаунт",
            "Settings → Subscription → Cancel",
            "Подтверди"
        )
        has("nordvpn", "nord") -> listOf(
            "Открой nordaccount.com",
            "Войди в аккаунт",
            "Billing → Cancel subscription",
            "Подтверди"
        )
        has("expressvpn", "express") -> listOf(
            "Открой expressvpn.com",
            "Войди в аккаунт",
            "Subscription → Cancel",
            "Подтверди"
        )

        // === ЗДОРОВЬЕ И СПОРТ ===
        has("fitmost", "фитмост") -> listOf(
            "Открой fitmost.ru",
            "Войди в аккаунт",
            "Профиль → Подписка → Отменить",
            "Подтверди"
        )

        // === APPLE / GOOGLE ===
        has("apple", "эпл", "айтюнс", "itunes", "icloud") -> listOf(
            "Настройки iPhone → Твой Apple ID",
            "Подписки",
            "Выбери подписку → Отменить",
            "Подтверди"
        )
        has("google", "гугл", "play") -> listOf(
            "Открой play.google.com/store/account/subscriptions",
            "Найди подписку",
            "Отменить подписку",
            "Подтверди"
        )

        // === ОПЕРАТОРЫ СВЯЗИ ===
        has("megafon", "мегафон", "beeline", "билайн", "tele2", "теле2") -> listOf(
            "Открой приложение оператора",
            "Раздел «Услуги» или «Подписки»",
            "Найди ненужную подписку",
            "Отключи"
        )

        // === ЗАГЛУШКА ===
        else -> listOf(
            "Зайди в личный кабинет сервиса",
            "Найди раздел «Подписка» или «Оплата»",
            "Нажми «Отменить»",
            "Подтверди отмену"
        )
    }
}
