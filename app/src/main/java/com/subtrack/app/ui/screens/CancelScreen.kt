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
fun CancelScreen(viewModel: SubTrackViewModel, id: Long?, onDone: () -> Unit) {
    val subscriptions by viewModel.activeSubscriptions.collectAsState()
    val sub = subscriptions.find { it.id == id }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сменить тариф") },
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
                    "Как сменить тариф:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val instructions = getCancelInstructions(sub.name)
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
                    Text("Я сменил тариф — добавить в сэкономленное")
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

data class ChangeTariffInstructions(
    val steps: List<String>,
    val link: String?
)

fun getCancelInstructions(name: String): ChangeTariffInstructions {
    val lower = name.lowercase().replace(" ", "").replace("-", "")

    fun has(vararg keys: String) = keys.any { it in lower }

    return when {
        // === МУЛЬТИПОДПИСКИ ===
        has("яндекс", "yandex", "плюс", "plus") -> ChangeTariffInstructions(
            listOf(
                "Открой plus.yandex.ru в браузере",
                "Войди в Яндекс ID",
                "Управление подпиской → Сменить тариф",
                "Подтверди изменение"
            ),
            "https://plus.yandex.ru"
        )
        has("сбер", "sber", "сберпрайм", "sberprime") -> ChangeTariffInstructions(
            listOf(
                "Открой СберБанк Онлайн",
                "Раздел «СберПрайм» в главном меню",
                "Нажми «Управление подпиской» → «Сменить тариф»",
                "Подтверди изменение"
            ),
            "https://www.sberbank.ru/ru/person/sberprime"
        )
        has("мтс", "mts", "premium") -> ChangeTariffInstructions(
            listOf(
                "Открой приложение «МТС» или личный кабинет mts.ru",
                "Раздел «Услуги» → «МТС Premium»",
                "Нажми «Сменить тариф»",
                "Подтверди"
            ),
            "https://www.mts.ru/tariffs"
        )
        has("ozon", "озон") -> ChangeTariffInstructions(
            listOf(
                "Открой OZON.ru или приложение",
                "Профиль → Ozon Premium",
                "Нажми «Сменить тариф»",
                "Подтверди"
            ),
            "https://www.ozon.ru/ozon-premium/"
        )
        has("тинькофф", "tinkoff", "тбанк", "tbank", "pro") -> ChangeTariffInstructions(
            listOf(
                "Открой приложение Т-Банк",
                "Раздел «Т-Банк Pro»",
                "Настройки подписки → Сменить тариф",
                "Подтверди"
            ),
            "https://www.tbank.ru/tinkoff-pro/"
        )

        // === СВЯЗЬ (ОПЕРАТОРЫ) ===
        has("мтс", "mts") -> ChangeTariffInstructions(
            listOf(
                "Открой приложение «МТС» или личный кабинет mts.ru",
                "Раздел «Тарифы»",
                "Выбери новый тариф",
                "Нажми «Сменить тариф»"
            ),
            "https://www.mts.ru/tariffs"
        )
        has("мегафон", "megafon") -> ChangeTariffInstructions(
            listOf(
                "Открой приложение «МегаФон» или личный кабинет megafon.ru",
                "Раздел «Тарифы»",
                "Выбери новый тариф",
                "Нажми «Сменить тариф»"
            ),
            "https://moscow.megafon.ru/tariffs/"
        )
        has("билайн", "beeline") -> ChangeTariffInstructions(
            listOf(
                "Открой приложение «Билайн» или личный кабинет beeline.ru",
                "Раздел «Тарифы»",
                "Выбери новый тариф",
                "Нажми «Сменить тариф»"
            ),
            "https://spb.beeline.ru/customers/products/mobile/tariffs/"
        )
        has("т2", "t2", "tele2", "теле2") -> ChangeTariffInstructions(
            listOf(
                "Открой приложение Т2 или личный кабинет t2.ru",
                "Раздел «Тарифы»",
                "Выбери новый тариф",
                "Нажми «Сменить тариф»"
            ),
            "https://t2.ru/tariffs"
        )
        has("yota", "йота") -> ChangeTariffInstructions(
            listOf(
                "Открой приложение Yota",
                "Раздел «Настроить тариф»",
                "Выбери параметры",
                "Нажми «Сохранить»"
            ),
            "https://www.yota.ru/voice"
        )
        has("сбермобайл", "sbermobile") -> ChangeTariffInstructions(
            listOf(
                "Открой приложение СберМобайл",
                "Раздел «Тарифы»",
                "Выбери новый тариф",
                "Нажми «Сменить тариф»"
            ),
            "https://sbermobile.ru/tariffs/"
        )
        has("тинькофф", "tinkoff", "тбанк", "tbank") -> ChangeTariffInstructions(
            listOf(
                "Открой приложение Т-Банк",
                "Раздел «Мобильная связь»",
                "Выбери новый тариф",
                "Нажми «Сменить тариф»"
            ),
            "https://www.tbank.ru/mobile/"
        )

        // === КИНО И ТВ ===
        has("netflix", "нетфликс") -> ChangeTariffInstructions(
            listOf(
                "Открой netflix.com в браузере",
                "Войди в аккаунт",
                "Профиль → Аккаунт → Сменить тариф",
                "Подтверди"
            ),
            "https://www.netflix.com/youraccount"
        )
        has("кинопоиск", "kinopoisk") -> ChangeTariffInstructions(
            listOf(
                "Открой kinopoisk.ru",
                "Войди в аккаунт",
                "Профиль → Плюс → Сменить тариф",
                "Подтверди"
            ),
            "https://www.kinopoisk.ru/plus/"
        )

        // === ЗАГЛУШКА ===
        else -> ChangeTariffInstructions(
            listOf(
                "Зайди в личный кабинет сервиса",
                "Найди раздел «Тариф» или «Подписка»",
                "Нажми «Сменить тариф»",
                "Подтверди изменение"
            ),
            null
        )
    }
}
