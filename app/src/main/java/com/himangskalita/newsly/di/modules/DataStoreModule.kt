package com.himangskalita.newsly.di.modules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.himangskalita.newsly.data.repository.appSettingsDataStoreRepository
import com.himangskalita.newsly.data.repository.appSettingsDataStoreRepositoryIml
import com.himangskalita.newsly.data.repository.userPreferenceDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.userPreferenceDataStore
    }

    @Provides
    @Singleton
    fun provideDataStoreRepository(@ApplicationContext context: Context): appSettingsDataStoreRepository {

        return appSettingsDataStoreRepositoryIml(context)
    }

//    @Module
//    @InstallIn(SingletonComponent::class)
//    abstract class RepositoryModule {
//
//        @Binds
//        abstract fun bindDataStoreRepository(
//            DataStoreRepositoryIml: DataStoreRepositoryIml
//        ): DataStoreRepository
//    }
}