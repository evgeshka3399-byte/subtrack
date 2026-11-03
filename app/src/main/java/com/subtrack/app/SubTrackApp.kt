package com.subtrack.app

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.subtrack.app.data.AppDatabase
import com.subtrack.app.data.SubscriptionRepository
import com.subtrack.app.notifications.NotificationScheduler
import com.my.target.common.MyTargetManager
import java.io.BufferedReader
import java.io.InputStreamReader

class SubTrackApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { SubscriptionRepository(database.subscriptionDao()) }

    override fun onCreate() {
        super.onCreate()
        NotificationScheduler.createChannel(this)
        
        // Включаем отладку и инициализируем SDK
        MyTargetManager.setDebugMode(true)
        MyTargetManager.initSdk(this)
        
        // Запускаем фоновый поток, который читает системный лог
        // и ищет там instanceId от myTarget
        Thread {
            try {
                val process = Runtime.getRuntime().exec("logcat -d")
                val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                var foundId: String? = null
                
                while (bufferedReader.readLine().also { line = it } != null) {
                    // Ищем строку с instanceId от myTarget
                    if (line!!.contains("Device instanceId is")) {
                        // Вытаскиваем ID из строки (он идёт после "is ")
                        val parts = line.split("Device instanceId is ")
                        if (parts.size > 1) {
                            foundId = parts[1].trim().split(" ")[0]
                            break
                        }
                    }
                }
                
                if (foundId != null) {
                    // Показываем ID в Toast на главном потоке
                    runOnUiThread {
                        Toast.makeText(this@SubTrackApp, "InstanceId: $foundId", Toast.LENGTH_LONG).show()
                        Log.d("SubTrackDebug", "Found instanceId: $foundId")
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@SubTrackApp, "ID не найден в логах (попробуй открыть баннер)", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@SubTrackApp, "Ошибка чтения лога: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
    
    // Вспомогательный метод для запуска кода в главном потоке
    private fun runOnUiThread(action: () -> Unit) {
        android.os.Handler(mainLooper).post(action)
    }
}
