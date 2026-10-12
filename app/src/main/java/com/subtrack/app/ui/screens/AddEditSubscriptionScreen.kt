package com.subtrack.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.subtrack.app.data.Subscription
import com.subtrack.app.data.SubscriptionCatalog
import com.subtrack.app.ui.SubTrackViewModel
import com.subtrack.app.util.calculateNextPaymentDate
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddEditSubscriptionScreen(
    viewModel: SubTrackViewModel,
    editId: Long?,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale("ru")) }

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Другое") }
    var cycle by remember { mutableStateOf("MONTHLY") }
    var firstPaymentDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var originalFirstPayment by remember { mutableStateOf(System.currentTimeMillis()) }
    var originalNextPayment by remember { mutableStateOf<Long?>(null) }
    var hasTrial by remember { mutableStateOf(false) }
    var trialDays by remember { mutableStateOf("7") }
    var trialPrice by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    var categoryManuallyChanged by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<SubscriptionCatalog.CatalogItem>>(emptyList()) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(name) {
        if (name.isNotBlank() && !categoryManuallyChanged) {
            suggestions = SubscriptionCatalog.search(name)
            showSuggestions = suggestions.isNotEmpty() &&
                    !suggestions.any { it.name.equals(name, ignoreCase = true) }
        } else {
            showSuggestions = false
        }
    }

    LaunchedEffect(editId) {
        if (editId != null && !loaded) {
            viewModel.activeSubscriptions.value.find { it.id == editId }?.let { sub ->
                name = sub.name
                price = sub.price.toString()
                category = sub.category
                cycle = sub.cycle
                firstPaymentDate = sub.firstPaymentDate
                originalFirstPayment = sub.firstPaymentDate
                originalNextPayment = sub.nextPaymentDate
                hasTrial = sub.hasTrial
                trialPrice = sub.priceAfterTrial?.toString() ?: ""
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
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        showSuggestions = false
                    })
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column {
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

                    if (showSuggestions && suggestions.isNotEmpty()) {
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column {
                                suggestions.forEach { item ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                name = item.name
                                                category = item.category
                                                categoryManuallyChanged = true
                                                showSuggestions = false
                                                focusManager.clearFocus()
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(item.name, style = MaterialTheme.typography.bodyLarge)
                                            Text(
                                                item.category,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (item != suggestions.last()) {
                                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                if (category != "Другое") {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Категория определена: $category",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = { Text("Цена, ₽") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateFormat.format(Date(firstPaymentDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Дата первого списания") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Выбрать дату")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
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

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val priceValue = price.replace(',', '.').toDoubleOrNull() ?: 0.0
                        val trialDaysValue = trialDays.toIntOrNull() ?: 7

                        val next = if (editId != null &&
                            originalFirstPayment == firstPaymentDate &&
                            originalNextPayment != null) {
                            originalNextPayment!!
                        } else {
                            calculateNextPaymentDate(firstPaymentDate, cycle, 30)
                        }

                        val sub = Subscription(
                            id = editId ?: 0,
                            name = name.ifBlank { "Без названия" },
                            price = priceValue,
                            category = category,
                            cycle = cycle,
                            customCycleDays = 30,
                            firstPaymentDate = firstPaymentDate,
                            nextPaymentDate = next,
                            hasTrial = hasTrial,
                            trialEndDate = if (hasTrial) firstPaymentDate + trialDaysValue * 24L * 60 * 60 * 1000 else null,
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

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showDatePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = firstPaymentDate }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance().apply {
                    clear()
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                firstPaymentDate = cal.timeInMillis
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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
