package com.himangskalita.newsly.data.repository

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {

    suspend fun saveAppThemeToPreferenceDataStore(theme: Int)
    fun getAppThemeFromPreferenceDataStore() : Flow<Int>
}