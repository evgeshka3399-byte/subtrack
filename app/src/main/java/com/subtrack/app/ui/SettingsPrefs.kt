package com.subtrack.app.ui

import android.content.Context

object SettingsPrefs {
    private const val PREFS = "subtrack_settings"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_REMINDER_DAYS = "reminder_days"
    private const val KEY_IS_PREMIUM = "is_premium"
    private const val KEY_CANCELLATION_COUNT = "cancellation_count"
    private const val KEY_BONUS_SLOTS = "bonus_slots"
    private const val KEY_BONUS_EXPIRY = "bonus_expiry"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    // === ЛИМИТЫ DEMO ===
    const val FREE_SUBSCRIPTION_LIMIT = 5
    const val FREE_CANCELLATION_LIMIT = 5
    const val BONUS_DURATION_MS = 24L * 60 * 60 * 1000 // 24 часа

    // === ТЕМА ===
    fun getTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    fun setTheme(context: Context, theme: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, theme).apply()
    }

    // === НАПОМИНАНИЯ ===
    fun getReminderDays(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_REMINDER_DAYS, 2)
    }

    fun setReminderDays(context: Context, days: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_REMINDER_DAYS, days).apply()
    }

    // === PREMIUM ===
    fun isPremium(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_PREMIUM, false)
    }

    fun setPremium(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_PREMIUM, value).apply()
    }

    // === СЧЁТЧИК ОТМЕН ===
    fun getCancellationCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_CANCELLATION_COUNT, 0)
    }

    fun incrementCancellationCount(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_CANCELLATION_COUNT, 0)
        prefs.edit().putInt(KEY_CANCELLATION_COUNT, current + 1).apply()
    }

    fun resetCancellationCount(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_CANCELLATION_COUNT, 0).apply()
    }

    // === БОНУСНЫЕ СЛОТЫ ===
    fun getBonusSlots(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val expiry = prefs.getLong(KEY_BONUS_EXPIRY, 0L)
        // Если бонусы истекли — обнуляем
        if (expiry > 0 && System.currentTimeMillis() > expiry) {
            prefs.edit().putInt(KEY_BONUS_SLOTS, 0).putLong(KEY_BONUS_EXPIRY, 0L).apply()
            return 0
        }
        return prefs.getInt(KEY_BONUS_SLOTS, 0)
    }

    fun addBonusSlot(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = getBonusSlots(context)
        val newExpiry = System.currentTimeMillis() + BONUS_DURATION_MS
        prefs.edit()
            .putInt(KEY_BONUS_SLOTS, current + 1)
            .putLong(KEY_BONUS_EXPIRY, newExpiry)
            .apply()
    }

    // === ЛИМИТЫ ===
    fun getSubscriptionLimit(context: Context): Int {
        if (isPremium(context)) return Int.MAX_VALUE
        return FREE_SUBSCRIPTION_LIMIT + getBonusSlots(context)
    }

    fun getCancellationLimit(context: Context): Int {
        if (isPremium(context)) return Int.MAX_VALUE
        return FREE_CANCELLATION_LIMIT
    }

    fun canAddSubscription(context: Context, activeCount: Int): Boolean {
        if (isPremium(context)) return true
        return activeCount < getSubscriptionLimit(context)
    }

    fun canCancelSubscription(context: Context): Boolean {
        if (isPremium(context)) return true
        return getCancellationCount(context) < getCancellationLimit(context)
    }
}
