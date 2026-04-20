package com.solotodo.ui.cinematics.parts

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * S-rank only — a hex ring whose stroke-width pulses 0.5dp ↔ 2dp on a 2.4s
 * infinite loop during the CHARGE phase. Wraps [HexFrame] with the animated
 * stroke.
 */
@Composable
fun HexRing(
    color: Color,
    size: Dp = 320.dp,
    alpha: Float = 1f,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "hex-ring")
    val stroke by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hex-ring-stroke",
    )
    HexFrame(
        color = color,
        alpha = alpha,
        size = size,
        strokeWidth = stroke.dp,
        modifier = modifier,
    )
}
