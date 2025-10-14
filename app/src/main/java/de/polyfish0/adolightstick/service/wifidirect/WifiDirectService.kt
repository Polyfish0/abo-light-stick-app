package de.polyfish0.adolightstick.service.wifidirect

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import de.polyfish0.adolightstick.utils.wifiDirectSupported

class WifiDirectService: Service() {
    private val binder: LocalBinder = LocalBinder()
    private val receiver: WiFiDirectBroadcastReceiver = WiFiDirectBroadcastReceiver()
    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun startDiscovery() {
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {}

            override fun onFailure(reasonCode: Int) {
                Log.e("WifiDirectService", "Discovery failed: $reasonCode")
            }
        })
    }

    override fun onCreate() {
        super.onCreate()

        manager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)
        registerReceiver(receiver, intentFilter)
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

    inner class LocalBinder: Binder() {
        fun getService(): WifiDirectService = this@WifiDirectService
    }
}