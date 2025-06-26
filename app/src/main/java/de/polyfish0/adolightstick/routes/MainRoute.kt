package de.polyfish0.adolightstick.routes

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import de.polyfish0.adolightstick.composables.DonutIndicator
import de.polyfish0.adolightstick.routes.setup.LightStickSetupViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainRoute(navController: NavController) {
    DonutIndicator(Color(0, 255, 0))
}