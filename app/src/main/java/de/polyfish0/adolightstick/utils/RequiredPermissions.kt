package de.polyfish0.adolightstick.utils

import android.Manifest

object RequiredPermissions {
    val permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.NEARBY_WIFI_DEVICES
    )
}