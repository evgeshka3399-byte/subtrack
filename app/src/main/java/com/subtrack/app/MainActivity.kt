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
import com.subtrack.app.ui.MainScreen
import com.subtrack.app.ui.SubTrackViewModel
import com.subtrack.app.ui.theme.SubTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Раскомментировать после регистрации в RuStore Консоли и получения consoleApplicationId
        // RuStoreBillingManager.initialize(this)
        // RuStoreBillingManager.checkPurchases()
        // RuStoreBillingManager.handleIntent(intent)

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
        // TODO: Раскомментировать после интеграции RuStore Pay
        // RuStoreBillingManager.handleIntent(intent)
    }
}
