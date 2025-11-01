package com.worldvisionsoft.personalnetworktree.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import com.worldvisionsoft.personalnetworktree.R
import com.worldvisionsoft.personalnetworktree.data.model.Reminder

/**
 * ReminderScheduler - Opens the system Calendar app to create reminder events
 * No permissions required - delegates alarm/notification handling to the Calendar app
 */
object ReminderScheduler {

    private const val TAG = "ReminderScheduler"

    /**
     * Opens the Calendar app with pre-filled event details
     * The user can review and save the event with their preferred calendar settings
     */
    fun schedule(context: Context, reminder: Reminder) {
        var intent: Intent? = null

        try {
            intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, reminder.title)

                // Build description with contact and optional details
                val description = if (reminder.description.isNotEmpty()) {
                    context.getString(
                        R.string.reminder_description_format,
                        reminder.contactName,
                        reminder.description
                    )
                } else {
                    context.getString(
                        R.string.reminder_description_contact_only,
                        reminder.contactName
                    )
                }
                putExtra(CalendarContract.Events.DESCRIPTION, description)

                if (reminder.location.isNotEmpty()) {
                    putExtra(CalendarContract.Events.EVENT_LOCATION, reminder.location)
                }

                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, reminder.reminderDateTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, reminder.reminderDateTime + (60 * 60 * 1000)) // 1 hour duration
                putExtra(CalendarContract.Events.HAS_ALARM, true)
                putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)

            Log.d(TAG, context.getString(R.string.log_calendar_opened, reminder.title))

        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, context.getString(R.string.log_calendar_failed), e)
            Toast.makeText(
                context,
                context.getString(R.string.error_no_calendar_app),
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Log.e(TAG, context.getString(R.string.log_calendar_failed), e)
            Toast.makeText(
                context,
                context.getString(R.string.failed_to_open_calendar),
                Toast.LENGTH_LONG
            ).show()
        } finally {
            // Clean up intent resources if needed
            intent?.let {
                // Intent doesn't have streams to close, but we ensure proper cleanup
                // by nullifying the reference to help with garbage collection
            }
        }
    }
}
