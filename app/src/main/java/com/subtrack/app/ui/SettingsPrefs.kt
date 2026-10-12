package com.subtrack.app.ui

import android.content.Context

object SettingsPrefs {
    private const val PREFS = "subtrack_settings"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_REMINDER_DAYS = "reminder_days"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    fun getTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    fun setTheme(context: Context, theme: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, theme).apply()
    }

    fun getReminderDays(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_REMINDER_DAYS, 2)
    }

    fun setReminderDays(context: Context, days: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_REMINDER_DAYS, days).apply()
    }
}
