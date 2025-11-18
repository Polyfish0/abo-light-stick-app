package de.polyfish0.adolightstick.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBus {
    // TODO: Implement events into the BLE subsystem
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    suspend fun postEvent(event: Event) {
        _events.emit(event)
    }
}