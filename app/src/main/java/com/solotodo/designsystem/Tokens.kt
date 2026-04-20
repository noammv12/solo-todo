package com.solotodo.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Solo ToDo design tokens — Kotlin port of `styles.css`.
 *
 * Source of truth: `solo-todo-design/sololeveling/project/styles.css`
 * and `solo-todo-design/sololeveling/project/design-system/tokens.json`.
 * If this file disagrees with the prototype, the prototype wins.
 */
object SoloTokens {

    object Colors {
        // Backgrounds
        val BgVoid = Color(0xFF05070E)
        val BgPanel = Color(0xFF0A1628)
        val BgPanelRaised = Color(0xFF0E1C33)

        // Strokes / borders (default Hunter accent)
        val Stroke = Color(0xFF1FB6FF)
        val StrokeDim = Color(0x401FB6FF) // 25% alpha

        // Highlights / glow
        val Glow = Color(0xFF00D4FF)

        // Text
        val Text = Color(0xFFE5F4FF)
        val TextMuted = Color(0xFF7A93B0)
        val TextDim = Color(0xFF4A6380)

        // Accents
        val AccentShadow = Color(0xFF9D4EDD)
        val AccentGold = Color(0xFFFFD447)
        val Danger = Color(0xFFFF3355)

        // Rank colors (E→S) — cross-reference system.jsx during Phase 1
        val RankE = Color(0xFF7A93B0)
        val RankD = Color(0xFF5EEAD4)
        val RankC = Color(0xFF00D4FF)
        val RankB = Color(0xFF9D4EDD)
        val RankA = Color(0xFFFF3355)
        val RankS = Color(0xFFFFD447)

        // Stat colors
        val StatStr = Color(0xFFFF6B6B)
        val StatInt = Color(0xFF00D4FF)
        val StatSen = Color(0xFFC77DFF)
        val StatVit = Color(0xFF5EEAD4)
    }

    /**
     * Accent theme variants — swap Stroke / Glow / StrokeDim.
     * Defaults (Hunter) are in [Colors]. Shadow and Gold override.
     */
    object Accent {
        data class Palette(val stroke: Color, val glow: Color, val strokeDim: Color)

        val Hunter = Palette(
            stroke = Colors.Stroke,
            glow = Colors.Glow,
            strokeDim = Colors.StrokeDim,
        )
        val Shadow = Palette(
            stroke = Color(0xFF9D4EDD),
            glow = Color(0xFFC77DFF),
            strokeDim = Color(0x409D4EDD),
        )
        val Gold = Palette(
            stroke = Color(0xFFFFD447),
            glow = Color(0xFFFFE066),
            strokeDim = Color(0x4CFFD447), // 30% alpha per CSS
        )
    }

    object Typography {
        // Type scale — mirrors the prototype's sizes from design-system inventory.
        val size9 = 9.sp
        val size10 = 10.sp
        val size11 = 11.sp
        val size12 = 12.sp
        val size13 = 13.sp
        val size14 = 14.sp
        val size16 = 16.sp
        val size18 = 18.sp
        val size20 = 20.sp
        val size22 = 22.sp
        val size26 = 26.sp
        val size32 = 32.sp
        val size36 = 36.sp
        val size48 = 48.sp
        val size72 = 72.sp

        // Letter spacing (em → sp approximation; refined per-use-site in components)
        const val trackingNormal = 0.02f
        const val trackingDisplay = 0.08f
        const val trackingLabel = 0.10f
        const val trackingHeading = 0.12f
        const val trackingWide = 0.30f

        // Font families are defined in fonts/Fonts.kt once downloadable-fonts set up
    }

    object Spacing {
        val s0 = 0.dp
        val s2 = 2.dp
        val s4 = 4.dp
        val s6 = 6.dp
        val s8 = 8.dp
        val s10 = 10.dp
        val s12 = 12.dp
        val s14 = 14.dp
        val s16 = 16.dp
        val s20 = 20.dp
        val s24 = 24.dp
        val s28 = 28.dp
        val s32 = 32.dp
        val s40 = 40.dp
    }

    object Shape {
        /** Chamfer cut size for Panel corners (top-left + bottom-right). */
        val ChamferSm = 10.dp
        val ChamferMd = 14.dp
        val ChamferLg = 20.dp
    }

    /**
     * Glow/shadow specs. Compose doesn't have text-shadow; implement via layered
     * Canvas draws or `drawBehind` modifiers. These constants carry the spec.
     */
    object Glow {
        data class Spec(val innerRadiusDp: Float, val innerAlpha: Float, val outerRadiusDp: Float, val outerAlpha: Float, val color: Color)

        val Cyan = Spec(12f, 0.70f, 28f, 0.35f, Colors.Glow)
        val Gold = Spec(18f, 0.90f, 40f, 0.50f, Colors.AccentGold)
        val Shadow = Spec(14f, 0.80f, 32f, 0.40f, Colors.AccentShadow)
        val Red = Spec(12f, 0.70f, 28f, 0.35f, Colors.Danger)
    }

    object Motion {
        val Instant = 100.milliseconds
        val Quick = 180.milliseconds
        val PanelIn = 220.milliseconds
        val SheetIn = 320.milliseconds
        val Pulse = 320.milliseconds
        val CaretBlink = 800.milliseconds
        val CinematicCharge = 800.milliseconds
        val CinematicReveal = 1200.milliseconds
        val Ambient = 2400.milliseconds
        val ScanlineCycle = 20.seconds
    }

    /** Z-order matches the prototype's stacking rules. Useful for Popup / Dialog layering. */
    object ZIndex {
        const val Scanlines = 1f
        const val Content = 2f
        const val TabBar = 15f
        const val Fab = 20f
        const val Overlay = 50f
        const val DungeonDetail = 90f
        const val Sheet = 100f
        const val Toast = 150f
        const val Cinematic = 200f
    }

    /** Scanline overlay parameters. */
    object Scanline {
        const val opacity = 0.04f
        val color = Colors.Glow
        val periodPx = 3f       // 1px line + 2px gap
        val scrollDistance = 120.dp
    }
}
