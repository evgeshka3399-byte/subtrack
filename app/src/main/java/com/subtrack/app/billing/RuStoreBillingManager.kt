package com.subtrack.app.billing

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory
import ru.rustore.sdk.billingclient.model.purchase.Purchase
import ru.rustore.sdk.billingclient.model.product.Product
import ru.rustore.sdk.billingclient.model.product.ProductId
import ru.rustore.sdk.billingclient.model.purchase.PurchaseId
import ru.rustore.sdk.billingclient.model.purchase.PurchaseResult
import ru.rustore.sdk.billingclient.model.product.ProductType

/**
 * Менеджер для работы с RuStore Pay SDK.
 *
 * ВАЖНО: Перед использованием необходимо:
 * 1. Зарегистрировать приложение в RuStore Консоли: https://console.rustore.ru
 * 2. Получить consoleApplicationId и заменить его ниже.
 * 3. Включить возможность покупок для приложения в консоли.
 * 4. Подписать приложение тем же ключом, что и в RuStore.
 */
object RuStoreBillingManager {

    private const val TAG = "RuStoreBilling"

    // TODO: Замени на свой consoleApplicationId из RuStore Консоли
    private const val CONSOLE_APPLICATION_ID = "YOUR_CONSOLE_APP_ID"

    // ID продукта для разовой покупки "Premium навсегда"
    const val PRODUCT_ID_PREMIUM = "subtrack_premium_lifetime"

    private var billingClient: RuStoreBillingClient? = null

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState

    sealed class PurchaseState {
        object Idle : PurchaseState()
        object Loading : PurchaseState()
        object Success : PurchaseState()
        data class Error(val message: String) : PurchaseState()
    }

    /**
     * Инициализация клиента. Вызывать в onCreate MainActivity.
     */
    fun initialize(context: Context) {
        try {
            billingClient = RuStoreBillingClientFactory.create(
                context = context,
                consoleApplicationId = CONSOLE_APPLICATION_ID,
                deeplinkScheme = "subtrack"
            )
            Log.d(TAG, "RuStoreBillingClient initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize billing client", e)
        }
    }

    /**
     * Проверка, куплен ли Premium ранее.
     */
    fun checkPurchases() {
        billingClient?.purchases?.getPurchases(
            onSuccess = { purchases ->
                val hasPremium = purchases.any {
                    it.productId == PRODUCT_ID_PREMIUM &&
                    it.purchaseState == ru.rustore.sdk.billingclient.model.purchase.PurchaseState.PAID
                }
                _isPremium.value = hasPremium
                Log.d(TAG, "Purchases loaded. Premium: $hasPremium")
            },
            onFailure = { error ->
                Log.e(TAG, "Failed to load purchases", error)
            }
        )
    }

    /**
     * Запуск покупки Premium.
     */
    fun purchasePremium() {
        _purchaseState.value = PurchaseState.Loading

        billingClient?.purchases?.purchase(
            productId = PRODUCT_ID_PREMIUM,
            onSuccess = { result ->
                when (result) {
                    is PurchaseResult.Success -> {
                        billingClient?.purchases?.confirmPurchase(
                            purchaseId = result.purchaseId,
                            onSuccess = {
                                _isPremium.value = true
                                _purchaseState.value = PurchaseState.Success
                                Log.d(TAG, "Premium purchased and confirmed")
                            },
                            onFailure = { error ->
                                _purchaseState.value = PurchaseState.Error("Ошибка подтверждения")
                                Log.e(TAG, "Confirm failed", error)
                            }
                        )
                    }
                    is PurchaseResult.Failure -> {
                        _purchaseState.value = PurchaseState.Error(result.errorMessage)
                        Log.e(TAG, "Purchase failed: ${result.errorMessage}")
                    }
                    else -> {
                        _purchaseState.value = PurchaseState.Idle
                    }
                }
            },
            onFailure = { error ->
                _purchaseState.value = PurchaseState.Error("Ошибка покупки")
                Log.e(TAG, "Purchase request failed", error)
            }
        )
    }

    /**
     * Обработка deeplink при возврате из платёжного приложения.
     */
    fun handleIntent(intent: Intent?) {
        billingClient?.onNewIntent(intent)
    }

    fun resetState() {
        _purchaseState.value = PurchaseState.Idle
    }
}
