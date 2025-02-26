package com.example.advandroidlab1.domain


import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioFile(
    val uri: Uri,
    val title: String,
    val artist: String,
    val duration: Long
) : Parcelable

