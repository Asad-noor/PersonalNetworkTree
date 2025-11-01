package com.worldvisionsoft.personalnetworktree.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.worldvisionsoft.personalnetworktree.R
import com.worldvisionsoft.personalnetworktree.ui.theme.PersonalNetworkTreeTheme

enum class NavigationItem(
    val titleRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    NETWORK(R.string.network, Icons.Default.AccountTree),
    CONTACTS(R.string.contacts, Icons.Default.People),
    REMINDERS(R.string.reminders, Icons.Default.Notifications),
    SETTINGS(R.string.settings, Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onSignOut: () -> Unit = {},
    onAddContact: () -> Unit = {},
    onContactClick: (String) -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onAddReminder: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(NavigationItem.NETWORK) }
    var showSearchInContacts by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.personal_network_tree)) },
                actions = {
                    IconButton(onClick = {
                        // Switch to Contacts tab and show search
                        selectedTab = NavigationItem.CONTACTS
                        showSearchInContacts = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationItem.values().forEach { item ->
                    NavigationBarItem(
                        selected = selectedTab == item,
                        onClick = {
                            selectedTab = item
                            // Reset search visibility when switching tabs
                            if (item != NavigationItem.CONTACTS) {
                                showSearchInContacts = false
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(item.titleRes)
                            )
                        },
                        label = { Text(stringResource(item.titleRes)) }
                    )
                }
            }
        },
        floatingActionButton = {
            // Show FAB only on Network and Contacts tabs
            if (selectedTab == NavigationItem.NETWORK || selectedTab == NavigationItem.CONTACTS) {
                FloatingActionButton(
                    onClick = onAddContact,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_contact)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                NavigationItem.NETWORK -> NetworkGraphView(
                    currentUserEmail = currentUser?.email,
                    onNodeClick = onContactClick
                )
                NavigationItem.CONTACTS -> ContactsListView(
                    onContactClick = onContactClick,
                    showSearchInitially = showSearchInContacts
                )
                NavigationItem.REMINDERS -> RemindersView(
                    onAddReminderClick = onAddReminder
                )
                NavigationItem.SETTINGS -> SettingsView(
                    onSignOut = onSignOut,
                    onPrivacyPolicyClick = onPrivacyPolicyClick
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    PersonalNetworkTreeTheme {
        DashboardScreen()
    }
}

