package de.polyfish0.adolightstick.routes

import android.util.ArrayMap
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import de.polyfish0.adolightstick.routes.ui.theme.AdoLightStickTheme
import java.util.Locale

@Preview
@Composable
fun MappingBuilder() {
    var songDuration by remember { mutableStateOf("60") }
    var sliderPosition by remember { mutableFloatStateOf(0f) } // Hoisted state
    var colorPosition by remember { mutableStateOf(Color.White) }
    var mapping by remember { mutableStateOf(ArrayMap<Float, Color>()) }

    AdoLightStickTheme {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField( // Not yet by state, maybe in next Material Version
                value = songDuration,
                onValueChange = { if (it.isDigitsOnly()) songDuration = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Enter Clip Duration (in seconds)") }
            )
            ColorPicker(colorPosition) { colorPosition = it }
            SongSlider(songDuration.toIntOrNull(), sliderPosition) { sliderPosition = it }
            CurrentMapping() // Show gradient once model will be ready

            Button(
                onClick = {
                    mapping[sliderPosition] = colorPosition
                    Log.i("UserMapping", "Added $colorPosition at $sliderPosition")
                          },
                modifier = Modifier
            ) {
                Text("Add to Mapping")
            }

            Button(
                onClick = { SaveMapping(mapping) },
                modifier = Modifier
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun ColorPicker(colorPosition: Color, onValueChange: (Color) -> Unit) {
    val colorPickerController = rememberColorPickerController()

    HsvColorPicker(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(10.dp),
        controller = colorPickerController,
        onColorChanged = { onValueChange(it.color) }
    )
}

@Composable
fun SongSlider(nullableDuration : Int?, sliderPosition: Float, onValueChange: (Float) -> Unit) { // State Hoisting
    val duration = nullableDuration ?: 60
    val range = 0.0f..duration.toFloat() // Upper bound must be clip duration in s

    Column {
        Slider(
            value = sliderPosition,
            valueRange = range,
            steps = (duration - 1) * 10 + 9,
            onValueChange = { onValueChange(it) },
        )
        Text(text = "Current position : ${String.format(Locale.getDefault(), "%.2f", sliderPosition)}")
    }
}

@Composable
fun CurrentMapping() {
    // Draw a gradient based on a color list
}

fun SaveMapping(mapping: ArrayMap<Float, Color>) {
    Log.i("MappingBuilder", "Implement saving function")
    // Extract all values (sorted) and save in file
}