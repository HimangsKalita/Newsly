package com.himangskalita.newsly.utils

class NoSearchArticlesFound(message: String) : Throwable(message)

class NetworkException(message: String) : Throwable(message)

class DatabaseEmptyException(message: String) : Throwable(message)