package com.himangskalita.newsly.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import javax.annotation.Nonnull

@Parcelize
@Entity(tableName = "articles", indices = [Index(value = ["url"], unique = true)])
data class Article(
    val author: String?,
    val content: String?,
    val description: String?,
    val publishedAt: PublishedAt?,
    val source: Source?,
    val title: String?,
    @PrimaryKey(autoGenerate = false)
    @Nonnull
    val url: String,
    val urlToImage: String?

): Parcelable