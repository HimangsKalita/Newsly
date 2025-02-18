package com.himangskalita.newsly.data.api

import com.himangskalita.newsly.BuildConfig
import com.himangskalita.newsly.data.models.News
import com.himangskalita.newsly.utils.Constants.Companion.API_VERSION
import com.himangskalita.newsly.utils.Constants.Companion.COUNTRY_CODE
import com.himangskalita.newsly.utils.Constants.Companion.PAGE
import com.himangskalita.newsly.utils.Constants.Companion.PAGE_SIZE
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface NewsApi {

    @GET("/{version}/top-headlines/")
    suspend fun getNewsHeadlines(
        @Path("version")
        version: String = API_VERSION,
        @Query("country")
        countryCode: String = COUNTRY_CODE,
        @QueryMap
        queryParams:Map<String, String>,
        @Query("pageSize")
        pageSize: Int = PAGE_SIZE,
        @Query("page")
        page: Int = PAGE,
        @Query("apiKey")
        apiKey: String = BuildConfig.API_KEY
    ) : Response<News>

    @GET("/{version}/top-headlines/")
    suspend fun getNewsHeadlinesPagination(
        @Path("version")
        version: String = API_VERSION,
        @Query("country")
        countryCode: String = COUNTRY_CODE,
        @QueryMap
        queryParams:Map<String, String>,
        @Query("pageSize")
        pageSize: Int  = PAGE_SIZE,
        @Query("page")
        page: Int,
        @Query("apiKey")
        apiKey: String = BuildConfig.API_KEY
    ) : Response<News>

    @GET("/{version}/everything?language=en")
    suspend fun searchNewsEverything(
        @Path("version")
        version: String = API_VERSION,
        @Query("q")
        query: String,
        @Query("pageSize")
        pageSize: Int = PAGE_SIZE,
        @Query("page")
        page: Int = PAGE,
        @Query("apiKey")
        apiKey: String = BuildConfig.API_KEY
    ) : Response<News>

    @GET("/{version}/everything?language=en")
    suspend fun searchNewsEverythingPagination(
        @Path("version")
        version: String = API_VERSION,
        @Query("q")
        query: String,
        @Query("pageSize")
        pageSize: Int = PAGE_SIZE,
        @Query("page")
        page: Int,
        @Query("apiKey")
        apiKey: String = BuildConfig.API_KEY
    ) : Response<News>
}