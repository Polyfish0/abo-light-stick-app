package de.polyfish0.adolightstick.service.wifidirect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pManager

class WiFiDirectBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Determine if Wi-Fi Direct mode is enabled or not, alert
                // the Activity.
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {

                // The peer list has changed! We should probably do something about
                // that.

            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {

                // Connection state changed! We should probably do something about
                // that.

            }
        }
    }
}