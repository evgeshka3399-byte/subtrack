package com.subtrack.app.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.my.target.ads.MyTargetView

private const val VK_SLOT_ID = 2098927

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            // Создаем контейнер, который жестко держит размеры
            val frameLayout = FrameLayout(context)
            frameLayout.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // Создаем сам баннер
            val myTargetView = MyTargetView(context).apply {
                setAdSize(MyTargetView.AdSize.ADSIZE_320x50)
                setSlotId(VK_SLOT_ID)
                
                // Жестко задаем размеры для самого View
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                load()
            }

            frameLayout.addView(myTargetView)
            frameLayout
        }
    )
}
