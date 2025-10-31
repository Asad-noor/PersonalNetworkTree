package com.worldvisionsoft.personalnetworktree.ui.screens.contact

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    viewModel: ContactViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(InteractionType.MEETING) }
    var showTypeDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Interaction") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val interaction = Interaction(
                                contactId = contactId,
                                type = selectedType,
                                title = title,
                                description = description,
                                location = location,
                                date = selectedDate
                            )
                            viewModel.addInteraction(interaction)
                            onSaved()
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("SAVE")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
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
            // Contact name
            if (contactName.isNotEmpty()) {
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
                            text = "Interaction with $contactName",
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
                                text = "Type",
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
                label = { Text("Title *") },
                placeholder = { Text("e.g., Had coffee, Project discussion") },
                leadingIcon = { Icon(Icons.Default.Title, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Discussed project X, next steps...") },
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
                label = { Text("Location") },
                placeholder = { Text("e.g., Starbucks Downtown") },
                leadingIcon = { Icon(Icons.Default.Place, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Date
            OutlinedCard(
                onClick = { /* TODO: Add date picker */ },
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
                                text = "Date",
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
        }
    }

    // Type selection dialog
    if (showTypeDialog) {
        AlertDialog(
            onDismissRequest = { showTypeDialog = false },
            title = { Text("Select Interaction Type") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Text("Cancel")
                }
            }
        )
    }
}

