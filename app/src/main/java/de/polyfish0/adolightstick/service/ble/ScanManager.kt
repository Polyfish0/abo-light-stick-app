package de.polyfish0.adolightstick.service.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context.BLUETOOTH_SERVICE
import android.util.Log
import androidx.annotation.RequiresPermission

class ScanManager(
    private val application: Application,
    private val onDeviceFound: (BluetoothDevice) -> Unit = {}
) : ScanCallback() {
    private val deviceBlacklist = HashSet<String>()
    private var deviceMacFilter: String? = null
    private var device: BluetoothDevice? = null
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        (application.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
    private val scanner: BluetoothLeScanner by lazy { bluetoothAdapter.bluetoothLeScanner }

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
        // If we filter for a specific device, we disable the signal strength check
        if(deviceMacFilter == null && result.rssi < -60)
            return

        device = result.device
        onDeviceFound(result.device)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startDeviceSearch() {
        Log.d("BLEService", "Start device scan")
        scanner.startScan(this)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopDeviceSearch() {
        Log.d("BLEService", "Stop device scan")
        scanner.stopScan(this)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun blacklistDevice() {
        if(device == null)
            return

        deviceBlacklist.add(device!!.address)
    }

    fun clearBlacklist() {
        deviceBlacklist.clear()
    }

    fun getRemoteDeviceByAddress(address: String) {
        Log.d("BLEService", "Connecting to BLE Device: $address")
        device = bluetoothAdapter.getRemoteDevice(address)
        onDeviceFound(device!!)
    }

    fun setDeviceMacFilter(address: String?) {
        deviceMacFilter = address
    }
}