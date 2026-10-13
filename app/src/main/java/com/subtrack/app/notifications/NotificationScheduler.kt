package com.subtrack.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.*
import java.util.Calendar
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

    fun scheduleReminder(
        context: Context,
        subscriptionId: Long,
        name: String,
        nextPaymentDate: Long,
        daysBefore: Int,
        price: Double,
        currency: String
    ) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nextPaymentDate
            add(Calendar.DAY_OF_YEAR, -daysBefore)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val targetTime = cal.timeInMillis
        val now = System.currentTimeMillis()
        val delay = targetTime - now

        if (delay <= 0) return

        val data = workDataOf(
            "sub_id" to subscriptionId,
            "sub_name" to name,
            "price" to price,
            "currency" to currency,
            "days_before" to daysBefore
        )

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(30, TimeUnit.SECONDS)
            .setInputData(data)
            .addTag("reminder_$subscriptionId")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "reminder_$subscriptionId",
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    fun cancelReminder(context: Context, subscriptionId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_$subscriptionId")
    }
}
