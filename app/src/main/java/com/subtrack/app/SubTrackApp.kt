package com.subtrack.app

import android.app.Application
import androidx.work.WorkManager
import com.subtrack.app.data.AppDatabase
import com.subtrack.app.data.SubscriptionRepository
import com.subtrack.app.notifications.NotificationScheduler
import com.subtrack.app.ui.SettingsPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SubTrackApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { SubscriptionRepository(database.subscriptionDao()) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        NotificationScheduler.createChannel(this)

        // === ОБНОВЛЕНИЕ ВСЕХ УВЕДОМЛЕНИЙ ПРИ СТАРТЕ ===
        appScope.launch {
            try {
                // 1. Отменяем ВСЕ старые задачи
                WorkManager.getInstance(this@SubTrackApp).cancelAllWork()

                // 2. Читаем настройки напоминаний
                val daysBefore = SettingsPrefs.getReminderDays(this@SubTrackApp)

                // 3. Читаем все АКТИВНЫЕ подписки
                val active = repository.getActiveOnce()

                // 4. Планируем уведомления заново
                active.forEach { sub ->
                    NotificationScheduler.scheduleReminder(
                        context = this@SubTrackApp,
                        subscriptionId = sub.id,
                        name = sub.name,
                        nextPaymentDate = sub.nextPaymentDate,
                        daysBefore = daysBefore,
                        price = sub.price,
                        currency = sub.currency
                    )
                }
            } catch (e: Exception) {
                // Игнорируем — при следующем запуске попробуем снова
            }
        }
    }
}
