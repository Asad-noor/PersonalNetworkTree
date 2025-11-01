package com.worldvisionsoft.personalnetworktree.ui.screens.contact

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.worldvisionsoft.personalnetworktree.R
import com.worldvisionsoft.personalnetworktree.data.model.Contact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContactScreen(
    contactId: String? = null,
    onBackClick: () -> Unit = {},
    onSaved: () -> Unit = {},
    viewModel: ContactViewModel = viewModel()
) {
    val context = LocalContext.current

    // Create ViewModel with context
    val contextViewModel = remember { ContactViewModel(context) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var relationshipLevel by remember { mutableStateOf(3) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showLevelDialog by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
    }

    val uiState by contextViewModel.uiState.collectAsState()

    // Load existing contact if editing
    LaunchedEffect(contactId) {
        if (contactId != null) {
            contextViewModel.loadContact(contactId)
        }
    }

    // Populate fields when contact is loaded
    LaunchedEffect(uiState.contact) {
        uiState.contact?.let { contact ->
            name = contact.name
            email = contact.email
            phone = contact.phone
            company = contact.company
            position = contact.position
            notes = contact.notes
            selectedTags = contact.tags
            relationshipLevel = contact.relationshipLevel
            // Load existing photo if available
            if (contact.photoUrl.isNotEmpty()) {
                photoUri = Uri.parse(contact.photoUrl)
            }
        }
    }

    // Handle save success
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            uiState.successMessage?.let { message ->
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
            contextViewModel.resetSavedState()
            onSaved()
        }
    }

    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            contextViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (contactId == null) stringResource(R.string.add_contact_title) else stringResource(R.string.edit_contact_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val contact = Contact(
                                id = contactId ?: "",
                                name = name,
                                email = email,
                                phone = phone,
                                company = company,
                                position = position,
                                notes = notes,
                                tags = selectedTags,
                                relationshipLevel = relationshipLevel,
                                photoUrl = "" // Will be set by repository
                            )
                            contextViewModel.saveContact(contact, photoUri)
                        },
                        enabled = name.isNotBlank() && !uiState.isLoading
                    ) {
                        Text(stringResource(R.string.save_button))
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
            // Photo section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
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
                        )
                        .clickable {
                            // Launch photo picker
                            photoPickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        // Display selected photo
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else if (name.isNotEmpty()) {
                        // Display first letter of name
                        Text(
                            text = name.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        // Display person icon
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.tap_to_add_photo),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // Basic Info
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name_required)) },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email_label)) },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(R.string.phone_label)) },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = company,
                onValueChange = { company = it },
                label = { Text(stringResource(R.string.company_label)) },
                leadingIcon = { Icon(Icons.Default.Business, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = position,
                onValueChange = { position = it },
                label = { Text(stringResource(R.string.position_label)) },
                leadingIcon = { Icon(Icons.Default.Work, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Relationship Level Section
            Text(
                text = stringResource(R.string.relationship_level),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                onClick = { showLevelDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = getLevelLabel(relationshipLevel),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getLevelDescription(relationshipLevel),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            }

            // Tags Section
            Text(
                text = stringResource(R.string.tags_and_categories),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Selected tags
            if (selectedTags.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedTags.forEach { tag ->
                        FilterChip(
                            selected = true,
                            onClick = { selectedTags = selectedTags - tag },
                            label = { Text(tag) },
                            trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { showTagDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add_tags))
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }

    // Tag selection dialog
    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text(stringResource(R.string.select_tags)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.availableTags.forEach { tag ->
                        FilterChip(
                            selected = selectedTags.contains(tag.name),
                            onClick = {
                                selectedTags = if (selectedTags.contains(tag.name)) {
                                    selectedTags - tag.name
                                } else {
                                    selectedTags + tag.name
                                }
                            },
                            label = { Text(tag.name) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text(stringResource(R.string.done))
                }
            }
        )
    }

    // Relationship level selection dialog
    if (showLevelDialog) {
        AlertDialog(
            onDismissRequest = { showLevelDialog = false },
            title = { Text(stringResource(R.string.select_relationship_level)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (level in 1..5) {
                        Card(
                            onClick = {
                                relationshipLevel = level
                                showLevelDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.level_format, level, getLevelLabel(level)),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (relationshipLevel == level) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = getLevelDescription(level),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLevelDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun getLevelLabel(level: Int): String {
    return when (level) {
        1 -> stringResource(R.string.level_1)
        2 -> stringResource(R.string.level_2)
        3 -> stringResource(R.string.level_3)
        4 -> stringResource(R.string.level_4)
        5 -> stringResource(R.string.level_5)
        else -> stringResource(R.string.unknown_level)
    }
}

@Composable
private fun getLevelDescription(level: Int): String {
    return when (level) {
        1 -> stringResource(R.string.level_1_desc)
        2 -> stringResource(R.string.level_2_desc)
        3 -> stringResource(R.string.level_3_desc)
        4 -> stringResource(R.string.level_4_desc)
        5 -> stringResource(R.string.level_5_desc)
        else -> ""
    }
}

