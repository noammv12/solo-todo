package com.solotodo.ui.onboarding.parts

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.fonts.SystemMono
import com.solotodo.domain.nl.NaturalLanguageDateParser.Token

/**
 * Render a single parsed [Token] as a small cyan-framed chip.
 * Colors map per kind so users see at-a-glance what the System understood.
 */
@Composable
fun TokenChip(token: Token, modifier: Modifier = Modifier) {
    val color = when (token.kind) {
        Token.Kind.DATE, Token.Kind.TIME -> SoloTokens.Colors.Glow
        Token.Kind.STAT -> SoloTokens.Colors.StatInt
        Token.Kind.LIST -> SoloTokens.Colors.StatVit
        Token.Kind.PRIORITY -> SoloTokens.Colors.Danger
    }
    val label = when (token.kind) {
        Token.Kind.DATE -> token.value.uppercase()
        Token.Kind.TIME -> token.value.uppercase()
        Token.Kind.STAT -> "@${token.value.uppercase()}"
        Token.Kind.LIST -> "#${token.value.lowercase()}"
        Token.Kind.PRIORITY -> "!".repeat((token.value.toIntOrNull() ?: 1).coerceIn(1, 3))
    }
    Box(
        modifier = modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .border(1.dp, color.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            color = color,
            fontFamily = SystemMono,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            letterSpacing = 0.24.em,
        )
    }
}
