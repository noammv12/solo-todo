package com.solotodo.ui.onboarding.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.solotodo.designsystem.SoloTokens

/**
 * 5-dot horizontal step indicator, filled left-to-right per [current] (1..5).
 * Used as the header affordance across Awakening Steps 2–5.
 *
 * Unfilled dots render at 15% opacity; filled dots render at full glow alpha.
 */
@Composable
fun ProgressDots(
    current: Int,
    modifier: Modifier = Modifier,
    total: Int = 5,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(total) { i ->
            val filled = i < current
            Box(
                modifier = Modifier
                    .size(width = 18.dp, height = 2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        if (filled) SoloTokens.Colors.Glow
                        else SoloTokens.Colors.Glow.copy(alpha = 0.15f),
                    ),
            )
        }
    }
}
