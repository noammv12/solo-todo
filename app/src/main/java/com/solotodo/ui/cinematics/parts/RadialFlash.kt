package com.solotodo.ui.cinematics.parts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Fullscreen radial gradient used as the opening flash of every cinematic.
 *
 * [opacity] is driven from outside via an `animateFloatAsState` running over
 * the flash window (400ms for D/C/B/A, 700ms for S). Peak at mid-phase,
 * fading on either side — caller supplies the curve.
 */
@Composable
fun RadialFlash(
    opacity: Float,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                if (opacity <= 0f) return@drawBehind
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = maxOf(size.width, size.height) * 0.75f
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(tint.copy(alpha = opacity), Color.Transparent),
                        center = center,
                        radius = radius,
                    ),
                )
            },
    )
}
