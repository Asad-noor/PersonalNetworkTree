package com.worldvisionsoft.personalnetworktree.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.worldvisionsoft.personalnetworktree.R
import com.worldvisionsoft.personalnetworktree.data.model.Reminder
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReminderRepository(private val context: Context? = null) {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private companion object {
        private const val TAG = "ReminderRepository"
    }

    private fun getUserId(): String = auth.currentUser?.uid ?: ""

    private fun getRemindersRef() = database.getReference("users/${getUserId()}/reminders")

    val reminders: Flow<List<Reminder>> = callbackFlow {
        val userId = getUserId()
        if (userId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val remindersList = mutableListOf<Reminder>()
                for (childSnapshot in snapshot.children) {
                    try {
                        val reminder = childSnapshot.getValue(Reminder::class.java)
                        reminder?.let { remindersList.add(it.copy(userId = userId)) }
                    } catch (e: Exception) {
                        context?.let {
                            Log.e(TAG, it.getString(R.string.log_error_parsing_reminder), e)
                        } ?: Log.e(TAG, "Error parsing reminder", e)
                    }
                }
                // Sort by reminder date time (upcoming first)
                remindersList.sortBy { it.reminderDateTime }
                trySend(remindersList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        getRemindersRef().addValueEventListener(listener)

        awaitClose {
            getRemindersRef().removeEventListener(listener)
        }
    }

    suspend fun addReminder(reminder: Reminder): Result<Unit> {
        return try {
            val userId = getUserId()
            if (userId.isEmpty()) {
                val errorMessage = context?.getString(R.string.error_user_not_logged_in)
                    ?: "User not logged in"
                return Result.failure(Exception(errorMessage))
            }

            val reminderWithUser = reminder.copy(userId = userId)
            getRemindersRef().child(reminder.id).setValue(reminderWithUser).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return try {
            getRemindersRef().child(reminderId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsCompleted(reminderId: String): Result<Unit> {
        return try {
            getRemindersRef().child(reminderId).child("isCompleted").setValue(true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
