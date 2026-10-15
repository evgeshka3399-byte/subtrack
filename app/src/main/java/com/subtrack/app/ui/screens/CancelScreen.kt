package com.subtrack.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.subtrack.app.ui.SubTrackViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelScreen(
    viewModel: SubTrackViewModel,
    id: Long?,
    onDone: () -> Unit,
    onEditClick: (Long) -> Unit = {}
) {
    val subscriptions by viewModel.activeSubscriptions.collectAsState()
    val sub = subscriptions.find { it.id == id }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showSuccessDialog by remember { mutableStateOf(false) }

    val isConnection = sub?.category == "Связь"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isConnection) "Сменить тариф" else "Отмена подписки")
                },
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

                Text(
                    if (isConnection) "Как сменить тариф:" else "Как отменить:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val instructions = getInstructions(sub.name, isConnection)
                instructions.steps.forEachIndexed { index, step ->
                    Text(
                        "${index + 1}. $step",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (instructions.link != null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(instructions.link))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Открыть тарифы")
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (isConnection) {
                    // Для «Связи» — «Я сменил тариф» → открывает редактирование
                    Button(
                        onClick = { onEditClick(sub.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Я сменил тариф")
                    }
                } else {
                    // Для остальных — «Я отменил — добавить в сэкономленное»
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.cancelSubscription(sub)
                                showSuccessDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Я отменил — добавить в сэкономленное")
                    }
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

data class Instructions(
    val steps: List<String>,
    val link: String?
)

fun getInstructions(name: String, isConnection: Boolean): Instructions {
    val lower = name.lowercase().replace(" ", "").replace("-", "")

    fun has(vararg keys: String) = keys.any { it in lower }

    // === ДЛЯ СВЯЗИ ===
    if (isConnection) {
        return when {
            has("мтс", "mts") -> Instructions(
                listOf(
                    "Открой приложение «МТС» или личный кабинет mts.ru",
                    "Раздел «Тарифы»",
                    "Выбери новый тариф",
                    "Нажми «Сменить тариф»"
                ),
                "https://www.mts.ru/tariffs"
            )
            has("мегафон", "megafon") -> Instructions(
                listOf(
                    "Открой приложение «МегаФон» или личный кабинет megafon.ru",
                    "Раздел «Тарифы»",
                    "Выбери новый тариф",
                    "Нажми «Сменить тариф»"
                ),
                "https://moscow.megafon.ru/tariffs/"
            )
            has("билайн", "beeline") -> Instructions(
                listOf(
                    "Открой приложение «Билайн» или личный кабинет beeline.ru",
                    "Раздел «Тарифы»",
                    "Выбери новый тариф",
                    "Нажми «Сменить тариф»"
                ),
                "https://spb.beeline.ru/customers/products/mobile/tariffs/"
            )
            has("т2", "t2", "tele2", "теле2") -> Instructions(
                listOf(
                    "Открой приложение Т2 или личный кабинет t2.ru",
                    "Раздел «Тарифы»",
                    "Выбери новый тариф",
                    "Нажми «Сменить тариф»"
                ),
                "https://t2.ru/tariffs"
            )
            has("yota", "йота") -> Instructions(
                listOf(
                    "Открой приложение Yota",
                    "Раздел «Настроить тариф»",
                    "Выбери параметры",
                    "Нажми «Сохранить»"
                ),
                "https://www.yota.ru/voice"
            )
            has("сбермобайл", "sbermobile") -> Instructions(
                listOf(
                    "Открой приложение СберМобайл",
                    "Раздел «Тарифы»",
                    "Выбери новый тариф",
                    "Нажми «Сменить тариф»"
                ),
                "https://sbermobile.ru/tariffs/"
            )
            has("тинькофф", "tinkoff", "тбанк", "tbank") -> Instructions(
                listOf(
                    "Открой приложение Т-Банк",
                    "Раздел «Мобильная связь»",
                    "Выбери новый тариф",
                    "Нажми «Сменить тариф»"
                ),
                "https://www.tbank.ru/mobile/"
            )
            else -> Instructions(
                listOf(
                    "Зайди в личный кабинет оператора",
                    "Найди раздел «Тарифы»",
                    "Выбери новый тариф",
                    "Нажми «Сменить тариф»"
                ),
                null
            )
        }
    }

    // === ДЛЯ ОСТАЛЬНЫХ ===
    return when {
        has("яндекс", "yandex", "плюс", "plus") -> Instructions(
            listOf(
                "Открой plus.yandex.ru в браузере",
                "Войди в Яндекс ID",
                "Управление подпиской → Отменить",
                "Подтверди отмену"
            ),
            "https://plus.yandex.ru"
        )
        has("сбер", "sber", "сберпрайм", "sberprime") -> Instructions(
            listOf(
                "Открой СберБанк Онлайн",
                "Раздел «СберПрайм»",
                "Управление подпиской → Отключить",
                "Подтверди"
            ),
            "https://www.sberbank.ru/ru/person/sberprime"
        )
        has("ozon", "озон") -> Instructions(
            listOf(
                "Открой OZON.ru или приложение",
                "Профиль → Ozon Premium",
                "Отменить подписку",
                "Подтверди"
            ),
            "https://www.ozon.ru/ozon-premium/"
        )
        has("тинькофф", "tinkoff", "тбанк", "tbank", "pro") -> Instructions(
            listOf(
                "Открой приложение Т-Банк",
                "Раздел «Т-Банк Pro»",
                "Настройки подписки → Отключить",
                "Подтверди"
            ),
            "https://www.tbank.ru/tinkoff-pro/"
        )
        has("netflix", "нетфликс") -> Instructions(
            listOf(
                "Открой netflix.com в браузере",
                "Войди в аккаунт",
                "Профиль → Аккаунт → Отменить подписку",
                "Подтверди"
            ),
            "https://www.netflix.com/youraccount"
        )
        has("spotify", "спотифай", "спотифаи") -> Instructions(
            listOf(
                "Открой spotify.com/account",
                "Войди в аккаунт",
                "План → Отменить Premium",
                "Подтверди"
            ),
            "https://www.spotify.com/account/subscription/"
        )
        has("кинопоиск", "kinopoisk") -> Instructions(
            listOf(
                "Открой kinopoisk.ru",
                "Войди в аккаунт",
                "Профиль → Плюс → Отменить",
                "Подтверди"
            ),
            "https://www.kinopoisk.ru/plus/"
        )
        has("иви", "ivi") -> Instructions(
            listOf(
                "Открой ivi.ru",
                "Войди в аккаунт",
                "Профиль → Подписка → Отменить",
                "Подтверди"
            ),
            "https://www.ivi.ru/profile/subscriptions"
        )
        has("okko", "окко") -> Instructions(
            listOf(
                "Открой okko.tv",
                "Войди в аккаунт",
                "Профиль → Мои подписки → Отменить",
                "Подтверди"
            ),
            "https://okko.tv/"
        )
        has("литрес", "litres") -> Instructions(
            listOf(
                "Открой litres.ru",
                "Войди в аккаунт",
                "Профиль → Мои подписки → Отменить",
                "Подтверди"
            ),
            "https://www.litres.ru/"
        )
        has("vk", "вк", "вконтакте") -> Instructions(
            listOf(
                "Открой vk.com или приложение VK",
                "Настройки → Платежи и подписки",
                "Найди VK Музыка → Отменить",
                "Подтверди"
            ),
            "https://vk.com/settings?act=payments"
        )
        has("касперский", "kaspersky") -> Instructions(
            listOf(
                "Открой my.kaspersky.com",
                "Войди в аккаунт",
                "Подписки → Отменить автопродление",
                "Подтверди"
            ),
            "https://my.kaspersky.com/"
        )
        has("apple", "эпл", "айтюнс", "itunes", "icloud") -> Instructions(
            listOf(
                "Настройки iPhone → Твой Apple ID",
                "Подписки",
                "Выбери подписку → Отменить",
                "Подтверди"
            ),
            "https://support.apple.com/ru-ru/HT202039"
        )
        has("google", "гугл", "play") -> Instructions(
            listOf(
                "Открой play.google.com/store/account/subscriptions",
                "Найди подписку",
                "Отменить подписку",
                "Подтверди"
            ),
            "https://play.google.com/store/account/subscriptions"
        )
        else -> Instructions(
            listOf(
                "Зайди в личный кабинет сервиса",
                "Найди раздел «Подписка» или «Оплата»",
                "Нажми «Отменить»",
                "Подтверди отмену"
            ),
            null
        )
    }
}
