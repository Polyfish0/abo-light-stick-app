package de.polyfish0.adolightstick

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
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
import de.polyfish0.adolightstick.routes.LightStickRoutes
import de.polyfish0.adolightstick.routes.MainRoute
import de.polyfish0.adolightstick.routes.setup.LightStickSetup
import de.polyfish0.adolightstick.routes.setup.RequiredPermissions
import de.polyfish0.adolightstick.routes.setup.PermissionRequestScreen
import de.polyfish0.adolightstick.service.BLEService
import de.polyfish0.adolightstick.ui.theme.AdoLightStickTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
    private val _bleServiceReady = MutableStateFlow(false)
    private val bleServiceReady: StateFlow<Boolean> = _bleServiceReady
    private var bleService: BLEService? = null

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            AdoLightStickTheme {
                Scaffold { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = if (
                            rememberMultiplePermissionsState(RequiredPermissions.permissions).allPermissionsGranted
                            ) LightStickRoutes.LightStickSetup else LightStickRoutes.PermissionRequestScreen,
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