package de.polyfish0.adolightstick.routes

import android.R.attr.duration
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import de.polyfish0.adolightstick.models.MappingColor
import de.polyfish0.adolightstick.routes.ui.theme.AdoLightStickTheme
import de.polyfish0.adolightstick.utils.Either
import java.util.Locale
import java.util.SortedMap
import java.util.stream.Collectors.mapping
import kotlin.collections.set

@Preview
@Composable
fun MappingBuilder() {
    var songDuration by remember { mutableStateOf("60") }
    var sliderPosition by remember { mutableFloatStateOf(0f) } // Hoisted states
    var colorPosition by remember { mutableStateOf(Color.White) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val mapping = remember { mutableStateMapOf<Float, MappingColor>() }

    AdoLightStickTheme {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField( // Not yet by state, maybe in next Material Version
                value = songDuration,
                onValueChange = { if (it.isDigitsOnly()) songDuration = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Enter Clip Duration (in seconds)") }
            )
            ColorPicker() { colorPosition = it }
            Tabs(selectedTab) {selectedTab = it}
            FrameSlider(songDuration.toIntOrNull(), sliderPosition) { sliderPosition = it }
            CurrentMapping(fillHoles(songDuration.toIntOrNull(), mapping)) // Show gradient once model will be ready

            Button(
                onClick = {
                    mapping[sliderPosition] = MappingColor(colorPosition, defaultColor = mapping[sliderPosition]?.defaultColor
                        ?: Color.Black)
                    Log.i("UserMapping", "Added $colorPosition at $sliderPosition")
                          },
                modifier = Modifier
            ) {
                Text("Add to Mapping")
            }

            Button(
                onClick = { SaveMapping(fillHoles(songDuration.toIntOrNull(), mapping.toSortedMap())) },
                modifier = Modifier
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun Tabs(selected: Int, onSelected: (Int) -> Unit) {

    TabRow(selectedTabIndex = selected, modifier = Modifier.fillMaxWidth()) {
        Tab(selected = selected == 0,
            onClick = { onSelected(0) },
            text = { Text("Interval") }
        )
        Tab(selected = selected == 1,
            onClick = { onSelected(1) },
            text = { Text("Frame") }
        )
    }
}

@Composable
fun ColorPicker(onValueChange: (Color) -> Unit) {
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

// Prepare a Slider wrapper taking the selected and color as arg.
// Wrapper should contain the slider and the add button
@Composable
fun SliderWrapper(selected: Int, nullableDuration: Int?, color: Color, mapping: MutableMap<Float, MappingColor>) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var sliderRange by remember { mutableStateOf(0f..duration.toFloat()) }

    when (selected) {
        0 -> {IntervalSlider(nullableDuration, sliderRange) { sliderRange = it}; AddToMappingButton(mapping, color, Either.Right(sliderRange))}
        1 -> {FrameSlider(nullableDuration, sliderPosition) { sliderPosition = it }; AddToMappingButton(mapping, color, Either.Left(sliderPosition))}
    }
}

@Composable
fun FrameSlider(nullableDuration : Int?, sliderPosition: Float, onValueChange: (Float) -> Unit) { // State Hoisting
    val duration = nullableDuration ?: 60
    val range = 0.0f..duration.toFloat() // Upper bound must be clip duration in s

    Column {
        Slider(
            value = sliderPosition,
            valueRange = range,
            steps = (duration - 1) * 10 + 9,
            onValueChange = { onValueChange(it) }, // 'it' could be .999999 causing bugs. Slider can't have Int as value
        )
        Text(text = "Current position : ${String.format(Locale.getDefault(), "%.2f", sliderPosition)}")
    }
}

@Composable
fun IntervalSlider(nullableDuration: Int?, sliderRange: ClosedFloatingPointRange<Float>, onValueChange: (ClosedFloatingPointRange<Float>) -> Unit) { // State Hoisting
    // Refactor those for transforming models here ?
    // Replace sliderPos and onValueChange by Model editing.
    val duration = nullableDuration ?: 60
    val range = 0.0f..duration.toFloat() // Upper bound must be clip duration in s
    var sliderPosition by remember { mutableStateOf(0f..duration.toFloat()) }

    Column {
        RangeSlider(
            value = sliderPosition,
            valueRange = range,
            steps = (duration - 1) * 10 + 9,
            onValueChange = { sliderPosition = it }, // 'it' could be .999999 causing bugs. Slider can't have Int as value
        )
        Text(text = "Current position : ${String.format(Locale.getDefault(), "%.2f", sliderPosition)}")
    }
}

@Composable
fun AddToMappingButton(mapping: MutableMap<Float, MappingColor>, colorPosition: Color, sliderPosition: Either<Float, ClosedFloatingPointRange<Float>>) {
    // Replace Slider Pos by Either ?

    Button(
        onClick = {
            when (sliderPosition) {
                is Either.Left -> mapping[sliderPosition.value] = MappingColor(colorPosition, defaultColor = mapping[sliderPosition.value]?.defaultColor
                ?: Color.Black) // Delete the whole MappingColor model
                is Either.Right -> TODO() // Interval
            }

            Log.i("UserMapping", "Added $colorPosition at $sliderPosition")
        }
    ) {
        Text("Add to Mapping")
    }
}

@Composable
fun CurrentMapping(mapping: SortedMap<Float, MappingColor>) {
    // Draw a gradient based on a color list
    Log.i("UserMapping", "Rebuild impression")
    val brush = Brush.horizontalGradient(mapping.values.map { it.manualColor ?: it.defaultColor})

    Canvas(
        modifier = Modifier.height(50.dp).fillMaxWidth(),
        onDraw = {
            drawRect(brush)
        }
    )
}

// This function help fill the mapping instead of manually inputing color for each 100 milliseconds
fun fillHoles(nullableDuration : Int?, mapping: Map<Float, MappingColor>): SortedMap<Float, MappingColor> {
    val durationInt = (nullableDuration ?: 60) * 10
    var sorted = mapping.toSortedMap()
    var sortedIndex: Float
    var currentColor = Color.Black // Color iter

    Log.i("MappingBuilder", "fillHoles Called")

    for (i in 0..durationInt) {
        sortedIndex = i / 10.toFloat()
        when (sorted[sortedIndex]) {
            null -> sorted[sortedIndex] = MappingColor(defaultColor = currentColor)
            else -> currentColor = sorted[sortedIndex]!!.manualColor!! // Replace current color
        }
    }

    // Penser à la seconde passe
    // Idée de base foireuse ? Comment je determine jusqu'ou j'écrase ?
    // Ok fuck filling holes, fuck the model, instead use double slider. One for frame and one for interval.
    // Does it update the map ?
    return sorted
}

fun SaveMapping(mapping: SortedMap<Float, MappingColor>) {
    // Trim values greater than clip len ?
    Log.i("MappingBuilder", "Implement saving function")
    // Extract all values (sorted) and save in file
}

// Compute fillHole after each addition ?
// Save file as .adostick (for using intents and sharing)
// Repo of .adostick ? Allow people to share freely their mapping, but would need cash to store that.
// The ado community would pay for it I'm sure (if the app is sexy enough)