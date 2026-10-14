package com.subtrack.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.subtrack.app.data.Subscription
import com.subtrack.app.ui.SubTrackViewModel
import com.subtrack.app.ui.theme.CategoryColors
import com.subtrack.app.util.daysUntil
import com.subtrack.app.util.formatMoney
import com.subtrack.app.util.monthlyEquivalent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionListScreen(
    viewModel: SubTrackViewModel,
    onEditClick: (Long) -> Unit,
    onCancelClick: (Long) -> Unit,
    onAddInCategory: (String) -> Unit,
    onBuyPremium: () -> Unit
) {
    val subscriptions by viewModel.activeSubscriptions.collectAsState()

    val grouped = subscriptions.groupBy { it.category }
    val expandedState = remember { mutableStateMapOf<String, Boolean>() }

    // Сортировка: сначала с подписками, потом пустые
    val allCategories = CategoryColors.categories
    val filledCategories = allCategories
        .filter { (grouped[it]?.size ?: 0) > 0 }
        .sortedByDescending { grouped[it]?.size ?: 0 }
    val emptyCategories = allCategories
        .filter { (grouped[it]?.size ?: 0) == 0 }
        .sorted()
    val categoryOrder = filledCategories + emptyCategories

    // === ДИАЛОГИ ЛИМИТОВ ===
    var showSubscriptionLimitDialog by remember { mutableStateOf(false) }
    var pendingCategory by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categoryOrder.forEach { category ->
            val items = grouped[category] ?: emptyList()
            val isExpanded = expandedState[category] ?: false
            val categoryColor = CategoryColors.colorFor(category)
            val totalMonthly = items.sumOf {
                monthlyEquivalent(it.price, it.cycle, it.customCycleDays)
            }

            item(key = "category_$category") {
                CategoryCard(
                    category = category,
                    count = items.size,
                    monthlyTotal = totalMonthly,
                    color = categoryColor,
                    isExpanded = isExpanded,
                    onToggle = {
                        if (items.isEmpty()) {
                            if (viewModel.canAddSubscription()) {
                                onAddInCategory(category)
                            } else {
                                pendingCategory = category
                                showSubscriptionLimitDialog = true
                            }
                        } else {
                            expandedState[category] = !isExpanded
                        }
                    },
                    onAddInCategory = {
                        if (viewModel.canAddSubscription()) {
                            onAddInCategory(category)
                        } else {
                            pendingCategory = category
                            showSubscriptionLimitDialog = true
                        }
                    }
                )
            }

            if (isExpanded && items.isNotEmpty()) {
                items(items, key = { it.id }) { sub ->
                    SubscriptionCard(
                        sub = sub,
                        color = categoryColor,
                        onEditClick = onEditClick,
                        onCancelClick = {
                            if (viewModel.canCancelSubscription()) {
                                onCancelClick(sub.id)
                            } else {
                                showSubscriptionLimitDialog = true // TODO: заменить на CancellationLimitDialog
                            }
                        },
                        onDelete = { viewModel.deleteSubscription(sub) }
                    )
                }
            }
        }
    }

    // === ДИАЛОГ ЛИМИТА ПОДПИСОК ===
    if (showSubscriptionLimitDialog) {
        SubscriptionLimitDialog(
            onBuyPremium = {
                showSubscriptionLimitDialog = false
                pendingCategory = null
                onBuyPremium()
            },
            onWatchAd = {
                showSubscriptionLimitDialog = false
                viewModel.addBonusSlotForAd()
                // После бонуса — можно добавить подписку
                val cat = pendingCategory
                pendingCategory = null
                if (cat != null) {
                    onAddInCategory(cat)
                }
            },
            onDismiss = {
                showSubscriptionLimitDialog = false
                pendingCategory = null
            }
        )
    }
}

@Composable
fun CategoryCard(
    category: String,
    count: Int,
    monthlyTotal: Double,
    color: androidx.compose.ui.graphics.Color,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAddInCategory: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 0f else -90f,
        label = "rotation"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(14.dp)
                    .background(color, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    if (count == 0) "Нет подписок — нажми, чтобы добавить"
                    else "$count · ${formatMoney(monthlyTotal)} ₽/мес",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (count > 0) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.rotate(rotation)
                )
            } else {
                Text(
                    "+",
                    style = MaterialTheme.typography.titleLarge,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SubscriptionCard(
    sub: Subscription,
    color: androidx.compose.ui.graphics.Color,
    onEditClick: (Long) -> Unit,
    onCancelClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    val daysLeft = daysUntil(sub.nextPaymentDate)
    val isTrialActive = sub.hasTrial && sub.trialEndDate != null &&
            sub.trialEndDate > System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            sub.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isTrialActive) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    "Пробный",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isTrialActive)
                            "Сейчас: 0 ₽ (до ${dateFormat.format(Date(sub.trialEndDate!!))})"
                        else
                            "${formatMoney(sub.price)} ${sub.currency} · ${cycleLabel(sub.cycle)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Списание: ${dateFormat.format(Date(sub.nextPaymentDate))} (через $daysLeft дн.)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onEditClick(sub.id) },
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Изменить")
                }
                OutlinedButton(
                    onClick = onCancelClick,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
                ) {
                    Text("Сменить тариф")
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete) {
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
