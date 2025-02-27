package com.example.advandroidlab1.data.repository.calendar

import android.app.Application
import android.content.ContentUris
import android.icu.util.Calendar
import android.provider.CalendarContract
import com.example.advandroidlab1.domain.CalendarRepository
import com.example.advandroidlab1.ui.content_provider.models.CalendarEvent
import java.util.concurrent.TimeUnit

class DefaultCalendarRepository(private val application: Application) : CalendarRepository {
    override suspend fun getUpcomingEvents(): List<CalendarEvent> {
        val eventsList = mutableListOf<CalendarEvent>()
        val currentTime = Calendar.getInstance().timeInMillis
        val futureTime = currentTime + TimeUnit.DAYS.toMillis(7)

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, currentTime)
        ContentUris.appendId(builder, futureTime)
        val uri = builder.build()

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.STATUS
        )

        val contentResolver = application.contentResolver
        contentResolver.query(
            uri,
            projection,
            null,
            null,
            CalendarContract.Instances.BEGIN + " ASC"
        )
            ?.use { cursor ->
                while (cursor.moveToNext()) {
                    val eventId =
                        cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID))
                    val begin =
                        cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN))
                    val end =
                        cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Instances.END))
                    val title =
                        cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE))
                    val location =
                        cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION))
                    val description =
                        cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
                    val statusInt =
                        cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Events.STATUS))
                    val statusText = when (statusInt) {
                        CalendarContract.Events.STATUS_CONFIRMED -> "Confirmed"
                        CalendarContract.Events.STATUS_TENTATIVE -> "Tentative"
                        CalendarContract.Events.STATUS_CANCELED -> "Canceled"
                        else -> "Unknown"
                    }
                    eventsList.add(
                        CalendarEvent(
                            eventId,
                            title,
                            begin,
                            end,
                            location,
                            description,
                            statusText
                        )
                    )
                }
            }
        return eventsList
    }
}
