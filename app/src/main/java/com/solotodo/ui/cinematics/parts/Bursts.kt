package com.solotodo.ui.cinematics.parts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * Radial bursts that emanate from center at [intervalMs] intervals after
 * [startAtMs] kicks them off. Each burst appears as a short-lived dot on a
 * ring around the glyph — D has 0, C has 1, B has 2, A has 3, S has 5 (per
 * `sheets.jsx` rank-up cinematic spec).
 */
@Composable
fun Bursts(
    count: Int,
    startAtMs: Long,
    intervalMs: Long = 220,
    travelDistance: Dp = 300.dp,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (count <= 0) return

    var visible by remember(count, startAtMs) { mutableIntStateOf(0) }
    LaunchedEffect(count, startAtMs, intervalMs) {
        delay(startAtMs)
        repeat(count) { i ->
            visible = i + 1
            if (i < count - 1) delay(intervalMs)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val travel = travelDistance.toPx()
        for (i in 0 until visible) {
            // Even angular distribution around the circle.
            val angle = (i.toDouble() / count.coerceAtLeast(1)) * 2.0 * Math.PI
            val dx = (cos(angle) * travel).toFloat()
            val dy = (sin(angle) * travel).toFloat()
            drawCircle(
                color = color.copy(alpha = 0.9f),
                radius = 3.dp.toPx(),
                center = Offset(center.x + dx, center.y + dy),
            )
        }
    }
}
