package com.subtrack.app.ui.components

import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.my.target.ads.MyTargetView

private const val VK_SLOT_ID = 2098927

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(), // Только ширина, высоту не трогаем
        factory = { context ->
            val relativeLayout = RelativeLayout(context).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT // Высота по содержимому
                )
            }

            val myTargetView = MyTargetView(context).apply {
                setAdSize(MyTargetView.AdSize.ADSIZE_320x50)
                setSlotId(VK_SLOT_ID)
                
                // Устанавливаем LayoutParams как в документации
                layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                load()
            }

            relativeLayout.addView(myTargetView)
            relativeLayout
        }
    )
}
