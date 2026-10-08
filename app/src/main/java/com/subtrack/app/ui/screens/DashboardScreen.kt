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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: SubTrackViewModel, onShowList: () -> Unit) {
    val subscriptions by viewModel.activeSubscriptions.collectAsState()
    val totalSaved by viewModel.totalSaved.collectAsState()

    val monthlyTotal = calculateMonthlyTotal(subscriptions)
    val yearlyTotal = calculateYearlyTotal(subscriptions)

    Scaffold(
        topBar = { TopAppBar(title = { Text("SubTrack") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("В месяц", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${"%.0f".format(monthlyTotal)} ₽",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("В год: ${"%.0f".format(yearlyTotal)} ₽", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (totalSaved > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Ты уже сэкономил", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "${"%.0f".format(totalSaved)} ₽",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text("Активные подписки", style = MaterialTheme.typography.titleLarge)
            Text("Всего: ${subscriptions.size}", style = MaterialTheme.typography.bodyMedium)

            if (subscriptions.isEmpty()) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Пока нет подписок", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Нажми «+», чтобы добавить первую", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                Button(onClick = onShowList, modifier = Modifier.fillMaxWidth()) {
                    Text("Показать все подписки")
                }
            }
        }
    }
}
