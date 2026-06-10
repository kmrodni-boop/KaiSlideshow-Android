package com.kmrodni.kaislideshow.models

import android.content.SharedPreferences

/**
 * Slideshow settings model for persistence
 */
data class SlideshowSettings(
    val interval: Int = 5,
    val shuffle: Boolean = true
) {
    companion object {
        private const val KEY_INTERVAL = "interval"
        private const val KEY_SHUFFLE = "shuffle"
        
        /**
         * Default settings
         */
        val defaultSettings = SlideshowSettings()
        
        /**
         * Load settings from SharedPreferences
         */
        fun fromSharedPreferences(prefs: SharedPreferences): SlideshowSettings {
            return SlideshowSettings(
                interval = prefs.getInt(KEY_INTERVAL, 5),
                shuffle = prefs.getBoolean(KEY_SHUFFLE, true)
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
            apply()
        }
    }
}
