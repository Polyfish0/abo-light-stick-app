package de.polyfish0.adolightstick.routes

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import de.polyfish0.adolightstick.LightStickCommandBuilder
import de.polyfish0.adolightstick.service.BLEService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ColorViewModel(
    application: Application
) : AndroidViewModel(application) {
    private var bleService: BLEService? = null
    private val _bleServiceReady = MutableStateFlow(false)
    val bleServiceReady: StateFlow<Boolean> = _bleServiceReady

    var currentColor = mutableStateOf(Color(0x0, 0x68, 0x97))
        private set

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun updateColor(color: Color) {
        currentColor.value = color
        changeColor(color)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun changeColor(color: Color, brightness: Int = 100) {
        bleService?.addPackageToSendQueue(
            LightStickCommandBuilder.changeColor(
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt(),
                brightness.coerceIn(0, 100)
            )
        )
    }

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

    fun bindToService() {
        val intent = Intent(getApplication(), BLEService::class.java)
        getApplication<Application>().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        getApplication<Application>().unbindService(serviceConnection)
    }
}