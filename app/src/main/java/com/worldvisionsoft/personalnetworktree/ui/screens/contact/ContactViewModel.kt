package com.worldvisionsoft.personalnetworktree.ui.screens.contact

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldvisionsoft.personalnetworktree.R
import com.worldvisionsoft.personalnetworktree.data.model.Contact
import com.worldvisionsoft.personalnetworktree.data.model.Interaction
import com.worldvisionsoft.personalnetworktree.data.model.InteractionType
import com.worldvisionsoft.personalnetworktree.data.model.Reminder
import com.worldvisionsoft.personalnetworktree.data.model.Tag
import com.worldvisionsoft.personalnetworktree.data.repository.ContactRepository
import com.worldvisionsoft.personalnetworktree.data.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContactUiState(
    val isLoading: Boolean = false,
    val contact: Contact? = null,
    val interactions: List<Interaction> = emptyList(),
    val availableTags: List<Tag> = emptyList(),
    val error: String? = null,
    val isSaved: Boolean = false,
    val successMessage: String? = null
)

class ContactViewModel(
    context: Context
) : ViewModel() {

    private val appContext: Context = context.applicationContext
    private val repository = ContactRepository(appContext)
    private val reminderRepository = ReminderRepository(appContext)
    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    init {
        loadTags()
    }

    private fun loadTags() {
        viewModelScope.launch {
            repository.getAllTags().collect { tags ->
                _uiState.value = _uiState.value.copy(availableTags = tags)
            }
        }
    }

    fun loadContact(contactId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getContact(contactId).collect { contact ->
                if (contact != null) {
                    loadInteractions(contactId)
                    _uiState.value = _uiState.value.copy(
                        contact = contact,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadInteractions(contactId: String) {
        viewModelScope.launch {
            repository.getInteractionsForContact(contactId).collect { interactions ->
                _uiState.value = _uiState.value.copy(interactions = interactions)
            }
        }
    }

    fun saveContact(contact: Contact, photoUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = if (contact.id.isEmpty()) {
                repository.addContact(contact, photoUri)
            } else {
                repository.updateContact(contact, photoUri)
            }

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSaved = true,
                        contact = it,
                        successMessage = if (contact.id.isEmpty())
                            appContext.getString(R.string.contact_added_successfully)
                        else
                            appContext.getString(R.string.contact_updated_successfully)
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: appContext.getString(R.string.failed_to_save_contact)
                    )
                }
            )
        }
    }

    fun addInteraction(interaction: Interaction) {
        viewModelScope.launch {
            val result = repository.addInteraction(interaction)
            result.fold(
                onSuccess = {
                    // Interactions will be reloaded via Flow
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: appContext.getString(R.string.failed_to_add_interaction)
                    )
                }
            )
        }
    }

    fun addReminder(
        contactId: String,
        contactName: String,
        title: String,
        description: String,
        location: String,
        type: InteractionType,
        reminderDateTime: Long
    ) {
        viewModelScope.launch {
            val reminder = Reminder(
                contactId = contactId,
                contactName = contactName,
                title = title,
                description = description,
                location = location,
                interactionType = type,
                reminderDateTime = reminderDateTime
            )

            val result = reminderRepository.addReminder(reminder)
            result.fold(
                onSuccess = {
                    // Success - the UI will handle opening the Calendar app
                    _uiState.value = _uiState.value.copy(
                        successMessage = "reminder_saved", // Signal to UI
                        contact = _uiState.value.contact?.copy() // Trigger state change
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: appContext.getString(R.string.failed_to_set_reminder)
                    )
                }
            )
        }
    }

    fun deleteContact(contactId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteContact(contactId)
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: appContext.getString(R.string.failed_to_delete_contact)
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun resetSavedState() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}

