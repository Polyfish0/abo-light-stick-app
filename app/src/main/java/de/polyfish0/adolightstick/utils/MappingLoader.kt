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
    val trimmingRegex = "[^0-9,]".toRegex() // Trim parenthesis
    val reader = BufferedReader(InputStreamReader(file))
    val input: Sequence<String> = generateSequence { reader.readLine() }
    val lines = input.toList()

    //Log.i("DominantList", lines[0]) // For debug of file reader

    file.close()

    val colors = lines.map { line ->
        val (r, g, b) = line // Inverting colors for mapping shenanigans
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