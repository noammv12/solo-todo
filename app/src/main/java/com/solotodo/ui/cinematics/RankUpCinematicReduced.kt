package com.solotodo.ui.cinematics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.solotodo.data.local.Rank
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.components.RankGlow
import com.solotodo.designsystem.components.RankGlyph
import com.solotodo.designsystem.fonts.SystemDisplay
import com.solotodo.ui.cinematics.parts.CopyStrings

/**
 * Reduce-Motion variant. Single frame, no animation — the rank glyph at its
 * final position plus a one-line copy. Host holds this frame for at least
 * `BeatScheduler.ReducedHoldMs` before auto-dismissing; a single TAP haptic
 * fires at T=0 on the host side.
 */
@Composable
fun RankUpCinematicReduced(
    toRank: Rank,
    modifier: Modifier = Modifier,
) {
    val copy = CopyStrings.forRank(toRank, reduceMotion = true)
    val color = if (toRank == Rank.S) SoloTokens.Colors.AccentGold else SoloTokens.Colors.Text
    val glowOption = if (toRank == Rank.S) RankGlow.Gold else RankGlow.Cyan

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SoloTokens.Colors.BgVoid)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            RankGlyph(rank = rankGlow(toRank), size = 160.dp, glowOption = glowOption)
            Spacer(Modifier.height(20.dp))
            Text(
                text = copy.title,
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SystemDisplay,
                letterSpacing = 0.30.em,
            )
        }
    }
}
