package de.polyfish0.adolightstick.routes.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import de.polyfish0.adolightstick.R
import de.polyfish0.adolightstick.routes.LightStickRoutes

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(navController: NavController) {
    val requiredPermissions = rememberMultiplePermissionsState(RequiredPermissions.permissions)

    if(requiredPermissions.allPermissionsGranted) {
        navController.navigate(LightStickRoutes.MainMenu)
    }else {
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "",
                        modifier = Modifier.Companion.size(64.dp)
                    )
                }
                Row {
                    Text(stringResource(R.string.allow_permissions))
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    onClick = {
                        requiredPermissions.launchMultiplePermissionRequest()
                    }
                ) {
                    Text(stringResource(R.string.request_permission))
                }
            }
        }
    }
}