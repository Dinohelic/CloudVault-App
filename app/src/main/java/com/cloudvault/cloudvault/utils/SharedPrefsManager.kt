package com.cloudvault.cloudvault.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("VaultSharePrefs", Context.MODE_PRIVATE)

    companion object {
        const val IS_LOGGED_IN = "is_logged_in"
        const val IS_DARK_THEME = "is_dark_theme"
        const val PROFILE_IMAGE_URI = "profile_image_uri"
    }

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(IS_LOGGED_IN, value).apply()

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(IS_DARK_THEME, true)
        set(value) = prefs.edit().putBoolean(IS_DARK_THEME, value).apply()

    var profileImageUri: String?
        get() = prefs.getString(PROFILE_IMAGE_URI, null)
        set(value) = prefs.edit().putString(PROFILE_IMAGE_URI, value).apply()
}
