package com.worldvisionsoft.personalnetworktree.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.worldvisionsoft.personalnetworktree.ui.theme.PersonalNetworkTreeTheme

enum class NavigationItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    NETWORK("Network", Icons.Default.AccountTree),
    CONTACTS("Contacts", Icons.Default.People),
    REMINDERS("Reminders", Icons.Default.Notifications),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onSignOut: () -> Unit = {},
    onAddContact: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onContactClick: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(NavigationItem.NETWORK) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Network Tree") },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
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
                        onClick = { selectedTab = item },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddContact,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Contact"
                )
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
                    onContactClick = onContactClick
                )
                NavigationItem.REMINDERS -> RemindersView()
                NavigationItem.SETTINGS -> SettingsView(onSignOut = onSignOut)
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

