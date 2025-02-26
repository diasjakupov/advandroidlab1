package com.example.advandroidlab1.data.repository.music

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.advandroidlab1.domain.AudioFile
import java.io.File


class DefaultAudioRepository(private val applicationContext: Context) {

    fun getAllAudioFiles(): List<AudioFile> {
        val audioFiles = mutableListOf<AudioFile>()
        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val cursor = applicationContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        if (cursor == null) {
            Log.e("DefaultAudioRepository", "Cursor is null")
            return emptyList()
        }
        cursor.use {
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (it.moveToNext()) {
                val path = it.getString(dataColumn).orEmpty()
                val title = it.getString(titleColumn) ?: "Unknown"
                val artist = it.getString(artistColumn) ?: "Unknown"
                val duration = it.getLong(durationColumn)
                val fileUri: Uri = FileProvider.getUriForFile(
                    applicationContext,
                    applicationContext.packageName + ".provider",
                    File(path)
                )
                Log.e("TEST", "title: $title")
                audioFiles.add(
                    AudioFile(
                        uri = fileUri,
                        title = title,
                        artist = artist,
                        duration = duration
                    )
                )
            }
        }
        return audioFiles
    }
}
