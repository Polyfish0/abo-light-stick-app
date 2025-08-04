package de.polyfish0.adolightstick.service.ble

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import java.util.LinkedList
import java.util.UUID

class GattManager(
    private val context: Context,
    private val onReady: () -> Unit = {},
    private val onDisconnected: () -> Unit = {}
) : BluetoothGattCallback() {
    private val packageQueue = LinkedList<ByteArray>()
    private var gatt: BluetoothGatt? = null
    private var lightStickCharacteristic: BluetoothGattCharacteristic? = null
    private var isTransmitting = false

    companion object {
        val LIGHTSTICK_UUID: UUID = UUID.fromString("00000000-0000-1000-8000-00805f9b34fb")
    }

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

            this@GattManager.gatt = gatt
            gatt.discoverServices()
        }else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.d("BLEService", "Gatt disconnected")

            packageQueue.clear()
            this@GattManager.gatt = null
            lightStickCharacteristic = null
            onDisconnected()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        val lightStickService = gatt?.getService(LIGHTSTICK_UUID)
        lightStickCharacteristic = lightStickService?.getCharacteristic(LIGHTSTICK_UUID)
        onReady()
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendNextPackage() {
        if(lightStickCharacteristic == null) {
            throw RuntimeException("Characteristic is null")
        }

        if(packageQueue.isEmpty())
            return

        isTransmitting = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt?.writeCharacteristic(lightStickCharacteristic!!, packageQueue.pop(), BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
        }else {
            lightStickCharacteristic!!.setValue(packageQueue.pop())
            gatt?.writeCharacteristic(lightStickCharacteristic)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun addPackageToSendQueue(data: ByteArray) {
        packageQueue.add(data)

        if(!isTransmitting)
            sendNextPackage()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(device: BluetoothDevice) {
        Log.d("BLEService", "Connecting to GATT server")
        device.connectGatt(context, false, this)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        Log.d("BLEService", "Disconnect from GATT server")

        gatt?.disconnect()
        gatt?.close()
        gatt = null
        lightStickCharacteristic = null
        isTransmitting = false
        packageQueue.clear()
    }
}