package com.worldvisionsoft.personalnetworktree.ui.screens.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.worldvisionsoft.personalnetworktree.R
import com.worldvisionsoft.personalnetworktree.data.model.Interaction
import com.worldvisionsoft.personalnetworktree.data.model.InteractionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contactId: String,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onAddInteraction: () -> Unit = {}
) {
    val context = LocalContext.current
    val contextViewModel = remember { ContactViewModel(context) }
    val uiState by contextViewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(contactId) {
        contextViewModel.loadContact(contactId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.contact_details)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, "")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddInteraction,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(stringResource(R.string.log_interaction)) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                uiState.contact?.let { contact ->
                    // Profile Header
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (contact.photoUrl.isNotEmpty()) {
                                // Display contact photo - parse string to Uri
                                var imageLoadFailed by remember { mutableStateOf(false) }

                                if (!imageLoadFailed) {
                                    AsyncImage(
                                        model = contact.photoUrl.toUri(),
                                        contentDescription = contact.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        onError = { imageLoadFailed = true }
                                    )
                                } else {
                                    // Fallback when image fails to load
                                    Text(
                                        text = contact.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            } else {
                                // Display first letter of name
                                Text(
                                    text = contact.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (contact.position.isNotEmpty()) {
                            Text(
                                text = contact.position,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (contact.company.isNotEmpty()) {
                            Text(
                                text = contact.company,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Tags
            item {
                uiState.contact?.tags?.takeIf { it.isNotEmpty() }?.let { tags ->
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.tags),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                tags.forEach { tag ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(tag) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Contact Information
            item {
                uiState.contact?.let { contact ->
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.contact_information),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))

                            if (contact.email.isNotEmpty()) {
                                ContactInfoRow(
                                    icon = Icons.Default.Email,
                                    label = stringResource(R.string.label_email),
                                    value = contact.email
                                )
                                Spacer(Modifier.height(12.dp))
                            }

                            if (contact.phone.isNotEmpty()) {
                                ContactInfoRow(
                                    icon = Icons.Default.Phone,
                                    label = stringResource(R.string.label_phone),
                                    value = contact.phone
                                )
                                Spacer(Modifier.height(12.dp))
                            }

                            if (contact.notes.isNotEmpty()) {
                                ContactInfoRow(
                                    icon = Icons.Default.Description,
                                    label = stringResource(R.string.label_notes),
                                    value = contact.notes
                                )
                            }
                        }
                    }
                }
            }

            // Interactions Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.past_interactions, uiState.interactions.size),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (uiState.interactions.isEmpty()) {
                item {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.EventNote,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.no_interactions_yet),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.log_first_interaction),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.interactions) { interaction ->
                    InteractionCard(interaction)
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.delete_contact_question),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_contact_confirmation, uiState.contact?.name ?: "this contact"),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        contextViewModel.deleteContact(contactId) {
                            // On successful deletion, navigate back
                            onBackClick()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun ContactInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun InteractionCard(interaction: Interaction) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon based on interaction type
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (interaction.type) {
                            InteractionType.MEETING -> Icons.Default.People
                            InteractionType.CALL -> Icons.Default.Phone
                            InteractionType.EMAIL -> Icons.Default.Email
                            InteractionType.COFFEE -> Icons.Default.LocalCafe
                            InteractionType.EVENT -> Icons.Default.Event
                            InteractionType.NOTE -> Icons.AutoMirrored.Filled.Note
                            InteractionType.OTHER -> Icons.Default.MoreHoriz
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = interaction.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (interaction.description.isNotEmpty()) {
                    Text(
                        text = interaction.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = dateFormat.format(Date(interaction.date)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (interaction.location.isNotEmpty()) {
                        Text(
                            text = "• ${interaction.location}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
