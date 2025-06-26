package de.polyfish0.adolightstick.routes.setup

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import de.polyfish0.adolightstick.service.BLEService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LightStickSetupViewModel(application: Application) : AndroidViewModel(application) {
    val device: StateFlow<BluetoothDevice?> get() = bleService?.device ?: MutableStateFlow(null)

    private val _bleServiceReady = MutableStateFlow(false)
    val bleServiceReady: StateFlow<Boolean> = _bleServiceReady

    private var bleService: BLEService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bleService = (service as BLEService.LocalBinder).getService()
            _bleServiceReady.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
            _bleServiceReady.value = false
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startDeviceSearch() {
        bleService?.startDeviceSearch()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopDeviceSearch() {
        bleService?.stopDeviceSearch()
    }

    fun getRemoteDeviceByAddress(address: String) {
        bleService?.getRemoteDeviceByAddress(address)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToGatt() {
        bleService?.connectToGatt()
    }

    fun bindToService() {
        val intent = Intent(getApplication(), BLEService::class.java)
        getApplication<Application>().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        getApplication<Application>().unbindService(serviceConnection)
    }
}