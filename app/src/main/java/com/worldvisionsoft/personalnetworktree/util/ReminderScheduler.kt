package com.worldvisionsoft.personalnetworktree.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.worldvisionsoft.personalnetworktree.data.model.Reminder

/**
 * ReminderScheduler - Opens the system Calendar app to create reminder events
 * No permissions required - delegates alarm/notification handling to the Calendar app
 */
object ReminderScheduler {

    /**
     * Opens the Calendar app with pre-filled event details
     * The user can review and save the event with their preferred calendar settings
     */
    fun schedule(context: Context, reminder: Reminder) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, reminder.title)
            putExtra(CalendarContract.Events.DESCRIPTION,
                buildString {
                    append("Contact: ${reminder.contactName}")
                    if (reminder.description.isNotEmpty()) {
                        append("\n\n${reminder.description}")
                    }
                }
            )
            if (reminder.location.isNotEmpty()) {
                putExtra(CalendarContract.Events.EVENT_LOCATION, reminder.location)
            }
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, reminder.reminderDateTime)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, reminder.reminderDateTime + (60 * 60 * 1000)) // 1 hour duration
            putExtra(CalendarContract.Events.HAS_ALARM, true)
            putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
            android.util.Log.d("ReminderScheduler", "Opened Calendar app to add reminder: ${reminder.title}")
        } catch (e: Exception) {
            android.util.Log.e("ReminderScheduler", "Failed to open Calendar app", e)
        }
    }
}
