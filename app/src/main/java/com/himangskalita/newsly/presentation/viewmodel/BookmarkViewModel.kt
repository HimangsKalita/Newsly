package com.himangskalita.newsly.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.himangskalita.newsly.data.models.Article
import com.himangskalita.newsly.data.models.BookmarkArticle
import com.himangskalita.newsly.data.models.Source
import com.himangskalita.newsly.data.repository.DatabaseNewsRepository
import com.himangskalita.newsly.utils.DatabaseEmptyException
import com.himangskalita.newsly.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(

    private val databaseNewsRepository: DatabaseNewsRepository
) : ViewModel() {

    private val _hasFetchedBookmarks = MutableLiveData(false)
    val hasFetchedBookmarks: LiveData<Boolean>
        get() = _hasFetchedBookmarks

    private val _bookmarks = MutableStateFlow<Resource<List<BookmarkArticle>>>(Resource.Ini())
    val bookmarks: StateFlow<Resource<List<BookmarkArticle>>>
        get() = _bookmarks

    fun getBookmarkArticlesList() {

        _bookmarks.value = Resource.Loading()

        viewModelScope.launch(Dispatchers.IO) {

            try {

                val bookmarkResults = databaseNewsRepository.getBookmarkArticles()

                _bookmarks.value = bookmarkResults.fold(

                    onSuccess = { bookmarkArticles ->

                        Resource.Success(bookmarkArticles)
                    },

                    onFailure = { throwable ->

                        if (throwable is DatabaseEmptyException) {

                            Resource.Error(
                                "empty database: ${throwable.message}, ${throwable.cause}",
                                emptyList()
                            )
                        } else {

                            Resource.Error(
                                "Error fetching bookmark articles: ${throwable.message}, ${throwable.cause}",
                                emptyList()
                            )
                        }
                    }
                )

            } catch (e: Exception) {

                _bookmarks.value = Resource.Error(
                    "Error fetching bookmark articles: ${e.message}, ${e.cause}",
                    emptyList()
                )
            }
        }
    }

    fun addBookmarkArticle(article: Article) {

        viewModelScope.launch(Dispatchers.IO) {

            val bookmarkArticle = mapToBookmarkArticle(article)
            databaseNewsRepository.saveBookmark(bookmarkArticle)
            getBookmarkArticlesList()
        }
    }

    fun clearBookmarkArticles() {

        viewModelScope.launch(Dispatchers.IO) {

            databaseNewsRepository.clearBookmarkArticles()
            getBookmarkArticlesList()
        }
    }

    private fun mapToBookmarkArticle(article: Article): BookmarkArticle {

        return BookmarkArticle(
            id = 0,
            author = article.author ?: "Author",
            content = article.content ?: "",
            description = article.description ?: "",
            publishedAt = article.publishedAt ?: "XXXX-XX-XX",
            source = article.source ?: Source("Souce ID", "Source"),
            title = article.title ?: "Article Title",
            url = article.url,
            urlToImage = article.urlToImage,
        )
    }
}