package com.himangskalita.newsly.data.repository

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.himangskalita.newsly.utils.Constants.Companion.APP_THEME_KEY
import com.himangskalita.newsly.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


const val USER_PREFERENCE = "user_preference"

val Context.userPreferenceDataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCE)

private val APP_THEME = intPreferencesKey(APP_THEME_KEY)

@Singleton
class DataStoreRepositoryIml @Inject constructor(private val preferencesDataStore: DataStore<Preferences>) :
    DataStoreRepository {

    override suspend fun saveAppThemeToPreferenceDataStore(theme: Int) {

        preferencesDataStore.edit { preferences ->

            preferences[APP_THEME] = theme
        }
    }

    override fun getAppThemeFromPreferenceDataStore(): Flow<Int> {

        return preferencesDataStore.data
            .map { preferences ->
                preferences[APP_THEME] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            .catch { exception ->
                if (exception is IOException) {

                    emit(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    Logger.d("IO Exception ${exception.message}")
                } else {
                    throw exception
                }
            }
    }
}