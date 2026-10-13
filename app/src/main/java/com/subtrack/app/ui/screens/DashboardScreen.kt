package com.subtrack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subtrack.app.ui.SubTrackViewModel
import com.subtrack.app.ui.components.AreaChart
import com.subtrack.app.ui.components.BarChart
import com.subtrack.app.ui.components.CategoryBreakdown
import com.subtrack.app.ui.components.ChartSlice
import com.subtrack.app.ui.components.ForecastMode
import com.subtrack.app.ui.components.MonthlySpendingChart
import com.subtrack.app.ui.components.PieChart
import com.subtrack.app.ui.components.buildForecast
import com.subtrack.app.ui.theme.CategoryColors
import com.subtrack.app.util.calculateMonthlyTotal
import com.subtrack.app.util.calculateYearlyTotal
import com.subtrack.app.util.formatMoney
import com.subtrack.app.util.monthlyEquivalent
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SubTrackViewModel,
    onShowList: () -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    val subscriptions by viewModel.activeSubscriptions.collectAsState()
    val cancelled by viewModel.cancelledSubscriptions.collectAsState()

    val monthlySpent = calculateMonthlyTotal(subscriptions)
    val yearlySpent = calculateYearlyTotal(subscriptions)

    val monthlySaved = cancelled.sumOf {
        monthlyEquivalent(it.price, it.cycle, it.customCycleDays)
    }
    val yearlySaved = monthlySaved * 12

    var chartMode by remember { mutableStateOf(ChartMode.MONTHLY) }
    var forecastMode by remember { mutableStateOf(ForecastMode.AVERAGE) }
    var sliderPosition by remember { mutableStateOf(0f) }

    val slices = remember(subscriptions) {
        subscriptions.groupBy { it.category }
            .map { (category, subs) ->
                ChartSlice(
                    label = category,
                    value = subs.sumOf {
                        monthlyEquivalent(it.price, it.cycle, it.customCycleDays)
                    },
                    color = CategoryColors.colorFor(category)
                )
            }
            .sortedByDescending { it.value }
    }

    val forecast = remember(subscriptions, forecastMode) {
        if (subscriptions.isEmpty()) emptyList()
        else buildForecast(subscriptions, 24, forecastMode)
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                Modifier.weight(1f),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "ТРАТЫ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "В месяц",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "${formatMoney(monthlySpent)} ₽",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "В год",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "${formatMoney(yearlySpent)} ₽",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Card(
                Modifier.weight(1f),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "ЭКОНОМИЯ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "В месяц",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "${formatMoney(monthlySaved)} ₽",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "В год",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "${formatMoney(yearlySaved)} ₽",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
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
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onAddClick,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Добавить подписку")
                    }
                }
            }
        } else {
            Column {
                Text(
                    "График: ${chartMode.title}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        chartMode = ChartMode.values()[
                            it.roundToInt().coerceIn(0, ChartMode.values().size - 1)
                        ]
                    },
                    valueRange = 0f..(ChartMode.values().size - 1).toFloat(),
                    steps = ChartMode.values().size - 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ChartMode.values().forEach { mode ->
                        Text(
                            mode.shortTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (chartMode == mode) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (chartMode == ChartMode.AREA) {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = forecastMode == ForecastMode.AVERAGE,
                        onClick = { forecastMode = ForecastMode.AVERAGE },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Среднее")
                    }
                    SegmentedButton(
                        selected = forecastMode == ForecastMode.ACTUAL,
                        onClick = { forecastMode = ForecastMode.ACTUAL },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("По факту")
                    }
                }
            }

            Card(
                Modifier.fillMaxWidth().height(400.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Box(Modifier.fillMaxSize().padding(16.dp)) {
                    when (chartMode) {
                        ChartMode.MONTHLY -> MonthlySpendingChart(
                            subscriptions = subscriptions,
                            modifier = Modifier.fillMaxSize()
                        )
                        ChartMode.PIE -> PieChart(slices = slices, modifier = Modifier.fillMaxSize())
                        ChartMode.BAR -> BarChart(slices = slices, modifier = Modifier.fillMaxSize())
                        ChartMode.AREA -> AreaChart(
                            points = forecast,
                            lineColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            if (chartMode == ChartMode.PIE || chartMode == ChartMode.BAR) {
                Text("По категориям", style = MaterialTheme.typography.titleMedium)
                CategoryBreakdown(slices)
            } else if (chartMode == ChartMode.AREA) {
                Text(
                    if (forecastMode == ForecastMode.AVERAGE)
                        "Среднемесячная нагрузка. Все годовые платежи усреднены по 12 месяцам."
                    else
                        "Реальные списания по датам. Видны крупные годовые платежи.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

enum class ChartMode(val title: String, val shortTitle: String) {
    MONTHLY("Траты в месяц", "Месяц"),
    PIE("Круговая", "Круг"),
    BAR("Столбчатая", "Столб"),
    AREA("Область", "Область")
}
