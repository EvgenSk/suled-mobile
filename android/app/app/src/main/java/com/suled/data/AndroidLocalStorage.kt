package com.suled.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Android implementation of LocalStorageProvider using SharedPreferences
 */
class AndroidLocalStorageProvider(context: Context) : LocalStorageProvider {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "suled_prefs",
        Context.MODE_PRIVATE
    )
    
    override fun getString(key: String, defaultValue: String?): String? {
        return prefs.getString(key, defaultValue)
    }
    
    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
    
    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
    
    override fun clear() {
        prefs.edit().clear().apply()
    }
}

/**
 * Convenience function to create LocalStorageService with Android provider
 */
fun createLocalStorageService(context: Context): LocalStorageService {
    return LocalStorageService(AndroidLocalStorageProvider(context))
}
