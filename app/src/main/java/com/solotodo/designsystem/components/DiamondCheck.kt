package com.solotodo.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solotodo.designsystem.SoloTokens

/**
 * Diamond checkbox — rotated square that fills with cyan when checked, with a
 * quick checkmark rendered on top in the void colour.
 *
 * Used everywhere a task is toggled in the Daily Quest widget and task rows.
 *
 * Animates fill and stroke colours over 200ms on state change.
 */
@Composable
fun DiamondCheck(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
) {
    val fillTarget = if (checked) SoloTokens.Colors.Glow else androidx.compose.ui.graphics.Color.Transparent
    val strokeTarget = if (checked) SoloTokens.Colors.Glow else SoloTokens.Colors.Stroke
    val animSpec = tween<androidx.compose.ui.graphics.Color>(
        durationMillis = 200,
    )
    val fill by animateColorAsState(targetValue = fillTarget, animationSpec = animSpec, label = "dc-fill")
    val stroke by animateColorAsState(targetValue = strokeTarget, animationSpec = animSpec, label = "dc-stroke")

    val interactionSource = remember { MutableInteractionSource() }

    Canvas(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { onCheckedChange(!checked) },
    ) {
        val w = this.size.width
        val center = Offset(w / 2f, w / 2f)

        rotate(degrees = 45f, pivot = center) {
            val squareSide = w * 0.64f
            val topLeft = Offset(center.x - squareSide / 2f, center.y - squareSide / 2f)
            val sz = androidx.compose.ui.geometry.Size(squareSide, squareSide)
            drawRect(color = fill, topLeft = topLeft, size = sz)
            drawRect(color = stroke, topLeft = topLeft, size = sz, style = Stroke(width = 1.3.dp.toPx()))
        }

        if (checked) {
            // Checkmark — drawn un-rotated (the square rotation is just visual).
            val tick = Path().apply {
                moveTo(w * 0.32f, w * 0.50f)
                lineTo(w * 0.45f, w * 0.64f)
                lineTo(w * 0.68f, w * 0.36f)
            }
            drawPath(
                path = tick,
                color = SoloTokens.Colors.BgVoid,
                style = Stroke(
                    width = 1.8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }
    }
}
