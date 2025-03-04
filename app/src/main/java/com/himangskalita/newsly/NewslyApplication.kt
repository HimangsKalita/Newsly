package com.himangskalita.newsly

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.himangskalita.newsly.data.repository.appSettingsDataStoreRepository
import com.himangskalita.newsly.utils.Constants.Companion.APP_THEME_KEY
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException
import javax.inject.Inject


@HiltAndroidApp
class NewslyApplication : Application() {

    @Inject
    lateinit var preferenceDataStore: DataStore<Preferences>

    override fun onCreate() {
        super.onCreate()

        runBlocking {
            val savedTheme = preferenceDataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[intPreferencesKey(APP_THEME_KEY)]
                        ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                .first()
            AppCompatDelegate.setDefaultNightMode(savedTheme)
        }
    }
}