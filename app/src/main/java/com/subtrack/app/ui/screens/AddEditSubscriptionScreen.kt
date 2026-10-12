package com.subtrack.app.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.subtrack.app.data.Subscription
import com.subtrack.app.ui.SubTrackViewModel
import com.subtrack.app.util.CategoryDetector
import com.subtrack.app.util.calculateNextPaymentDate
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddEditSubscriptionScreen(
    viewModel: SubTrackViewModel,
    editId: Long?,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Другое") }
    var cycle by remember { mutableStateOf("MONTHLY") }
    var startDay by remember { mutableStateOf("1") }
    var startMonth by remember { mutableStateOf("1") }
    var startYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }
    var hasTrial by remember { mutableStateOf(false) }
    var trialDays by remember { mutableStateOf("7") }
    var trialPrice by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    var categoryManuallyChanged by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var categoryOptions by remember { mutableStateOf<List<String>>(emptyList()) }

    // Автоподбор категории при вводе названия
    LaunchedEffect(name) {
        if (!categoryManuallyChanged && editId == null) {
            val detected = CategoryDetector.detect(name)
            when {
                detected.isEmpty() -> {
                    if (name.isBlank()) category = "Другое"
                }
                detected.size == 1 -> {
                    category = detected.first()
                }
                else -> {
                    // Несколько вариантов — предлагаем выбрать
                    categoryOptions = detected
                    showCategoryPicker = true
                    category = detected.first()
                }
            }
        }
    }

    LaunchedEffect(editId) {
        if (editId != null && !loaded) {
            viewModel.activeSubscriptions.value.find { it.id == editId }?.let { sub ->
                name = sub.name
                price = sub.price.toString()
                category = sub.category
                cycle = sub.cycle
                hasTrial = sub.hasTrial
                trialPrice = sub.priceAfterTrial?.toString() ?: ""
                val cal = Calendar.getInstance().apply { timeInMillis = sub.firstPaymentDate }
                startDay = cal.get(Calendar.DAY_OF_MONTH).toString()
                startMonth = (cal.get(Calendar.MONTH) + 1).toString()
                startYear = cal.get(Calendar.YEAR).toString()
                categoryManuallyChanged = true
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
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    categoryManuallyChanged = false
                },
                label = { Text("Название") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                label = { Text("Цена, ₽") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Дата первого списания", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startDay,
                    onValueChange = { startDay = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("День") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = startMonth,
                    onValueChange = { startMonth = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("Месяц") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = startYear,
                    onValueChange = { startYear = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("Год") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                "Укажи дату, когда подписка была оформлена. Если раньше — прогноз учтёт это.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DropdownField("Цикл", cycle, listOf(
                "MONTHLY" to "Месяц",
                "QUARTERLY" to "3 месяца",
                "HALF_YEARLY" to "6 месяцев",
                "YEARLY" to "Год"
            )) { cycle = it }

            DropdownField("Категория", category, listOf(
                "Мультиподписки" to "Мультиподписки",
                "Кино и ТВ" to "Кино и ТВ",
                "Музыка" to "Музыка",
                "Книги и обучение" to "Книги и обучение",
                "Софт и сервисы" to "Софт и сервисы",
                "Здоровье и спорт" to "Здоровье и спорт",
                "Другое" to "Другое"
            )) {
                category = it
                categoryManuallyChanged = true
            }

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = hasTrial, onCheckedChange = { hasTrial = it })
                Text("Пробный период")
            }
            if (hasTrial) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = trialDays,
                        onValueChange = { trialDays = it.filter { c -> c.isDigit() } },
                        label = { Text("Дней") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = trialPrice,
                        onValueChange = { trialPrice = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                        label = { Text("Цена после") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    val priceValue = price.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val trialDaysValue = trialDays.toIntOrNull() ?: 7

                    val cal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, startYear.toIntOrNull() ?: 2026)
                        set(Calendar.MONTH, ((startMonth.toIntOrNull() ?: 1) - 1).coerceIn(0, 11))
                        set(Calendar.DAY_OF_MONTH, (startDay.toIntOrNull() ?: 1).coerceIn(1, 31))
                        set(Calendar.HOUR_OF_DAY, 12)
                        set(Calendar.MINUTE, 0)
                    }
                    val firstPayment = cal.timeInMillis
                    val next = calculateNextPaymentDate(firstPayment, cycle, 30)

                    val sub = Subscription(
                        id = editId ?: 0,
                        name = name.ifBlank { "Без названия" },
                        price = priceValue,
                        category = category,
                        cycle = cycle,
                        customCycleDays = 30,
                        firstPaymentDate = firstPayment,
                        nextPaymentDate = next,
                        hasTrial = hasTrial,
                        trialEndDate = if (hasTrial) firstPayment + trialDaysValue * 24L * 60 * 60 * 1000 else null,
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

    // Диалог выбора категории, если автоподбор нашёл несколько вариантов
    if (showCategoryPicker && categoryOptions.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("Выбери категорию") },
            text = {
                Column {
                    Text("Мы нашли несколько подходящих категорий:")
                    Spacer(Modifier.height(12.dp))
                    categoryOptions.forEach { option ->
                        TextButton(
                            onClick = {
                                category = option
                                categoryManuallyChanged = true
                                showCategoryPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(option)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text("Оставить «${categoryOptions.first()}»")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: value

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, title) ->
                DropdownMenuItem(
                    text = { Text(title) },
                    onClick = {
                        onSelect(key)
                        expanded = false
                    }
                )
            }
        }
    }
}
