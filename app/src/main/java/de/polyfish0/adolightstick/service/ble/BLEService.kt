package de.polyfish0.adolightstick.service.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.LinkedList
import java.util.UUID

class BLEService: Service() {
    private val binder = LocalBinder()
    private lateinit var gattManager: GattManager
    private val deviceBlacklist = HashSet<String>()
    private var deviceMacFilter: String? = null

    private val _device = MutableStateFlow<BluetoothDevice?>(null)
    val device: StateFlow<BluetoothDevice?> = _device

    private val _deviceReady = MutableStateFlow(false)
    val deviceReady: StateFlow<Boolean> = _deviceReady

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        (application.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    private val scanner: BluetoothLeScanner by lazy { bluetoothAdapter.bluetoothLeScanner }
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if(result == null || result.device == null || result.device.name == null)
                return

            if(!(result.device.name.equals("Ado Light Stick")))
                return

            if(deviceBlacklist.contains(result.device.address))
                return

            if(deviceMacFilter != null && result.device.address != deviceMacFilter)
                return

            // Based on real-world tests, -60 dBm seems to be a reliable threshold
            // for identifying the user's own light stick held in hand.
            // However, I can't predict how this will behave in a crowded environment
            // like just before a concert when thousands of people activate their light sticks.
            // It doesn’t matter which app they use - interference and signal overlap are unavoidable.
            if(result.rssi < -60)
                return

            _device.value = result.device
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startDeviceSearch() {
        Log.d("BLEService", "Start device scan")
        scanner.startScan(scanCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopDeviceSearch() {
        Log.d("BLEService", "Stop device scan")
        scanner.stopScan(scanCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun blacklistDevice() {
        if(_device.value == null)
            return

        deviceBlacklist.add(_device.value!!.address)
    }

    fun clearBlacklist() {
        deviceBlacklist.clear()
    }

    fun getRemoteDeviceByAddress(address: String) {
        Log.d("BLEService", "Connecting to BLE Device: $address")
        _device.value = bluetoothAdapter.getRemoteDevice(address)
    }

    fun setDeviceMacFilter(address: String?) {
        deviceMacFilter = address
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
    }

    override fun onDestroy() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            gattManager.disconnect()
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
}