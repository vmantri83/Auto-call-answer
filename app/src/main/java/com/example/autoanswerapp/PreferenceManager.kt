package com.example.autoanswerapp

import android.content.Context

object PreferenceManager {
    private const val PREF_NAME = "AutoAnswerPrefs"
    private const val KEY_AUTO_ANSWER_ENABLED = "auto_answer_enabled"
    private const val KEY_ANSWER_DELAY = "answer_delay"

    fun isAutoAnswerEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_ANSWER_ENABLED, false)
    }

    fun setAutoAnswerEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_ANSWER_ENABLED, enabled).apply()
    }

    fun getAnswerDelay(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_ANSWER_DELAY, 5)
    }

    fun setAnswerDelay(context: Context, delay: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_ANSWER_DELAY, delay).apply()
    }
}