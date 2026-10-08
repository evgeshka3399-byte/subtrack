package com.subtrack.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.subtrack.app.R
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    const val CHANNEL_ID = "subtrack_reminders"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напоминания о подписках",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о скором списании и окончании пробного периода"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(context: Context, subscriptionId: Long, name: String, timestamp: Long) {
        val delay = timestamp - System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)
        if (delay <= 0) return

        val data = workDataOf(
            "sub_id" to subscriptionId,
            "sub_name" to name
        )

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("reminder_$subscriptionId")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork("reminder_$subscriptionId", ExistingWorkPolicy.REPLACE, request)
    }

    fun cancelReminder(context: Context, subscriptionId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_$subscriptionId")
    }
}
