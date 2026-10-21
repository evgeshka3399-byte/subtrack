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

// TODO: Замени на свой AdUnitId из РСЯ / Простой монетизации
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
                // В SDK 8.2.0 используется BannerAdSize.sticky (свойство)
                setAdSize(BannerAdSize.sticky)
                
                // adUnitId передаётся в AdRequest
                val adRequest = AdRequest.Builder(YANDEX_AD_UNIT_ID).build()
                loadAd(adRequest)
            }
        }
    )
}
