package com.solotodo.designsystem.modifiers

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solotodo.designsystem.SoloTokens

/**
 * CRT scanline overlay — thin horizontal lines at 4% opacity that drift downward
 * over a 20-second cycle. Mirrors the prototype's `@keyframes scan` animation.
 *
 * Disabled automatically when [enabled] is false — tie this to the user's
 * accessibility preference (prefers-reduced-motion or an in-app toggle).
 */
@Composable
fun Modifier.scanlineOverlay(
    enabled: Boolean = true,
    period: Dp = 3.dp,
    opacity: Float = SoloTokens.Scanline.opacity,
    cycleMs: Int = SoloTokens.Motion.ScanlineCycle.inWholeMilliseconds.toInt(),
): Modifier = if (!enabled) this else composed {
    val scrollDistancePx = remember { SoloTokens.Scanline.scrollDistance }
    val infinite = rememberInfiniteTransition(label = "scanlines")
    val offset by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = cycleMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "scanline-offset",
    )

    val lineColor = SoloTokens.Scanline.color.copy(alpha = opacity)

    this.drawWithCache {
        val periodPx = period.toPx()
        val scrollPx = scrollDistancePx.toPx() * offset
        val brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to lineColor,
                0.33f to lineColor,
                0.34f to androidx.compose.ui.graphics.Color.Transparent,
                1.00f to androidx.compose.ui.graphics.Color.Transparent,
            ),
            startY = scrollPx,
            endY = scrollPx + periodPx,
            tileMode = TileMode.Repeated,
        )
        onDrawWithContent {
            drawContent()
            drawRect(brush = brush, topLeft = Offset.Zero)
        }
    }
}
