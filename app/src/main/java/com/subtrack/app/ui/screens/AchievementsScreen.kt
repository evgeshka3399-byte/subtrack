package com.subtrack.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subtrack.app.ui.SubTrackViewModel
import com.subtrack.app.util.formatMoney
import com.subtrack.app.util.monthlyEquivalent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SAVINGS_QUOTES = listOf(
    "Копейка рубль бережёт",
    "Деньги любят счёт",
    "Сэкономил — значит заработал",
    "Не тот богат, кто много получает, а тот, кто мало тратит",
    "Бережливость — лучшее богатство",
    "Умный тратит, мудрый экономит",
    "Скупой платит дважды, а экономный — никогда",
    "Тот, кто экономит, всегда при деньгах",
    "Маленькие траты опустошают кошелёк",
    "Считай деньги, не отходя от кассы",
    "Без копейки рубля не бывает",
    "Деньги — как вода: утекут, если не следить",
    "Экономия — это тоже заработок",
    "Лучше меньше потратить, чем больше заработать",
    "Не имей сто рублей, а имей сто друзей — но и рубль береги",
    "Тратить легко, зарабатывать трудно",
    "Кто не считает копейки, тот не считает рубли",
    "Богатство начинается с экономии",
    "Сэкономленный рубль дороже заработанного",
    "Дешёвая покупка — не всегда выгодная",
    "Лишняя подписка — лишняя дыра в кармане",
    "Платишь за то, чем не пользуешься? Отмени!",
    "Деньги не растут на дереве — их надо беречь",
    "Одна отменённая подписка = один сэкономленный ужин",
    "Тот, кто контролирует траты, контролирует жизнь",
    "Береги деньги смолоду",
    "Меньше подписок — больше свободы",
    "Отмена подписки — маленькая победа над собой",
    "Кто платит за всё, тот не копит ни на что",
    "Лучше синица в руках, чем журавль в небе",
    "Не всё то золото, что блестит в рекламе",
    "Деньги — плохой хозяин, но хороший слуга",
    "Подписка — это не свобода, а ежемесячная клетка",
    "Одна копейка — и та бережётся",
    "Богатый не тот, кто много имеет, а тот, кто мало тратит",
    "Экономия — мать богатства",
    "Считай — не считай, а платить придётся. Лучше не платить",
    "Не трать, пока не заработал",
    "Деньги уходят тихо, если за ними не следить",
    "Отменил — сэкономил — победил",
    "Хочешь жить спокойно — отмени лишнее",
    "Каждая отмена — шаг к финансовой свободе",
    "Кто считает подписки, тот считает деньги",
    "Траты — как песок: утекают сквозь пальцы",
    "Один клик — и деньги остаются с тобой",
    "Умей тратить — умей и экономить",
    "Деньги — это не цель, но без них никуда",
    "Не подписывайся на то, чем не пользуешься",
    "Меньше трат — больше возможностей",
    "Тот, кто следит за подписками, следит за будущим"
)

private val START_SAVING_MESSAGES = listOf(
    "Готов начать экономить? Отмени первую подписку!",
    "Экономия начинается с одной отмены. Сделай её сейчас!"
)

data class SavingsLevel(
    val name: String,
    val emoji: String,
    val minCount: Int,
    val description: String
)

val LEVELS = listOf(
    SavingsLevel("Новичок", "🌱", 0, "Только начинаешь"),
    SavingsLevel("Экономный", "💡", 1, "Первая отмена"),
    SavingsLevel("Уверенный", "⚡", 3, "3 подписки отменено"),
    SavingsLevel("Мастер", "🏆", 5, "5 подписок отменено"),
    SavingsLevel("Гуру экономии", "👑", 10, "10 подписок отменено")
)

fun getCurrentLevel(count: Int): SavingsLevel =
    LEVELS.lastOrNull { count >= it.minCount } ?: LEVELS.first()

@Composable
fun AchievementsScreen(viewModel: SubTrackViewModel, onBack: () -> Unit = {}) {
    val cancelled by viewModel.cancelledSubscriptions.collectAsState()
    val count by viewModel.cancelledCount.collectAsState()

    val monthlySaved = cancelled.sumOf {
        monthlyEquivalent(it.price, it.cycle, it.customCycleDays)
    }
    val yearlySaved = monthlySaved * 12
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

    val quote = remember(count) {
        if (count == 0) null
        else SAVINGS_QUOTES[(count - 1) % SAVINGS_QUOTES.size]
    }

    val startMessage = remember(count) {
        if (count > 0) null else START_SAVING_MESSAGES[0]
    }

    val currentLevel = getCurrentLevel(count)

    // Нежный пастельный градиент
    val softGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFB2DFDB), // нежно-мятный
            Color(0xFFB3E5FC), // нежно-голубой
            Color(0xFFD1C4E9)  // нежно-лавандовый
        )
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

        item {
            Text("Достижения", style = MaterialTheme.typography.titleMedium)
        }

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
                                "Первая отмена!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Отличное начало — ты сделал первый шаг к экономии",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        item {
            when {
                quote != null -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .alpha(pulseAlpha)
                            .background(softGradient, RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            quote,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF2E3A46)
                        )
                    }
                }
                startMessage != null -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(24.dp)
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            startMessage,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

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

                    val nextLevel = LEVELS.firstOrNull { it.minCount > count }
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
                            "Максимальный уровень достигнут! 🎉",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Text("Вехи", style = MaterialTheme.typography.titleMedium)
        }

        val milestones = listOf(
            Achievement("Экономия 1000 ₽", "Сэкономить 1000 ₽ в год", yearlySaved >= 1000),
            Achievement("Экономия 5000 ₽", "Сэкономить 5000 ₽ в год", yearlySaved >= 5000),
            Achievement("Экономия 10000 ₽", "Сэкономить 10000 ₽ в год", yearlySaved >= 10000),
            Achievement("Экономия 50000 ₽", "Сэкономить 50000 ₽ в год", yearlySaved >= 50000)
        )

        items(milestones) { a ->
            Card(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = if (a.unlocked) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (a.unlocked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(a.title, style = MaterialTheme.typography.titleMedium)
                        Text(a.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (cancelled.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("История отмен", style = MaterialTheme.typography.titleMedium)
            }

            items(cancelled, key = { it.id }) { sub ->
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
                            Text(sub.name, style = MaterialTheme.typography.titleMedium)
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

data class Achievement(val title: String, val description: String, val unlocked: Boolean)
