package de.polyfish0.adolightstick.utils

import android.util.Log
import androidx.compose.animation.core.KeyframesSpec
import androidx.compose.animation.core.keyframes
import androidx.compose.ui.graphics.Color

fun assetsListTrimmer(list: Array<String>): Array<String> {
    list.forEach {
        //println(it)
        Log.i("DominantIntegration", it)
    }

    Log.i("DominantIntegration", list.size.toString())
    return list
}

fun loadMapping(filename: String) : KeyframesSpec<Color> {//Array<Color> {
    // asset open "Mappings/" + filename
    // Convert to color array
    // Manage timing // Animation * AsState ?

    val colors = arrayOf(Color.Red, Color.Blue, Color.Green, Color.Yellow)

    val frames = keyframes<Color> {
        durationMillis = colors.size * 1000
        for ((index, color) in colors.withIndex()) {
            color at index * 500
        }
        //Color.Red at 0
        //Color.Blue at 500
        //Color.Green at 1000
        //Color.Yellow at 1500
    }
    return frames
}