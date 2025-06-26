package de.polyfish0.adolightstick.routes.setup

import android.Manifest
import android.app.Application
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun LightStickSetup(navController: NavController) {
    val viewModel: LightStickSetupViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(LocalContext.current.applicationContext as Application)
    )
    val device by viewModel.device.collectAsState()
    val bleServiceReady by viewModel.bleServiceReady.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.bindToService()
    }

    if(!bleServiceReady) {
        Column {
            Text("Waiting for the Bluetooth service to finish starting")
        }
        return
    }

    LaunchedEffect(Unit) {
        viewModel.startDeviceSearch()
    }

    Column {
        if(device != null) {
            viewModel.stopDeviceSearch()
            Text("${device!!.name} - ${device!!.address}")
            viewModel.connectToGatt()
        }else {
            Text("Suche nach Light Stick...")
        }
    }
}