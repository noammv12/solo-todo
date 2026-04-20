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
import com.solotodo.ui.cinematics.parts.DenseScanlines
import com.solotodo.ui.cinematics.parts.HexRing
import com.solotodo.ui.cinematics.parts.RadialFlash

/**
 * S rank-up cinematic — 5400ms monumental sequence. Warm gold palette,
 * dense scanlines, animated hex ring, 5 radial bursts, no subtitle.
 *
 * Beats:
 *  - FLASH    0–700ms  warm gold radial + dense scanlines turn up
 *  - CHARGE   700–2700ms  HexRing pulses stroke 0.5↔2dp, old A fades
 *  - LIFT     2700–3600ms  S glyph resolves in gold, bursts emanate
 *  - TITLE    3600–5400ms  "ARISE" / "MONARCH" with gold glow
 */
@Composable
fun RankUpCinematicS(
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
    val ringAlpha = if (elapsedMs < beats.flashEndMs) 0f
    else ((elapsedMs - beats.flashEndMs).toFloat() / (beats.chargeEndMs - beats.flashEndMs)).coerceIn(0f, 1f)
    val scanlinesActive = elapsedMs >= 400L && elapsedMs <= beats.liftEndMs + 800L

    val palette = SoloTokens.Accent.Gold
    val copy = CopyStrings.forRank(toRank, reduceMotion = false)

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Warm gold radial flash
        RadialFlash(opacity = flashAlpha * 0.95f, tint = palette.glow)

        // Dense scanlines ride through CHARGE + LIFT to sell the monumental feel
        if (scanlinesActive) DenseScanlines(enabled = true)

        // Hex ring with pulsing stroke-width during CHARGE+
        if (ringAlpha > 0f) {
            HexRing(color = palette.stroke, size = 360.dp, alpha = ringAlpha)
        }

        // Old rank (A) glyph fades + blurs out
        Box(
            modifier = Modifier
                .alpha(oldGlyphAlpha)
                .graphicsLayer { scaleX = oldGlyphScale; scaleY = oldGlyphScale },
            contentAlignment = Alignment.Center,
        ) {
            RankGlyph(rankGlow(fromRank), size = 220.dp, glowOption = RankGlow.Gold)
        }

        // New S glyph resolves in gold
        Box(
            modifier = Modifier
                .alpha(newGlyphAlpha)
                .graphicsLayer { scaleX = newGlyphScale; scaleY = newGlyphScale },
            contentAlignment = Alignment.Center,
        ) {
            RankGlyph(rankGlow(toRank), size = 240.dp, glowOption = RankGlow.Gold)
        }

        // Title block with gold glow (no subtitle for S)
        if (titleProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = 260f },
                contentAlignment = Alignment.BottomCenter,
            ) {
                CinematicTitleBlock(
                    kicker = copy.kicker,
                    title = copy.title,
                    subtitle = copy.subtitle,
                    color = SoloTokens.Colors.AccentGold,
                    progress = titleProgress,
                    titleSize = 28,
                    titleGlow = SoloTokens.Glow.Gold,
                )
            }
        }

        // 5 radial bursts starting at LIFT
        Bursts(
            count = beats.burstCount,
            startAtMs = beats.burstStartMs,
            intervalMs = beats.burstIntervalMs,
            color = palette.glow,
        )
    }
}
