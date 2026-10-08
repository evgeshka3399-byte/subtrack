package com.subtrack.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subtrack.app.data.Subscription
import com.subtrack.app.data.SubscriptionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubTrackViewModel(private val repository: SubscriptionRepository) : ViewModel() {

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

    fun addSubscription(subscription: Subscription) = viewModelScope.launch {
        repository.add(subscription)
    }

    fun updateSubscription(subscription: Subscription) = viewModelScope.launch {
        repository.update(subscription)
    }

    fun cancelSubscription(subscription: Subscription) = viewModelScope.launch {
        repository.update(
            subscription.copy(isCancelled = true, cancelledDate = System.currentTimeMillis())
        )
    }

    fun restoreSubscription(subscription: Subscription) = viewModelScope.launch {
        repository.update(subscription.copy(isCancelled = false, cancelledDate = null))
    }

    fun deleteSubscription(subscription: Subscription) = viewModelScope.launch {
        repository.delete(subscription)
    }

    companion object {
        fun Factory(repository: SubscriptionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SubTrackViewModel(repository) as T
                }
            }
    }
}
