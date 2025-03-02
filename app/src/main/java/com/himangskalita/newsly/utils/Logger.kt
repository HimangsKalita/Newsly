package com.himangskalita.newsly.utils

import android.util.Log

object Logger {

    private const val TAG = "NewslyTAG"

    fun d(message: String) {

        Log.d(TAG, message)
    }

    fun e(message: String, throwable: Throwable) {

        Log.e(TAG, message, throwable)
    }
}