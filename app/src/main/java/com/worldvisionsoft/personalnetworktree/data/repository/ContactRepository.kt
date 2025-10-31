package com.worldvisionsoft.personalnetworktree.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.worldvisionsoft.personalnetworktree.data.model.Contact
import com.worldvisionsoft.personalnetworktree.data.model.Interaction
import com.worldvisionsoft.personalnetworktree.data.model.Tag
import com.worldvisionsoft.personalnetworktree.util.ImageUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ContactRepository(private val context: Context? = null) {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private fun getUserId(): String {
        return auth.currentUser?.uid ?: throw Exception("User not logged in")
    }

    // Firebase Database References
    private fun getContactsRef() = database.getReference("users/${getUserId()}/contacts")
    private fun getInteractionsRef() = database.getReference("users/${getUserId()}/interactions")
    private fun getTagsRef() = database.getReference("users/${getUserId()}/tags")

    // Contact operations
    val contacts: Flow<List<Contact>> = callbackFlow {
        val contactsRef = getContactsRef()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contactsList = mutableListOf<Contact>()
                for (childSnapshot in snapshot.children) {
                    childSnapshot.getValue(Contact::class.java)?.let { contact ->
                        contactsList.add(contact)
                    }
                }
                trySend(contactsList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        contactsRef.addValueEventListener(listener)
        awaitClose { contactsRef.removeEventListener(listener) }
    }

    suspend fun addContact(contact: Contact, photoUri: Uri?): Result<Contact> {
        return try {
            val userId = getUserId()
            val contactId = contact.id.ifEmpty { UUID.randomUUID().toString() }

            // Copy the photo to internal storage so it persists after app restart
            val photoUrl = if (photoUri != null && context != null) {
                ImageUtils.copyImageToInternalStorage(context, photoUri, contactId) ?: ""
            } else {
                ""
            }

            val newContact = contact.copy(
                id = contactId,
                userId = userId,
                photoUrl = photoUrl,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            getContactsRef().child(contactId).setValue(newContact).await()
            Result.success(newContact)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateContact(contact: Contact, photoUri: Uri?): Result<Contact> {
        return try {
            // Copy new photo to internal storage if provided
            val photoUrl = if (photoUri != null && context != null) {
                // Delete old photo if exists
                if (contact.photoUrl.isNotEmpty()) {
                    ImageUtils.deleteContactPhoto(context, contact.photoUrl)
                }
                // Copy new photo to internal storage
                ImageUtils.copyImageToInternalStorage(context, photoUri, contact.id) ?: contact.photoUrl
            } else {
                contact.photoUrl
            }

            val updatedContact = contact.copy(
                photoUrl = photoUrl,
                updatedAt = System.currentTimeMillis()
            )

            getContactsRef().child(contact.id).setValue(updatedContact).await()
            Result.success(updatedContact)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteContact(contactId: String): Result<Unit> {
        return try {
            // Get contact to retrieve photo path
            val contactSnapshot = getContactsRef().child(contactId).get().await()
            val contact = contactSnapshot.getValue(Contact::class.java)

            // Delete photo from internal storage if exists
            if (contact != null && context != null) {
                ImageUtils.deleteContactPhoto(context, contact.photoUrl)
            }

            // Delete contact from database
            getContactsRef().child(contactId).removeValue().await()

            // Delete interactions
            val interactionsSnapshot = getInteractionsRef()
                .orderByChild("contactId")
                .equalTo(contactId)
                .get()
                .await()

            for (snapshot in interactionsSnapshot.children) {
                snapshot.ref.removeValue().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getContact(contactId: String): Flow<Contact?> = callbackFlow {
        val contactRef = getContactsRef().child(contactId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contact = snapshot.getValue(Contact::class.java)
                trySend(contact)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        contactRef.addValueEventListener(listener)
        awaitClose { contactRef.removeEventListener(listener) }
    }

    fun getContactsByTag(tag: String): Flow<List<Contact>> = callbackFlow {
        val contactsRef = getContactsRef()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contactsList = mutableListOf<Contact>()
                for (childSnapshot in snapshot.children) {
                    childSnapshot.getValue(Contact::class.java)?.let { contact ->
                        if (contact.tags.contains(tag)) {
                            contactsList.add(contact)
                        }
                    }
                }
                trySend(contactsList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        contactsRef.addValueEventListener(listener)
        awaitClose { contactsRef.removeEventListener(listener) }
    }

    // Interaction operations
    val interactions: Flow<List<Interaction>> = callbackFlow {
        val interactionsRef = getInteractionsRef()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val interactionsList = mutableListOf<Interaction>()
                for (childSnapshot in snapshot.children) {
                    childSnapshot.getValue(Interaction::class.java)?.let { interaction ->
                        interactionsList.add(interaction)
                    }
                }
                trySend(interactionsList.sortedByDescending { it.date })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        interactionsRef.addValueEventListener(listener)
        awaitClose { interactionsRef.removeEventListener(listener) }
    }

    suspend fun addInteraction(interaction: Interaction): Result<Interaction> {
        return try {
            val userId = getUserId()
            val interactionId = interaction.id.ifEmpty { UUID.randomUUID().toString() }
            val newInteraction = interaction.copy(
                id = interactionId,
                userId = userId,
                createdAt = System.currentTimeMillis()
            )

            getInteractionsRef().child(interactionId).setValue(newInteraction).await()
            Result.success(newInteraction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getInteractionsForContact(contactId: String): Flow<List<Interaction>> = callbackFlow {
        val interactionsRef = getInteractionsRef()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val interactionsList = mutableListOf<Interaction>()
                for (childSnapshot in snapshot.children) {
                    childSnapshot.getValue(Interaction::class.java)?.let { interaction ->
                        if (interaction.contactId == contactId) {
                            interactionsList.add(interaction)
                        }
                    }
                }
                trySend(interactionsList.sortedByDescending { it.date })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        interactionsRef.addValueEventListener(listener)
        awaitClose { interactionsRef.removeEventListener(listener) }
    }

    // Tag operations
    val tags: Flow<List<Tag>> = callbackFlow {
        val tagsRef = getTagsRef()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tagsList = mutableListOf<Tag>()
                for (childSnapshot in snapshot.children) {
                    childSnapshot.getValue(Tag::class.java)?.let { tag ->
                        tagsList.add(tag)
                    }
                }
                // Add default tags if none exist
                if (tagsList.isEmpty()) {
                    tagsList.addAll(getDefaultTags())
                }
                trySend(tagsList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        tagsRef.addValueEventListener(listener)
        awaitClose { tagsRef.removeEventListener(listener) }
    }

    private fun getDefaultTags(): List<Tag> {
        return listOf(
            Tag(id = "1", name = "Close Friend", color = "#FF6B6B", userId = getUserId()),
            Tag(id = "2", name = "Family", color = "#4ECDC4", userId = getUserId()),
            Tag(id = "3", name = "Classmate", color = "#45B7D1", userId = getUserId()),
            Tag(id = "4", name = "Colleague", color = "#FFA07A", userId = getUserId()),
            Tag(id = "5", name = "Business", color = "#96CEB4", userId = getUserId()),
            Tag(id = "6", name = "Mentor", color = "#FFEAA7", userId = getUserId()),
            Tag(id = "7", name = "VC Investor", color = "#6200EE", userId = getUserId()),
            Tag(id = "8", name = "Entrepreneur", color = "#03DAC5", userId = getUserId())
        )
    }

    suspend fun addTag(tag: Tag): Result<Tag> {
        return try {
            val userId = getUserId()
            val tagId = tag.id.ifEmpty { UUID.randomUUID().toString() }
            val newTag = tag.copy(id = tagId, userId = userId)

            getTagsRef().child(tagId).setValue(newTag).await()
            Result.success(newTag)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllTags(): Flow<List<Tag>> = tags
}

