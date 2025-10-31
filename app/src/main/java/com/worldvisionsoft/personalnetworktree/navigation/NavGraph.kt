package com.worldvisionsoft.personalnetworktree.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.worldvisionsoft.personalnetworktree.ui.screens.auth.AuthScreen
import com.worldvisionsoft.personalnetworktree.ui.screens.home.HomeScreen
import com.worldvisionsoft.personalnetworktree.ui.screens.splash.SplashScreen
import com.worldvisionsoft.personalnetworktree.ui.screens.search.SearchScreen
import com.worldvisionsoft.personalnetworktree.ui.screens.contact.AddEditContactScreen
import com.worldvisionsoft.personalnetworktree.ui.screens.contact.ContactDetailScreen
import com.worldvisionsoft.personalnetworktree.ui.screens.contact.AddInteractionScreen
import com.worldvisionsoft.personalnetworktree.ui.screens.PrivacyPolicyScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable(route = "splash") {
            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate("auth") {
                        popUpTo("splash") {
                            inclusive = true
                        }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(route = "auth") {
            AuthScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(route = "home") {
            HomeScreen(
                onSignOut = {
                    navController.navigate("auth") {
                        popUpTo("home") {
                            inclusive = true
                        }
                    }
                },
                onAddContact = {
                    navController.navigate("addContact")
                },
                onContactClick = { contactId ->
                    navController.navigate("contactDetail/$contactId")
                },
                onSearchClick = {
                    navController.navigate("search")
                },
                onPrivacyPolicyClick = {
                    navController.navigate("privacyPolicy")
                },
                onAddReminder = {
                    navController.navigate("addReminder")
                }
            )
        }

        composable(route = "search") {
            SearchScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = "addContact") {
            AddEditContactScreen(
                contactId = null,
                onBackClick = {
                    navController.popBackStack()
                },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = "contactDetail/{contactId}") { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
            ContactDetailScreen(
                contactId = contactId,
                onBackClick = {
                    navController.popBackStack()
                },
                onEditClick = {
                    navController.navigate("editContact/$contactId")
                },
                onAddInteraction = {
                    navController.navigate("addInteraction/$contactId")
                }
            )
        }

        composable(route = "editContact/{contactId}") { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
            AddEditContactScreen(
                contactId = contactId,
                onBackClick = {
                    navController.popBackStack()
                },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = "addInteraction/{contactId}") { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
            AddInteractionScreen(
                contactId = contactId,
                contactName = "", // Will be loaded from contact data
                onBackClick = {
                    navController.popBackStack()
                },
                onSaved = {
                    navController.popBackStack()
                },
                isReminderMode = false
            )
        }

        composable(route = "addReminder") {
            AddInteractionScreen(
                contactId = "",
                contactName = "",
                onBackClick = {
                    navController.popBackStack()
                },
                onSaved = {
                    navController.popBackStack()
                },
                isReminderMode = true
            )
        }

        composable(route = "privacyPolicy") {
            PrivacyPolicyScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

