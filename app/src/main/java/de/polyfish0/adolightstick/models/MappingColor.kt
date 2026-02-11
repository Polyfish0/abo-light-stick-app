package de.polyfish0.adolightstick.models

import androidx.compose.ui.graphics.Color

// Each Mapping color is associated to a time in the MappingBuilder
// The manual color is the one selected by the user
class MappingColor(val manualColor: Color? = null, val defaultColor: Color = Color.Black)