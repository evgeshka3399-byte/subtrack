package com.subtrack.app.ui

import android.content.Context

object OnboardingPrefs {
    private const val PREFS = "subtrack_prefs"
    private const val KEY_SEEN = "onboarding_seen"

    fun hasSeenOnboarding(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SEEN, false)
    }

    fun setOnboardingSeen(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SEEN, true).apply()
    }
}
