package de.polyfish0.adolightstick.utils

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class SerializableColor(val r: Int, val g: Int, val b: Int, val a: Int) {
    fun toComposeColor(): Color = Color(r, g, b, a)
}
