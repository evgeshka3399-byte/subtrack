package com.subtrack.app

import android.app.Application
import com.subtrack.app.data.AppDatabase
import com.subtrack.app.data.SubscriptionRepository
import com.subtrack.app.notifications.NotificationScheduler
import com.my.target.common.MyTargetManager // Правильный импорт для VK/myTarget

class SubTrackApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { SubscriptionRepository(database.subscriptionDao()) }

    override fun onCreate() {
        super.onCreate()
        NotificationScheduler.createChannel(this)
        
        // Инициализация VK Рекламы (myTarget)
        // 1. Включаем режим отладки, чтобы поймать instanceId в логах
        MyTargetManager.setDebugMode(true)
        
        // 2. Инициализируем сам SDK
        MyTargetManager.initSdk(this)
    }
}
