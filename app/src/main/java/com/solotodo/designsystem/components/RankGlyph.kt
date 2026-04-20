package com.solotodo.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.fonts.SystemDisplay
import com.solotodo.designsystem.modifiers.glow

/** Glow palette options for [RankGlyph]. Cyan default, gold = S-rank, shadow = archive. */
enum class RankGlow { Cyan, Gold, Shadow, Red, None }

/** The hunter rank letters as they are shown in the app. */
enum class Rank(val label: String) {
    E("E"), D("D"), C("C"), B("B"), A("A"), S("S");
    companion object {
        fun fromString(value: String): Rank? = entries.firstOrNull { it.label == value.uppercase() }
    }
}

/**
 * Angular hexagonal rank badge with letter at center. Two nested hex rings frame
 * the letter; the outer ring is at 50% opacity, inner at 30% (per `system.jsx`).
 */
@Composable
fun RankGlyph(
    rank: Rank,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    glowOption: RankGlow = RankGlow.Cyan,
) {
    val color = when (rank) {
        Rank.E -> SoloTokens.Colors.RankE
        Rank.D -> SoloTokens.Colors.RankD
        Rank.C -> SoloTokens.Colors.RankC
        Rank.B -> SoloTokens.Colors.RankB
        Rank.A -> SoloTokens.Colors.RankA
        Rank.S -> SoloTokens.Colors.RankS
    }

    val glowSpec = when (glowOption) {
        RankGlow.Cyan -> SoloTokens.Glow.Cyan
        RankGlow.Gold -> SoloTokens.Glow.Gold
        RankGlow.Shadow -> SoloTokens.Glow.Shadow
        RankGlow.Red -> SoloTokens.Glow.Red
        RankGlow.None -> null
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val u = this.size.width / 64f
            val outer = hexPath(u, outer = true)
            drawPath(outer, color = color.copy(alpha = 0.5f), style = Stroke(width = 1.2.dp.toPx()))
            val inner = hexPath(u, outer = false)
            drawPath(inner, color = color.copy(alpha = 0.3f), style = Stroke(width = 0.8.dp.toPx()))
        }
        Text(
            text = rank.label,
            color = color,
            fontSize = (size.value * 0.55f).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = SystemDisplay,
            letterSpacing = SoloTokens.Typography.trackingNormal.em,
            modifier = if (glowSpec != null) Modifier.glow(glowSpec) else Modifier,
        )
    }
}

/** Hex polygon matching `system.jsx` viewbox (0,0 → 64,64). */
private fun hexPath(u: Float, outer: Boolean): Path {
    val points = if (outer) {
        listOf(32f to 4f, 56f to 18f, 56f to 46f, 32f to 60f, 8f to 46f, 8f to 18f)
    } else {
        listOf(32f to 9f, 51f to 20f, 51f to 44f, 32f to 55f, 13f to 44f, 13f to 20f)
    }
    return Path().apply {
        moveTo(points[0].first * u, points[0].second * u)
        for (i in 1..5) lineTo(points[i].first * u, points[i].second * u)
        close()
    }
}
