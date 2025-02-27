package com.example.advandroidlab1.ui.content_provider

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.advandroidlab1.data.repository.calendar.DefaultCalendarRepository
import com.example.advandroidlab1.domain.CalendarRepository
import com.example.advandroidlab1.ui.content_provider.models.CalendarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContentProviderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalendarRepository = DefaultCalendarRepository(application)

    private val _eventsFlow = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val eventsFlow: StateFlow<List<CalendarEvent>> = _eventsFlow

    fun fetchUpcomingEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            _eventsFlow.value = repository.getUpcomingEvents()
        }
    }
}