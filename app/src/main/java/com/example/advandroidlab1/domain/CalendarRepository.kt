package com.example.advandroidlab1.domain


import com.example.advandroidlab1.ui.content_provider.models.CalendarEvent

interface CalendarRepository {
    suspend fun getUpcomingEvents(): List<CalendarEvent>
}
