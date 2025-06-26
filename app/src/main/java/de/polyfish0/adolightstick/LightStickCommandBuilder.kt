package de.polyfish0.adolightstick

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object LightStickCommandBuilder {
    val cipher = Cipher.getInstance("AES/ECB/NoPadding")

    init {
        cipher.init(
            Cipher.ENCRYPT_MODE,
            SecretKeySpec(byteArrayOf(
                0x21.toByte(), 0x74.toByte(), 0x51.toByte(), 0x64.toByte(),
                0x73.toByte(), 0xA1.toByte(), 0xF5.toByte(), 0x35.toByte(),
                0x10.toByte(), 0x04.toByte(), 0xA1.toByte(), 0x3E.toByte(),
                0x6B.toByte(), 0x71.toByte(), 0x6A.toByte(), 0xB9.toByte()
            ), "AES")
        )
    }

    fun changeColor(r: Int, g: Int, b: Int, brightness: Int): ByteArray {
        val red = r * (brightness / 100)
        val green = g * (brightness / 100)
        val blue = b * (brightness / 100)

        return cipher.doFinal(byteArrayOf(
            0x82.toByte(),
            0x01,
            red.toByte(),
            green.toByte(),
            blue.toByte(),
            0x00.toByte(),
            0x00, 0x00, 0x00, 0x00,
            0xFD.toByte(),
            0x47, 0x52, 0x41, 0x56, 0x45
        ))
    }
}