package com.subtrack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subtrack.app.data.FirebaseManager
import com.subtrack.app.ui.SubTrackViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(
    viewModel: SubTrackViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

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

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isNotBlank() && email.contains("@")) {
                        isLoading = true
                        message = null
                        scope.launch {
                            val paymentId = "test_payment_${System.currentTimeMillis()}"
                            
                            // Таймаут 15 секунд, чтобы не висело вечно
                            val result = withTimeoutOrNull(15000L) {
                                FirebaseManager.savePremiumPurchase(email, paymentId)
                            }

                            isLoading = false

                            if (result == null) {
                                message = "Ошибка: Превышено время ожидания (проверьте интернет)"
                            } else if (result.isSuccess) {
                                viewModel.activatePremium()
                                isSuccess = true
                                message = "Оплата прошла успешно! Premium активирован."
                            } else {
                                val error = result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                                message = "Ошибка: $error"
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

            if (isSuccess) {
                Spacer(Modifier.height(16.dp))
                Text(
                    message ?: "",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onSuccess,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Готово")
                }
            } else if (message != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    message!!,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Оплата будет добавлена в следующем обновлении",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
