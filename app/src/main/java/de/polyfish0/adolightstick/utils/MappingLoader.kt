package de.polyfish0.adolightstick.utils

import android.util.Log
import androidx.compose.animation.core.KeyframesSpec
import androidx.compose.animation.core.keyframes
import androidx.compose.ui.graphics.Color
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

//Merely a debug function, will be removed in final version
fun assetsListTrimmer(list: Array<String>): Array<String> {
    list.forEach {
        //println(it)
        Log.i("DominantIntegration", it)
    }

    Log.i("DominantIntegration", list.size.toString())
    return list
}

fun loadMapping(file: InputStream) : KeyframesSpec<Color> {//Array<Color> {
    val pacingNumber = 42 // (in millis) Determine how fast the animation will play (must be as close as possible as the clip duration)
    val reader = BufferedReader(InputStreamReader(file))
    val input: Sequence<String> = generateSequence { reader.readLine() }
    val trimmingRegex = "[^0-9,]".toRegex()
    val numberRegex = "[0-9]".toRegex()
    val lines = input.toList().filter { line ->  line.contains(numberRegex)} // Somehow, either the file has weird invalid characters inside or the reading has a side effect.

    file.close()

    val colors = lines.map { line ->
        val (b, g, r) = line // Inverting colors for mapping shenanigans
            .replace(trimmingRegex, "")
            .split(",")
            .map { it.toInt()}
        Color(r, g, b)
    }

    Log.i("DominantList", colors.size.toString())

    //val colors = arrayOf(Color.Red, Color.Blue, Color.Green, Color.Yellow)

    val frames = keyframes<Color> {
        durationMillis = colors.size * pacingNumber // Magic pacing number
        for ((index, color) in colors.withIndex()) {
            color at index * pacingNumber
        }
    }
    return frames
}