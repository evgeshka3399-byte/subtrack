package com.subtrack.app.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.my.target.ads.RewardedAd
import com.my.target.ads.Reward
import com.subtrack.app.data.Subscription
import com.subtrack.app.data.SubscriptionRepository
import com.subtrack.app.notifications.NotificationScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubTrackViewModel(
    application: Application,
    private val repository: SubscriptionRepository
) : AndroidViewModel(application) {

    private val REWARDED_SLOT_ID = 2104786

    val activeSubscriptions: StateFlow<List<Subscription>> =
        repository.activeSubscriptions.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    val cancelledSubscriptions: StateFlow<List<Subscription>> =
        repository.cancelledSubscriptions.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    val cancelledCount: StateFlow<Int> =
        repository.cancelledCount.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0
        )

    val totalSaved: StateFlow<Double> =
        repository.totalSaved.map { it ?: 0.0 }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0
        )

    fun isPremium(): Boolean = SettingsPrefs.isPremium(getApplication())

    fun canAddSubscription(): Boolean {
        val context = getApplication<Application>()
        val activeCount = activeSubscriptions.value.size
        return SettingsPrefs.canAddSubscription(context, activeCount)
    }

    fun canCancelSubscription(): Boolean {
        return SettingsPrefs.canCancelSubscription(getApplication())
    }

    fun addBonusSlotForAd() {
        SettingsPrefs.addBonusSlot(getApplication())
    }

    fun showRewardedAd(onRewardEarned: () -> Unit) {
        val context = getApplication<Application>()
        val rewardedAd = RewardedAd(REWARDED_SLOT_ID, context)

        rewardedAd.setListener(object : RewardedAd.RewardedAdListener {
            override fun onLoad(ad: RewardedAd) {
                ad.show()
            }

            // String, как в документации VK
            override fun onNoAd(reason: String?, ad: RewardedAd) {
                Toast.makeText(context, "Реклама недоступна", Toast.LENGTH_SHORT).show()
            }

            override fun onClick(ad: RewardedAd) {}
            override fun onDisplay(ad: RewardedAd) {}
            override fun onDismiss(ad: RewardedAd) {}

            // Добавляем onFailedToShow, потому что Gradle его требует
            override fun onFailedToShow(ad: RewardedAd) {
                Toast.makeText(context, "Не удалось показать", Toast.LENGTH_SHORT).show()
            }

            override fun onReward(reward: Reward, ad: RewardedAd) {
                onRewardEarned()
            }
        })

        rewardedAd.load()
    }

    // ... остальные функции без изменений (activatePremium, addSubscription и т.д.)
    // Они у тебя уже есть, я их не трогаю.
}
