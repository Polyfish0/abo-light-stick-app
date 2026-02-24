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
import de.polyfish0.adolightstick.routes.ui.theme.AdoLightStickTheme
import de.polyfish0.adolightstick.utils.Either
import java.util.Locale
import java.util.SortedMap
import kotlin.collections.set
import kotlin.math.roundToInt

@Preview
@Composable
fun MappingBuilder() {
    var songDuration by remember { mutableStateOf("60") }
    var colorPosition by remember { mutableStateOf(Color.White) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val mapping = remember { mutableStateMapOf<Int, Color>() }

    AdoLightStickTheme {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField( // Not yet by state, maybe in next Material Version
                value = songDuration,
                onValueChange = { if (it.isDigitsOnly()) songDuration = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Enter Clip Duration (in seconds)") }
            )
            ColorPicker { colorPosition = it }
            Tabs(selectedTab) {selectedTab = it}
            SliderWrapper(selectedTab, songDuration.toIntOrNull(), colorPosition, mapping)
            CurrentMapping(fillHoles(songDuration.toIntOrNull(), mapping)) // Show gradient once model will be ready

            Button(
                onClick = { saveMapping(fillHoles(songDuration.toIntOrNull(), mapping.toSortedMap())) },
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

// Prepare a Slider wrapper taking the selectedTab and color as arg.
@Composable
fun SliderWrapper(selected: Int, nullableDuration: Int?, color: Color, mapping: MutableMap<Int, Color>) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var sliderRange by remember { mutableStateOf(0f..duration.toFloat()) }

    when (selected) {
        0 -> {IntervalSlider(nullableDuration) { sliderRange = it}; AddToMappingButton(mapping, color, Either.Right(sliderRange))}
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
fun IntervalSlider(nullableDuration: Int?, onValueChange: (ClosedFloatingPointRange<Float>) -> Unit) { // State Hoisting
    val duration = nullableDuration ?: 60
    val range = 0.0f..duration.toFloat() // Upper bound must be clip duration in s
    var sliderPosition by remember { mutableStateOf(0f..duration.toFloat()) }

    Column {
        RangeSlider(
            value = sliderPosition,
            valueRange = range,
            steps = (duration - 1) * 10 + 9,
            onValueChange = { sliderPosition = it }, // 'it' could be .999999 causing bugs. Slider can't have Int as value
            onValueChangeFinished = { onValueChange(sliderPosition) }
        )
        Text(text = "Current position : ${String.format(Locale.getDefault(), "%.2f", sliderPosition.start)} " +
                "to ${String.format(Locale.getDefault(), "%.2f", sliderPosition.endInclusive)}")
    }
}

@Composable
fun AddToMappingButton(mapping: MutableMap<Int, Color>, colorPosition: Color, sliderPosition: Either<Float, ClosedFloatingPointRange<Float>>) {
    Button(
        onClick = {
            when (sliderPosition) {
                is Either.Left -> mapping[(sliderPosition.value * 10).roundToInt()] = colorPosition
                is Either.Right -> colorInterval(mapping, colorPosition, sliderPosition.value) // Interval
            }
            Log.i("UserMapping", "Added $colorPosition at $sliderPosition")
        }
    ) {
        Text("Add to Mapping")
    }
}

@Composable
fun CurrentMapping(mapping: SortedMap<Int, Color>) {
    Log.i("UserMapping", "Rebuild impression")
    val brush = Brush.horizontalGradient(mapping.values.map { it })

    Canvas(
        modifier = Modifier.height(50.dp).fillMaxWidth(),
        onDraw = {
            drawRect(brush)
        }
    )
}

fun colorInterval(mapping: MutableMap<Int, Color>, color: Color, sliderRange: ClosedFloatingPointRange<Float>) {
    val startInt = (sliderRange.start * 10).toInt()
    val endInt = (sliderRange.endInclusive * 10).toInt()
    //var sortedIndex: Float

    Log.i("MappingBuilder", "colorInterval Called between ${sliderRange.start} and ${sliderRange.endInclusive}")
    Log.i("MappingBuilder", "colorInterval Called between $startInt and $endInt")

    for (i in startInt..endInt) {
        //sortedIndex = i / 10.toFloat() // FloatingPoint Shenanigans
        when (mapping[i]) {
            null -> mapping[i] = color
        }
    }
}

fun fillHoles(nullableDuration : Int?, mapping: Map<Int, Color>): SortedMap<Int, Color> {
    val durationInt = (nullableDuration ?: 60) * 10
    var sorted = mapping.toSortedMap()

    Log.i("MappingBuilder", "fillHoles Called")

    for (i in 0..durationInt) {
        when (sorted[i]) {
            null -> sorted[i] = Color.Black
        }
    }
    return sorted
}

fun saveMapping(mapping: SortedMap<Int, Color>) {
    // Trim values greater than clip len ?
    Log.i("MappingBuilder", "Implement saving function")
    // Extract all values (sorted) and save in file
}

// Compute fillHole after each addition ?
// Save file as .adostick (for using intents and sharing)
// Repo of .adostick ? Allow people to share freely their mapping ?