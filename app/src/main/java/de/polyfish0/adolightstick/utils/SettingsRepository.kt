package de.polyfish0.adolightstick.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val LIGHT_STICK_MAC = stringPreferencesKey("lightStickMacAddress")
    }

    val lightStickMac: Flow<String> = dataStore.data.map { prefs -> prefs[LIGHT_STICK_MAC] ?: "" }

    suspend fun setLightStickMac(mac: String) {
        dataStore.edit { prefs ->
            prefs[LIGHT_STICK_MAC] = mac
        }
    }

    suspend fun deleteLightStickMac() {
        dataStore.edit { prefs ->
            prefs.remove(LIGHT_STICK_MAC)
        }
    }
}