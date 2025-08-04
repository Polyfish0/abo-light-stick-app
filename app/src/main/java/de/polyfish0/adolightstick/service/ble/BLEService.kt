package de.polyfish0.adolightstick.service.ble

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BLEService: Service() {
    private val binder = LocalBinder()
    private lateinit var gattManager: GattManager
    private lateinit var scanner: ScanManager

    private val _device = MutableStateFlow<BluetoothDevice?>(null)
    val device: StateFlow<BluetoothDevice?> = _device

    private val _deviceReady = MutableStateFlow(false)
    val deviceReady: StateFlow<Boolean> = _deviceReady

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startDeviceSearch() {
        Log.d("BLEService", "Start device scan")
        scanner.startDeviceSearch()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopDeviceSearch() {
        Log.d("BLEService", "Stop device scan")
        scanner.stopDeviceSearch()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun blacklistDevice() {
        scanner.blacklistDevice()
    }

    fun clearBlacklist() {
        scanner.clearBlacklist()
    }

    fun getRemoteDeviceByAddress(address: String) {
        Log.d("BLEService", "Connecting to BLE Device: $address")
        scanner.getRemoteDeviceByAddress(address)
    }

    fun setDeviceMacFilter(address: String?) {
        scanner.setDeviceMacFilter(address)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendData(data: ByteArray) {
        gattManager.addPackageToSendQueue(data)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect() {
        if(_device.value == null)
            throw RuntimeException("Device is null")

        gattManager.connect(_device.value!!)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        gattManager.disconnect()
    }

    inner class LocalBinder: Binder() {
        fun getService(): BLEService = this@BLEService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        gattManager = GattManager(
            applicationContext,
            onReady = { _deviceReady.value = true },
            onDisconnected = { _deviceReady.value = false }
        )
        scanner = ScanManager(
            application,
            onDeviceFound = { _device.value = it }
        )
    }

    override fun onDestroy() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            gattManager.disconnect()
            scanner.stopDeviceSearch()
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
}