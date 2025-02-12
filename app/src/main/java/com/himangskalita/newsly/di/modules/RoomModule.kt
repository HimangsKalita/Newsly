package com.himangskalita.newsly.di.modules

import android.content.Context
import androidx.room.Room
import com.himangskalita.newsly.data.db.ArticleDao
import com.himangskalita.newsly.data.db.ArticleDatabase
import com.himangskalita.newsly.data.db.BookmarkArticleDao
import com.himangskalita.newsly.data.repository.DatabaseNewsRepository
import com.himangskalita.newsly.data.repository.DatabaseNewsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RoomModule {

    @Provides
    @Singleton
    fun getDatabaseNewsRepository(articleDao: ArticleDao, bookmarkArticleDao: BookmarkArticleDao): DatabaseNewsRepository {

        return DatabaseNewsRepositoryImpl(articleDao, bookmarkArticleDao)
    }

    @Provides
    @Singleton
    fun getArticleDao(articleDatabase: ArticleDatabase): ArticleDao {

        return articleDatabase.getArticleDao()
    }

    @Provides
    @Singleton
    fun getBookmarkArticleDao(articleDatabase: ArticleDatabase): BookmarkArticleDao {

        return articleDatabase.getBookmarkArticleDao()
    }

    @Provides
    @Singleton
    fun getRoomDatabaseInstance(@ApplicationContext applicationContext: Context): ArticleDatabase {

        return Room.databaseBuilder(
            applicationContext,
            ArticleDatabase::class.java,
            "articleDatabase"
        ).build()
    }
}