package com.subtrack.app.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.my.target.ads.MyTargetView

private const val VK_SLOT_ID = 2098927

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            // Создаем контейнер, который держит размеры
            val frameLayout = FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            // Создаем сам баннер
            val myTargetView = MyTargetView(context).apply {
                setAdSize(MyTargetView.AdSize.ADSIZE_320x50)
                setSlotId(VK_SLOT_ID)
                
                // Жестко задаем размеры самому View (в пикселях через dp)
                val density = resources.displayMetrics.density
                val heightPx = (50 * density).toInt()
                
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    heightPx
                )
                load()
            }

            frameLayout.addView(myTargetView)
            frameLayout
        }
    )
}
