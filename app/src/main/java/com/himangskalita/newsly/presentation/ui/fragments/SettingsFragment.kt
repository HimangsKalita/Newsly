package com.himangskalita.newsly.presentation.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.himangskalita.newsly.R

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.preferences, rootKey)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        updateThemeSummary()

        val themePreference = findPreference<Preference>("app_theme")

        themePreference?.let {

            it.setOnPreferenceClickListener {

                showThemeDialog()
                true
            }
        }
    }

    private fun showThemeDialog() {

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.theme_dialog, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.tdRadioGroup)

        val currentTheme = getSavedTheme()

        when (currentTheme) {

            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> radioGroup.check(R.id.tdRbSystemDefault)
            AppCompatDelegate.MODE_NIGHT_NO -> radioGroup.check(R.id.tdRbLightTheme)
            AppCompatDelegate.MODE_NIGHT_YES -> radioGroup.check(R.id.tdRbDarkTheme)

        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Select Theme")
            .setView(dialogView)
            .setCancelable(true)
            .setNegativeButton("Cancel", null)

        val themeAlertDialog = alertDialog.create()

        themeAlertDialog.show()

        radioGroup.setOnCheckedChangeListener { _, checkedId ->

            val selectedTheme = when (checkedId) {

                R.id.tdRbSystemDefault -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                R.id.tdRbLightTheme -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.tdRbDarkTheme -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }

            saveTheme(selectedTheme)
            AppCompatDelegate.setDefaultNightMode(selectedTheme)
            updateThemeSummary()
            themeAlertDialog.dismiss()
        }
    }

    private fun saveTheme(selectedTheme: Int) {

        sharedPreferences.edit().putInt("theme", selectedTheme).apply()
    }

    private fun getSavedTheme(): Int {

        return sharedPreferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    private fun updateThemeSummary() {

        val themePreference = findPreference<Preference>("app_theme")
        val currentTheme = getSavedTheme()

        val summary = when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "System default"
            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
            else -> "System default"
        }

        themePreference?.summary = summary
    }
}