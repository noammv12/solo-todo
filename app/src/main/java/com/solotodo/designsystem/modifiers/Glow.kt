package com.solotodo.designsystem.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.solotodo.designsystem.SoloTokens

/**
 * Double-radius glow rendered behind the element. Compose has no text-shadow, so
 * we approximate the prototype's CSS `text-shadow: 0 0 Npx colorA, 0 0 Mpx colorB`
 * by painting two soft circles in a `drawBehind` pass.
 *
 * Use on Text and small icons where the shadow needs to escape the draw region.
 * For Phase 5 cinematic polish this will be upgraded to a RenderEffect blur.
 */
fun Modifier.glow(spec: SoloTokens.Glow.Spec): Modifier =
    this.drawBehind {
        drawCircleFalloff(
            center = Offset(size.width / 2f, size.height / 2f),
            radiusPx = spec.outerRadiusDp.dp.toPx(),
            color = spec.color.copy(alpha = spec.outerAlpha),
        )
        drawCircleFalloff(
            center = Offset(size.width / 2f, size.height / 2f),
            radiusPx = spec.innerRadiusDp.dp.toPx(),
            color = spec.color.copy(alpha = spec.innerAlpha),
        )
    }

private fun DrawScope.drawCircleFalloff(center: Offset, radiusPx: Float, color: Color) {
    if (radiusPx <= 0f) return
    drawCircle(color = color, radius = radiusPx, center = center)
}
