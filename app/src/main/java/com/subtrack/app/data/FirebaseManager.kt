package com.subtrack.app.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseManager {

    private const val TAG = "FirebaseManager"

    // Auth — для отправки кодов на email
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    // Firestore — для хранения покупок
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    /**
     * Отправляет код подтверждения на email.
     * Firebase сам генерирует и отправляет код.
     */
    suspend fun sendVerificationCode(email: String): Result<Unit> {
        return try {
            val settings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
                .setUrl("https://subtrack.app/verify")
                .setHandleCodeInApp(true)
                .build()
            
            auth.sendSignInLinkToEmail(email, settings).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending code", e)
            Result.failure(e)
        }
    }

    /**
     * Проверяет код и восстанавливает Premium.
     * В реальности здесь нужно проверять код через Firebase.
     * Для MVP — проверяем, есть ли email в Firestore.
     */
    suspend fun restorePremium(email: String): Result<Boolean> {
        return try {
            val doc = db.collection("premium_users").document(email).get().await()
            if (doc.exists()) {
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring premium", e)
            Result.failure(e)
        }
    }

    /**
     * Сохраняет покупку Premium в Firestore.
     * Вызывается после успешной оплаты через ЮKassa.
     */
    suspend fun savePremiumPurchase(email: String, paymentId: String): Result<Unit> {
        return try {
            val data = hashMapOf(
                "email" to email,
                "paymentId" to paymentId,
                "timestamp" to System.currentTimeMillis(),
                "isPremium" to true
            )
            db.collection("premium_users").document(email).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving purchase", e)
            Result.failure(e)
        }
    }
}
