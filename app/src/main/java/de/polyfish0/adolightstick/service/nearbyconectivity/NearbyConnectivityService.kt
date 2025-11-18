package de.polyfish0.adolightstick.service.nearbyconectivity

import android.Manifest
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.annotation.RequiresPermission
import de.polyfish0.adolightstick.events.Event
import de.polyfish0.adolightstick.events.EventBus
import de.polyfish0.adolightstick.utils.wifiDirectSupported
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NearbyConnectivityService : Service() {
    private val binder: LocalBinder = LocalBinder()
    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)
    private val nearbyScanner = NearbyScan(this, scope = serviceScope)
    private var joinedGroup: Group? = null
    private val foundGroups = HashSet<Group>()

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun createGroup(name: String = "Change Name in the settings", maxUserCount: Int = 6) {
        if(joinedGroup != null)
            disconnectGroup()

        nearbyScanner.stopAdvertising()
        nearbyScanner.stopDiscovery()

        joinedGroup = Group(name = name, maxUserCount = maxUserCount, currentHost = null)
        nearbyScanner.startAdvertising(joinedGroup!!.getName())
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun searchGroups() {
        if(joinedGroup != null)
            disconnectGroup()

        nearbyScanner.stopAdvertising()
        nearbyScanner.startDiscovery()
    }

    fun joinGroup() {

    }

    fun disconnectGroup() {
        if(joinedGroup != null) {
            joinedGroup == null
        }
        // TODO: Leave command senden
    }

    private fun subscribeToEvents() {
        serviceScope.launch {
            EventBus.events.collect {
                when(it) {
                    is Event. -> {
                        //foundGroups.add()
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!wifiDirectSupported(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    inner class LocalBinder : Binder() {
        fun getService(): NearbyConnectivityService = this@NearbyConnectivityService
    }

    companion object {
        private const val TAG = "NearbyConnectivityService"
    }
}