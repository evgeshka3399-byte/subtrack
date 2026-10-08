package com.subtrack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.subtrack.app.data.Subscription
import com.subtrack.app.ui.SubTrackViewModel
import com.subtrack.app.util.calculateNextPaymentDate
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionScreen(
    viewModel: SubTrackViewModel,
    editId: Long?,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Развлечения") }
    var cycle by remember { mutableStateOf("MONTHLY") }
    var customDays by remember { mutableStateOf("30") }
    var hasTrial by remember { mutableStateOf(false) }
    var trialPrice by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(editId) {
        if (editId != null && !loaded) {
            viewModel.activeSubscriptions.value.find { it.id == editId }?.let { sub ->
                name = sub.name
                price = sub.price.toString()
                category = sub.category
                cycle = sub.cycle
                customDays = sub.customCycleDays.toString()
                hasTrial = sub.hasTrial
                trialPrice = sub.priceAfterTrial?.toString() ?: ""
            }
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editId == null) "Новая подписка" else "Редактировать") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = price,
                onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                label = { Text("Цена, ₽") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Цикл списания", style = MaterialTheme.typography.labelLarge)
            val cycles = listOf(
                "MONTHLY" to "Месяц",
                "QUARTERLY" to "3 месяца",
                "HALF_YEARLY" to "6 месяцев",
                "YEARLY" to "Год",
                "CUSTOM" to "Свой"
            )
            cycles.forEach { (value, label) ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = cycle == value, onClick = { cycle = value })
                    Text(label)
                }
            }
            if (cycle == "CUSTOM") {
                OutlinedTextField(
                    value = customDays,
                    onValueChange = { customDays = it.filter { c -> c.isDigit() } },
                    label = { Text("Дней в цикле") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text("Категория", style = MaterialTheme.typography.labelLarge)
            val categories = listOf("Развлечения", "Музыка", "Софт", "Здоровье", "Другое")
            categories.forEach { cat ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = category == cat, onClick = { category = cat })
                    Text(cat)
                }
            }

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = hasTrial, onCheckedChange = { hasTrial = it })
                Text("Есть бесплатный пробный период")
            }
            if (hasTrial) {
                OutlinedTextField(
                    value = trialPrice,
                    onValueChange = { trialPrice = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = { Text("Цена после пробного, ₽") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val priceValue = price.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val now = System.currentTimeMillis()
                    val next = calculateNextPaymentDate(now, cycle, customDays.toIntOrNull() ?: 30)
                    val sub = Subscription(
                        id = editId ?: 0,
                        name = name.ifBlank { "Без названия" },
                        price = priceValue,
                        category = category,
                        cycle = cycle,
                        customCycleDays = customDays.toIntOrNull() ?: 30,
                        firstPaymentDate = now,
                        nextPaymentDate = next,
                        hasTrial = hasTrial,
                        trialEndDate = if (hasTrial) now + 7L * 24 * 60 * 60 * 1000 else null,
                        priceAfterTrial = trialPrice.replace(',', '.').toDoubleOrNull()
                    )
                    scope.launch {
                        if (editId == null) viewModel.addSubscription(sub)
                        else viewModel.updateSubscription(sub)
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }
}
