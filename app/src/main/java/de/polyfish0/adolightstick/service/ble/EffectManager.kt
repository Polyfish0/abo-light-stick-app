package de.polyfish0.adolightstick.service.ble

import de.polyfish0.adolightstick.effects.Effect

class EffectManager(
    private val onCurrentEffectChange: (Effect?) -> Unit,
    private val onColorUpdate: (r: Int, g: Int, b: Int, brightness: Int) -> Unit
) {
    private var _currentEffect: Effect? = null

    fun start(effect: Effect) {
        _currentEffect?.interrupt()
        _currentEffect = effect
        _currentEffect?.onColorUpdate(onColorUpdate)
        onCurrentEffectChange(_currentEffect)
        _currentEffect!!.start()
    }

    fun stop() {
        _currentEffect?.interrupt()
        _currentEffect = null
        onCurrentEffectChange(null)
    }
}