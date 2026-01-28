package de.polyfish0.adolightstick.service.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import de.polyfish0.adolightstick.LightStickCommandBuilder
import de.polyfish0.adolightstick.effects.Effect
import de.polyfish0.adolightstick.service.ble.EffectManager
import de.polyfish0.adolightstick.exceptions.EffectInUseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BLEService: Service() {
    private val binder = LocalBinder()
    private val NOTIFICATION_ID = 1337
    private val CHANNEL_ID = "AdoLightStickServiceChannel"
    private lateinit var gattManager: GattManager
    private lateinit var scanner: ScanManager
    private lateinit var effectManager: EffectManager

    private val _device = MutableStateFlow<BluetoothDevice?>(null)
    val device: StateFlow<BluetoothDevice?> = _device

    private val _deviceReady = MutableStateFlow(false)
    val deviceReady: StateFlow<Boolean> = _deviceReady

    private val _currentEffect = MutableStateFlow<Effect?>(null)
    val currentEffect: StateFlow<Effect?> = _currentEffect

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
    fun connect() {
        if(_device.value == null)
            throw RuntimeException("Device is null")

        gattManager.connect(_device.value!!)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        gattManager.disconnect()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendColor(r: Int, g: Int, b: Int, brightness: Int) {
        if(_currentEffect.value != null)
            throw EffectInUseException("manual color change is not possible while an effect is running")

        sendColorInternal(r, g, b, brightness)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun sendColorInternal(r: Int, g: Int, b: Int, brightness: Int) {
        gattManager.addPackageToSendQueue(LightStickCommandBuilder.changeColor(r, g, b, brightness))
    }

    fun updateEffect(effect: Effect?) {
        _currentEffect.value = effect

        if(_currentEffect.value != null)
            effectManager.start(_currentEffect.value!!)
        else
            effectManager.stop()
    }

    fun startForegroundServiceWithNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Light Stick Connection",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ado Light Stick")
            .setContentText("Keeping connection alive...")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .build()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        }else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    inner class LocalBinder: Binder() {
        fun getService(): BLEService = this@BLEService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission")
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
        effectManager = EffectManager(
            { _currentEffect.value = it },
            { r, g, b, a -> sendColorInternal(r, g, b, a)}
        )

        startForegroundServiceWithNotification()
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