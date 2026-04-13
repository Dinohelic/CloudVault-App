package com.cloudvault.cloudvault.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cloudvault.cloudvault.R
import com.cloudvault.cloudvault.ui.activities.LoginActivity
import com.cloudvault.cloudvault.utils.SharedPrefsManager
import com.cloudvault.cloudvault.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val prefsManager = SharedPrefsManager(requireContext())
        val themeSwitch = view.findViewById<SwitchMaterial>(R.id.theme_switch)
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)

        themeSwitch.isChecked = prefsManager.isDarkTheme

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isDarkTheme = isChecked
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        logoutButton.setOnClickListener {
            // Sign out from Firebase
            authViewModel.logout()

            // Sign out from Google
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
            googleSignInClient.signOut().addOnCompleteListener {
                // Clear local preferences
                prefsManager.isLoggedIn = false
                
                // Navigate to Login screen
                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }
        }

        return view
    }
}
