package de.polyfish0.adolightstick.effects

abstract class Effect(
    var delay: Long
) : Thread() {
    protected var onColorUpdate: (Int, Int, Int, Int) -> Unit = { _, _, _, _, -> throw NotImplementedError("onColorUpdate needs to be implemented") }

    fun onColorUpdate(callback: (Int, Int, Int, Int) -> Unit) {
        onColorUpdate = callback
    }

    override fun run() {
        try {
            while (true) {
                tick()
                sleep(delay)
            }
        } catch (_: InterruptedException) { }
    }

    abstract fun tick()
}