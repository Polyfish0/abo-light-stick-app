package de.polyfish0.adolightstick.routes

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.polyfish0.adolightstick.routes.ui.theme.AdoLightStickTheme
import java.util.Locale

@Preview
@Composable
fun MappingBuilder() {
    var songDuration by remember { mutableStateOf("60") }
    AdoLightStickTheme {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField( // Not yet by state
                value = songDuration,
                onValueChange = { songDuration = it },
                label = { Text("Label") }
            )
            ColorPicker()
            SongSlider()
            CurrentMapping()
            Button(
                onClick = { Log.i("MappingBuilder", "Implement saving function") },
                modifier = Modifier
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun ColorPicker() {

}

@Composable
fun SongSlider() {
    val duration = 10
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    val range = 0.0f..duration.toFloat() // Upper bound must be clip duration in s

    Column {
        Slider(
            value = sliderPosition,
            valueRange = range,
            steps = (duration - 1) * 10 + 9,
            onValueChange = { sliderPosition = it },
        )
        Text(text = "Current position : ${String.format(Locale.getDefault(), "%.2f", sliderPosition)}")
    }
}

@Composable
fun CurrentMapping() {
    // Draw a gradient based on a color list
}