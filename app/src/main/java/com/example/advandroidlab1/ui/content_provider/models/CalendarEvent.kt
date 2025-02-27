package com.example.advandroidlab1.ui.content_provider.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CalendarEvent(
    val id: Long,
    val title: String,
    val begin: Long,
    val end: Long,
    val location: String? = null,
    val description: String? = null,
    val status: String? = null
) : Parcelable

