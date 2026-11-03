package com.subtrack.app.ui.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.my.target.ads.MyTargetView
import com.my.target.common.IAdLoadingError

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
                
                setListener(object : MyTargetView.MyTargetViewListener {
                    override fun onLoad(view: MyTargetView) {
                        Log.d(TAG, "Реклама загружена")
                    }
                    override fun onNoAd(error: IAdLoadingError, view: MyTargetView) {
                        // Просто логируем, без вызова error.message
                        Log.w(TAG, "Реклама не найдена. Код ошибки: ${error.code}")
                    }
                    override fun onShow(view: MyTargetView) {
                        Log.d(TAG, "Реклама показана")
                    }
                    override fun onClick(view: MyTargetView) {
                        Log.d(TAG, "Клик")
                    }
                })
                
                load()
            }
        }
    )
}
