package com.solotodo.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solotodo.designsystem.SoloTokens

/**
 * Linear or segmented progress bar.
 *
 *  - `segmented = true` with `total in 1..10` — discrete blocks with a gap
 *    between each; filled blocks glow. Used for Daily Quest progress (3 / 5).
 *  - Otherwise — single horizontal bar with continuous fill (rank XP bars).
 */
@Composable
fun ProgressBar(
    value: Int,
    total: Int,
    modifier: Modifier = Modifier,
    color: Color = SoloTokens.Colors.Glow,
    height: Dp = 6.dp,
    segmented: Boolean = false,
) {
    if (segmented && total in 1..10) {
        Row(modifier = modifier.height(height)) {
            repeat(total) { index ->
                if (index > 0) Spacer(Modifier.width(3.dp))
                val filled = index < value
                val segColor = if (filled) color else color.copy(alpha = 0.15f)
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(height),
                ) {
                    drawRect(color = segColor)
                }
            }
        }
        return
    }

    val pct = if (total > 0) (value.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val w = this.size.width
        val h = this.size.height
        drawRect(color = color.copy(alpha = 0.15f), size = androidx.compose.ui.geometry.Size(w, h))
        if (pct > 0f) {
            drawRect(color = color, size = androidx.compose.ui.geometry.Size(w * pct, h))
        }
    }
}
