package com.himangskalita.newsly.presentation.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.himangskalita.newsly.data.repository.appSettingsDataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(

    private val appSettingsDataStoreRepository: appSettingsDataStoreRepository
) : ViewModel() {

    val theme = appSettingsDataStoreRepository.getAppThemeFromPreferenceDataStore()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )

    suspend fun getAppTheme(): Int {

        return appSettingsDataStoreRepository.getAppThemeFromPreferenceDataStore().first()
    }

    fun saveTheme(selectedTheme: Int) {

        viewModelScope.launch {

            appSettingsDataStoreRepository.saveAppThemeToPreferenceDataStore(selectedTheme)
        }
    }

    suspend fun getThemeSummary(): String {
        val currentTheme = getAppTheme()
        return when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "System default"
            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
            else -> "System default"
        }
    }

    val themeSummary: StateFlow<String> = theme.map { currentTheme ->
        when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "System default"
            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
            else -> "System default"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "System default"
    )
}