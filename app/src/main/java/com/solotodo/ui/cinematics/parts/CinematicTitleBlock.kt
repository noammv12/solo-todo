package com.solotodo.ui.cinematics.parts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.fonts.SystemDisplay
import com.solotodo.designsystem.modifiers.glow

/**
 * The kicker + title + subtitle block that rises 12dp and fades in during the
 * TITLE phase. Caller provides a [progress] float (0..1) spanning the fade window.
 */
@Composable
fun CinematicTitleBlock(
    kicker: String?,
    title: String,
    subtitle: String?,
    color: Color,
    progress: Float,
    titleSize: Int = 22,
    titleGlow: SoloTokens.Glow.Spec? = null,
    modifier: Modifier = Modifier,
) {
    val translateY = (1f - progress) * 12f
    val a = progress.coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .alpha(a)
            .graphicsLayer { translationY = translateY },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (!kicker.isNullOrBlank()) {
            Text(
                text = kicker,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SystemDisplay,
                letterSpacing = 0.35.em,
                textAlign = TextAlign.Center,
                modifier = if (titleGlow != null) Modifier.glow(titleGlow) else Modifier,
            )
            Spacer(Modifier.height(12.dp))
        }
        Text(
            text = title,
            color = color,
            fontSize = titleSize.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = SystemDisplay,
            letterSpacing = 0.35.em,
            textAlign = TextAlign.Center,
            style = TextStyle.Default,
            modifier = if (titleGlow != null) Modifier.glow(titleGlow) else Modifier,
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = subtitle,
                color = color.copy(alpha = 0.75f),
                fontSize = 12.sp,
                fontFamily = SystemDisplay,
                letterSpacing = 0.25.em,
                textAlign = TextAlign.Center,
            )
        }
    }
}
