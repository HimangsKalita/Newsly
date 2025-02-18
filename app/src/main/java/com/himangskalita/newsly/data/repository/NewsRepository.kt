package com.himangskalita.newsly.data.repository

import com.himangskalita.newsly.data.models.Article
import com.himangskalita.newsly.data.models.BookmarkArticle

interface ApiNewsRepository {

    suspend fun getNewsHeadlines(queryParams: Map<String, String>): Result<List<Article>>
    suspend fun getNewsHeadlinesPagination(queryParams: Map<String, String>, headlinePage: Int): Result<List<Article>>
    suspend fun searchNews(query: String): Result<List<Article>>
    suspend fun searchNewsPagination(query: String, headlinePage: Int): Result<List<Article>>
}

interface DatabaseNewsRepository {

    suspend fun saveArticleList(articleList: List<Article>)
    suspend fun getSavedArticles(): Result<List<Article>>

    suspend fun saveBookmark(bookmarkArticle: BookmarkArticle)
    suspend fun getBookmarkArticles(): Result<List<BookmarkArticle>>
    suspend fun isBookmarked(url: String): Boolean
    suspend fun deleteBookmarkArticle(url: String)
    suspend fun clearBookmarkArticles()
}