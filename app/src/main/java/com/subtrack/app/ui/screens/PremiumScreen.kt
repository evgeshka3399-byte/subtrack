package com.subtrack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.subtrack.app.billing.RuStoreBillingManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(onBack: () -> Unit) {
    val purchaseState by RuStoreBillingManager.purchaseState.collectAsState()
    val isPremium by RuStoreBillingManager.isPremium.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "SubTrack Premium",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Разовая покупка — навсегда",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            val features = listOf(
                "Без рекламы навсегда",
                "Безлимит отмен подписок",
                "Диаграммы и аналитика",
                "Экспорт данных в CSV",
                "Приоритетная поддержка"
            )

            features.forEach { feature ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(feature, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(32.dp))

            if (isPremium) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Premium активирован ✓",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                Button(
                    onClick = { RuStoreBillingManager.purchasePremium() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = purchaseState !is RuStoreBillingManager.PurchaseState.Loading,
                    shape = MaterialTheme.shapes.large
                ) {
                    if (purchaseState is RuStoreBillingManager.PurchaseState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Купить навсегда — 299 ₽", fontWeight = FontWeight.Bold)
                    }
                }
            }

            when (val state = purchaseState) {
                is RuStoreBillingManager.PurchaseState.Success -> {
                    Text(
                        "Покупка успешна! Спасибо за поддержку 🎉",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                is RuStoreBillingManager.PurchaseState.Error -> {
                    Text(
                        "Ошибка: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                else -> {}
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Оплата через RuStore. Безопасно и удобно.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
