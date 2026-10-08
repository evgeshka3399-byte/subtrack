package com.subtrack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.subtrack.app.data.Subscription
import com.subtrack.app.ui.SubTrackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelScreen(viewModel: SubTrackViewModel, id: Long?, onDone: () -> Unit) {
    val subscriptions by viewModel.activeSubscriptions.collectAsState()
    val sub = subscriptions.find { it.id == id }
    val scope = rememberCoroutineScope()

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
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
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
                Text("${"%.0f".format(sub.price)} ${sub.currency}", style = MaterialTheme.typography.titleMedium)

                Divider()
                Text("Как отменить:", style = MaterialTheme.typography.titleMedium)

                val instructions = getCancelInstructions(sub.name)
                instructions.forEachIndexed { index, step ->
                    Text("${index + 1}. $step", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.cancelSubscription(sub)
                            onDone()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Я отменил — добавить в сэкономленное")
                }
            }
        }
    }
}

fun getCancelInstructions(name: String): List<String> {
    val lower = name.lowercase()
    return when {
        "netflix" in lower -> listOf(
            "Открой сайт netflix.com в браузере",
            "Войди в аккаунт",
            "Профиль → Аккаунт → Отменить подписку",
            "Подтверди отмену"
        )
        "spotify" in lower -> listOf(
            "Открой spotify.com/account",
            "Войди в аккаунт",
            "План → Отменить Premium",
            "Подтверди отмену"
        )
        "яндекс" in lower || "yandex" in lower -> listOf(
            "Открой plus.yandex.ru",
            "Войди в Яндекс ID",
            "Управление подпиской → Отменить",
            "Подтверди"
        )
        "apple" in lower -> listOf(
            "Настройки iPhone → Твой Apple ID",
            "Подписки",
            "Выбери подписку → Отменить",
            "Подтверди"
        )
        "google" in lower -> listOf(
            "Открой play.google.com/store/account/subscriptions",
            "Найди подписку",
            "Отменить подписку",
            "Подтверди"
        )
        else -> listOf(
            "Зайди в личный кабинет сервиса",
            "Найди раздел «Подписка» или «Оплата»",
            "Нажми «Отменить»",
            "Подтверди отмену"
        )
    }
}
