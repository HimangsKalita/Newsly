package com.himangskalita.newsly.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.himangskalita.newsly.data.models.PublishedAt
import com.himangskalita.newsly.data.models.Source

class Convertors {

    private val gson = Gson()

    @TypeConverter
    fun fromSource(source: Source?): String? {

        return source?.let {

            gson.toJson(it)
        }
    }

    @TypeConverter
    fun toSource(sourceString: String?): Source? {

        return sourceString?.let {

            gson.fromJson(sourceString, Source::class.java)
        }
    }

    @TypeConverter
    fun fromPublishedAt(publishedAt: PublishedAt?): String? {
        return publishedAt?.formattedDate
    }

    @TypeConverter
    fun toPublishedAt(dateString: String?): PublishedAt? {
        return dateString?.let { PublishedAt(it) }
    }
}