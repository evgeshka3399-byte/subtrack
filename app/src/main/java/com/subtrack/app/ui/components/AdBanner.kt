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

// ID рекламного блока из VK Рекламы
private const val VK_SLOT_ID = 2098927

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(8.dp),
        factory = { context ->
            MyTargetView(context).apply {
                setAdSize(MyTargetView.AdSize.AdSize320x50)
                setSlotId(VK_SLOT_ID)
                
                setListener(object : MyTargetView.MyTargetViewListener {
                    override fun onLoad(view: MyTargetView) {
                        Log.d("AdBanner", "VK Ad loaded successfully")
                    }
                    override fun onNoAd(reason: String, view: MyTargetView) {
                        Log.d("AdBanner", "No VK Ad: $reason")
                    }
                    override fun onClick(view: MyTargetView) {
                        Log.d("AdBanner", "VK Ad clicked")
                    }
                })
                
                load()
            }
        }
    )
}
