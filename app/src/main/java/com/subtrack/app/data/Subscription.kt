package com.subtrack.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Double,
    val currency: String = "₽",
    val category: String = "Другое",
    val cycle: String = "MONTHLY", // MONTHLY, QUARTERLY, HALF_YEARLY, YEARLY, CUSTOM
    val customCycleDays: Int = 0,
    val firstPaymentDate: Long, // timestamp в миллисекундах
    val nextPaymentDate: Long,
    val hasTrial: Boolean = false,
    val trialEndDate: Long? = null,
    val priceAfterTrial: Double? = null,
    val isCancelled: Boolean = false,
    val cancelledDate: Long? = null,
    val notes: String = ""
)
