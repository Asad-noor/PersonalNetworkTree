package com.worldvisionsoft.personalnetworktree.ui.screens.dashboard
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.worldvisionsoft.personalnetworktree.R
import com.worldvisionsoft.personalnetworktree.data.model.Reminder
import com.worldvisionsoft.personalnetworktree.data.model.InteractionType
import com.worldvisionsoft.personalnetworktree.data.repository.ReminderRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
@Composable
fun RemindersView(
    onAddReminderClick: () -> Unit = {}
) {
    val repository = remember { ReminderRepository() }
    val reminders by repository.reminders.collectAsState(initial = emptyList())
    val upcomingReminders = remember(reminders) {
        reminders.filter { !it.isCompleted && it.reminderDateTime >= System.currentTimeMillis() }
    }
    val pastReminders = remember(reminders) {
        reminders.filter { !it.isCompleted && it.reminderDateTime < System.currentTimeMillis() }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (reminders.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.no_reminders),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.set_reminders_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Past due reminders
                if (pastReminders.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.overdue),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(pastReminders) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            repository = repository,
                            isOverdue = true
                        )
                    }
                }
                // Upcoming reminders
                if (upcomingReminders.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.upcoming),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(upcomingReminders) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            repository = repository,
                            isOverdue = false
                        )
                    }
                }
            }
        }
        // Floating Action Button
        FloatingActionButton(
            onClick = onAddReminderClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_reminder)
            )
        }
    }
}
@Composable
fun ReminderItem(
    reminder: Reminder,
    repository: ReminderRepository,
    isOverdue: Boolean
) {
    val dateTimeFormat = remember { SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (reminder.interactionType) {
                        InteractionType.MEETING -> Icons.Default.People
                        InteractionType.CALL -> Icons.Default.Phone
                        InteractionType.EMAIL -> Icons.Default.Email
                        InteractionType.COFFEE -> Icons.Default.LocalCafe
                        InteractionType.EVENT -> Icons.Default.Event
                        InteractionType.NOTE -> Icons.Default.Note
                        InteractionType.OTHER -> Icons.Default.MoreHoriz
                    },
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (isOverdue)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = reminder.contactName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isOverdue)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateTimeFormat.format(Date(reminder.reminderDateTime)),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOverdue)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = { showCompleteDialog = true }) {
                    Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.mark_complete))
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
            if (reminder.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reminder.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (reminder.location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = reminder.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_reminder_title)) },
            text = { Text(stringResource(R.string.delete_reminder_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.deleteReminder(reminder.id)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text(stringResource(R.string.mark_complete_title)) },
            text = { Text(stringResource(R.string.mark_complete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.markAsCompleted(reminder.id)
                        }
                        showCompleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.complete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
