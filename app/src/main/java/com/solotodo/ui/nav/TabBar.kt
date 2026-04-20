package com.solotodo.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.theme.SystemMonoLabel

enum class SoloTab(val route: String, val label: String) {
    STATUS("status", "STATUS"),
    QUESTS("quests", "QUESTS"),
}

/**
 * Bottom tab bar. Two tabs for now (Status, Quests). A centre FAB floats above
 * this bar via [FloatingCaptureButton]; the two are rendered as siblings in
 * the app's root layout.
 */
@Composable
fun SoloTabBar(
    current: SoloTab,
    onSelect: (SoloTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SoloTokens.Colors.BgVoid.copy(alpha = 0f),
                        SoloTokens.Colors.BgVoid.copy(alpha = 0.95f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SoloTab.entries.forEach { tab ->
                val selected = tab == current
                Text(
                    text = tab.label,
                    color = if (selected) SoloTokens.Colors.Glow else SoloTokens.Colors.TextMuted,
                    style = SystemMonoLabel,
                    modifier = Modifier
                        .clickable { onSelect(tab) }
                        .padding(PaddingValues(vertical = 12.dp, horizontal = 8.dp)),
                )
            }
        }
    }
}

/** Chamfered cyan capture button that opens Quick Add. */
@Composable
fun FloatingCaptureButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .background(SoloTokens.Colors.Glow)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+",
            color = SoloTokens.Colors.BgVoid,
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
        )
    }
}
