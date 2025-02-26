package com.example.advandroidlab1.ui.deeplinks

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeeplinksViewModel : ViewModel() {

    private val _uri = MutableStateFlow<Uri?>(null)
    val uri: StateFlow<Uri?> get() = _uri


    fun setUri(uri: Uri?) = viewModelScope.launch{
        _uri.emit(uri)
    }
}