package com.cloudvault.cloudvault.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.cloudvault.cloudvault.R
import com.cloudvault.cloudvault.utils.SharedPrefsManager
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val prefsManager = SharedPrefsManager(requireContext())
        val themeSwitch = view.findViewById<SwitchMaterial>(R.id.theme_switch)

        themeSwitch.isChecked = prefsManager.isDarkTheme

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isDarkTheme = isChecked
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        return view
    }
}
