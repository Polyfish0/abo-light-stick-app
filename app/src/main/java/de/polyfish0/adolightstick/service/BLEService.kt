package de.polyfish0.adolightstick.service

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
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import de.polyfish0.adolightstick.LightStickCommandBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.LinkedList
import java.util.UUID

class BLEService: Service() {
    private val binder = LocalBinder()
    private val packageQueue = LinkedList<ByteArray>()
    private var isTransmitting = false
    private var lightStickService: BluetoothGattService? = null
    private var lightStickCharacteristic: BluetoothGattCharacteristic? = null
    private var lightStickGatt: BluetoothGatt? = null

    private val _device = MutableStateFlow<BluetoothDevice?>(null)
    val device: StateFlow<BluetoothDevice?> = _device

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

    private val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if(newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BLEService", "Gatt connected")

                if(gatt == null) {
                    Log.d("BLEService", "gatt in onConnectionStateChange is null")
                    return
                }

                packageQueue.add(byteArrayOf(
                    0xD2.toByte(),
                    0xFC.toByte(),
                    0x45,
                    0x6A,
                    0x57,
                    0xC9.toByte(),
                    0x8F.toByte(),
                    0xD9.toByte(),
                    0xA3.toByte(),
                    0x06,
                    0x8E.toByte(),
                    0x0F,
                    0xCA.toByte(),
                    0x9A.toByte(),
                    0x91.toByte(),
                    0xC7.toByte()
                ))

                lightStickGatt = gatt
                gatt.discoverServices()
            }else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BLEService", "Gatt disconnected")

                packageQueue.clear()
                lightStickGatt = null
                lightStickService = null
                lightStickCharacteristic = null
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            lightStickService = gatt?.getService(UUID.fromString("00000000-0000-1000-8000-00805f9b34fb"))
            lightStickCharacteristic = lightStickService?.getCharacteristic(UUID.fromString("00000000-0000-1000-8000-00805f9b34fb"))

            addPackageToSendQueue(LightStickCommandBuilder.changeColor(0, 255, 0, 100))
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {

            if(packageQueue.isNotEmpty()) {
                sendNextPackage()
            }else {
                isTransmitting = false
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendNextPackage() {
        if(lightStickService == null || lightStickCharacteristic == null) {
            throw RuntimeException("LightStickService or Characteristic is null")
            return
        }

        isTransmitting = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            lightStickGatt?.writeCharacteristic(lightStickCharacteristic!!, packageQueue.pop(), BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
        }else {
            lightStickCharacteristic!!.setValue(packageQueue.pop())
            lightStickGatt?.writeCharacteristic(lightStickCharacteristic)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun addPackageToSendQueue(data: ByteArray) {
        packageQueue.add(data)

        if(!isTransmitting)
            sendNextPackage()
    }

    fun getRemoteDeviceByAddress(address: String) {
        Log.d("BLEService", "Connecting to BLE Device: $address")
        _device.value = bluetoothAdapter.getRemoteDevice(address)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToGatt() {
        Log.d("BLEService", "Connecting to GATT server")
        _device.value?.connectGatt(applicationContext, false, gattCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        Log.d("BLEService", "Disconnect from GATT server")
        lightStickGatt.disconnect()
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

    inner class LocalBinder: Binder() {
        fun getService(): BLEService = this@BLEService
    }
    
    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
}