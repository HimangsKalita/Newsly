package com.himangskalita.newsly.di.modules

import com.himangskalita.newsly.data.api.NewsApi
import com.himangskalita.newsly.data.api.RetrofitBuilder
import com.himangskalita.newsly.data.db.ArticleDao
import com.himangskalita.newsly.data.repository.ApiNewsRepository
import com.himangskalita.newsly.data.repository.ApiNewsRepositoryIml
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    @Provides
    @Singleton
    fun getApiNewsRepository(newsApi: NewsApi, articleDao: ArticleDao) : ApiNewsRepository {

        return ApiNewsRepositoryIml(newsApi, articleDao)
    }

    @Provides
    @Singleton
    fun getNewsApi() : NewsApi {

        return RetrofitBuilder.getRetrofitInstance().create(NewsApi::class.java)
    }
}