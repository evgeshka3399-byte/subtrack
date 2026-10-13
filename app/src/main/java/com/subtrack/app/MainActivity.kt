package com.subtrack.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.subtrack.app.ui.MainScreen
import com.subtrack.app.ui.SettingsPrefs
import com.subtrack.app.ui.SubTrackViewModel
import com.subtrack.app.ui.theme.SubTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var themeKey by remember { mutableStateOf(0) }
            val context = LocalContext.current
            val themePref = remember(themeKey) { SettingsPrefs.getTheme(context) }

            val darkTheme = when (themePref) {
                SettingsPrefs.THEME_LIGHT -> false
                SettingsPrefs.THEME_DARK -> true
                else -> isSystemInDarkTheme()
            }

            // Запрос разрешения на уведомления (Android 13+)
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* результат не важен */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            SubTrackTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val app = application as SubTrackApp
                    val viewModel: SubTrackViewModel = viewModel(
                        factory = SubTrackViewModel.Factory(app, app.repository)
                    )
                    MainScreen(
                        viewModel = viewModel,
                        onThemeChanged = { themeKey++ }
                    )
                }
            }
        }
    }
}
