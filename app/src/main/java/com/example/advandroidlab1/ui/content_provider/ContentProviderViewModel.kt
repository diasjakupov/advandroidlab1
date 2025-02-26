package com.example.advandroidlab1.ui.content_provider

import android.app.Application
import android.content.ContentUris
import android.icu.util.Calendar
import android.provider.CalendarContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.advandroidlab1.ui.content_provider.models.CalendarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ContentProviderViewModel(application: Application) : AndroidViewModel(application) {

    private val _eventsFlow = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val eventsFlow: StateFlow<List<CalendarEvent>> = _eventsFlow

    fun fetchUpcomingEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            val eventsList = mutableListOf<CalendarEvent>()
            val currentTime = Calendar.getInstance().timeInMillis
            val futureTime = currentTime + TimeUnit.DAYS.toMillis(7) // Next 7 days

            // Build the URI for instances between currentTime and futureTime.
            val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, currentTime)
            ContentUris.appendId(builder, futureTime)
            val uri = builder.build()

            val projection = arrayOf(
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.TITLE
            )


            // Query the Calendar Provider for upcoming events.
            val contentResolver = getApplication<Application>().contentResolver
            contentResolver.query(
                uri,
                projection,
                null,
                null,
                CalendarContract.Instances.BEGIN + " ASC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val eventId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
                    )
                    val begin = cursor.getLong(
                        cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                    )
                    val end = cursor.getLong(
                        cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
                    )
                    val title = cursor.getString(
                        cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                    )
                    eventsList.add(CalendarEvent(eventId, title, begin, end))
                }
            }
            _eventsFlow.value = eventsList
        }
    }
}