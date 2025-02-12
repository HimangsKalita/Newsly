package com.himangskalita.newsly.data.repository

import com.himangskalita.newsly.data.api.NewsApi
import com.himangskalita.newsly.data.db.ArticleDao
import com.himangskalita.newsly.data.db.BookmarkArticleDao
import com.himangskalita.newsly.data.models.Article
import com.himangskalita.newsly.data.models.BookmarkArticle
import com.himangskalita.newsly.utils.DatabaseEmptyException
import com.himangskalita.newsly.utils.Logger
import com.himangskalita.newsly.utils.NoSearchArticlesFound
import javax.inject.Inject

class ApiNewsRepositoryIml @Inject constructor(
    private val newsApi: NewsApi,
    private val articleDao: ArticleDao
) : ApiNewsRepository {

    override suspend fun getNewsHeadlines(queryParams: Map<String, String>): Result<List<Article>> {

        return try {

            val response = newsApi.getNewsHeadlines(queryParams = queryParams)

            if (response.isSuccessful) {

                val articleList = response.body()?.articles.orEmpty()

                if (articleList.isNotEmpty()) {

                    val cachedArticles = articleDao.getArticles()

                    if (cachedArticles.isNotEmpty()) {

                        Logger.d("Database before clearing: ${articleDao.getArticles().size}")
                        articleDao.clearArticles()
                        Logger.d("Database after clearing: ${articleDao.getArticles().size}")

//                        val newArticles = articleList.filterNot { article ->
//
//                            cachedArticles.any { it.url == article.url }
//                        }

                        articleDao.insertArticleList(articleList)
                        Logger.d("Database: New Database Cache: ${articleDao.getArticles().size}")

                    }else {

                        articleDao.insertArticleList(articleList)
                        Logger.d("Database: Initial Database Cache: ${articleDao.getArticles().size}")
                    }
                }

                Result.success(articleList)

            }else {

                Result.failure(Throwable("${response.code()}: ${mapError(response.code())}"))
            }

        }catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun searchNews(query: String): Result<List<Article>> {

        return try {

            val response = newsApi.searchNewsEverything(query = query)

            if (response.isSuccessful) {

                val articleList = response.body()?.articles.orEmpty()

                if (articleList.isNotEmpty()) {

                    Result.success(articleList)
                }else {

                    Result.failure(NoSearchArticlesFound("No Articles found"))
                }
            }else{

                Result.failure(Throwable("${response.code()}: ${mapError(response.code())}"))
            }

        }catch (e: Exception) {

            Result.failure(Throwable("Error searching news: ${e.message}, ${e.cause}"))
        }
    }

    private fun mapError(code: Int): String {

        return when(code) {

            400 -> "Bad request - Invalid or missing parameters"
            401 -> "Unauthorized - Authentication failed"
            403 -> "Forbidden - Server refuses action"
            404 -> "Not found - Resource does not exist"
            405 -> "Method not allowed"
            408 -> "Request timeout"
            409 -> "Conflict - Resource state conflict"
            415 -> "Unsupported media type"
            429 -> "Too many requests - Rate limit exceeded"
            500 -> "Internal server error"
            502 -> "Bad gateway - Invalid response from upstream server"
            503 -> "Service unavailable - Server is down or overloaded"
            504 -> "Gateway timeout"
            else -> "Unknown error"
        }
    }

}

class DatabaseNewsRepositoryImpl @Inject constructor(

    private val articleDao: ArticleDao,
    private val bookmarkArticleDao: BookmarkArticleDao
): DatabaseNewsRepository {

    override suspend fun saveArticleList(articleList: List<Article>) {

        articleDao.insertArticleList(articleList)
    }

    override suspend fun getSavedArticles(): Result<List<Article>> {

        return try {

            val articleList = articleDao.getArticles()

            if (articleList.isNotEmpty()) {

                Result.success(articleList)
            }else {

                Result.failure(DatabaseEmptyException("Error database is empty"))
            }

        }catch (e: Exception) {

            Result.failure(Throwable("Error fetching articles from database: ${e.message}, ${e.cause}"))
        }
    }

    override suspend fun saveBookmark(bookmarkArticle: BookmarkArticle) {

        bookmarkArticleDao.insertBookmarkArticle(bookmarkArticle)
    }

    override suspend fun getBookmarkArticles(): Result<List<BookmarkArticle>> {

        return try {

            val bookmarkArticleList = bookmarkArticleDao.getBookmarkArticles()

            if (bookmarkArticleList.isNotEmpty()) {

                Result.success(bookmarkArticleList)
            }else {

                Result.failure(DatabaseEmptyException("Empty Bookmark exception"))
            }

        }catch (e: Exception) {

            Result.failure(Throwable("Error fetching bookmark articles: ${e.message}, ${e.cause}"))
        }
    }

    override suspend fun deleteBookmarkArticle(url: String) {

        bookmarkArticleDao.deleteBookmarkArticle(url)
    }

    override suspend fun clearBookmarkArticles() {

        bookmarkArticleDao.clearBookmarkArticles()
    }
}