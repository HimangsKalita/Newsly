package com.himangskalita.newsly.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class NetworkConnectivityObserver @Inject constructor(

    @ApplicationContext private val context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected

    private val networkCallback = object : NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val isOnline =
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            updateConnectionState(isOnline)
        }

//        override fun onAvailable(network: Network) {
//            super.onAvailable(network)
//            updateConnectionState(true)
//        }

        override fun onUnavailable() {
            super.onUnavailable()
            updateConnectionState(false)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            updateConnectionState(false)
        }
    }

    private fun updateConnectionState(newState: Boolean) {

        if (newState != _isConnected.value) {

            _isConnected.value = newState
        }
    }

    init {

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun unregisterNetworkCallback() {

        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}