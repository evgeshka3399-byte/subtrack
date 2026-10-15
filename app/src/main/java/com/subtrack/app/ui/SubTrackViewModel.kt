package com.subtrack.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

    fun activatePremium() {
        val context = getApplication<Application>()
        SettingsPrefs.setPremium(context, true)
        SettingsPrefs.resetCancellationCount(context)
    }

    fun deactivatePremium() {
        val context = getApplication<Application>()
        SettingsPrefs.setPremium(context, false)
    }

    fun addSubscription(subscription: Subscription) = viewModelScope.launch {
        val id = repository.add(subscription)
        scheduleNotification(subscription.copy(id = id))
    }

    fun updateSubscription(subscription: Subscription) = viewModelScope.launch {
        repository.update(subscription)
        NotificationScheduler.cancelReminder(getApplication(), subscription.id)
        if (!subscription.isCancelled) {
            scheduleNotification(subscription)
        }
    }

    /**
     * Смена тарифа (для категории «Связь»).
     * @param oldSub старая подписка
     * @param newName новое название (оператор)
     * @param newPrice новая цена
     */
    fun changeTariff(
        oldSub: Subscription,
        newName: String,
        newPrice: Double
    ) = viewModelScope.launch {
        val context = getApplication<Application>()
        val oldPrice = oldSub.price
        val oldName = oldSub.name
        val nameChanged = oldName != newName
        val priceDiff = oldPrice - newPrice

        // Формируем название записи в истории
        val recordName = if (nameChanged) {
            "Смена оператора: $oldName → $newName"
        } else {
            "Смена тарифа: $oldName"
        }

        // Если цена стала ниже — записываем разницу
        if (priceDiff > 0) {
            val savingsRecord = Subscription(
                id = 0,
                name = recordName,
                price = priceDiff,
                currency = oldSub.currency,
                category = oldSub.category,
                cycle = oldSub.cycle,
                customCycleDays = oldSub.customCycleDays,
                firstPaymentDate = System.currentTimeMillis(),
                nextPaymentDate = System.currentTimeMillis(),
                isCancelled = true,
                cancelledDate = System.currentTimeMillis(),
                notes = "Было: ${"%.0f".format(oldPrice)} ₽, стало: ${"%.0f".format(newPrice)} ₽"
            )
            repository.add(savingsRecord)
        } else if (nameChanged) {
            // Если название изменилось, но цена выросла — всё равно пишем «Смена оператора» с нулевой разницей
            val record = Subscription(
                id = 0,
                name = recordName,
                price = 0.0,
                currency = oldSub.currency,
                category = oldSub.category,
                cycle = oldSub.cycle,
                customCycleDays = oldSub.customCycleDays,
                firstPaymentDate = System.currentTimeMillis(),
                nextPaymentDate = System.currentTimeMillis(),
                isCancelled = true,
                cancelledDate = System.currentTimeMillis(),
                notes = "Было: ${"%.0f".format(oldPrice)} ₽, стало: ${"%.0f".format(newPrice)} ₽"
            )
            repository.add(record)
        }

        // Обновляем активную подписку
        val updated = oldSub.copy(name = newName, price = newPrice)
        repository.update(updated)
        SettingsPrefs.incrementCancellationCount(context)
    }

    fun cancelSubscription(subscription: Subscription) = viewModelScope.launch {
        val context = getApplication<Application>()
        val updated = subscription.copy(
            isCancelled = true,
            cancelledDate = System.currentTimeMillis()
        )
        repository.update(updated)
        NotificationScheduler.cancelReminder(context, subscription.id)
        SettingsPrefs.incrementCancellationCount(context)
    }

    fun restoreSubscription(subscription: Subscription) = viewModelScope.launch {
        val updated = subscription.copy(isCancelled = false, cancelledDate = null)
        repository.update(updated)
        scheduleNotification(updated)
    }

    fun deleteSubscription(subscription: Subscription) = viewModelScope.launch {
        repository.delete(subscription)
        NotificationScheduler.cancelReminder(getApplication(), subscription.id)
    }

    fun clearAll() = viewModelScope.launch {
        val context = getApplication<Application>()
        activeSubscriptions.value.forEach {
            NotificationScheduler.cancelReminder(context, it.id)
            repository.delete(it)
        }
        cancelledSubscriptions.value.forEach {
            NotificationScheduler.cancelReminder(context, it.id)
            repository.delete(it)
        }
    }

    private fun scheduleNotification(subscription: Subscription) {
        val context = getApplication<Application>()
        val daysBefore = SettingsPrefs.getReminderDays(context)
        NotificationScheduler.scheduleReminder(
            context = context,
            subscriptionId = subscription.id,
            name = subscription.name,
            nextPaymentDate = subscription.nextPaymentDate,
            daysBefore = daysBefore,
            price = subscription.price,
            currency = subscription.currency
        )
    }

    companion object {
        fun Factory(application: Application, repository: SubscriptionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SubTrackViewModel(application, repository) as T
                }
            }
    }
}
