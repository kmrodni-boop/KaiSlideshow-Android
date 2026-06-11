package com.kmrodni.kaislideshow.models

import android.content.SharedPreferences

/**
 * Slideshow settings model for persistence
 */
data class SlideshowSettings(
    val interval: Int = 5,
    val shuffle: Boolean = true,
    val transitionEnabled: Boolean = true,
    val kenBurnsEnabled: Boolean = true,
    val sleepTimerMinutes: Int = 0
) {
    companion object {
        private const val KEY_INTERVAL = "interval"
        private const val KEY_SHUFFLE = "shuffle"
        private const val KEY_TRANSITION = "transition"
        private const val KEY_KEN_BURNS = "ken_burns"
        private const val KEY_SLEEP_TIMER = "sleep_timer"
        
        /**
         * Load settings from SharedPreferences
         */
        fun fromSharedPreferences(prefs: SharedPreferences): SlideshowSettings {
            return SlideshowSettings(
                interval = prefs.getInt(KEY_INTERVAL, 5),
                shuffle = prefs.getBoolean(KEY_SHUFFLE, true),
                transitionEnabled = prefs.getBoolean(KEY_TRANSITION, true),
                kenBurnsEnabled = prefs.getBoolean(KEY_KEN_BURNS, true),
                sleepTimerMinutes = prefs.getInt(KEY_SLEEP_TIMER, 0)
            )
        }
    }
    
    /**
     * Save settings to SharedPreferences
     */
    fun saveToSharedPreferences(prefs: SharedPreferences) {
        with(prefs.edit()) {
            putInt(KEY_INTERVAL, interval)
            putBoolean(KEY_SHUFFLE, shuffle)
            putBoolean(KEY_TRANSITION, transitionEnabled)
            putBoolean(KEY_KEN_BURNS, kenBurnsEnabled)
            putInt(KEY_SLEEP_TIMER, sleepTimerMinutes)
            apply()
        }
    }
}
