package com.subtrack.app.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseManager {

    private const val TAG = "FirebaseManager"

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    /**
     * Отправляет письмо для подтверждения почты.
     * Используем сброс пароля как временный механизм кода.
     */
    suspend fun sendVerificationCode(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Auth error: ${e.errorCode}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending code", e)
            Result.failure(e)
        }
    }

    /**
     * Проверяет, есть ли email в базе Premium.
     */
    suspend fun checkEmailInPremium(email: String): Result<Boolean> {
        return try {
            val doc = db.collection("premium_users").document(email).get().await()
            Result.success(doc.exists())
        } catch (e: Exception) {
            Log.e(TAG, "Error checking email", e)
            Result.failure(e)
        }
    }

    /**
     * Сохраняет покупку Premium в Firestore.
     * Вызывается после успешной оплаты.
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
