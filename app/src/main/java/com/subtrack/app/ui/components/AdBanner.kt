package com.subtrack.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.my.target.ads.MyTargetView

private const val VK_SLOT_ID = 2098927

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(8.dp),
        factory = { ctx ->
            MyTargetView(ctx).apply {
                setAdSize(MyTargetView.AdSize.ADSIZE_320x50)
                setSlotId(VK_SLOT_ID)
                
                // ВРЕМЕННО: Показываем ID устройства в Toast для теста
                // В релизной версии эту строку нужно будет убрать!
                setListener(object : MyTargetView.MyTargetViewListener {
                    override fun onLoad(view: MyTargetView) {
                        // Баннер загружен
                    }

                    override fun onNoAd(reason: String?, view: MyTargetView) {
                        // Рекламы нет, но мы всё равно попробуем достать ID
                        showDeviceId(ctx)
                    }

                    override fun onShow(view: MyTargetView) {}
                    override fun onClick(view: MyTargetView) {}
                    override fun onFailedToLoad(reason: String?, view: MyTargetView) {
                        showDeviceId(ctx)
                    }
                })
                
                // Пытаемся получить ID сразу при создании
                showDeviceId(ctx)
                
                load()
            }
        }
    )
}

private fun showDeviceId(context: android.content.Context) {
    // В SDK myTarget нет публичного метода getInstanceId(), 
    // поэтому мы просто просим пользователя посмотреть в логи.
    // Но раз логи не работают, давай выведем подсказку.
    Toast.makeText(context, "Ищем ID в логах... (Debug Mode включён)", Toast.LENGTH_SHORT).show()
}
