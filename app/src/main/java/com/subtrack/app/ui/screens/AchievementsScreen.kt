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

// 50 пословиц и поговорок про экономию и деньги
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

// Альтернативные надписи, если отмен пока нет
private val START_SAVING_MESSAGES = listOf(
    "Готов начать экономить? Отмени первую подписку!",
    "Твоя первая отмена запустит счётчик экономии!",
    "Отмени лишнее — и увидишь, как растёт экономия",
    "Первый шаг к экономии — отмена ненужной подписки",
    "С чего начнём? Отмени что-нибудь лишнее!",
    "Пока пусто. Отмени подписку — и начнётся магия",
    "Тут появится твоя первая пословица — после первой отмены",
    "Экономия начинается с одной отмены. Сделай её сейчас!"
)

@Composable
fun AchievementsScreen(viewModel: SubTrackViewModel, onBack: () -> Unit = {}) {
    val cancelled by viewModel.cancelledSubscriptions.collectAsState()
    val count by viewModel.cancelledCount.collectAsState()

    val monthlySaved = cancelled.sumOf {
        monthlyEquivalent(it.price, it.cycle, it.customCycleDays)
    }
    val yearlySaved = monthlySaved * 12
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

    // Пословица показывается только если есть отмены
    val quote = remember(count) {
        if (count == 0) null
        else SAVINGS_QUOTES[(count - 1) % SAVINGS_QUOTES.size]
    }

    // Надпись-призыв показывается, если отмен нет
    val startMessage = remember(count) {
        if (count > 0) null
        else START_SAVING_MESSAGES[0]
    }

    val rainbowBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE53935),
            Color(0xFFFB8C00),
            Color(0xFFFDD835),
            Color(0xFF43A047),
            Color(0xFF00ACC1),
            Color(0xFF2962FF),
            Color(0xFF8E24AA)
        )
    )

    // Пульсация
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // === ПОСЛОВИЦА ИЛИ ПРИЗЫВ ===
        item {
            when {
                quote != null -> {
                    // Радужная пословица
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .alpha(pulseAlpha)
                            .background(rainbowBrush, RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            quote,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                }
                startMessage != null -> {
                    // Надпись-призыв, если отмен нет
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
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
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

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

        // === ДОСТИЖЕНИЯ ===
        item {
            Text("Достижения", style = MaterialTheme.typography.titleMedium)
        }

        val achievements = listOf(
            Achievement("Первая отмена", "Отменить первую подписку", count >= 1),
            Achievement("5 подписок", "Отменить 5 подписок", count >= 5),
            Achievement("10 подписок", "Отменить 10 подписок", count >= 10),
            Achievement("Экономия 1000 ₽", "Сэкономить 1000 ₽", yearlySaved >= 1000),
            Achievement("Экономия 5000 ₽", "Сэкономить 5000 ₽", yearlySaved >= 5000),
            Achievement("Экономия 10000 ₽", "Сэкономить 10000 ₽", yearlySaved >= 10000)
        )

        items(achievements) { a ->
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

        // === ИСТОРИЯ ОТМЕН ===
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
