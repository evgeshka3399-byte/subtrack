package com.subtrack.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.subtrack.app.billing.RuStoreBillingManager
import com.subtrack.app.ui.MainScreen
import com.subtrack.app.ui.SubTrackViewModel
import com.subtrack.app.ui.theme.SubTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация RuStore Pay
        RuStoreBillingManager.initialize(this)
        RuStoreBillingManager.checkPurchases()

        // Обработка deeplink при запуске
        RuStoreBillingManager.handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            SubTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val app = application as SubTrackApp
                    val viewModel: SubTrackViewModel = viewModel(
                        factory = SubTrackViewModel.Factory(app.repository)
                    )
                    MainScreen(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Обработка deeplink при возврате из платёжного приложения
        RuStoreBillingManager.handleIntent(intent)
    }
}
