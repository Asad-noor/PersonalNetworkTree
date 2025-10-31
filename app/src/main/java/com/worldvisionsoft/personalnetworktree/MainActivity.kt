package com.worldvisionsoft.personalnetworktree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.worldvisionsoft.personalnetworktree.navigation.NavGraph
import com.worldvisionsoft.personalnetworktree.ui.theme.PersonalNetworkTreeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalNetworkTreeTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
