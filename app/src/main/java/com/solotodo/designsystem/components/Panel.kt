package com.solotodo.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solotodo.designsystem.SoloTokens

/**
 * Panel — the base chamfered surface that every Solo ToDo UI region sits on.
 *
 * The chamfer is cut from the top-left and bottom-right corners, producing a
 * hexagonal outline:
 *
 *       (c,0)────────────────(w,0)
 *         /                     │
 *        /                      │
 *     (0,c)                  (w, h-c)
 *       │                       \
 *       │                        \
 *       (0,h)───────────────(w-c, h)
 *
 * Two short accent lines are drawn inside the chamfer cuts to echo the stroke
 * discipline of the prototype (`system.jsx` lines 40–41).
 *
 * Variants:
 *  - default — stroke = Colors.Stroke, fill = BgPanel
 *  - glow = true — stroke = Colors.Glow (brighter, for active/selected state)
 *  - danger = true — stroke = Danger, fill = BgPanelDanger (penalty)
 */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    chamfer: Dp = SoloTokens.Shape.ChamferMd,
    glow: Boolean = false,
    danger: Boolean = false,
    inset: Boolean = true,
    strokeWidth: Dp = 1.dp,
    content: @Composable () -> Unit,
) {
    val stroke = when {
        danger -> SoloTokens.Colors.Danger
        glow -> SoloTokens.Colors.Glow
        else -> SoloTokens.Colors.Stroke
    }
    val fill = if (danger) SoloTokens.Colors.BgPanelDanger else SoloTokens.Colors.BgPanel

    Box(
        modifier = modifier
            .drawBehind {
                val c = chamfer.toPx()
                val w = size.width
                val h = size.height
                if (w <= 0f || h <= 0f) return@drawBehind

                val outline = Path().apply {
                    moveTo(c, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h - c)
                    lineTo(w - c, h)
                    lineTo(0f, h)
                    lineTo(0f, c)
                    close()
                }
                drawPath(path = outline, color = fill)
                drawPath(
                    path = outline,
                    color = stroke,
                    style = Stroke(width = strokeWidth.toPx()),
                )

                val accentStroke = Stroke(width = strokeWidth.toPx())
                val accentColor = stroke.copy(alpha = 0.9f)
                // Top-left chamfer echo: short line from (c, 0) to (0, c)
                drawLine(
                    color = accentColor,
                    start = Offset(c, 0f),
                    end = Offset(0f, c),
                    strokeWidth = accentStroke.width,
                )
                // Bottom-right chamfer echo: short line from (w-c, h) to (w, h-c)
                drawLine(
                    color = accentColor,
                    start = Offset(w - c, h),
                    end = Offset(w, h - c),
                    strokeWidth = accentStroke.width,
                )
            }
            .padding(if (inset) PaddingValues(16.dp) else PaddingValues(0.dp)),
    ) {
        content()
    }
}

/** Convenience: a Panel with a custom stroke colour (for accent-theme previews). */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    strokeColor: Color,
    fillColor: Color = SoloTokens.Colors.BgPanel,
    chamfer: Dp = SoloTokens.Shape.ChamferMd,
    inset: Boolean = true,
    strokeWidth: Dp = 1.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .drawBehind {
                val c = chamfer.toPx()
                val w = size.width
                val h = size.height
                if (w <= 0f || h <= 0f) return@drawBehind
                val outline = Path().apply {
                    moveTo(c, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h - c)
                    lineTo(w - c, h)
                    lineTo(0f, h)
                    lineTo(0f, c)
                    close()
                }
                drawPath(path = outline, color = fillColor)
                drawPath(path = outline, color = strokeColor, style = Stroke(width = strokeWidth.toPx()))
                val accentColor = strokeColor.copy(alpha = 0.9f)
                drawLine(accentColor, Offset(c, 0f), Offset(0f, c), strokeWidth.toPx())
                drawLine(accentColor, Offset(w - c, h), Offset(w, h - c), strokeWidth.toPx())
            }
            .padding(if (inset) PaddingValues(16.dp) else PaddingValues(0.dp)),
    ) {
        content()
    }
}
