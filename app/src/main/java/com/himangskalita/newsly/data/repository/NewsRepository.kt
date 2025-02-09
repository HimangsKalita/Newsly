package com.himangskalita.newsly.data.repository

import com.himangskalita.newsly.data.models.Article

interface ApiNewsRepository {

    suspend fun getNewsHeadlines(queryParams: Map<String, String>): Result<List<Article>>
}

interface DatabaseNewsRepository {

    suspend fun saveArticleList(articleList: List<Article>)
    suspend fun getSavedArticles(): Result<List<Article>>
}