package de.polyfish0.adolightstick.routes

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainRoute(navController: NavController) {
    val navBarController = rememberNavController()
    val startDestination = Destination.COLORS
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    navBarController.enableOnBackPressed(false)

    Scaffold(
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                Destination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            navBarController.navigate(destination.route) {
                                popUpTo(navBarController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            selectedDestination = index
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = {
                            Text(destination.label)
                        }
                    )
                }
            }
        }
    ) { contentPadding ->
        AppNavHost(navBarController, startDestination, modifier = Modifier.padding(contentPadding))
    }
}

@SuppressLint("MissingPermission")
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.route
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.COLORS -> ColorScreen()
                    Destination.SETTINGS -> AppSettingsScreen()
                }
            }
        }
    }
}

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    COLORS("colors", "Colors", Icons.Default.Home, "Colors"),
    SETTINGS("settings", "Settings", Icons.Default.Settings, "Settings"),
}