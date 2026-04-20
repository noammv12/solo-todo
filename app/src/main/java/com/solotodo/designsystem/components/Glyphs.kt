package com.solotodo.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solotodo.designsystem.SoloTokens

/**
 * Quest diamond icon — used in task rows, DQ progress dots.
 *
 * Variants:
 *  - `filled`  — rotated square is filled with [color]
 *  - `ring`    — draws an outer ghost ring around the diamond (empty-state hint)
 */
@Composable
fun QuestGlyph(
    modifier: Modifier = Modifier,
    size: Dp = 14.dp,
    filled: Boolean = false,
    color: Color = SoloTokens.Colors.Stroke,
    ring: Boolean = false,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val center = Offset(w / 2f, w / 2f)
        val outerHalf = w * 0.43f
        val innerHalf = w * 0.25f

        rotate(degrees = 45f, pivot = center) {
            if (ring) {
                drawRect(
                    color = color.copy(alpha = 0.4f),
                    topLeft = Offset(w * 0.07f, w * 0.07f),
                    size = androidx.compose.ui.geometry.Size(w * 0.86f, w * 0.86f),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            val innerTopLeft = Offset(center.x - innerHalf, center.y - innerHalf)
            val innerSize = androidx.compose.ui.geometry.Size(innerHalf * 2, innerHalf * 2)
            if (filled) {
                drawRect(color = color, topLeft = innerTopLeft, size = innerSize)
            }
            drawRect(
                color = color,
                topLeft = innerTopLeft,
                size = innerSize,
                style = Stroke(width = 1.2.dp.toPx()),
            )
            // outerHalf is used implicitly — fills the visual footprint; keeping the
            // variable makes it easy to re-tune if the spec changes proportions.
            @Suppress("UnusedPrivateProperty")
            val _marker = outerHalf
        }

        drawCircle(color = color, radius = 1.dp.toPx(), center = center)
    }
}

/**
 * Dungeon gate icon — arched frame with an inner door outline.
 * Used in Dungeon list rows and Open Gate cinematic.
 */
@Composable
fun GateGlyph(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    color: Color = SoloTokens.Colors.Stroke,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val u = w / 18f
        val stroke1 = Stroke(width = 1.4.dp.toPx())
        val stroke2 = Stroke(width = 1.2.dp.toPx())

        val outer = Path().apply {
            moveTo(2f * u, 16f * u)
            lineTo(2f * u, 6f * u)
            quadraticBezierTo(2f * u, 2f * u, 6f * u, 2f * u)
            lineTo(12f * u, 2f * u)
            quadraticBezierTo(16f * u, 2f * u, 16f * u, 6f * u)
            lineTo(16f * u, 16f * u)
        }
        drawPath(outer, color = color, style = stroke1)

        val inner = Path().apply {
            moveTo(6f * u, 16f * u)
            lineTo(6f * u, 9f * u)
            quadraticBezierTo(6f * u, 6.5f * u, 9f * u, 6.5f * u)
            quadraticBezierTo(12f * u, 6.5f * u, 12f * u, 9f * u)
            lineTo(12f * u, 16f * u)
        }
        drawPath(inner, color = color.copy(alpha = 0.6f), style = stroke2)

        drawLine(
            color = color,
            start = Offset(2f * u, 16f * u),
            end = Offset(16f * u, 16f * u),
            strokeWidth = 1.4.dp.toPx(),
        )
    }
}

/**
 * Three shadow soldiers — used in Shadow Archive entry and archive nav icon.
 */
@Composable
fun ShadowGlyph(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    color: Color = SoloTokens.Colors.Stroke,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val u = w / 18f
        val stroke = Stroke(width = 1.dp.toPx())

        listOf(0f, 6f, 12f).forEach { xBase ->
            drawCircle(
                color = color,
                radius = 1.8f * u,
                center = Offset((xBase + 3f) * u, 5f * u),
                style = stroke,
            )
            val body = Path().apply {
                moveTo((xBase + 0.5f) * u, 16f * u)
                quadraticBezierTo((xBase + 3f) * u, 9f * u, (xBase + 5.5f) * u, 16f * u)
            }
            drawPath(body, color = color, style = stroke)
        }
    }
}

/**
 * Four-corner bracket decorations used around cinematic frames and cards.
 * Each bracket is `[` at a corner rotation.
 */
@Composable
fun CornerBrackets(
    modifier: Modifier = Modifier,
    length: Dp = 12.dp,
    inset: Dp = 4.dp,
    color: Color = SoloTokens.Colors.Stroke,
) {
    Canvas(modifier = modifier) {
        val lenPx = length.toPx()
        val insetPx = inset.toPx()
        val strokePx = 1.dp.toPx()
        val w = this.size.width
        val h = this.size.height

        // Top-left
        drawLine(color, Offset(insetPx, insetPx), Offset(insetPx + lenPx, insetPx), strokePx)
        drawLine(color, Offset(insetPx, insetPx), Offset(insetPx, insetPx + lenPx), strokePx)
        // Top-right
        drawLine(color, Offset(w - insetPx - lenPx, insetPx), Offset(w - insetPx, insetPx), strokePx)
        drawLine(color, Offset(w - insetPx, insetPx), Offset(w - insetPx, insetPx + lenPx), strokePx)
        // Bottom-left
        drawLine(color, Offset(insetPx, h - insetPx - lenPx), Offset(insetPx, h - insetPx), strokePx)
        drawLine(color, Offset(insetPx, h - insetPx), Offset(insetPx + lenPx, h - insetPx), strokePx)
        // Bottom-right
        drawLine(color, Offset(w - insetPx - lenPx, h - insetPx), Offset(w - insetPx, h - insetPx), strokePx)
        drawLine(color, Offset(w - insetPx, h - insetPx - lenPx), Offset(w - insetPx, h - insetPx), strokePx)
    }
}
