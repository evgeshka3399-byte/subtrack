package com.subtrack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.subtrack.app.util.calculateMonthlyTotal
import com.subtrack.app.util.calculateYearlyTotal
import com.subtrack.app.util.formatMoney

@Composable
fun DashboardScreen(viewModel: SubTrackViewModel, onShowList: () -> Unit) {
    val subscriptions by viewModel.activeSubscriptions.collectAsState()
    val totalSaved by viewModel.totalSaved.collectAsState()

    val monthlyTotal = calculateMonthlyTotal(subscriptions)
    val yearlyTotal = calculateYearlyTotal(subscriptions)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("В месяц", style = MaterialTheme.typography.labelMedium)
                Text(
                    "${formatMoney(monthlyTotal)} ₽",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "В год: ${formatMoney(yearlyTotal)} ₽",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (totalSaved > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Ты уже сэкономил", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${formatMoney(totalSaved)} ₽",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text("Активные подписки", style = MaterialTheme.typography.titleLarge)
        Text("Всего: ${subscriptions.size}", style = MaterialTheme.typography.bodyMedium)

        if (subscriptions.isEmpty()) {
            Card(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Пока нет подписок", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Нажми «+», чтобы добавить первую",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Button(
                onClick = onShowList,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Показать все подписки")
            }
        }
    }
}
