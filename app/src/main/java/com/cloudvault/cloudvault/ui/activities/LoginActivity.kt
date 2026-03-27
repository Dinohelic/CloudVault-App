package com.cloudvault.cloudvault.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cloudvault.cloudvault.R
import com.cloudvault.cloudvault.utils.SharedPrefsManager

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val prefsManager = SharedPrefsManager(this)

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            Toast.makeText(this, "Login Clicked", Toast.LENGTH_SHORT).show()
            
            prefsManager.isLoggedIn = true

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("USER_EMAIL", "sampleuser@vaultshare.com")
            startActivity(intent)
            finish()
        }

        val googleSignInButton = findViewById<Button>(R.id.googleSignInButton)
        googleSignInButton.setOnClickListener {
            Toast.makeText(this, "Google Sign-in Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
