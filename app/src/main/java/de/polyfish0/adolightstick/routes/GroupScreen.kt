package de.polyfish0.adolightstick.routes

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import de.polyfish0.adolightstick.viewmodel.GroupScreenViewModel

@Composable
@androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.NEARBY_WIFI_DEVICES])
fun GroupScreen() {
    val viewModel: GroupScreenViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(LocalContext.current.applicationContext as Application)
    )
    LaunchedEffect(Unit) {
        viewModel.bindToService()
    }

    Column {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            viewModel.groups.forEach { group ->
                Text(group.name)
            }
        }
        TextButton(onClick = { viewModel.registerService() }) {
            Text("Service")
        }
    }
}