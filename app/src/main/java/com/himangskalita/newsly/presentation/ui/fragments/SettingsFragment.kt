package com.himangskalita.newsly.presentation.ui.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.himangskalita.newsly.R
import com.himangskalita.newsly.utils.CubicBezierInterpolator
import com.himangskalita.newsly.utils.Logger
import kotlin.math.max

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var screenShotImageView: ImageView

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

        val titleTextView = TextView(requireContext()).apply {
            text = "Select Theme"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setPadding(48, 48, 48, 0) // Optional: Add padding
        }

        val alertDialog = MaterialAlertDialogBuilder(requireContext())
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

            val screenShot = takeScreenshot()
            showScreenShot(screenShot)

            saveTheme(selectedTheme)
            AppCompatDelegate.setDefaultNightMode(selectedTheme)
            updateThemeSummary()

            startCircularReveal()

            themeAlertDialog.dismiss()
        }
    }

    private fun takeScreenshot(): Bitmap {

        val rootView = requireActivity().window.decorView.rootView
        val bitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        rootView.draw(canvas)

        return bitmap
    }

    private fun showScreenShot(screenShot: Bitmap) {

        screenShotImageView = ImageView(requireContext()).apply {

            setImageBitmap(screenShot)

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        screenShotImageView.visibility = View.VISIBLE

        Logger.d("screenShotImageView visibility: ${screenShotImageView.visibility}")
        Logger.d("screenShotImageView dimensions: ${screenShotImageView.width}x${screenShotImageView.height}")

        (requireActivity().window.decorView as ViewGroup).addView(screenShotImageView)

    }

    private fun startCircularReveal() {

        screenShotImageView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Remove the listener to avoid multiple calls
                screenShotImageView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // Start the circular reveal animation
                val rootView = requireActivity().window.decorView.rootView

                rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {

                        rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                        val centerX = rootView.width / 2
                        val centerY = rootView.height / 2
                        val startRadius = 0f
                        val endRadius = max(rootView.width, rootView.height).toFloat()

                        Logger.d("Center: ($centerX, $centerY), Radius: $endRadius")

                        val anim = ViewAnimationUtils.createCircularReveal(rootView, centerX, centerY, startRadius, endRadius)

                        anim.duration = 500

                        anim.setInterpolator(CubicBezierInterpolator.EASE_IN_OUT_QUAD)

                        anim.addListener(object : AnimatorListenerAdapter() {

                            override fun onAnimationStart(animation: Animator) {
                                Logger.d("Animation started")
                            }

                            override fun onAnimationEnd(animation: Animator) {
                                Logger.d("Animation ended")
                                if (isAdded) {
                                    screenShotImageView.setImageDrawable(null)
                                    screenShotImageView.visibility = View.GONE
                                    (requireActivity().window.decorView as ViewGroup).removeView(screenShotImageView)
                                }
                            }
                        })

                        anim.start()
                    }
                })
            }
        })

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