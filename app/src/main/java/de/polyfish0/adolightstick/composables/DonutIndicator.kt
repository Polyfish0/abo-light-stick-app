package de.polyfish0.adolightstick.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DonutIndicator(
    color: Color,
    thickness: Dp = 12.dp,
    size: Dp = 256.dp
) {
    Canvas(modifier = Modifier.size(size)) {
        val radius = size.toPx() / 2f
        drawCircle(
            color = color,
            radius = radius - thickness.toPx() / 2f,
            center = Offset(radius, radius),
            style = Stroke(width = thickness.toPx())
        )
    }
}
