package com.worldvisionsoft.personalnetworktree.ui.screens.contact

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worldvisionsoft.personalnetworktree.R
import com.worldvisionsoft.personalnetworktree.data.model.Interaction
import com.worldvisionsoft.personalnetworktree.data.model.InteractionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInteractionScreen(
    contactId: String,
    contactName: String = "",
    onBackClick: () -> Unit = {},
    onSaved: () -> Unit = {},
    viewModel: ContactViewModel = viewModel(),
    isReminderMode: Boolean = false
) {
    val context = LocalContext.current
    val contactViewModel: ContactViewModel = viewModel(
        factory = remember(context) {
            object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return ContactViewModel(context) as T
                }
            }
        }
    )

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(InteractionType.MEETING) }
    var showTypeDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var setReminder by remember { mutableStateOf(isReminderMode) }
    var reminderDateTime by remember { mutableStateOf(System.currentTimeMillis() + 86400000) } // Default: tomorrow
    var selectedContactId by remember { mutableStateOf(contactId) }
    var selectedContactName by remember { mutableStateOf(contactName) }
    var showContactPicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dateTimeFormat = remember { SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()) }

    // Observe ViewModel state for success/error messages
    val uiState by contactViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // React to success: show snackbar and navigate back
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            if (message == "reminder_saved") {
                // Open Calendar app with the reminder details
                val reminder = com.worldvisionsoft.personalnetworktree.data.model.Reminder(
                    contactId = selectedContactId,
                    contactName = selectedContactName,
                    title = title,
                    description = description,
                    location = location,
                    interactionType = selectedType,
                    reminderDateTime = reminderDateTime
                )
                com.worldvisionsoft.personalnetworktree.util.ReminderScheduler.schedule(context, reminder)
                snackbarHostState.showSnackbar(context.getString(R.string.reminder_saved_message))
            } else {
                snackbarHostState.showSnackbar(message)
            }
            contactViewModel.clearSuccessMessage()
            onSaved()
        }
    }

    // React to error: show snackbar and clear error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            contactViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (setReminder || isReminderMode) stringResource(R.string.set_reminder) else stringResource(R.string.log_interaction_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (setReminder || isReminderMode) {
                                // Save reminder and open Calendar app
                                contactViewModel.addReminder(
                                    contactId = selectedContactId,
                                    contactName = selectedContactName,
                                    title = title,
                                    description = description,
                                    location = location,
                                    type = selectedType,
                                    reminderDateTime = reminderDateTime
                                )
                            } else {
                                // Save as interaction only
                                val interaction = Interaction(
                                    contactId = selectedContactId,
                                    type = selectedType,
                                    title = title,
                                    description = description,
                                    location = location,
                                    date = selectedDate
                                )
                                contactViewModel.addInteraction(interaction)
                            }
                        },
                        enabled = title.isNotEmpty() && selectedContactId.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Contact selection (shown when creating reminder from Reminders screen)
            if (isReminderMode || selectedContactId.isEmpty()) {
                OutlinedCard(
                    onClick = { showContactPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, null)
                            Column {
                                Text(
                                    text = stringResource(R.string.contact_required),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (selectedContactName.isNotEmpty())
                                        selectedContactName
                                    else
                                        stringResource(R.string.select_a_contact),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                }
            } else if (selectedContactName.isNotEmpty()) {
                // Contact name display for existing contact interaction
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = stringResource(R.string.interaction_with, selectedContactName),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            // Interaction Type
            OutlinedCard(
                onClick = { showTypeDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (selectedType) {
                                InteractionType.MEETING -> Icons.Default.People
                                InteractionType.CALL -> Icons.Default.Phone
                                InteractionType.EMAIL -> Icons.Default.Email
                                InteractionType.COFFEE -> Icons.Default.LocalCafe
                                InteractionType.EVENT -> Icons.Default.Event
                                InteractionType.NOTE -> Icons.Default.Note
                                InteractionType.OTHER -> Icons.Default.MoreHoriz
                            },
                            contentDescription = null
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.type_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedType.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.title_required)) },
                placeholder = { Text(stringResource(R.string.title_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Title, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                placeholder = { Text(stringResource(R.string.description_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 6
            )

            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text(stringResource(R.string.location)) },
                placeholder = { Text(stringResource(R.string.location_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Place, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Date
            OutlinedCard(
                onClick = { /* Date picker functionality */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, null)
                        Column {
                            Text(
                                text = stringResource(R.string.date),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dateFormat.format(Date(selectedDate)),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            }

            // Set Reminder Toggle
            if (!isReminderMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (setReminder)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Alarm,
                                null,
                                tint = if (setReminder)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.set_reminder),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.get_notified_message),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = setReminder,
                            onCheckedChange = { setReminder = it }
                        )
                    }
                }
            }

            // Reminder Date & Time (shown when reminder is enabled or in reminder mode)
            if (setReminder || isReminderMode) {
                OutlinedCard(
                    onClick = {
                        // Use default Android DatePickerDialog followed by TimePickerDialog
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = reminderDateTime }
                        val initYear = cal.get(java.util.Calendar.YEAR)
                        val initMonth = cal.get(java.util.Calendar.MONTH)
                        val initDay = cal.get(java.util.Calendar.DAY_OF_MONTH)
                        val initHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                        val initMinute = cal.get(java.util.Calendar.MINUTE)

                        val datePicker = android.app.DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                cal.set(java.util.Calendar.YEAR, year)
                                cal.set(java.util.Calendar.MONTH, month)
                                cal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)

                                val timePicker = android.app.TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        cal.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                                        cal.set(java.util.Calendar.MINUTE, minute)
                                        cal.set(java.util.Calendar.SECOND, 0)
                                        cal.set(java.util.Calendar.MILLISECOND, 0)
                                        reminderDateTime = cal.timeInMillis
                                    },
                                    initHour,
                                    initMinute,
                                    false // 12-hour view with AM/PM
                                )
                                timePicker.show()
                            },
                            initYear,
                            initMonth,
                            initDay
                        )
                        datePicker.show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Notifications, null)
                            Column {
                                Text(
                                    text = stringResource(R.string.reminder_date_time),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dateTimeFormat.format(Date(reminderDateTime)),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                }
            }
        }
    }

    // Type selection dialog
    if (showTypeDialog) {
        AlertDialog(
            onDismissRequest = { showTypeDialog = false },
            title = { Text(stringResource(R.string.select_interaction_type)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InteractionType.values().forEach { type ->
                        OutlinedCard(
                            onClick = {
                                selectedType = type
                                showTypeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (type) {
                                        InteractionType.MEETING -> Icons.Default.People
                                        InteractionType.CALL -> Icons.Default.Phone
                                        InteractionType.EMAIL -> Icons.Default.Email
                                        InteractionType.COFFEE -> Icons.Default.LocalCafe
                                        InteractionType.EVENT -> Icons.Default.Event
                                        InteractionType.NOTE -> Icons.Default.Note
                                        InteractionType.OTHER -> Icons.Default.MoreHoriz
                                    },
                                    contentDescription = null
                                )
                                Text(
                                    text = type.name.lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTypeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Contact Picker Dialog
    if (showContactPicker) {
        val repository = remember { com.worldvisionsoft.personalnetworktree.data.repository.ContactRepository(context) }
        val contacts by repository.contacts.collectAsState(initial = emptyList())

        AlertDialog(
            onDismissRequest = { showContactPicker = false },
            title = { Text(stringResource(R.string.select_contact)) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(contacts) { contact ->
                        OutlinedCard(
                            onClick = {
                                selectedContactId = contact.id
                                selectedContactName = contact.name
                                showContactPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, null)
                                Column {
                                    Text(
                                        text = contact.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (contact.company.isNotEmpty()) {
                                        Text(
                                            text = contact.company,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContactPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

