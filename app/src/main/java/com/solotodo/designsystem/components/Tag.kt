package com.solotodo.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.fonts.SystemDisplay

/**
 * Bracketed system tag: `[DAILY QUEST]`, `[RANK E]`, etc.
 *
 * Bracket characters are rendered at 0.7 opacity so the inner text reads as the
 * primary element. Input is uppercase-wrapped at render time; callers pass in
 * normal-case text and we handle the display transform.
 */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SoloTokens.Colors.Stroke,
    size: TextUnit = 11.sp,
) {
    val upper = text.uppercase()
    Row(modifier = modifier) {
        Text(
            text = "[",
            color = color.copy(alpha = 0.7f),
            fontSize = size,
            fontFamily = SystemDisplay,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = SoloTokens.Typography.trackingWide.em,
        )
        Text(
            text = upper,
            color = color,
            fontSize = size,
            fontFamily = SystemDisplay,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = SoloTokens.Typography.trackingWide.em,
        )
        Text(
            text = "]",
            color = color.copy(alpha = 0.7f),
            fontSize = size,
            fontFamily = SystemDisplay,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = SoloTokens.Typography.trackingWide.em,
        )
    }
}
