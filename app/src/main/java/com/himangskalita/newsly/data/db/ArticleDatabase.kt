package com.himangskalita.newsly.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.himangskalita.newsly.data.models.Article
import com.himangskalita.newsly.data.models.BookmarkArticle

@Database(entities = [Article::class, BookmarkArticle::class], version = 1)
@TypeConverters(Convertors::class)
abstract class ArticleDatabase: RoomDatabase() {

    abstract fun getArticleDao(): ArticleDao

    abstract fun getBookmarkArticleDao(): BookmarkArticleDao
}