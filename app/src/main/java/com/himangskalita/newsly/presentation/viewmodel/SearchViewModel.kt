package com.himangskalita.newsly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.himangskalita.newsly.data.models.Article
import com.himangskalita.newsly.data.repository.ApiNewsRepository
import com.himangskalita.newsly.utils.ConnectivityObserver
import com.himangskalita.newsly.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val connectivityObserver: ConnectivityObserver,
    private val newsRepository: ApiNewsRepository
) : ViewModel() {

//    private val _networkState = MutableStateFlow<NetworkUIState>(NetworkUIState.Unknown)
//    val networkState: StateFlow<NetworkUIState>
//        get() = _networkState

    private val _result = MutableStateFlow<Resource<List<Article>>>(Resource.Ini())
    val result: StateFlow<Resource<List<Article>>>
        get() = _result

    private var _hasFetchSearch = false
    val hasFetchSearch: Boolean
        get() = _hasFetchSearch

    private var isPaginationSearching = false
    fun changePaginationTrue() {

        isPaginationSearching = true
    }

    private val articleList = mutableListOf<Article>()
    private var headlinePage = 2
    fun incrementHeadlinePage() = headlinePage++

//    init {
//
//        observeNetworkState()
//    }
//
//    private fun observeNetworkState() {
//
//        viewModelScope.launch {
//
//            connectivityObserver.isConnected.collect { isConnected ->
//
//                if (isConnected) {
//
//                    _networkState.value = NetworkUIState.Connected
//                } else {
//
//                    _networkState.value = NetworkUIState.Disconnected
//                }
//            }
//        }
//    }

    fun searchNewsQuery(query: String) {

        _result.value = Resource.Loading()

        viewModelScope.launch(Dispatchers.IO) {

            try {

                val result = newsRepository.searchNews(query)

                _result.value = result.fold(

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

            }catch (e: Exception) {

                _result.value = Resource.Error(
                    e.message ?: "An error occurred searching news from api", emptyList()
                )
            }
        }
    }

    fun searchNewsQueryPagination(query: String) {

        if (isPaginationSearching) return

        changePaginationTrue()

        _result.value = Resource.PaginationLoading()

        viewModelScope.launch(Dispatchers.IO) {

            delay(1000)

            try {

                val result = newsRepository.searchNewsPagination(query, headlinePage)

                _result.value = result.fold(

                    onSuccess = { articles ->

                        incrementHeadlinePage()
                        Resource.Success(articleList + articles)
                    },
                    onFailure = { throwable ->

                        Resource.Error(
                            throwable.message ?: "An error occurred fetching api news",
                            emptyList()
                        )
                    }
                )

            }catch (e: Exception) {

                _result.value = Resource.Error(
                    e.message ?: "An error occurred searching news from api", emptyList()
                )
            }finally {

                isPaginationSearching = false
            }
        }
    }
}