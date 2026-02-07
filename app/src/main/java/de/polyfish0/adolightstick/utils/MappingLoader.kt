package de.polyfish0.adolightstick.utils

import android.util.Log
import androidx.compose.animation.core.KeyframesSpec
import androidx.compose.animation.core.keyframes
import androidx.compose.ui.graphics.Color
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

fun assetsListTrimmer(list: Array<String>): Array<String> {
    list.forEach {
        //println(it)
        Log.i("DominantIntegration", it)
    }

    Log.i("DominantIntegration", list.size.toString())
    return list
}

fun loadMapping(file: InputStream) : KeyframesSpec<Color> {//Array<Color> {
    // asset open "Mappings/" + filename
    // Convert to color array
    // Manage timing // Animation * AsState ?
    val reader = BufferedReader(InputStreamReader(file))

    val input: Sequence<String> = generateSequence { reader.readLine() }
    val trimmingRegex = "[^0-9,]".toRegex()
    val numberRegex = "[0-9]".toRegex()
    val lines = input.toList().filter { line ->  line.contains(numberRegex)} // Somehow, either the file has weird invalid characters inside or the reading has a side effect.

    file.close()

    //Log.i("DominantIntegration", lines.size.toString()) //[0].replace(trimmingRegex, "").split(",").toString())

    val colors = lines.map { line ->
        val (b, g, r) = line // Inverting colors for mapping shenanigans
            .replace(trimmingRegex, "")
            .split(",")
            .map { it.toInt()}
        //Log.i("DominantLambda", "red = $r, g = $g, b = $b")
        Color(r, g, b)
        //Color.Blue
    }

    Log.i("DominantList", colors.size.toString())
    //for (line = reader.readLine(), line != null, )
    /*
    while (line != null) {
        line = reader.readLine()
        rawColor.add(line)
    }*/

    //val colors = arrayOf(Color.Red, Color.Blue, Color.Green, Color.Yellow)

    val frames = keyframes<Color> {
        /*
        durationMillis = colors.size * 1000
        for ((index, color) in colors.withIndex()) {
            color at index * 500
        }
        */
        durationMillis = colors.size * 30
        for ((index, color) in colors.withIndex()) {
            color at index * 30
        }
        //Color.Red at 0
        //Color.Blue at 500
        //Color.Green at 1000
        //Color.Yellow at 1500
    }
    return frames
}