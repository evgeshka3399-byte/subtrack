package com.subtrack.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest

private const val YANDEX_AD_UNIT_ID = "demo-banner-yandex"

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(8.dp),
        factory = { context ->
            BannerAdView(context).apply {
                // Пробуем через свойство (Kotlin-синтаксис для Java-сеттеров)
                adUnitId = YANDEX_AD_UNIT_ID
                // stickySize с контекстом и шириной
                adSize = BannerAdSize.stickySize(context, 320)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
