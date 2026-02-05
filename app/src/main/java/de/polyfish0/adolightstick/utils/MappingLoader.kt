package de.polyfish0.adolightstick.utils

import android.util.Log
import androidx.compose.ui.graphics.Color

fun assetsListTrimmer(list: Array<String>): Array<String> {
    list.forEach {
        //println(it)
        Log.i("DominantIntegration", it)
    }

    Log.i("DominantIntegration", list.size.toString())
    return list
}

fun loadMapping(filename: String) : Array<Color> {
    // asset open "Mappings/" + filename
    // Convert to color array
    // Manage timing
    return arrayOf(Color.Red, Color.Blue)
}