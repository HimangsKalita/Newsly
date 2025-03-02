package com.himangskalita.newsly.presentation.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.himangskalita.newsly.data.models.Article
import com.himangskalita.newsly.data.models.BookmarkArticle
import com.himangskalita.newsly.data.models.Source
import com.himangskalita.newsly.data.repository.DatabaseNewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor(

    private val databaseNewsRepository: DatabaseNewsRepository
) : ViewModel() {

    private val _webViewState = MutableLiveData<Bundle?>()
    val webViewState: Bundle?
        get() = _webViewState.value

    private val _isBookMarked = MutableLiveData<Boolean>()
    val isBookMarked: LiveData<Boolean>
        get() = _isBookMarked

    fun checkArticleBookmarked(url: String) {

        viewModelScope.launch(Dispatchers.IO) {

            _isBookMarked.postValue(databaseNewsRepository.isBookmarked(url))
        }
    }

    fun toggleBookmark(article: Article) {

        viewModelScope.launch {
            if (_isBookMarked.value!!) {
                databaseNewsRepository.deleteBookmarkArticle(article.url)
            } else {
                databaseNewsRepository.saveBookmark(mapToBookmarkArticle(article))
            }
            _isBookMarked.value = !_isBookMarked.value!!
        }
    }

    private fun mapToBookmarkArticle(article: Article): BookmarkArticle {

        return BookmarkArticle(
            id = 0,
            author = article.author ?: "Author",
            content = article.content ?: "",
            description = article.description ?: "",
            publishedAt = article.publishedAt?.formattedDate ?: "(Date)",
            source = article.source ?: Source("Source ID", "Source"),
            title = article.title ?: "Article Title",
            url = article.url,
            urlToImage = article.urlToImage,
        )
    }
}