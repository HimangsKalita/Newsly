package com.himangskalita.newsly.di.modules

import com.himangskalita.newsly.utils.ConnectivityObserver
import com.himangskalita.newsly.utils.NetworkConnectivityObserver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class ConnectivityModule {

    @Binds
    @Singleton
    abstract fun getConnectivityObserver(networkConnectivityObserver: NetworkConnectivityObserver): ConnectivityObserver

}