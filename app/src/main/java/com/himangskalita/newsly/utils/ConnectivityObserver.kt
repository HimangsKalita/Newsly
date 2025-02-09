package com.himangskalita.newsly.utils

import kotlinx.coroutines.flow.StateFlow

interface ConnectivityObserver {

    val isConnected: StateFlow<Boolean>

    fun unregisterNetworkCallback()
    
}