package de.polyfish0.adolightstick.service.wifidirect

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionType
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Strategy
import de.polyfish0.adolightstick.utils.wifiDirectSupported

class WifiDirectService: Service() {
    private val binder: LocalBinder = LocalBinder()
    val advertisingOptions: AdvertisingOptions = AdvertisingOptions.Builder()
        .setStrategy(Strategy.P2P_STAR)
        .build()

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun startDiscovery() {

    }

    fun registerService(instanceName: String, serviceType: String, txtRecord: Map<String, String>) {
        Nearby.getConnectionsClient(this)
            .startAdvertising("Ado", "de.polyfish0.adolightstick", ConnectionLifecycle(), advertisingOptions)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(!wifiDirectSupported(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    inner class ConnectionLifecycle: ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(
            p0: String,
            p1: ConnectionInfo
        ) {
            TODO("Not yet implemented")
        }

        override fun onConnectionResult(
            p0: String,
            p1: ConnectionResolution
        ) {
            TODO("Not yet implemented")
        }

        override fun onDisconnected(p0: String) {
            TODO("Not yet implemented")
        }
    }

    inner class LocalBinder: Binder() {
        fun getService(): WifiDirectService = this@WifiDirectService
    }
}