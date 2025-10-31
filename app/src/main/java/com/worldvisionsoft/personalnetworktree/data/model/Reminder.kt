package com.worldvisionsoft.personalnetworktree.data.model

import java.util.*

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val contactId: String = "",
    val contactName: String = "",
    val title: String = "",
    val description: String = "",
    val reminderDateTime: Long = System.currentTimeMillis(),
    val interactionType: InteractionType = InteractionType.MEETING,
    val location: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

