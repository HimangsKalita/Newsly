package com.himangskalita.newsly.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.himangskalita.newsly.data.models.BookmarkArticle

@Dao
interface BookmarkArticleDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmarkArticle(bookmarkArticle: BookmarkArticle)

    @Query("DELETE FROM bookmark_articles WHERE url = :url")
    suspend fun deleteBookmarkArticle(url: String)

    @Query("SELECT * FROM bookmark_articles")
    suspend fun getBookmarkArticles(): List<BookmarkArticle>

    @Query("DELETE FROM bookmark_articles")
    suspend fun clearBookmarkArticles()

}