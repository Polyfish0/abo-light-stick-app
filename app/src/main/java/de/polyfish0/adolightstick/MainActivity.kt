package de.polyfish0.adolightstick

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import de.polyfish0.adolightstick.utils.LightStickRoutes
import de.polyfish0.adolightstick.routes.MainRoute
import de.polyfish0.adolightstick.routes.setup.LightStickSetup
import de.polyfish0.adolightstick.utils.RequiredPermissions
import de.polyfish0.adolightstick.routes.setup.PermissionRequestScreen
import de.polyfish0.adolightstick.ui.theme.AdoLightStickTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            navController.enableOnBackPressed(false)

            val defaultDestination = if (rememberMultiplePermissionsState(RequiredPermissions.permissions).allPermissionsGranted
            ) LightStickRoutes.LightStickSetup else LightStickRoutes.PermissionRequestScreen

            AdoLightStickTheme {
                Scaffold { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = LightStickRoutes.MainMenu,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(LightStickRoutes.MainMenu) { MainRoute(navController) }
                        composable(LightStickRoutes.LightStickSetup) { LightStickSetup(navController) }
                        composable(LightStickRoutes.PermissionRequestScreen) { PermissionRequestScreen(navController) }
                    }
                }
            }
        }
    }
}