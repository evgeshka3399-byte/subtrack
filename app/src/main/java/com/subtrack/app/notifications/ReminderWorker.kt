package com.subtrack.app.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.subtrack.app.MainActivity
import com.subtrack.app.SubTrackApp
import com.subtrack.app.util.formatMoney
import kotlinx.coroutines.runBlocking

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val name = inputData.getString("sub_name") ?: "Подписка"
        val id = inputData.getLong("sub_id", 0)
        val price = inputData.getDouble("price", 0.0)
        val currency = inputData.getString("currency") ?: "₽"
        val daysBefore = inputData.getInt("days_before", 2)

        // === ПРОВЕРКА: подписка существует и НЕ отменена ===
        val app = applicationContext as? SubTrackApp ?: return Result.failure()
        val subscription = runBlocking {
            try {
                app.repository.getById(id)
            } catch (e: Exception) {
                null
            }
        }

        // Если подписки нет — не показываем
        if (subscription == null) return Result.success()
        // Если отменена — не показываем
        if (subscription.isCancelled) return Result.success()

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            applicationContext, id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Скоро списание"
        val text = "Через $daysBefore дн. спишут ${formatMoney(price)} $currency за «$name». Отменить?"

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationScheduler.CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(id.toInt(), notification)
        } catch (e: SecurityException) {
            // Нет разрешения POST_NOTIFICATIONS
        }

        return Result.success()
    }
}
