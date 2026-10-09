package com.subtrack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subtrack.app.ui.SubTrackViewModel
import com.subtrack.app.ui.components.BarChart
import com.subtrack.app.ui.components.CategoryBreakdown
import com.subtrack.app.ui.components.ChartSlice
import com.subtrack.app.ui.components.PieChart
import com.subtrack.app.ui.theme.CategoryColors
import com.subtrack.app.util.calculateMonthlyTotal
import com.subtrack.app.util.calculateYearlyTotal
import com.subtrack.app.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: SubTrackViewModel, onShowList: () -> Unit) {
    val subscriptions by viewModel.activeSubscriptions.collectAsState()
    val totalSaved by viewModel.totalSaved.collectAsState()

    val monthlyTotal = calculateMonthlyTotal(subscriptions)
    val yearlyTotal = calculateYearlyTotal(subscriptions)

    var chartMode by remember { mutableStateOf(ChartMode.PIE) }

    val slices = remember(subscriptions) {
        val grouped = subscriptions.groupBy { it.category }
        grouped.map { (category, subs) ->
            ChartSlice(
                label = category,
                value = subs.sumOf { com.subtrack.app.util.monthlyEquivalent(it.price, it.cycle, it.customCycleDays) },
                color = CategoryColors.colorFor(category)
            )
        }.sortedByDescending { it.value }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("В месяц", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${formatMoney(monthlyTotal)} ₽",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "В год: ${formatMoney(yearlyTotal)} ₽",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (subscriptions.isNotEmpty()) {
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = chartMode == ChartMode.PIE,
                        onClick = { chartMode = ChartMode.PIE },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = { Icon(Icons.Default.PieChart, null) }
                    ) {}
                    SegmentedButton(
                        selected = chartMode == ChartMode.BAR,
                        onClick = { chartMode = ChartMode.BAR },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = { Icon(Icons.Default.BarChart, null) }
                    ) {}
                }
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
                Column(Modifier.padding(16.dp)) {
                    Text("Ты уже сэкономил",
                        style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${formatMoney(totalSaved)} ₽",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (subscriptions.isEmpty()) {
            Card(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    Modifier.padding(32.dp).fillMaxWidth(),
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
            Card(
                Modifier.fillMaxWidth().height(340.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Box(Modifier.fillMaxSize().padding(16.dp)) {
                    when (chartMode) {
                        ChartMode.PIE -> PieChart(slices = slices, modifier = Modifier.fillMaxSize())
                        ChartMode.BAR -> BarChart(slices = slices, modifier = Modifier.fillMaxSize())
                    }
                }
            }

            Text("По категориям", style = MaterialTheme.typography.titleMedium)
            CategoryBreakdown(slices)

            Spacer(Modifier.height(8.dp))
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

enum class ChartMode { PIE, BAR }
