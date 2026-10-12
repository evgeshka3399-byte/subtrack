package com.subtrack.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.subtrack.app.ui.SettingsPrefs
import com.subtrack.app.ui.SubTrackViewModel
import com.subtrack.app.util.exportToCsv

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SubTrackViewModel,
    onBack: () -> Unit,
    onOpenPremium: () -> Unit = {}
) {
    val context = LocalContext.current
    var theme by remember { mutableStateOf(SettingsPrefs.getTheme(context)) }
    var reminderDays by remember { mutableStateOf(SettingsPrefs.getReminderDays(context)) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val subscriptions by viewModel.activeSubscriptions.collectAsState()
    val cancelled by viewModel.cancelledSubscriptions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsItem(
                icon = Icons.Default.Star,
                title = "SubTrack Premium",
                subtitle = "Без рекламы, безлимит отмен",
                onClick = onOpenPremium
            )
            Divider()

            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = "Тема",
                subtitle = when (theme) {
                    SettingsPrefs.THEME_SYSTEM -> "Системная"
                    SettingsPrefs.THEME_LIGHT -> "Светлая"
                    else -> "Тёмная"
                },
                onClick = { showThemeDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Напоминания",
                subtitle = "За $reminderDays дн. до списания",
                onClick = { showReminderDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.Share,
                title = "Экспорт данных",
                subtitle = "Сохранить в CSV (${subscriptions.size + cancelled.size} записей)",
                onClick = {
                    val csv = exportToCsv(subscriptions + cancelled)
                    shareCsv(context, csv)
                }
            )

            SettingsItem(
                icon = Icons.Default.Email,
                title = "Предложения / баги",
                subtitle = "evgeshka3399@vk.com",
                onClick = { sendFeedback(context) }
            )

            SettingsItem(
                icon = Icons.Default.Info,
                title = "О приложении",
                subtitle = "SubTrack 1.0",
                onClick = { showAboutDialog = true }
            )
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Выбери тему") },
            text = {
                Column {
                    ThemeOption("Системная", SettingsPrefs.THEME_SYSTEM, theme) {
                        theme = it
                        SettingsPrefs.setTheme(context, it)
                        showThemeDialog = false
                    }
                    ThemeOption("Светлая", SettingsPrefs.THEME_LIGHT, theme) {
                        theme = it
                        SettingsPrefs.setTheme(context, it)
                        showThemeDialog = false
                    }
                    ThemeOption("Тёмная", SettingsPrefs.THEME_DARK, theme) {
                        theme = it
                        SettingsPrefs.setTheme(context, it)
                        showThemeDialog = false
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("За сколько дней напоминать?") },
            text = {
                Column {
                    listOf(1, 2, 3, 7).forEach { d ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    reminderDays = d
                                    SettingsPrefs.setReminderDays(context, d)
                                    showReminderDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = reminderDays == d,
                                onClick = {
                                    reminderDays = d
                                    SettingsPrefs.setReminderDays(context, d)
                                    showReminderDialog = false
                                }
                            )
                            Text("За $d дн.")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReminderDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("О приложении") },
            text = {
                Column {
                    Text("SubTrack", fontWeight = FontWeight.Bold)
                    Text("Версия 1.0")
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Приложение помогает контролировать подписки и экономить деньги. " +
                        "Все данные хранятся только на твоём устройстве. " +
                        "Мы не подключаемся к банку и не передаём информацию третьим лицам."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("ОК") }
            }
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ThemeOption(
    label: String,
    value: String,
    current: String,
    onSelect: (String) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onSelect(value) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = current == value, onClick = { onSelect(value) })
        Text(label)
    }
}

private fun shareCsv(context: Context, csv: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_SUBJECT, "SubTrack Export")
        putExtra(Intent.EXTRA_TEXT, csv)
    }
    context.startActivity(Intent.createChooser(intent, "Сохранить CSV"))
}

private fun sendFeedback(context: Context) {
    val email = "evgeshka3399@vk.com"
    val subject = "Предложения/баги SubTrack"
    val body = """

        ---
        Мы будем стараться и меняться вместе с вами.
        Опишите проблему или предложение выше этой строки.
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$email")
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Отправить письмо"))
    } catch (e: Exception) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:$email"))
        context.startActivity(webIntent)
    }
}
