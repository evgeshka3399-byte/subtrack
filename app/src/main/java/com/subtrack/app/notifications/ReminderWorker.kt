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
import java.util.concurrent.TimeUnit

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val name = inputData.getString("sub_name") ?: "Подписка"
        val id = inputData.getLong("sub_id", 0)
        val price = inputData.getDouble("price", 0.0)
        val currency = inputData.getString("currency") ?: "₽"
        val daysBefore = inputData.getInt("days_before", 2)

        val app = applicationContext as? SubTrackApp ?: return Result.failure()
        val subscription = runBlocking {
            try {
                app.repository.getById(id)
            } catch (e: Exception) {
                null
            }
        }

        if (subscription == null) return Result.success()
        if (subscription.isCancelled) return Result.success()

        val now = System.currentTimeMillis()
        val diff = subscription.nextPaymentDate - now
        val daysLeft = TimeUnit.MILLISECONDS.toDays(diff)

        if (daysLeft > daysBefore + 1) return Result.success()
        if (daysLeft < 0) return Result.success()

        // Открываем вкладку «Подписки» (page = 1)
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_tab", 1) // 1 = вкладка «Подписки»
        }

        val pending = PendingIntent.getActivity(
            applicationContext,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Скоро списание"
        val text = "Через $daysLeft дн. спишут ${formatMoney(price)} $currency за «$name»."

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
            // Нет разрешения
        }

        return Result.success()
    }
}
