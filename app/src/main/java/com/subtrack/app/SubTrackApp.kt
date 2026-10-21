package com.subtrack.app

import android.app.Application
import com.subtrack.app.data.AppDatabase
import com.subtrack.app.data.SubscriptionRepository
import com.subtrack.app.notifications.NotificationScheduler
import com.yandex.mobile.ads.common.YandexAds

class SubTrackApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { SubscriptionRepository(database.subscriptionDao()) }

    override fun onCreate() {
        super.onCreate()
        NotificationScheduler.createChannel(this)
        // В SDK 8.x MobileAds переименован в YandexAds
        YandexAds.initialize(this) {}
    }
}
