package com.solotodo.ui.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.solotodo.designsystem.SoloTokens

@Composable
fun PlaceholderScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloTokens.Colors.BgVoid),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "[ SYSTEM ]",
                color = SoloTokens.Colors.Glow,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "SOLO TODO",
                color = SoloTokens.Colors.Text,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Phase 0 ready. Awakening pending.",
                color = SoloTokens.Colors.TextMuted,
            )
        }
    }
}
