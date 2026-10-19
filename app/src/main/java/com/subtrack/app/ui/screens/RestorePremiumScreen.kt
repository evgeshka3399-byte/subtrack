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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.subtrack.app.data.FirebaseManager
import com.subtrack.app.ui.SubTrackViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestorePremiumScreen(
    viewModel: SubTrackViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var step by remember { mutableStateOf(1) } // 1 = email, 2 = подтверждение
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Восстановить Premium") },
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
            if (step == 1) {
                Text(
                    "Введите email, который вы использовали при покупке",
                    style = MaterialTheme.typography.titleMedium,
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

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            if (email.isNotBlank() && email.contains("@")) {
                                isLoading = true
                                scope.launch {
                                    val result = FirebaseManager.sendVerificationCode(email)
                                    isLoading = false
                                    if (result.isSuccess) {
                                        message = "Письмо отправлено на $email"
                                        step = 2
                                    } else {
                                        message = "Ошибка отправки: ${result.exceptionOrNull()?.message}"
                                    }
                                }
                            } else {
                                message = "Введите корректный email"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Отправить письмо")
                    }
                }
            } else {
                Text(
                    "Мы отправили письмо на $email. Подтвердите его и нажмите кнопку ниже.",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            isLoading = true
                            scope.launch {
                                val result = FirebaseManager.checkEmailInPremium(email)
                                isLoading = false
                                if (result.isSuccess && result.getOrNull() == true) {
                                    viewModel.activatePremium()
                                    isSuccess = true
                                    message = "Premium успешно восстановлен!"
                                } else {
                                    message = "Покупка с таким email не найдена"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Я подтвердил почту — Восстановить")
                    }

                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { step = 1; message = null }) {
                        Text("Назад")
                    }
                }
            }

            if (message != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    message!!,
                    color = if (isSuccess) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
