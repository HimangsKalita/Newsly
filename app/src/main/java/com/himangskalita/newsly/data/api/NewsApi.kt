package com.himangskalita.newsly.data.api

import com.himangskalita.newsly.BuildConfig
import com.himangskalita.newsly.data.models.News
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface NewsApi {

    @GET("/{version}/top-headlines?language=en")
    suspend fun getNewsHeadlines(
        @Path("version") version: String = "v2",
        @QueryMap queryParams:Map<String, String>,
        @Query("apiKey") apiKey: String = BuildConfig.API_KEY
    ) : Response<News>
}