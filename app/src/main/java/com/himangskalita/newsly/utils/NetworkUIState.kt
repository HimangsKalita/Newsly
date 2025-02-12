package com.himangskalita.newsly.utils

sealed class NetworkUIState {

    object Unknown: NetworkUIState()
    object Connected: NetworkUIState()
    object Disconnected: NetworkUIState()
}