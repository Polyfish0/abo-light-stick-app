package de.polyfish0.adolightstick.routes

import android.app.Application
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@OptIn(ExperimentalComposeUiApi::class)
@RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun ColorScreen() {
    val colorPickerController = rememberColorPickerController()
    val viewModel: ColorViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(LocalContext.current.applicationContext as Application)
    )

    LaunchedEffect(Unit) {
        viewModel.bindToService()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HsvColorPicker(
            modifier = Modifier.fillMaxWidth()
                .height(450.dp)
                .padding(10.dp),
            controller = colorPickerController,
            initialColor = viewModel.currentColor.value,
            onColorChanged = {
                viewModel.updateColor(it.color)
            }
        )
    }
}