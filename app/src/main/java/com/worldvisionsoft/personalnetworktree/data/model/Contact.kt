package com.worldvisionsoft.personalnetworktree.data.model

import java.util.*

data class Contact(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "", // Firebase user ID
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val company: String = "",
    val position: String = "",
    val photoUrl: String = "",
    val tags: List<String> = emptyList(), // e.g., "VC Investor", "Tech Startup"
    val relationshipLevel: Int = 3, // 1=Close Friends/Family, 2=Classmates, 3=Batch Mates, 4=Colleagues, 5=Friends of Friends
    val notes: String = "",
    val connectedTo: List<String> = emptyList(), // IDs of other contacts
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class RelationshipLevel(val level: Int, val label: String, val description: String) {
    CLOSE_CIRCLE(1, "Close Friends & Family", "Your strongest bonds"),
    CLASSMATES(2, "Classmates & Close Colleagues", "Regular interaction"),
    BATCH_MATES(3, "Batch Mates & Professional Network", "Occasional interaction"),
    COLLEAGUES(4, "Colleagues & Business Contacts", "Professional relationships"),
    EXTENDED_NETWORK(5, "Friends of Friends & Acquaintances", "Extended network")
}

data class Interaction(
    val id: String = UUID.randomUUID().toString(),
    val contactId: String = "",
    val userId: String = "",
    val type: InteractionType = InteractionType.MEETING,
    val title: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val location: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class InteractionType {
    MEETING,
    CALL,
    EMAIL,
    COFFEE,
    EVENT,
    NOTE,
    OTHER
}

data class Tag(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val color: String = "#6200EE", // Hex color
    val userId: String = ""
)

