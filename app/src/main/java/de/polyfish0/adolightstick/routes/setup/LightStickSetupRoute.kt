package de.polyfish0.adolightstick.routes.setup

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import de.polyfish0.adolightstick.R
import de.polyfish0.adolightstick.composables.DonutIndicator
import de.polyfish0.adolightstick.routes.LightStickRoutes
import kotlinx.coroutines.delay

@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
@Composable
fun LightStickSetup(navController: NavController) {
    val viewModel: LightStickSetupViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(LocalContext.current.applicationContext as Application),
    )
    val deviceReady by viewModel.deviceReady.collectAsState()
    val device by viewModel.device.collectAsState()
    val bleServiceReady by viewModel.bleServiceReady.collectAsState()
    val lightStickMac by viewModel.savedLightStickMac.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.bindToService()
    }

    if(!bleServiceReady) {
        Column {
            Text(stringResource(R.string.waiting_for_the_bluetooth_service_to_finish_starting))
        }
        return
    }

    LaunchedEffect(!deviceReady, lightStickMac) {
        if(lightStickMac?.isNotEmpty() ?: false) {
            viewModel.setMacFilterInService(lightStickMac)
        }

        viewModel.startDeviceSearch()
    }

    if(device != null) {
        LaunchedEffect(device) {
            viewModel.stopDeviceSearch()
            viewModel.connectToGatt()
        }

        if(lightStickMac?.isNotEmpty() == true) {
            navController.navigate(LightStickRoutes.MainMenu) {
                launchSingleTop = true
                popUpTo(navController.graph.findStartDestination().route!!) {
                    inclusive = true
                }
            }
        }else {
            LightStickFound(viewModel)
        }
    }else {
        SearchingLightStick(viewModel, lightStickMac?.isNotEmpty() ?: false)
    }
}

@SuppressLint("MissingPermission")
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun LightStickFound(viewModel: LightStickSetupViewModel) {
    val deviceReady by viewModel.deviceReady.collectAsState()
    var currentColor by remember { mutableStateOf(Color(0x0, 0x68, 0x97)) }

    LaunchedEffect(deviceReady) {
        if(!deviceReady) return@LaunchedEffect

        while (true) {
            delay(1000L)

            var newColor: Color?
            val colorOptions = listOf(
                Color.Red,
                Color.Green,
                Color.Blue,
                Color.Yellow,
                Color.Magenta,
                Color.White,
            )

            do {
                newColor = colorOptions.random()
            } while (newColor == currentColor)

            currentColor = newColor
            viewModel.changeColor(currentColor)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.setup_colors_matching),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Spacer(modifier = Modifier.weight(1f))

        DonutIndicator(currentColor)

        Spacer(modifier = Modifier.weight(1f))

        Column(modifier = Modifier.fillMaxWidth()) {
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                viewModel.saveMacAddress()
            }) {
                Text(stringResource(R.string.correct_one))
            }

            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                viewModel.blacklistCurrentDevice()
            }) {
                Text(stringResource(R.string.search_for_the_next_one))
            }

            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                viewModel.clearBlackList()
            }) {
                Text(stringResource(R.string.reset_device_blacklist))
            }
        }
    }
}

@Composable
fun SearchingLightStick(viewModel: LightStickSetupViewModel, savedLightStick: Boolean) {
    var showForget by remember { mutableStateOf(false) }

    LaunchedEffect(savedLightStick) {
        if(savedLightStick) {
            delay(10000)
            showForget = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(if(savedLightStick) R.string.searching_for_the_ado_hibana_light_stick else R.string.searching_for_stored_light_stick),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            CircularProgressIndicator(
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if(showForget) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    viewModel.connectToStoredLightStick()
                }) {
                    Text(stringResource(R.string.connect_to_light_stick))
                }
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    viewModel.deleteStoredMac()
                    viewModel.setMacFilterInService(null)
                }) {
                    Text(stringResource(R.string.forget_light_stick))
                }
            }
        }else {
            Text(
                text = stringResource(R.string.made_by_fans_for_fans),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}