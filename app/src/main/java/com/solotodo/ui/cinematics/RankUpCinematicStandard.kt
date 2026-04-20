package com.solotodo.ui.cinematics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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
 * D / C / B rank-up cinematic (2800ms, standard tier, cyan palette).
 *
 * Beats: FLASH 0–400ms, CHARGE 400–1200ms, LIFT 1200–1800ms, TITLE 1800–2800ms.
 * Burst counts vary by rank (D=0, C=1, B=2).
 */
@Composable
fun RankUpCinematicStandard(
    fromRank: Rank,
    toRank: Rank,
    elapsedMs: Long,
    beats: BeatScheduler.Beats,
    modifier: Modifier = Modifier,
) {
    val flashAlpha = pulseWindow(elapsedMs, from = 0L, to = beats.flashEndMs, peakAt = beats.flashEndMs / 2f)
    val oldGlyphAlpha = interpolate(
        elapsedMs = elapsedMs,
        chargeStart = beats.flashEndMs,
        chargeEnd = beats.chargeEndMs,
        liftEnd = beats.liftEndMs,
        atStart = 1f,
        atCharge = 0.55f,
        atLift = 0f,
    )
    val oldGlyphScale = interpolate(
        elapsedMs = elapsedMs,
        chargeStart = beats.flashEndMs,
        chargeEnd = beats.chargeEndMs,
        liftEnd = beats.liftEndMs,
        atStart = 1f,
        atCharge = 1f,
        atLift = 0.65f,
    )
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
    val burstCount = BeatScheduler.standardBurstCount(toRank)

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        RadialFlash(opacity = flashAlpha * 0.9f, tint = palette.glow)

        // Hex frame fades in during CHARGE
        if (hexAlpha > 0f) {
            HexFrame(color = palette.stroke, alpha = hexAlpha, size = 240.dp)
        }

        // Old rank glyph
        Box(
            modifier = Modifier
                .alpha(oldGlyphAlpha)
                .graphicsLayer { scaleX = oldGlyphScale; scaleY = oldGlyphScale },
            contentAlignment = Alignment.Center,
        ) {
            RankGlyph(
                rank = rankGlow(fromRank),
                size = 160.dp,
                glowOption = RankGlow.Cyan,
            )
        }

        // New rank glyph (appears during LIFT)
        Box(
            modifier = Modifier
                .alpha(newGlyphAlpha)
                .graphicsLayer { scaleX = newGlyphScale; scaleY = newGlyphScale },
            contentAlignment = Alignment.Center,
        ) {
            RankGlyph(
                rank = rankGlow(toRank),
                size = 160.dp,
                glowOption = RankGlow.Cyan,
            )
        }

        // Title block appears during TITLE phase
        if (titleProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = 180f },
                contentAlignment = Alignment.BottomCenter,
            ) {
                CinematicTitleBlock(
                    kicker = copy.kicker,
                    title = copy.title,
                    subtitle = copy.subtitle,
                    color = SoloTokens.Colors.Text,
                    progress = titleProgress,
                    titleSize = 22,
                    titleGlow = SoloTokens.Glow.Cyan,
                )
            }
        }

        // Radial bursts
        if (burstCount > 0) {
            Bursts(
                count = burstCount,
                startAtMs = beats.burstStartMs,
                intervalMs = beats.burstIntervalMs,
                color = palette.glow,
            )
        }
    }
}

/** Piecewise linear interpolation across FLASH → CHARGE → LIFT boundaries. */
internal fun interpolate(
    elapsedMs: Long,
    chargeStart: Long,
    chargeEnd: Long,
    liftEnd: Long,
    atStart: Float,
    atCharge: Float,
    atLift: Float,
): Float {
    return when {
        elapsedMs < chargeStart -> atStart
        elapsedMs < chargeEnd -> {
            val t = (elapsedMs - chargeStart).toFloat() / (chargeEnd - chargeStart)
            atStart + (atCharge - atStart) * t
        }
        elapsedMs < liftEnd -> {
            val t = (elapsedMs - chargeEnd).toFloat() / (liftEnd - chargeEnd)
            atCharge + (atLift - atCharge) * t
        }
        else -> atLift
    }
}

/** Triangular pulse: 0 at [from], peak 1 at [peakAt], 0 at [to]. */
internal fun pulseWindow(elapsedMs: Long, from: Long, to: Long, peakAt: Float): Float {
    if (elapsedMs < from || elapsedMs > to) return 0f
    val e = elapsedMs.toFloat()
    return if (e <= peakAt) (e - from) / (peakAt - from).coerceAtLeast(1f)
    else (to - e) / (to - peakAt).coerceAtLeast(1f)
}

/** Maps our persistence [Rank] to the UI design-system enum. */
internal fun rankGlow(rank: Rank): com.solotodo.designsystem.components.Rank =
    com.solotodo.designsystem.components.Rank.valueOf(rank.name)
