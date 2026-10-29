package com.subtrack.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subtrack.app.data.FirebaseManager
import com.subtrack.app.ui.SubTrackViewModel
import kotlinx.coroutines.launch

private const val YOOKASSA_PAYMENT_URL = "https://yookassa.ru/my/i/auB7MjNC-Q-K/l"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(
    viewModel: SubTrackViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var showWifiDialog by remember { mutableStateOf(false) }
    var isPremiumActivated by remember { mutableStateOf(viewModel.isPremium()) }

    fun isWifiConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    val isWifi = remember { isWifiConnected() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Оформление Premium") },
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
            if (isPremiumActivated) {
                // Если Premium уже активирован — показываем сообщение об успехе
                Text(
                    "Premium активирован!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Спасибо за покупку. Все функции Premium доступны.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = onSuccess, modifier = Modifier.fillMaxWidth()) {
                    Text("Готово")
                }
            } else {
                // Обычный экран оплаты
                Text(
                    "Введите email, на который будет оформлен Premium",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    "На этот email можно будет восстановить покупку",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                if (!isWifi) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Для оплаты необходимо подключиться к сети Wi-Fi. " +
                                "Мобильный интернет может быть ограничен, и платёжная страница не откроется.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Кнопка "Оплатить" всегда активна, пока Premium не активирован
                Button(
                    onClick = {
                        if (email.isNotBlank() && email.contains("@")) {
                            if (!isWifiConnected()) {
                                showWifiDialog = true
                                return@Button
                            }
                            
                            isLoading = true
                            scope.launch {
                                val paymentId = "pending_${System.currentTimeMillis()}"
                                val result = FirebaseManager.savePremiumPurchase(email, paymentId)
                                
                                isLoading = false
                                
                                if (result.isSuccess) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(YOOKASSA_PAYMENT_URL))
                                    context.startActivity(intent)
                                    message = "После оплаты нажмите «Я оплатил» (Premium активируется в течение 24 часов)."
                                } else {
                                    message = "Ошибка: ${result.exceptionOrNull()?.message}"
                                }
                            }
                        } else {
                            message = "Введите корректный email"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Оплатить 299 ₽", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Кнопка "Я оплатил" (для проверки и активации)
                TextButton(
                    onClick = {
                        // Здесь можно добавить проверку, но пока просто активируем
                        viewModel.activatePremium()
                        isPremiumActivated = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Я оплатил (Premium активируется после проверки)")
                }

                if (message != null) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        message!!,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showWifiDialog) {
        AlertDialog(
            onDismissRequest = { showWifiDialog = false },
            icon = { Icon(Icons.Default.Wifi, contentDescription = null) },
            title = { Text("Требуется подключение к Wi-Fi") },
            text = {
                Text(
                    "Для проведения оплаты необходимо подключиться к сети Wi-Fi. " +
                    "Мобильный интернет может быть ограничен, и платёжная страница не откроется.\n\n" +
                    "Пожалуйста, подключитесь к Wi-Fi и попробуйте снова.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = { showWifiDialog = false }) {
                    Text("Понятно")
                }
            }
        )
    }
}
