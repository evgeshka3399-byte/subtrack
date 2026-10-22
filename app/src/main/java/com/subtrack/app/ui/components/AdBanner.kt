package com.subtrack.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
// ВАЖНО: проверь эти импорты
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
                setAdUnitId(YANDEX_AD_UNIT_ID)
                // В 8.6.0 stickySize требует контекст и ширину в dp
                setAdSize(BannerAdSize.stickySize(context, 320))
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
