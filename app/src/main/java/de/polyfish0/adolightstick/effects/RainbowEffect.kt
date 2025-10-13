package de.polyfish0.adolightstick.effects

class RainbowEffect : Effect(10) {
    private var hue = 0f

    override fun tick() {
        hue += 3f
        if (hue >= 360f) hue -= 360f

        val rgb = hsvToRgb(hue, 1f, 1f)
        onColorUpdate(rgb[0], rgb[1], rgb[2], 100)
    }

    private fun hsvToRgb(h: Float, s: Float, v: Float): IntArray {
        val c = v * s
        val x = c * (1 - kotlin.math.abs((h / 60f) % 2 - 1))
        val m = v - c

        val (r1, g1, b1) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        val r = ((r1 + m) * 255).toInt().coerceIn(0, 255)
        val g = ((g1 + m) * 255).toInt().coerceIn(0, 255)
        val b = ((b1 + m) * 255).toInt().coerceIn(0, 255)

        return intArrayOf(r, g, b)
    }
}