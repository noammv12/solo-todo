package com.solotodo.ui.cinematics.parts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Angular hex frame drawn around the rank glyph during the CHARGE phase.
 * Two nested hexagons matching the `system.jsx` viewBox (0,0 → 64,64).
 *
 * [strokeWidth] is animated (especially on S rank, where it pulses 0.5↔2dp
 * over a 2.4s loop during CHARGE).
 */
@Composable
fun HexFrame(
    color: Color,
    alpha: Float = 1f,
    size: Dp = 220.dp,
    strokeWidth: Dp = 1.2.dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 64f
        val outer = hexPath(u, outer = true)
        drawPath(outer, color = color.copy(alpha = 0.5f * alpha), style = Stroke(width = strokeWidth.toPx()))
        val inner = hexPath(u, outer = false)
        drawPath(inner, color = color.copy(alpha = 0.3f * alpha), style = Stroke(width = (strokeWidth.toPx() * 0.66f)))
    }
}

private fun hexPath(u: Float, outer: Boolean): Path {
    val points = if (outer) {
        listOf(32f to 4f, 56f to 18f, 56f to 46f, 32f to 60f, 8f to 46f, 8f to 18f)
    } else {
        listOf(32f to 9f, 51f to 20f, 51f to 44f, 32f to 55f, 13f to 44f, 13f to 20f)
    }
    return Path().apply {
        moveTo(points[0].first * u, points[0].second * u)
        for (i in 1..5) lineTo(points[i].first * u, points[i].second * u)
        close()
    }
}
