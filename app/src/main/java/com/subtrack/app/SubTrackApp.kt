package com.subtrack.app

import android.app.Application
import com.subtrack.app.data.AppDatabase
import com.subtrack.app.data.SubscriptionRepository
import com.subtrack.app.notifications.NotificationScheduler
import com.my.target.common.MyTargetConfig
import com.my.target.common.MyTargetManager

class SubTrackApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { SubscriptionRepository(database.subscriptionDao()) }

    override fun onCreate() {
        super.onCreate()
        NotificationScheduler.createChannel(this)
        
        // 1. Включаем режим отладки (полезно для проверки)
        MyTargetManager.setDebugMode(true)
        
        // 2. Настраиваем тестовое устройство с помощью нашего ID
        val config = MyTargetConfig.Builder()
            .withTestDevices("1ba91b67-489f-4b6f-af0a-d114c3ab5d40")
            .build()
        MyTargetManager.setSdkConfig(config)
        
        // 3. Инициализируем SDK
        MyTargetManager.initSdk(this)
    }
}
