package com.subtrack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subtrack.app.ui.SubTrackViewModel
import com.subtrack.app.util.formatMoney
import com.subtrack.app.util.monthlyEquivalent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SavingsLevel(
    val name: String,
    val emoji: String,
    val minCount: Int,
    val description: String,
    val isPremium: Boolean = false
)

val ALL_LEVELS = listOf(
    SavingsLevel("Новичок", "🌱", 0, "Только начинаешь"),
    SavingsLevel("Экономный", "💡", 1, "Первая отмена"),
    SavingsLevel("Уверенный", "⚡", 3, "3 подписки отменено", isPremium = true),
    SavingsLevel("Мастер", "🏆", 5, "5 подписок отменено", isPremium = true),
    SavingsLevel("Гуру экономии", "👑", 10, "10 подписок отменено", isPremium = true)
)

fun getAvailableLevels(isPremium: Boolean): List<SavingsLevel> =
    if (isPremium) ALL_LEVELS else ALL_LEVELS.filter { !it.isPremium }

fun getCurrentLevel(count: Int, isPremium: Boolean): SavingsLevel {
    val available = getAvailableLevels(isPremium)
    return available.lastOrNull { count >= it.minCount } ?: available.first()
}

fun getOrdinalLabel(count: Int): String {
    return when (count) {
        1 -> "Первая отмена!"
        2 -> "Вторая отмена!"
        3 -> "Третья отмена!"
        4 -> "Четвёртая отмена!"
        5 -> "Пятая отмена!"
        6 -> "Шестая отмена!"
        7 -> "Седьмая отмена!"
        8 -> "Восьмая отмена!"
        9 -> "Девятая отмена!"
        10 -> "Десятая отмена!"
        else -> "$count отмен!"
    }
}

@Composable
fun AchievementsScreen(
    viewModel: SubTrackViewModel,
    onBack: () -> Unit = {},
    onBuyPremium: () -> Unit = {}
) {
    val cancelled by viewModel.cancelledSubscriptions.collectAsState()
    val count by viewModel.cancelledCount.collectAsState()
    val isPremium = viewModel.isPremium()

    val monthlySaved = cancelled.sumOf {
        monthlyEquivalent(it.price, it.cycle, it.customCycleDays)
    }
    val yearlySaved = monthlySaved * 12
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

    val currentLevel = getCurrentLevel(count, isPremium)
    val availableLevels = getAvailableLevels(isPremium)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // === СУММА ЭКОНОМИИ ===
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Ты сэкономил",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${formatMoney(yearlySaved)} ₽",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "в год · ${formatMoney(monthlySaved)} ₽ в месяц",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Отменено подписок: $count",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // === ПОХВАЛА ===
        if (count >= 1) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                getOrdinalLabel(count),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                when (count) {
                                    1 -> "Отличное начало — ты сделал первый шаг к экономии"
                                    2 -> "Уже две! Продолжай в том же духе"
                                    3 -> "Три подписки долой — ты в ударе"
                                    4 -> "Четыре! Экономия набирает обороты"
                                    5 -> "Пять отмен — ты мастер экономии"
                                    else -> "Отличная работа! Продолжай"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // === ТЕКУЩИЙ УРОВЕНЬ ===
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Твой уровень",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(currentLevel.emoji, fontSize = 32.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                currentLevel.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                currentLevel.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    val nextLevel = availableLevels.firstOrNull { it.minCount > count }
                    if (nextLevel != null) {
                        val currentMin = currentLevel.minCount
                        val nextMin = nextLevel.minCount
                        val progress = ((count - currentMin).toFloat() / (nextMin - currentMin))
                            .coerceIn(0f, 1f)

                        Text(
                            "До уровня «${nextLevel.name}»: ${nextMin - count}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            if (isPremium) "Максимальный уровень достигнут! 🎉"
                            else "Хочешь больше уровней? Купи Premium!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // === ВСЕ УРОВНИ ===
        item {
            Text("Все уровни", style = MaterialTheme.typography.titleMedium)
        }

        items(ALL_LEVELS) { level ->
            val isUnlocked = !level.isPremium || isPremium
            val isAchieved = count >= level.minCount

            Card(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked && isAchieved)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isUnlocked) level.emoji else "🔒",
                        fontSize = 28.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            level.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            level.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!isUnlocked) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (isAchieved) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // === CTA для Demo ===
        if (!isPremium) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Открой все 5 уровней",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Уровни «Уверенный», «Мастер» и «Гуру экономии» доступны в Premium.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = onBuyPremium) {
                            Text("Купить Premium →")
                        }
                    }
                }
            }
        }

        // === ИСТОРИЯ ОТМЕН ===
        if (cancelled.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("История отмен", style = MaterialTheme.typography.titleMedium)
            }

            items(cancelled, key = { it.id }) { sub ->
                val isTariffChange = sub.name.startsWith("Смена тарифа:") ||
                        sub.name.startsWith("Смена оператора:")

                Card(
                    Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                sub.name,
                                style = MaterialTheme.typography.titleMedium
                            )

                            if (isTariffChange) {
                                if (sub.cancelledDate != null) {
                                    Text(
                                        "Дата смены: ${dateFormat.format(Date(sub.cancelledDate!!))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                if (sub.cancelledDate != null) {
                                    Text(
                                        "Отменено: ${dateFormat.format(Date(sub.cancelledDate!!))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(onClick = {
                                    viewModel.restoreSubscription(sub)
                                }) {
                                    Text("Восстановить")
                                }
                            }
                        }

                        if (isTariffChange) {
                            if (sub.price > 0) {
                                Text(
                                    "+${formatMoney(sub.price)} ₽",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Text(
                                "+${formatMoney(sub.price)} ₽",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
