package com.himangskalita.newsly.di.modules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.himangskalita.newsly.data.repository.DataStoreRepository
import com.himangskalita.newsly.data.repository.DataStoreRepositoryIml
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
    fun provideDataStoreRepository(preferencesDataStore: DataStore<Preferences>): DataStoreRepository {

        return DataStoreRepositoryIml(preferencesDataStore)
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