package de.polyfish0.adolightstick.service.nearbyconectivity

import android.Manifest
import android.app.Service
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.ui.graphics.Color
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import de.polyfish0.adolightstick.events.Event
import de.polyfish0.adolightstick.events.EventBus
import de.polyfish0.adolightstick.utils.NearbyPayloadData
import de.polyfish0.adolightstick.utils.SerializableColor
import de.polyfish0.adolightstick.utils.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class NearbyScan(
    val service: Service,
    private val scope: CoroutineScope
) {
    private val serviceId = "de.polyfish0.adolightstick.service"
    private val repository: SettingsRepository = SettingsRepository(service.applicationContext)

    private val advertisingOptions: AdvertisingOptions = AdvertisingOptions.Builder()
        .setStrategy(Strategy.P2P_STAR)
        .build()
    private val discoveryOptions: DiscoveryOptions = DiscoveryOptions.Builder()
        .setStrategy(Strategy.P2P_STAR)
        .build()

    private val endpointDiscoveryCallback = EndpointDiscovery()
    private val connectionLifecycleCallback = ConnectionLifecycle()
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.d(TAG, "onPayloadReceived from $endpointId")
            val byteDataFromPayload = payload.asBytes() ?: return

            // I ignore the group owner checks for now because we now have 14.11. and on 19.11 it
            // has to be usable because of the Kyocera Dome concert which is on the 22th but developing
            // an app without Internet inside an plane is just a big pain and I will not continue the development
            // while I am in Japan

            val rawData = Json.decodeFromString<NearbyPayloadData>(byteDataFromPayload.decodeToString())
            when(rawData.eventId) {
                "changeColor" -> {
                    val color = Json.decodeFromString<SerializableColor>(rawData.data)
                    scope.launch {
                        EventBus.postEvent(
                            Event.LightStickChangeColor(
                                Color(color.r, color.g, color.b, color.a)
                            )
                        )
                    }
                }
                "groupUserLeft" -> {
                    scope.launch {
                        EventBus.postEvent(Event.GroupUserLeft(endpointId))
                    }
                }
                "groupUserJoined" -> {
                    scope.launch {
                        EventBus.postEvent(Event.GroupUserJoined(endpointId))
                    }
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) { }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun startDiscovery() {
        Nearby.getConnectionsClient(service)
            .startDiscovery(serviceId, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener {
                Log.d(TAG, "Discovery started")
                scope.launch {
                    EventBus.postEvent(Event.NearbyDiscoveryStarted)
                }
            }
            .addOnFailureListener {
                scope.launch {
                    Log.e(TAG, "Advertising failed", it)
                    EventBus.postEvent(Event.NearbyDiscoveryFailed)
                }
            }
    }

    fun stopDiscovery() {
        Nearby.getConnectionsClient(service).stopDiscovery()
        Log.d(TAG, "Discovery stopped")
        scope.launch {
            EventBus.postEvent(Event.NearbyDiscoveryStopped)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun startAdvertising(nickname: String) {
        Nearby.getConnectionsClient(service)
            .startAdvertising(nickname, serviceId, connectionLifecycleCallback, advertisingOptions)
            .addOnSuccessListener {
                Log.d(TAG, "Advertising started")
                scope.launch {
                    EventBus.postEvent(Event.NearbyAdvertisingStarted)
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Advertising failed", it)
                scope.launch {
                    EventBus.postEvent(Event.NearbyAdvertisingFailed)
                }
            }
    }

    fun stopAdvertising() {
        Nearby.getConnectionsClient(service).stopAdvertising()
        Log.d(TAG, "Advertising stopped")
        scope.launch {
            EventBus.postEvent(Event.NearbyAdvertisingStopped)
        }
    }

    private inner class ConnectionLifecycle : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(
            endpointId: String,
            connectionInfo: ConnectionInfo
        ) {
            Log.d(TAG, "onConnectionInitiated: accepting connection from ${connectionInfo.endpointName}")
            Nearby.getConnectionsClient(service).acceptConnection(endpointId, payloadCallback)

            scope.launch {
                EventBus.postEvent(Event.NearbyNewClientConnected(endpointId, connectionInfo.endpointName))
            }
        }

        override fun onConnectionResult(
            endpointId: String,
            result: ConnectionResolution
        ) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d(TAG, "onConnectionResult: connection successful")
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d(TAG, "onConnectionResult: connection rejected")
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.d(TAG, "onConnectionResult: connection error")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "onDisconnected: $endpointId")
            scope.launch {
                EventBus.postEvent(Event.GroupUserLeft(endpointId))
            }
        }
    }

    private inner class EndpointDiscovery : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo) {
            Log.d(TAG, "onEndpointFound: ${discoveredEndpointInfo.endpointName} found, requesting connection")
            scope.launch {
                Nearby.getConnectionsClient(service)
                    .requestConnection(repository.username.first(), endpointId, connectionLifecycleCallback)
                    .addOnSuccessListener {
                        Log.d(TAG, "Requested connection to $endpointId")
                        scope.launch {
                            EventBus.postEvent(Event.NearbyConnectedToHost(endpointId))
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to request connection to $endpointId", e)
                    }
            }
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "onEndpointLost: endpointLost: $endpointId")
        }
    }

    companion object {
        private const val TAG = "NearbyScan"
    }
}