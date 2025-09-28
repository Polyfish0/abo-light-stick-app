package de.polyfish0.adolightstick.routes

import android.app.Application
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())) {
                repeat(10) {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "")
                    }
                }
            }
        }
        HorizontalDivider(modifier = Modifier
            .padding(8.dp, 0.dp)
            .height(1.dp)
            .fillMaxWidth()
        )
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)) {
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
}