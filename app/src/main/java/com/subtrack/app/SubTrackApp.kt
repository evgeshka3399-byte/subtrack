package com.subtrack.app

import android.app.Application
import android.widget.Toast
import com.subtrack.app.data.AppDatabase
import com.subtrack.app.data.SubscriptionRepository
import com.subtrack.app.notifications.NotificationScheduler
import com.my.target.common.MyTargetManager

class SubTrackApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { SubscriptionRepository(database.subscriptionDao()) }

    override fun onCreate() {
        super.onCreate()
        NotificationScheduler.createChannel(this)
        
        // Включаем отладку
        MyTargetManager.setDebugMode(true)
        MyTargetManager.initSdk(this)
        
        // Пытаемся получить instanceId и показать его в Toast
        // Это сработает, если SDK успел инициализироваться
        try {
            val clazz = MyTargetManager::class.java
            val method = clazz.getDeclaredMethod("getInstanceId")
            method.isAccessible = true
            val instanceId = method.invoke(null) as? String
            
            if (!instanceId.isNullOrBlank()) {
                Toast.makeText(this, "InstanceId: $instanceId", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "InstanceId пустой (попробуй позже)", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось получить ID: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
