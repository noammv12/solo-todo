package com.solotodo.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.solotodo.designsystem.SoloTokens
import kotlinx.coroutines.delay

/**
 * Char-by-char text reveal with blinking caret (`▌`). Default rate is 38 chars
 * per second (matches prototype's `speed` param). Caret blinks at 800ms.
 *
 * Fires [onDone] exactly once when the full [text] has been revealed.
 */
@Composable
fun TypeIn(
    text: String,
    modifier: Modifier = Modifier,
    charsPerSecond: Int = 38,
    caretColor: Color = SoloTokens.Colors.Glow,
    textColor: Color = SoloTokens.Colors.Text,
    style: TextStyle = LocalTextStyle.current,
    onDone: (() -> Unit)? = null,
) {
    var revealed by remember(text) { mutableIntStateOf(0) }
    var done by remember(text) { mutableStateOf(false) }

    LaunchedEffect(text) {
        revealed = 0
        done = false
        val msPerChar = (1000f / charsPerSecond).toLong().coerceAtLeast(1L)
        while (revealed < text.length) {
            delay(msPerChar)
            revealed += 1
        }
        done = true
        onDone?.invoke()
    }

    val cycleMs = SoloTokens.Motion.CaretBlink.inWholeMilliseconds.toInt()
    val infinite = rememberInfiniteTransition(label = "caret")
    val caretAlpha by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = cycleMs / 2, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "caret-alpha",
    )

    Row(modifier = modifier) {
        Text(
            text = text.substring(0, revealed),
            color = textColor,
            style = style,
        )
        Text(
            text = "▌",
            color = caretColor.copy(alpha = caretAlpha),
            style = style,
        )
    }
}
