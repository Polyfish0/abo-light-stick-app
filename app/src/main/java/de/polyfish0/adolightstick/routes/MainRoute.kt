package de.polyfish0.adolightstick.routes

import android.content.Intent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import de.polyfish0.adolightstick.service.BLEService

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainRoute(navController: NavController) {
    Text("Main Route")
    LocalContext.current.startService(Intent(LocalContext.current, BLEService::class.java))
}