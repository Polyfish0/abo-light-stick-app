package de.polyfish0.adolightstick.routes

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
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
import java.util.TreeMap

@Preview
@Composable
fun MappingBuilder() {
    var songDuration by remember { mutableStateOf("60") }
    var sliderPosition by remember { mutableFloatStateOf(0f) } // Hoisted state
    var colorPosition by remember { mutableStateOf(Color.White) }
    var mapping by remember { mutableStateOf(TreeMap<Float, Color>()) }

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
            CurrentMapping(fillHoles(songDuration.toIntOrNull(), mapping)) // Show gradient once model will be ready

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
            onValueChange = { onValueChange(it) }, // 'it' could be .999999 causing bugs ?
        )
        Text(text = "Current position : ${String.format(Locale.getDefault(), "%.2f", sliderPosition)}")
    }
}

@Composable
fun CurrentMapping(mapping: TreeMap<Float, Color>) {
    // Draw a gradient based on a color list
    val brush = Brush.horizontalGradient(mapping.values.toList())

    Canvas(
        modifier = Modifier.height(50.dp).fillMaxWidth(),
        onDraw = {
            drawRect(brush)
        }
    )
}

// This function help fill the mapping instead of manually inputing color for each 100 milliseconds
fun fillHoles(nullableDuration : Int?, mapping: TreeMap<Float, Color>): TreeMap<Float, Color> {
    val duration = nullableDuration?.toFloat() ?: 60.0f
    var i = 0.0f // While iterator
    var currentColor = Color.Black // Color iter

    //for (i in 0.0f..duration step 0.1f)
    while (i < duration) { //Can't for loop with float range
        when (mapping[i]) {
            null -> mapping[i] = currentColor
            else -> currentColor = mapping[i]!!
        }
        i += 0.1f
    }

    // Penser à la seconde passe
    // Does it update the map ?
    return mapping
}

fun SaveMapping(mapping: TreeMap<Float, Color>) {
    // Trim values greater than clip len ?
    Log.i("MappingBuilder", "Implement saving function")
    // Extract all values (sorted) and save in file
}

// Compute fillHole after each addition ?