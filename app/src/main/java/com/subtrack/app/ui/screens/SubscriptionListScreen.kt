package com.subtrack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.subtrack.app.data.Subscription
import com.subtrack.app.ui.SubTrackViewModel
import com.subtrack.app.util.daysUntil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionListScreen(
    viewModel: SubTrackViewModel,
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onCancelClick: (Long) -> Unit
) {
    val subscriptions by viewModel.activeSubscriptions.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Подписки") }) }
    ) { padding ->
        if (subscriptions.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Пока нет подписок. Нажми «+»")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subscriptions, key = { it.id }) { sub ->
                    SubscriptionCard(sub, onEditClick, onCancelClick, viewModel)
                }
            }
        }
    }
}

@Composable
fun SubscriptionCard(
    sub: Subscription,
    onEditClick: (Long) -> Unit,
    onCancelClick: (Long) -> Unit,
    viewModel: SubTrackViewModel
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    val daysLeft = daysUntil(sub.nextPaymentDate)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(sub.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (sub.hasTrial && sub.trialEndDate != null && sub.trialEndDate > System.currentTimeMillis()) {
                            Spacer(Modifier.width(8.dp))
                            AssistChip(onClick = {}, label = { Text("Пробный") })
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (sub.hasTrial && sub.trialEndDate != null && sub.trialEndDate > System.currentTimeMillis())
                            "Сейчас: 0 ₽ (пробный до ${dateFormat.format(Date(sub.trialEndDate))})"
                        else
                            "${"%.0f".format(sub.price)} ${sub.currency} · ${cycleLabel(sub.cycle)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Списание: ${dateFormat.format(Date(sub.nextPaymentDate))} (через $daysLeft дн.)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onEditClick(sub.id) }) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Изменить")
                }
                OutlinedButton(onClick = { onCancelClick(sub.id) }) {
                    Text("Отменить")
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { viewModel.deleteSubscription(sub) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
            }
        }
    }
}

fun cycleLabel(cycle: String): String = when (cycle) {
    "MONTHLY" -> "мес"
    "QUARTERLY" -> "3 мес"
    "HALF_YEARLY" -> "6 мес"
    "YEARLY" -> "год"
    "CUSTOM" -> "кастом"
    else -> cycle
}
