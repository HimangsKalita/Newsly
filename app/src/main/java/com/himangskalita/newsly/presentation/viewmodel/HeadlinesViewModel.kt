package com.himangskalita.newsly.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.himangskalita.newsly.data.models.Article
import com.himangskalita.newsly.data.repository.ApiNewsRepository
import com.himangskalita.newsly.data.repository.DatabaseNewsRepository
import com.himangskalita.newsly.utils.ConnectivityObserver
import com.himangskalita.newsly.utils.Logger
import com.himangskalita.newsly.utils.NetworkUIState
import com.himangskalita.newsly.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeadlinesViewModel @Inject constructor(
    private val connectivityObserver: ConnectivityObserver,
    private val apiNewsRepository: ApiNewsRepository,
    private val databaseNewsRepository: DatabaseNewsRepository
) : ViewModel() {

    private val _result = MutableStateFlow<Resource<List<Article>>>(Resource.Ini())
    val result: StateFlow<Resource<List<Article>>>
        get() = _result
    private var headlinePage = 1
    fun getHeadlinePageCount() = headlinePage

    private var _hasFetchedNews = false
    val hasFetchedNews: Boolean
        get() = _hasFetchedNews
    fun changeHasFetchedNewsTrue() {

        _hasFetchedNews = true
    }
//    fun changeHasFetchedNewsFalse() {
//
//        _hasFetchedNews = false
//    }

    private val _firstConnection = MutableLiveData(true)
    val firstConnection: LiveData<Boolean>
        get() = _firstConnection
    fun changeFirstConnectionValue() {

        _firstConnection.value = false
    }
    fun changeFirstConnectionValueTrue() {

        _firstConnection.value = true
    }

    private val _swipeRefreshLoading = MutableLiveData<Boolean>(false)
    val swipeRefreshLoading: LiveData<Boolean>
        get() = _swipeRefreshLoading
    fun changeSwipeRefreshLoadingFalse() {

        _swipeRefreshLoading.value = false
    }
    fun changeSwipeRefreshLoadingTrue() {

        _swipeRefreshLoading.value = true
    }

    private val _networkUIState = MutableStateFlow<NetworkUIState>(NetworkUIState.Unknown)
    val networkUIState: StateFlow<NetworkUIState>
        get() = _networkUIState

    init {

        observeNetworkState()
    }

    private fun observeNetworkState() {

        viewModelScope.launch {

            connectivityObserver.isConnected.collectLatest { isConnected ->

                Logger.d("New Network State: $isConnected")

                if (isConnected) {

                    Logger.d("Network State: Connected")
                    _networkUIState.value = NetworkUIState.Connected
                } else {

                    Logger.d("Network State: Disconnected")
                    _networkUIState.value = NetworkUIState.Disconnected
                }
            }
        }
    }

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

//    override fun onCleared() {
//        super.onCleared()
//        connectivityObserver.unregisterNetworkCallback()
//    }

}