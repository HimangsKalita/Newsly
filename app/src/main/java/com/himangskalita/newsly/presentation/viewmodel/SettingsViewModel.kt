package com.himangskalita.newsly.presentation.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.himangskalita.newsly.data.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(

    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    val appTheme = dataStoreRepository.getAppThemeFromPreferenceDataStore()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000) , AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    fun saveTheme(selectedTheme: Int) {

        viewModelScope.launch {

            dataStoreRepository.saveAppThemeToPreferenceDataStore(selectedTheme)
        }
    }

}