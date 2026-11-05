package com.subtrack.app.ui.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.my.target.ads.MyTargetView
import com.my.target.common.IAdLoadingError

private const val VK_SLOT_ID = 2098927
private const val TAG = "AdBannerDebug"

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Жестко фиксируем высоту контейнера
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(50.dp), // 50dp под сам баннер 320x50
            factory = { ctx ->
                MyTargetView(ctx).apply {
                    setAdSize(MyTargetView.AdSize.ADSIZE_320x50)
                    setSlotId(VK_SLOT_ID)
                    
                    setListener(object : MyTargetView.MyTargetViewListener {
                        override fun onLoad(view: MyTargetView) {
                            Log.d(TAG, "Реклама загружена")
                            Toast.makeText(context, "Баннер загружен!", Toast.LENGTH_SHORT).show()
                        }
                        override fun onNoAd(error: IAdLoadingError, view: MyTargetView) {
                            Log.w(TAG, "Нет рекламы, код: ${error.code}")
                            Toast.makeText(context, "Нет рекламы. Код: ${error.code}", Toast.LENGTH_LONG).show()
                        }
                        override fun onShow(view: MyTargetView) {}
                        override fun onClick(view: MyTargetView) {}
                    })
                    
                    load()
                }
            }
        )
    }
}
