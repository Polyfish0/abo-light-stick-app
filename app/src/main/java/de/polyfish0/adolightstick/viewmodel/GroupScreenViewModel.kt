package de.polyfish0.adolightstick.viewmodel

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import de.polyfish0.adolightstick.service.wifidirect.WifiDirectService

class GroupScreenViewModel(
    application: Application
) : AndroidViewModel(application) {
    private var wifiDirectService: WifiDirectService? = null

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun startDiscovery() {
        wifiDirectService!!.startDiscovery()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun registerService() {
        wifiDirectService!!.registerService(
            instanceName = "adoLightStickGroup",
            serviceType = "_colorSync._tcp",
            txtRecord = mapOf("groupname" to "Knie tut weh :painchamp:")
        )
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            wifiDirectService = (service as WifiDirectService.LocalBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            wifiDirectService = null
        }
    }

    fun bindToService() {
        val intent = Intent(getApplication(), WifiDirectService::class.java)
        getApplication<Application>().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        getApplication<Application>().unbindService(serviceConnection)
    }
}