package de.polyfish0.adolightstick.utils

import android.os.Debug
import android.util.Log

fun assetsListTrimmer(list: Array<String>): Array<String> {
    list.forEach {
        //println(it)
        Log.i("DominantIntegration", it)
    }

    Log.i("DominantIntegration", list.size.toString())
    return list
}