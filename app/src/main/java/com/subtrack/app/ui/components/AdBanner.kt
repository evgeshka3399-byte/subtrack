package com.subtrack.app.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.my.target.ads.MyTargetView
import java.lang.reflect.Method

private const val VK_SLOT_ID = 2098927
private const val TAG = "AdBannerDebug"

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(8.dp),
        factory = { context ->
            MyTargetView(context).apply {
                setAdSize(MyTargetView.AdSize.ADSIZE_320x50)
                setSlotId(VK_SLOT_ID)
                
                // Слушатель, чтобы видеть ошибки
                setListener(object : MyTargetView.MyTargetViewListener {
                    override fun onLoad(view: MyTargetView) {
                        Log.d(TAG, "Реклама загружена")
                    }
                    override fun onNoAd(reason: String?, view: MyTargetView) {
                        Log.w(TAG, "Нет рекламы: $reason")
                        showInstanceIdIfPossible(context)
                    }
                    override fun onShow(view: MyTargetView) {}
                    override fun onClick(view: MyTargetView) {}
                    override fun onFailedToLoad(reason: String?, view: MyTargetView) {
                        Log.e(TAG, "Ошибка: $reason")
                        showInstanceIdIfPossible(context)
                    }
                })
                
                // Пытаемся достать ID сразу при создании
                showInstanceIdIfPossible(context)
                
                load()
            }
        }
    )
}

/**
 * Пытаемся вытащить instanceId из SDK через рефлексию.
 * Если получится — показываем в Toast. Если нет — показываем заглушку.
 */
private fun showInstanceIdIfPossible(context: Context) {
    try {
        // Пытаемся найти класс, который хранит ID (обычно это MyTargetManager или внутренний класс)
        val managerClass = Class.forName("com.my.target.common.MyTargetManager")
        
        // Пробуем разные варианты методов (названия могут отличаться в версиях SDK)
        val possibleMethods = listOf("getInstanceId", "getDeviceId", "getAdvertisingId")
        
        var foundId: String? = null
        for (methodName in possibleMethods) {
            try {
                val method: Method = managerClass.getDeclaredMethod(methodName)
                method.isAccessible = true
                val result = method.invoke(null) // Статический вызов
                if (result is String && result.isNotBlank()) {
                    foundId = result
                    break
                }
            } catch (e: Exception) {
                // Метод не найден или не подходит — идём дальше
            }
        }
        
        if (foundId != null) {
            Toast.makeText(context, "Ваш ID для теста: $foundId", Toast.LENGTH_LONG).show()
            Log.d(TAG, "Найден ID: $foundId")
        } else {
            Toast.makeText(context, "ID не найден через код. Смотрите в логи (тег myTarget).", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Не удалось получить ID: ${e.message}", Toast.LENGTH_LONG).show()
        Log.e(TAG, "Ошибка рефлексии: ${e.message}")
    }
}
