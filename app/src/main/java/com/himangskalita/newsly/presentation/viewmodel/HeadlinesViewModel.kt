package com.himangskalita.newsly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.himangskalita.newsly.data.models.Article
import com.himangskalita.newsly.data.repository.ApiNewsRepository
import com.himangskalita.newsly.data.repository.DatabaseNewsRepository
import com.himangskalita.newsly.utils.ConnectivityObserver
import com.himangskalita.newsly.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeadlinesViewModel @Inject constructor(
    private val apiNewsRepository: ApiNewsRepository,
    private val databaseNewsRepository: DatabaseNewsRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _result = MutableStateFlow<Resource<List<Article>>>(Resource.Ini())
    val result: StateFlow<Resource<List<Article>>>
        get() = _result

    val connectivityStatus = connectivityObserver.isConnected
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun fetchApiHeadlines(queryParams: Map<String, String>) {

        _result.value = Resource.Loading()

        viewModelScope.launch {

            try {

                val response =
                    apiNewsRepository.getNewsHeadlines(queryParams)

                _result.value = response.fold(

                    onSuccess = { articles ->

                        Resource.Success(articles)
                    },
                    onFailure = { throwable ->

                        Resource.Error(
                            throwable.message ?: "An error occurred fetching api news",
                            emptyList()
                        )
                    }
                )

            } catch (e: Exception) {

                _result.value = Resource.Error(
                    e.message ?: "An error occurred fetching news from api",
                    emptyList()
                )
            }

        }
    }

    fun fetchDatabaseHeadlines() {

        _result.value = Resource.Loading()

        viewModelScope.launch {

            try {
                val response = databaseNewsRepository.getSavedArticles()

                _result.value = response.fold(

                    onSuccess = {
                        Resource.Success(it)
                    },
                    onFailure = {
                        Resource.Error(
                            it.message ?: "An error occurred fetching database news",
                            emptyList()
                        )
                    }
                )

            } catch (e: Exception) {

                _result.value = Resource.Error(
                    e.message ?: "An error occurred fetching news from api",
                    emptyList()
                )
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        connectivityObserver.unregisterNetworkCallback()
    }

}