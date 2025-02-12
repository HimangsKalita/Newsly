package com.himangskalita.newsly.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import javax.annotation.Nonnull

@Entity(tableName = "bookmark_articles", indices = [Index(value = ["url"], unique = true)])
data class BookmarkArticle(
    @PrimaryKey(autoGenerate = true)
    @Nonnull
    val id: Int,
    val author: String?,
    val content: String?,
    val description: String?,
    val publishedAt: String?,
    val source: Source?,
    val title: String?,
    @Nonnull
    val url: String,
    val urlToImage: String?
)