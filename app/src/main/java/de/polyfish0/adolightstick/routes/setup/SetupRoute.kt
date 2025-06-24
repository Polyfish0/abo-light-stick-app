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
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import de.polyfish0.adolightstick.routes.LightStickRoutes

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SetupScreen(navController: NavController) {
    val requiredPermissions = rememberMultiplePermissionsState(RequiredPermissions.permissions)

    if(requiredPermissions.allPermissionsGranted) {
        navController.navigate(LightStickRoutes.MainMenu.name)
    }else {
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
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "",
                        modifier = Modifier.size(64.dp)
                    )
                }
                Row {
                    Text("Please allow the following permissions. They are required because android requires them so that the app is able to scan for the light stick over Bluetooth.")
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        requiredPermissions.launchMultiplePermissionRequest()
                    }
                ) {
                    Text("Request Permission")
                }
            }
        }
    }
}