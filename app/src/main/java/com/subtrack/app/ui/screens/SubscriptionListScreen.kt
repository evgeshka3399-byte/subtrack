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

    val allCategories = CategoryColors.categories
    val filledCategories = allCategories
        .filter { (grouped[it]?.size ?: 0) > 0 }
        .sortedByDescending { grouped[it]?.size ?: 0 }
    val emptyCategories = allCategories
        .filter { (grouped[it]?.size ?: 0) == 0 }
        .sorted()
    val categoryOrder = filledCategories + emptyCategories

    var showSubscriptionLimitDialog by remember { mutableStateOf(false) }
    var showCancellationLimitDialog by remember { mutableStateOf(false) }
    var pendingCategory by remember { mutableStateOf<String?>(null) }
    var pendingCancelId by remember { mutableStateOf<Long?>(null) }

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
                                pendingCancelId = sub.id
                                showCancellationLimitDialog = true
                            }
                        },
                        onDelete = { viewModel.deleteSubscription(sub) }
                    )
                }
            }
        }
    }

    // ДИАЛОГ ЛИМИТА ПОДПИСОК
    if (showSubscriptionLimitDialog) {
        AlertDialog(
            onDismissRequest = {
                showSubscriptionLimitDialog = false
                pendingCategory = null
            },
            title = { Text("Лимит подписок") },
            text = { Text("Вы достигли лимита в 5 активных подписок.\n\nПосмотрите короткое видео, чтобы добавить ещё одну подписку бесплатно.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSubscriptionLimitDialog = false
                        viewModel.showRewardedAd {
                            // Награда получена — выдаем бонусный слот
                            viewModel.addBonusSlotForAd()
                            val cat = pendingCategory
                            pendingCategory = null
                            if (cat != null) {
                                onAddInCategory(cat)
                            }
                        }
                    }
                ) {
                    Text("Посмотреть видео")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSubscriptionLimitDialog = false
                        pendingCategory = null
                        onBuyPremium()
                    }
                ) {
                    Text("Купить Premium")
                }
            }
        )
    }

    // ДИАЛОГ ЛИМИТА ОТМЕН
    if (showCancellationLimitDialog) {
        AlertDialog(
            onDismissRequest = {
                showCancellationLimitDialog = false
                pendingCancelId = null
            },
            title = { Text("Лимит отмен") },
            text = { Text("Вы достигли лимита в 5 отмен.\n\nПосмотрите короткое видео, чтобы отменить подписку бесплатно.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancellationLimitDialog = false
                        viewModel.showRewardedAd {
                            // Награда получена — даем возможность отменить
                            // Увеличиваем лимит отмен
                            SettingsPrefs.incrementCancellationCount(getApplication<Application>()) // Временный костыль
                            val id = pendingCancelId
                            pendingCancelId = null
                            if (id != null) {
                                onCancelClick(id)
                            }
                        }
                    }
                ) {
                    Text("Посмотреть видео")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCancellationLimitDialog = false
                        pendingCancelId = null
                        onBuyPremium()
                    }
                ) {
                    Text("Купить Premium")
                }
            }
        )
    }
}
