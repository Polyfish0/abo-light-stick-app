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
import androidx.lifecycle.viewModelScope
import de.polyfish0.adolightstick.effects.Effect
import de.polyfish0.adolightstick.service.ble.BLEService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ColorViewModel(
    application: Application
) : AndroidViewModel(application) {
    private var bleService: BLEService? = null
    private val _bleServiceReady = MutableStateFlow(false)
    val bleServiceReady: StateFlow<Boolean> = _bleServiceReady

    private val _currentEffect = MutableStateFlow<Effect?>(null)
    val currentEffect: StateFlow<Effect?> = _currentEffect

    var currentColor = mutableStateOf(Color(0x0, 0x68, 0x97))
        private set

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun updateColor(color: Color) {
        currentColor.value = color
        changeColor(color)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun changeColor(color: Color, brightness: Int = 100) {
        bleService?.sendColor(
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt(),
            brightness.coerceIn(0, 100)
        )
    }

    fun updateEffect(effect: Effect?) {
        bleService?.updateEffect(effect)
        _currentEffect.value = effect
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bleService = (service as BLEService.LocalBinder).getService()
            _bleServiceReady.value = true

            viewModelScope.launch {
                bleService!!.currentEffect.collect { eff ->
                    _currentEffect.value = eff
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
            _bleServiceReady.value = false
            _currentEffect.value = null
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