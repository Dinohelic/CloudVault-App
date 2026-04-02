package com.cloudvault.cloudvault.utils

import android.content.Context
import android.content.SharedPreferences

class PinManager(context: Context, userId: String) {
    private val prefs: SharedPreferences

    companion object {
        private const val VAULT_PIN_KEY = "vault_pin"
        private const val PREFS_NAME_PREFIX = "VaultPinPrefs_"
    }

    init {
        // Create a user-specific preferences file name
        val prefsName = "$PREFS_NAME_PREFIX$userId"
        prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    }

    fun isPinSet(): Boolean {
        return prefs.contains(VAULT_PIN_KEY)
    }

    fun savePin(pin: String) {
        // In a future phase, this value should be encrypted before saving.
        prefs.edit().putString(VAULT_PIN_KEY, pin).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val storedPin = prefs.getString(VAULT_PIN_KEY, null)
        return storedPin == pin
    }
}
