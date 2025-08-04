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
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.polyfish0.adolightstick.LightStickCommandBuilder
import de.polyfish0.adolightstick.service.ble.BLEService
import de.polyfish0.adolightstick.utils.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LightStickSetupViewModel(
    application: Application
) : AndroidViewModel(application) {
    val device: StateFlow<BluetoothDevice?> get() = bleService?.device ?: MutableStateFlow(null)
    val deviceReady: StateFlow<Boolean> get() = bleService?.deviceReady ?: MutableStateFlow(false)
    val repository: SettingsRepository = SettingsRepository(application.applicationContext)

    private val _bleServiceReady = MutableStateFlow(false)
    val bleServiceReady: StateFlow<Boolean> = _bleServiceReady
    val savedLightStickMac = repository.lightStickMac.stateIn(viewModelScope, SharingStarted.Lazily, null)

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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToGatt() {
        bleService?.connect()
    }

    fun connectToStoredLightStick() {
        if(savedLightStickMac.value != null)
            bleService?.getRemoteDeviceByAddress(savedLightStickMac.value!!)
    }

    fun clearBlackList() {
        bleService?.clearBlacklist()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun changeColor(color: Color, brightness: Int = 100) {
        bleService?.sendData(
            LightStickCommandBuilder.changeColor(
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt(),
                brightness.coerceIn(0, 100)
            )
        )
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    fun blacklistCurrentDevice() {
        bleService?.blacklistDevice()
        bleService?.disconnect()
        bleService?.startDeviceSearch()
    }

    fun saveMacAddress() {
        if(device.value == null)
            throw RuntimeException("device is null")

        viewModelScope.launch {
            repository.setLightStickMac(device.value!!.address)
        }
    }

    fun deleteStoredMac() {
        viewModelScope.launch {
            repository.deleteLightStickMac()
        }
    }

    fun setMacFilterInService(mac: String?) {
        bleService?.setDeviceMacFilter(mac)
    }

    fun getRemoteDeviceByAddress(address: String) {
        bleService?.getRemoteDeviceByAddress(address)
    }

    fun bindToService() {
        val intent = Intent(getApplication(), BLEService::class.java)
        getApplication<Application>().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        getApplication<Application>().unbindService(serviceConnection)
    }
}