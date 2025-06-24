package de.polyfish0.adolightstick

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
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import de.polyfish0.adolightstick.routes.LightStickRoutes
import de.polyfish0.adolightstick.routes.MainRoute
import de.polyfish0.adolightstick.routes.setup.LightStickSetup
import de.polyfish0.adolightstick.routes.setup.SetupScreen
import de.polyfish0.adolightstick.ui.theme.AdoLightStickTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val locationPermissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
            val bleScanPermissionState = rememberPermissionState(android.Manifest.permission.BLUETOOTH_SCAN)
            val bleConnectPermissionState = rememberPermissionState(android.Manifest.permission.BLUETOOTH_CONNECT)

            AdoLightStickTheme {
                Scaffold { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = if (
                                    locationPermissionState.status == PermissionStatus.Granted &&
                                    bleScanPermissionState.status == PermissionStatus.Granted &&
                                    bleConnectPermissionState.status == PermissionStatus.Granted
                                ) LightStickRoutes.MainMenu.name else LightStickRoutes.Setup.name,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(LightStickRoutes.MainMenu.name) { MainRoute(navController) }
                        composable(LightStickRoutes.LightStickSetup.name) { LightStickSetup(navController) }
                        composable(LightStickRoutes.Setup.name) { SetupScreen(navController) }
                    }
                }
            }
        }
    }
}