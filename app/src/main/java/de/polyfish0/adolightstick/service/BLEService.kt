package de.polyfish0.adolightstick.service

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.IBinder
import android.util.Log

class BLEService : Service() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var scanner: BluetoothLeScanner
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d("BLE", "Found: ${result?.device?.name} - ${result?.device?.address}")
        }
    }

    override fun onCreate() {
        super.onCreate()
        bluetoothAdapter = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
        scanner = bluetoothAdapter.bluetoothLeScanner
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scanner.startScan(scanCallback)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scanner.stopScan(scanCallback)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}