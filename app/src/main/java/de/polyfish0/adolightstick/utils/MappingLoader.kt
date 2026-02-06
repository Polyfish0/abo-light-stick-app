package de.polyfish0.adolightstick.utils

import android.animation.Keyframe
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


    val colors = arrayOf(Color.Red, Color.Blue, Color.Green)
    val frames = keyframes {
        durationMillis = 2000
        Color.Red at 0
        Color.Blue at 500
        Color.Green at 1000
        Color.Yellow at 1500
    } //arrayOf<Keyframe>(Keyframe())
    return frames
}