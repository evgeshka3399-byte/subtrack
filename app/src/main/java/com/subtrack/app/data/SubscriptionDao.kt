package com.subtrack.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions WHERE isCancelled = 0 ORDER BY nextPaymentDate ASC")
    fun getActiveSubscriptions(): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE isCancelled = 1 ORDER BY cancelledDate DESC")
    fun getCancelledSubscriptions(): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getById(id: Long): Subscription?

    @Insert
    suspend fun insert(subscription: Subscription): Long

    @Update
    suspend fun update(subscription: Subscription)

    @Delete
    suspend fun delete(subscription: Subscription)

    @Query("SELECT COUNT(*) FROM subscriptions WHERE isCancelled = 1")
    fun getCancelledCount(): Flow<Int>

    @Query("SELECT SUM(price) FROM subscriptions WHERE isCancelled = 1")
    fun getTotalSaved(): Flow<Double?>
}
