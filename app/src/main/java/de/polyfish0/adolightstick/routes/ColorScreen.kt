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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import de.polyfish0.adolightstick.R
import de.polyfish0.adolightstick.effects.Effect
import de.polyfish0.adolightstick.effects.RainbowEffect
import de.polyfish0.adolightstick.viewmodel.ColorViewModel

@OptIn(ExperimentalComposeUiApi::class)
@RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun ColorScreen() {
    val colorPickerController = rememberColorPickerController()
    val viewModel: ColorViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(LocalContext.current.applicationContext as Application)
    )
    val currentEffect by viewModel.currentEffect.collectAsState()

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
            .padding(8.dp)) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Center
            ) {
                Effects.entries.forEach {
                    IconButton(onClick = {
                        viewModel.updateEffect(it.effect())
                    }) {
                        Icon(imageVector = it.icon, contentDescription = stringResource(it.effectNameResourceID))
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
            .padding(8.dp)) {
            when(currentEffect) {
                null -> ManualColorView(viewModel, colorPickerController)
                is RainbowEffect -> RainbowEffectSettings(viewModel)
            }
        }
    }
}

@RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun ManualColorView(viewModel: ColorViewModel, colorPickerController: ColorPickerController) {
    HsvColorPicker(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
            .padding(10.dp),
        controller = colorPickerController,
        initialColor = viewModel.currentColor.value,
        onColorChanged = {
            viewModel.updateColor(it.color)
        }
    )
    BrightnessSlider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .height(35.dp),
        controller = colorPickerController,
    )
}

@Composable
fun RainbowEffectSettings(viewModel: ColorViewModel) {
    Text("E")
}

enum class Effects(
    val effectNameResourceID: Int,
    val icon: ImageVector,
    val effect: (() -> Effect?)
) {
    MANUAL(R.string.manually, Icons.Filled.Build, { null }),
    RAINBOW(R.string.rainbow, Icons.Filled.Refresh, { RainbowEffect() })
}