package com.himangskalita.newsly.utils

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object DateUtils {

    private val publishedDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")
    private val outputDateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy")

    fun formatPublishedDate(publishedAt: String?): String {

        if (publishedAt.isNullOrEmpty()) return "(Date)"

        return try {
            val publishedDate = LocalDateTime.parse(publishedAt, publishedDateTimeFormatter)
            val currentDate = LocalDateTime.now().atZone(ZoneOffset.UTC).toLocalDateTime()

            val diff = Duration.between(publishedDate, currentDate)
            val diffDays = diff.toDays().toInt()
            val diffHours = diff.toHours().toInt()
            val diffMinutes = diff.toMinutes().toInt()

            when (diffDays) {
                0 -> when (diffHours) {
                    0 -> when {
                        diffMinutes == 0 -> "Moments ago"
                        diffMinutes <= 30 -> "$diffMinutes Minutes ago"
                        else -> "Today"
                    }
                    in 1..6 -> "$diffHours Hour${if (diffHours > 1) "s" else ""} ago"
                    else -> "Today"
                }
                1 -> "Yesterday"
                else -> publishedDate.format(outputDateTimeFormatter)
            }
        } catch (e: Exception) {

            Logger.d("PublishedDate: Message - ${e.message}, Cause - ${e.cause}")
            "(Date)"
        }
    }
}