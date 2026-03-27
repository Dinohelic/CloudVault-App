package com.cloudvault.cloudvault.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.cloudvault.cloudvault.R
import com.cloudvault.cloudvault.utils.SharedPrefsManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefsManager = SharedPrefsManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (prefsManager.isLoggedIn) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
