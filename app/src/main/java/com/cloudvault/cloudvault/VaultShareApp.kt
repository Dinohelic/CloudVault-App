package com.cloudvault.cloudvault

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.cloudvault.cloudvault.utils.SharedPrefsManager

class VaultShareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val prefsManager = SharedPrefsManager(this)
        if (prefsManager.isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
