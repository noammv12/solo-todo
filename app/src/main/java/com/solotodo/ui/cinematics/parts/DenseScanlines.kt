package com.solotodo.ui.cinematics.parts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.solotodo.designsystem.modifiers.scanlineOverlay

/**
 * S-rank uses a denser scanline pattern (0.10 opacity vs the ambient 0.04).
 * Disabled when the reduce-motion policy is active.
 */
@Composable
fun DenseScanlines(enabled: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .scanlineOverlay(enabled = enabled, opacity = 0.10f),
    )
}
