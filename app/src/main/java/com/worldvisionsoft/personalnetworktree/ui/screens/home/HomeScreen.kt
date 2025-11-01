package com.worldvisionsoft.personalnetworktree.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.worldvisionsoft.personalnetworktree.ui.screens.dashboard.DashboardScreen
import com.worldvisionsoft.personalnetworktree.ui.theme.PersonalNetworkTreeTheme

@Composable
fun HomeScreen(
    onSignOut: () -> Unit = {},
    onAddContact: () -> Unit = {},
    onContactClick: (String) -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onAddReminder: () -> Unit = {}
) {
    DashboardScreen(
        onSignOut = onSignOut,
        onAddContact = onAddContact,
        onContactClick = onContactClick,
        onPrivacyPolicyClick = onPrivacyPolicyClick,
        onAddReminder = onAddReminder
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PersonalNetworkTreeTheme {
        HomeScreen()
    }
}

