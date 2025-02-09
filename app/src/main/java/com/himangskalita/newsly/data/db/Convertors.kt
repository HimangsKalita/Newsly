package com.himangskalita.newsly.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.himangskalita.newsly.data.models.Source


class Convertors {

    @TypeConverter
    fun fromSource(source: Source?): String? {

        return source?.let {

            Gson().toJson(it)
        }
    }

    @TypeConverter
    fun toSource(sourceString: String?): Source? {

        return sourceString?.let {

            Gson().fromJson(sourceString, Source::class.java)
        }
    }
}