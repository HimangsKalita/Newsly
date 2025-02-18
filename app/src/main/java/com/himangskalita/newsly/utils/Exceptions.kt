package com.himangskalita.newsly.utils

class ErrorResponseException(message: String) : Throwable(message)

class NoSearchArticlesFound(message: String) : Throwable(message)

class NetworkException(message: String) : Throwable(message)

class DatabaseEmptyException(message: String) : Throwable(message)