package com.subtrack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.subtrack.app.ui.SubTrackViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelledScreen(viewModel: SubTrackViewModel) {
    val cancelled by viewModel.cancelledSubscriptions.collectAsState()
    val totalSaved by viewModel.totalSaved.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Отменённые") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Сэкономлено", style = MaterialTheme.typography.labelMedium)
                    Text("${"%.0f".format(totalSaved)} ₽", style = MaterialTheme.typography.headlineMedium)
                }
            }
            if (cancelled.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Пока ничего не отменено")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cancelled, key = { it.id }) { sub ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(sub.name, style = MaterialTheme.typography.titleMedium)
                                    Text("${"%.0f".format(sub.price)} ${sub.currency} · ${cycleLabel(sub.cycle)}")
                                }
                                TextButton(onClick = { viewModel.restoreSubscription(sub) }) {
                                    Text("Восстановить")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
