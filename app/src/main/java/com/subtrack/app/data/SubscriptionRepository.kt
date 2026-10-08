package com.subtrack.app.data

import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(private val dao: SubscriptionDao) {

    val activeSubscriptions: Flow<List<Subscription>> = dao.getActiveSubscriptions()
    val cancelledSubscriptions: Flow<List<Subscription>> = dao.getCancelledSubscriptions()
    val cancelledCount: Flow<Int> = dao.getCancelledCount()
    val totalSaved: Flow<Double?> = dao.getTotalSaved()

    suspend fun add(subscription: Subscription): Long = dao.insert(subscription)
    suspend fun update(subscription: Subscription) = dao.update(subscription)
    suspend fun delete(subscription: Subscription) = dao.delete(subscription)
    suspend fun getById(id: Long): Subscription? = dao.getById(id)
}
