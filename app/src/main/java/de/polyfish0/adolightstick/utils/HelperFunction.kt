package de.polyfish0.adolightstick.utils

import android.content.Context
import android.content.pm.FeatureInfo
import android.content.pm.PackageManager

fun wifiDirectSupported(context: Context): Boolean {
    val pm = context.packageManager
    val featureInfo = pm.systemAvailableFeatures
    featureInfo.forEach {
        if(it.name.lowercase() == "android.hardware.wifi.direct")
            return true
    }
    return false
}