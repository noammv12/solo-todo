package com.solotodo.ui.cinematics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.solotodo.data.local.Rank
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.components.RankGlow
import com.solotodo.designsystem.components.RankGlyph
import com.solotodo.ui.cinematics.parts.Bursts
import com.solotodo.ui.cinematics.parts.CinematicTitleBlock
import com.solotodo.ui.cinematics.parts.CopyStrings
import com.solotodo.ui.cinematics.parts.HexFrame
import com.solotodo.ui.cinematics.parts.RadialFlash

/**
 * A rank-up cinematic (3400ms extended tier). Same beat structure as Standard
 * but the TITLE phase runs 1800–3400ms and the rank glyph is scaled up to
 * 200dp, with 3 radial bursts emanating from center.
 */
@Composable
fun RankUpCinematicA(
    fromRank: Rank,
    toRank: Rank,
    elapsedMs: Long,
    beats: BeatScheduler.Beats,
    modifier: Modifier = Modifier,
) {
    val flashAlpha = pulseWindow(elapsedMs, 0L, beats.flashEndMs, beats.flashEndMs / 2f)
    val oldGlyphAlpha = interpolate(elapsedMs, beats.flashEndMs, beats.chargeEndMs, beats.liftEndMs, 1f, 0.55f, 0f)
    val oldGlyphScale = interpolate(elapsedMs, beats.flashEndMs, beats.chargeEndMs, beats.liftEndMs, 1f, 1f, 0.65f)
    val newGlyphAlpha = if (elapsedMs < beats.chargeEndMs) 0f
    else ((elapsedMs - beats.chargeEndMs).toFloat() / (beats.liftEndMs - beats.chargeEndMs)).coerceIn(0f, 1f)
    val newGlyphScale = if (elapsedMs < beats.chargeEndMs) 1.5f
    else 1.5f - 0.5f * ((elapsedMs - beats.chargeEndMs).toFloat() / (beats.liftEndMs - beats.chargeEndMs)).coerceIn(0f, 1f)
    val titleProgress = if (elapsedMs < beats.titleStartMs) 0f
    else ((elapsedMs - beats.titleStartMs).toFloat() / (beats.titleEndMs - beats.titleStartMs)).coerceIn(0f, 1f)
    val hexAlpha = if (elapsedMs < beats.flashEndMs) 0f
    else ((elapsedMs - beats.flashEndMs).toFloat() / (beats.chargeEndMs - beats.flashEndMs)).coerceIn(0f, 1f)

    val palette = SoloTokens.Accent.Hunter
    val copy = CopyStrings.forRank(toRank, reduceMotion = false)

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        RadialFlash(opacity = flashAlpha * 0.9f, tint = palette.glow)

        if (hexAlpha > 0f) {
            HexFrame(color = palette.stroke, alpha = hexAlpha, size = 300.dp)
        }

        Box(
            modifier = Modifier
                .alpha(oldGlyphAlpha)
                .graphicsLayer { scaleX = oldGlyphScale; scaleY = oldGlyphScale },
            contentAlignment = Alignment.Center,
        ) {
            RankGlyph(rankGlow(fromRank), size = 200.dp, glowOption = RankGlow.Cyan)
        }
        Box(
            modifier = Modifier
                .alpha(newGlyphAlpha)
                .graphicsLayer { scaleX = newGlyphScale; scaleY = newGlyphScale },
            contentAlignment = Alignment.Center,
        ) {
            RankGlyph(rankGlow(toRank), size = 200.dp, glowOption = RankGlow.Cyan)
        }

        if (titleProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = 220f },
                contentAlignment = Alignment.BottomCenter,
            ) {
                CinematicTitleBlock(
                    kicker = copy.kicker,
                    title = copy.title,
                    subtitle = copy.subtitle,
                    color = SoloTokens.Colors.Text,
                    progress = titleProgress,
                    titleSize = 26,
                    titleGlow = SoloTokens.Glow.Cyan,
                )
            }
        }

        Bursts(
            count = beats.burstCount,
            startAtMs = beats.burstStartMs,
            intervalMs = beats.burstIntervalMs,
            color = palette.glow,
        )
    }
}
