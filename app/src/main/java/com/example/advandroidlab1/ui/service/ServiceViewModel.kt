package com.example.advandroidlab1.ui.service

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.advandroidlab1.data.repository.music.DefaultAudioRepository
import com.example.advandroidlab1.domain.AudioFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServiceViewModel(application: Application) : AndroidViewModel(application) {

    private val audioRepository = DefaultAudioRepository(application)

    private val _audioFiles = MutableStateFlow<List<AudioFile>>(emptyList())
    val audioFiles: StateFlow<List<AudioFile>> = _audioFiles

    fun loadFiles() = viewModelScope.launch(Dispatchers.IO) {
       _audioFiles.emit(audioRepository.getAllAudioFiles())
    }
}

