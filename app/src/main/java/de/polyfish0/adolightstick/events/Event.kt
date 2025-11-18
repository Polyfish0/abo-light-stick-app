package de.polyfish0.adolightstick.events

import androidx.compose.ui.graphics.Color

sealed class Event {
    data class NearbyNewClientConnected(val endpointId: String, val username: String) : Event()
    data class NearbyConnectedToHost(val endpointId: String) : Event()
    object NearbyDiscoveryStarted : Event()
    object NearbyDiscoveryStopped : Event()
    object NearbyDiscoveryFailed : Event()
    object NearbyAdvertisingStarted : Event()
    object NearbyAdvertisingStopped : Event()
    object NearbyAdvertisingFailed : Event()
    data class LightStickChangeColor(val color: Color) : Event()
    data class GroupUserJoined(val endpointId: String) : Event()
    data class GroupUserLeft(val endpointId: String) : Event()
}