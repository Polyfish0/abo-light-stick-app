package de.polyfish0.adolightstick.routes.setup

import android.util.Log
import android.widget.NumberPicker
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import de.polyfish0.adolightstick.R
import de.polyfish0.adolightstick.routes.LightStickRoutes
import de.polyfish0.adolightstick.utils.assetsListTrimmer
import de.polyfish0.adolightstick.utils.loadMapping

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(navController: NavController) {
    val requiredPermissions = rememberMultiplePermissionsState(RequiredPermissions.permissions)
    //val songs = arrayOf("Magic", "Kira Kira", "Kick Back")
    val songs : Array<String> = assetsListTrimmer( LocalContext.current.assets.list("Mappings")!!)
    //val file = LocalContext.current.assets.open("Mappings/Magic").toString()
    val frames = loadMapping("Dummy")
    var changeColor: Boolean by remember { mutableStateOf(false) }
    val animatedColor: Color by animateColorAsState(targetValue = if (changeColor) Color.Black else Color.White, animationSpec = frames)
    var selectedItem = "Magic"

    if(requiredPermissions.allPermissionsGranted) {
        navController.navigate(LightStickRoutes.LightStickSetup)
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
                    Text(stringResource(R.string.allow_permissions))
                }
                Spacer(modifier = Modifier.weight(1f))
                Row {
                    @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
                    AndroidView(
                        factory = { context ->
                            NumberPicker(context).apply {
                                minValue = 0
                                maxValue = songs.size - 1
                                displayedValues = songs
                                setOnValueChangedListener { _, _, newVal ->
                                    selectedItem = songs[newVal]
                                }
                            }
                        },
                        modifier = Modifier
                            //.fillMaxWidth()
                            .padding(horizontal = 50.dp)
                            .size(width = 200.dp, height = 50.dp)
                            .background(color = Color.White)
                    )
                    Button(
                        onClick = {
                            Log.d("DominantIntegration", selectedItem)
                            changeColor = !changeColor
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.play_circle),
                            contentDescription = stringResource(id = R.string.play_circle_description)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Canvas(modifier = Modifier.fillMaxWidth()) {
                    drawCircle(animatedColor, radius = 100.dp.toPx())
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    modifier = Modifier.fillMaxWidth(),
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