package com.solotodo.designsystem.modifiers

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import com.solotodo.designsystem.SoloTokens

/**
 * Entrance animation for panels and sheets: 220ms ease-out from
 * opacity 0 + scale 0.98 → opacity 1 + scale 1.
 *
 * Matches the prototype's `@keyframes panelIn`.
 */
@Composable
fun Modifier.panelIn(): Modifier = composed {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val duration = SoloTokens.Motion.PanelIn.inWholeMilliseconds.toInt()
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = duration),
        label = "panelIn-alpha",
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.98f,
        animationSpec = tween(durationMillis = duration),
        label = "panelIn-scale",
    )
    this.alpha(alpha).scale(scale)
}
