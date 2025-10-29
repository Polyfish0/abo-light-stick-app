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
import de.polyfish0.adolightstick.utils.wifiDirectSupported

class WifiDirectService: Service() {
    private val binder: LocalBinder = LocalBinder()
    private val receiver: WiFiDirectBroadcastReceiver = WiFiDirectBroadcastReceiver()
    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private val handler = Handler(Looper.getMainLooper())
    private var currentServiceRequest: WifiP2pDnsSdServiceRequest? = null
    private var discoveryCallback: ((device: WifiP2pDevice, txt: Map<String, String>) -> Unit)? = null

    private val TAG = "WifiDirectHelper"
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

    fun registerService(instanceName: String, serviceType: String, txtRecord: Map<String, String>) {
        val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(instanceName, serviceType, txtRecord)
        manager.stopPeerDiscovery(channel, object : WifiP2pManager.ActionListener {
            override fun onFailure(reason: Int) {
                TODO("Not yet implemented")
            }

            override fun onSuccess() {
                Log.d("WifiDirectHelper", "Peer discovery gestoppt")
                clearAndAddService(serviceInfo)
            }
        })
    }

    private fun clearAndAddService(serviceInfo: WifiP2pDnsSdServiceInfo, attempts: Int = 0) {
        if (attempts >= 5) {
            Log.e(TAG, "Gebe auf nach $attempts Versuchen")
            return
        }

        manager.clearLocalServices(channel, object : WifiP2pManager.ActionListener {
            @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
            override fun onSuccess() {
                Log.d(TAG, "Local services erfolgreich gelöscht")

                manager.addLocalService(channel, serviceInfo, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.i(TAG, "Service erfolgreich registriert")
                    }

                    override fun onFailure(reason: Int) {
                        Log.e(TAG, "addLocalService failed: $reason")
                    }
                })
            }

            override fun onFailure(reason: Int) {
                if (reason == WifiP2pManager.BUSY) {
                    Log.w(TAG, "BUSY beim Löschen – retry in 500ms")
                    handler.postDelayed({
                        clearAndAddService(serviceInfo, attempts + 1)
                    }, 500)
                } else {
                    Log.e(TAG, "clearLocalServices failed: $reason")
                }
            }
        })
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun discoverServices(onServiceFound: (device: WifiP2pDevice, txt: Map<String, String>) -> Unit) {
        this.discoveryCallback = onServiceFound

        // 1. TXT record listener
        manager.setDnsSdResponseListeners(channel,
            { instanceName, regType, device ->
                Log.d(TAG, "Service entdeckt: $instanceName von ${device.deviceName}")
                // Die eigentlichen Daten kommen im TXT-Record
            },
            { fullDomain, txtRecord, device ->
                Log.d(TAG, "TXT-Record: $txtRecord von ${device.deviceName}")
                discoveryCallback?.invoke(device, txtRecord)
            }
        )

        // 2. Add Service Request
        currentServiceRequest?.let {
            manager.removeServiceRequest(channel, it, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "Alte Service-Request entfernt")
                }

                override fun onFailure(reason: Int) {
                    Log.w(TAG, "removeServiceRequest fehlgeschlagen: $reason")
                }
            })
        }

        val request = WifiP2pDnsSdServiceRequest.newInstance()
        currentServiceRequest = request

        manager.addServiceRequest(channel, request, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Service request hinzugefügt")
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "addServiceRequest failed: $reason")
            }
        })

        // 3. Start discovery
        manager.discoverServices(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Service discovery gestartet")
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "discoverServices failed: $reason")
            }
        })
    }

    override fun onCreate() {
        super.onCreate()

        manager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)
        registerReceiver(receiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
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