package com.himangskalita.newsly.data.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.himangskalita.newsly.data.models.PublishedAt
import com.himangskalita.newsly.utils.Logger
import java.lang.reflect.Type
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class DateTimeDeserializer: JsonDeserializer<PublishedAt> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PublishedAt {

        val jsonString = json?.asString ?: return PublishedAt("(Date)")

         val publishedDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")
         val outputDateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy")

        try {
            val currentDateTime = LocalDateTime.now().atZone(ZoneOffset.UTC).toLocalDateTime()
            val publishedDateTime = LocalDateTime.parse(jsonString, publishedDateTimeFormatter)

            val diff = Duration.between(publishedDateTime, currentDateTime)

            val formattedDate = when {

                diff.toMinutes() < 1 -> "Now"
                diff.toMinutes() < 30 -> "${diff.toMinutes()} mins ago"
                diff.toMinutes() == 30L -> "30 mins ago"
                diff.toHours() == 1L -> "1 hours ago"
                diff.toHours() in 2L..6L -> "${diff.toHours()} hours ago"
                diff.toDays() == 0L -> "Today"
                diff.toDays() == 1L -> "Yesterday"
                else -> publishedDateTime.format(outputDateTimeFormatter)
            }

            return PublishedAt(formattedDate)

        }catch (e: Exception){

            Logger.d("PublishedDate: Message - ${e.message}, Cause - ${e.cause}")
            return PublishedAt("(Date)")
        }
    }
}