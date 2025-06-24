package de.polyfish0.adolightstick.routes.setup

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import de.polyfish0.adolightstick.routes.LightStickRoutes

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SetupScreen(navController: NavController) {
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val bleScanPermissionState = rememberPermissionState(Manifest.permission.BLUETOOTH_SCAN)
    val bleConnectPermissionState = rememberPermissionState(Manifest.permission.BLUETOOTH_CONNECT)

    if(locationPermissionState.status != PermissionStatus.Granted) {
        PermissionComposable(locationPermissionState, Icons.Filled.LocationOn, "Please allow the location permission. This is required because the app needs it to find the light stick over Bluetooth.")
        return
    }

    if(bleScanPermissionState.status != PermissionStatus.Granted) {
        PermissionComposable(bleScanPermissionState, Icons.Filled.LocationOn, "Please allow the Bluetooth permission. This is required because the app needs it to find the light stick over Bluetooth.")
        return
    }

    if(bleConnectPermissionState.status != PermissionStatus.Granted) {
        PermissionComposable(bleConnectPermissionState, Icons.Filled.LocationOn, "Please allow the second Bluetooth permission. This is required because the app needs it to find the light stick over Bluetooth.")
        return
    }

    navController.navigate(LightStickRoutes.LightStickSetup.name)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionComposable(
    permission: PermissionState,
    image: ImageVector,
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = image,
                    contentDescription = "",
                    modifier = Modifier.size(64.dp)
                )
            }
            Row {
                Text(text)
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { permission.launchPermissionRequest() }
            ) {
                Text("Request Permission")
            }
        }
    }
}