package com.solotodo.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.fonts.SystemBody
import com.solotodo.designsystem.fonts.SystemDisplay
import com.solotodo.designsystem.fonts.SystemMono

/**
 * Material 3 Typography mapped to Solo ToDo's display / mono / body fonts + size scale.
 *
 * UI chrome is **SystemDisplay** (Rajdhani) with uppercase + tracking. Long-form prose
 * uses **SystemBody** (Inter). System signals, metadata, and numeric readouts use
 * **SystemMono** (JetBrains Mono) via explicit TextStyle at call sites — they don't
 * go through this Material typography table.
 */
val SoloTypography = Typography(
    // Cinematic-scale titles (A→S rank lifts, dungeon clear)
    displayLarge = TextStyle(
        fontFamily = SystemDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = SoloTokens.Typography.size72,
        letterSpacing = SoloTokens.Typography.trackingDisplay.em,
    ),
    displayMedium = TextStyle(
        fontFamily = SystemDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = SoloTokens.Typography.size48,
        letterSpacing = SoloTokens.Typography.trackingDisplay.em,
    ),
    displaySmall = TextStyle(
        fontFamily = SystemDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = SoloTokens.Typography.size36,
        letterSpacing = SoloTokens.Typography.trackingDisplay.em,
    ),

    // Screen titles (Status, Quests, Profile)
    headlineLarge = TextStyle(
        fontFamily = SystemDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = SoloTokens.Typography.size32,
        letterSpacing = SoloTokens.Typography.trackingHeading.em,
    ),
    headlineMedium = TextStyle(
        fontFamily = SystemDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = SoloTokens.Typography.size26,
        letterSpacing = SoloTokens.Typography.trackingHeading.em,
    ),
    headlineSmall = TextStyle(
        fontFamily = SystemDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = SoloTokens.Typography.size22,
        letterSpacing = SoloTokens.Typography.trackingHeading.em,
    ),

    // Panel titles
    titleLarge = TextStyle(
        fontFamily = SystemDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = SoloTokens.Typography.size20,
        letterSpacing = SoloTokens.Typography.trackingDisplay.em,
    ),
    titleMedium = TextStyle(
        fontFamily = SystemDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = SoloTokens.Typography.size18,
        letterSpacing = SoloTokens.Typography.trackingDisplay.em,
    ),
    titleSmall = TextStyle(
        fontFamily = SystemDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = SoloTokens.Typography.size14,
        letterSpacing = SoloTokens.Typography.trackingLabel.em,
    ),

    // Body (long prose — Reflection, task descriptions only)
    bodyLarge = TextStyle(
        fontFamily = SystemBody,
        fontWeight = FontWeight.Normal,
        fontSize = SoloTokens.Typography.size16,
        letterSpacing = SoloTokens.Typography.trackingNormal.em,
    ),
    bodyMedium = TextStyle(
        fontFamily = SystemBody,
        fontWeight = FontWeight.Normal,
        fontSize = SoloTokens.Typography.size14,
        letterSpacing = SoloTokens.Typography.trackingNormal.em,
    ),
    bodySmall = TextStyle(
        fontFamily = SystemBody,
        fontWeight = FontWeight.Normal,
        fontSize = SoloTokens.Typography.size12,
        letterSpacing = SoloTokens.Typography.trackingNormal.em,
    ),

    // Labels: small + tracked for System UI
    labelLarge = TextStyle(
        fontFamily = SystemDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = SoloTokens.Typography.size14,
        letterSpacing = SoloTokens.Typography.trackingLabel.em,
    ),
    labelMedium = TextStyle(
        fontFamily = SystemDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = SoloTokens.Typography.size12,
        letterSpacing = SoloTokens.Typography.trackingLabel.em,
    ),
    labelSmall = TextStyle(
        fontFamily = SystemDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = SoloTokens.Typography.size10,
        letterSpacing = SoloTokens.Typography.trackingLabel.em,
    ),
)

/**
 * Explicit TextStyle for monospace contexts (system signals, metadata, tabular numbers).
 * Use at call sites; not part of the Material Typography table.
 */
val SystemMonoLabel = TextStyle(
    fontFamily = SystemMono,
    fontWeight = FontWeight.Medium,
    fontSize = SoloTokens.Typography.size11,
    letterSpacing = SoloTokens.Typography.trackingMonoHeading.em,
)
