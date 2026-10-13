package com.subtrack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun AchievementsScreen(viewModel: SubTrackViewModel, onBack: () -> Unit = {}) {
    val cancelled by viewModel.cancelledSubscriptions.collectAsState()
    val count by viewModel.cancelledCount.collectAsState()

    val monthlySaved = cancelled.sumOf {
        monthlyEquivalent(it.price, it.cycle, it.customCycleDays)
    }
    val yearlySaved = monthlySaved * 12
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

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

        // === ДОСТИЖЕНИЯ ===
        item {
            Text("Достижения", style = MaterialTheme.typography.titleMedium)
        }

        val achievements = listOf(
            Achievement("Первая отмена", "Отменить первую подписку", count >= 1),
            Achievement("5 подписок", "Отменить 5 подписок", count >= 5),
            Achievement("Экономия 1000 ₽", "Сэкономить 1000 ₽", yearlySaved >= 1000),
            Achievement("Экономия 5000 ₽", "Сэкономить 5000 ₽", yearlySaved >= 5000)
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
                                Text("Вос
