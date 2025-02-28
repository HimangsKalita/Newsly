package com.himangskalita.newsly.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PublishedAt(
    val formattedDate: String
): Parcelable