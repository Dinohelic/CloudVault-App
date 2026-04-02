package com.cloudvault.cloudvault.ui.activities

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.cloudvault.cloudvault.R
import com.cloudvault.cloudvault.ui.fragments.HomeFragment
import com.cloudvault.cloudvault.ui.fragments.ProfileFragment
import com.cloudvault.cloudvault.ui.fragments.SettingsFragment
import com.cloudvault.cloudvault.ui.fragments.VaultFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val vaultPinLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadFragment(VaultFragment())
            // Manually set the item as selected since we blocked the initial selection
            findViewById<BottomNavigationView>(R.id.bottom_nav_view).menu.findItem(R.id.navigation_vault).isChecked = true
        } else {
            // If user cancels PIN entry, revert to the previously selected tab
            // This is complex, a simpler way is to just go to Home
            findViewById<BottomNavigationView>(R.id.bottom_nav_view).selectedItemId = R.id.navigation_home
        }
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        if (item.itemId == R.id.navigation_vault) {
            val intent = Intent(this, PinEntryActivity::class.java)
            vaultPinLauncher.launch(intent)
            return@OnNavigationItemSelectedListener false // Prevents the tab from being selected until PIN is verified
        }
        
        val fragment = when (item.itemId) {
            R.id.navigation_profile -> ProfileFragment()
            R.id.navigation_settings -> SettingsFragment()
            else -> HomeFragment() // Default to Home
        }
        loadFragment(fragment)
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.bottom_nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        showWelcomeNotification()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    private fun showWelcomeNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "welcome_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Welcome Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Welcome to VaultShare")
            .setContentText("Your secure cloud storage solution.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(1, builder.build())
    }
}
