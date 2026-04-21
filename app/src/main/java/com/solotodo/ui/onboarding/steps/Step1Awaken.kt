@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.solotodo.ui.onboarding.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.solotodo.core.haptics.SoloHaptics
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.components.TypeIn
import com.solotodo.designsystem.fonts.SystemDisplay
import com.solotodo.designsystem.fonts.SystemMono
import com.solotodo.designsystem.modifiers.glow
import com.solotodo.ui.cinematics.parts.HexFrame
import com.solotodo.ui.onboarding.parts.AwakeningBeats
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Awakening Step 1 — the System-detection cinematic.
 *
 * Four phases driven by a single wall-clock `LaunchedEffect` loop (same idiom
 * as [com.solotodo.ui.cinematics.CinematicHost]):
 *
 *   0 – 600ms     pure black void. Dramatic silence is the hook.
 *   600 – 2200ms  hex fades in (alpha 0→1) and scales up (0.8→1.0).
 *                 Cyan glow layer sits behind the frame.
 *                 Haptic [SoloHaptics.light] fires at T=600ms.
 *   2200 – 4000ms kicker + body type on at 38 cps.
 *   4000 – 4800ms AWAKEN CTA rises 12dp + fades in with its own glow.
 *
 * Tap AWAKEN → [SoloHaptics.rigid] + [onAwaken].
 *
 * Reduce-motion branch: single static frame rendered instantly, CTA tappable
 * from T=0, a single `light()` haptic to acknowledge.
 *
 * Foreground pause: when the host observes `isForeground` going false, the
 * LaunchedEffect is cancelled and `elapsed` resets. On return, Step 1
 * restarts from 0 — the locked spec's "Step 1 has no draft, restart if
 * interrupted."
 */
@Composable
fun Step1Awaken(
    reduceMotion: Boolean,
    isForeground: Boolean,
    haptics: SoloHaptics,
    onAwaken: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val beats = AwakeningBeats
    var elapsed by remember { mutableLongStateOf(0L) }
    var hapticFired by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reduceMotion, isForeground) {
        if (reduceMotion) {
            elapsed = beats.totalMs
            hapticFired = true
            haptics.light()
            return@LaunchedEffect
        }
        if (!isForeground) {
            elapsed = 0L
            hapticFired = false
            return@LaunchedEffect
        }
        elapsed = 0L
        hapticFired = false
        val start = System.currentTimeMillis()
        while (true) {
            val e = (System.currentTimeMillis() - start).coerceAtMost(beats.totalMs)
            elapsed = e
            if (!hapticFired && e >= beats.hapticAtMs) {
                hapticFired = true
                haptics.light()
            }
            if (e >= beats.totalMs) break
            delay(16)
        }
    }

    val hexAlpha = ((elapsed - beats.flashEndMs).toFloat() /
        (beats.chargeEndMs - beats.flashEndMs).toFloat()).coerceIn(0f, 1f)
    val hexScale = 0.8f + 0.2f * hexAlpha
    val typeVisible = elapsed >= beats.chargeEndMs
    val ctaProgress = ((elapsed - beats.titleStartMs).toFloat() /
        (beats.titleEndMs - beats.titleStartMs).toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SoloTokens.Colors.BgVoid)
            .semantics { contentDescription = STEP1_A11Y_TEXT },
        contentAlignment = Alignment.Center,
    ) {
        if (hexAlpha > 0f) {
            HexFrame(
                color = SoloTokens.Accent.Hunter.stroke,
                alpha = hexAlpha,
                size = 240.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        scaleX = hexScale
                        scaleY = hexScale
                    }
                    .glow(SoloTokens.Glow.Cyan),
            )
        }

        if (typeVisible) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 180.dp, start = 24.dp, end = 24.dp)
                    .widthIn(max = 360.dp)
                    .semantics { invisibleToUser() },
            ) {
                Text(
                    text = "▸ NOTIFICATION",
                    color = SoloTokens.Colors.Glow,
                    fontFamily = SystemMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = 0.30.em,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.glow(SoloTokens.Glow.Cyan),
                )
                TypeIn(
                    text = BODY_COPY,
                    charsPerSecond = 38,
                    caretColor = SoloTokens.Colors.Glow,
                    textColor = SoloTokens.Colors.Text,
                    style = TextStyle(
                        fontFamily = SystemDisplay,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.06.em,
                    ),
                )
            }
        }

        if (ctaProgress > 0f) {
            AwakenButton(
                onTap = {
                    scope.launch {
                        haptics.rigid()
                        onAwaken()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp)
                    .graphicsLayer {
                        translationY = (1f - ctaProgress) * 12.dp.toPx()
                    }
                    .alpha(ctaProgress),
            )
        }
    }
}

/**
 * AWAKEN CTA. Cyan-stroked rectangle with tint fill + outer glow. Clickable
 * area is the whole box; ripple is suppressed so the glow carries the feedback
 * weight instead.
 */
@Composable
private fun AwakenButton(
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .glow(SoloTokens.Glow.Cyan)
            .background(SoloTokens.Colors.Glow.copy(alpha = 0.08f))
            .border(1.5.dp, SoloTokens.Colors.Glow)
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onTap,
            )
            .padding(horizontal = 36.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "▸ AWAKEN",
            color = SoloTokens.Colors.Glow,
            fontFamily = SystemDisplay,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            letterSpacing = 0.22.em,
        )
    }
}

private const val BODY_COPY = "THE SYSTEM HAS DETECTED A NEW HUNTER."
private const val STEP1_A11Y_TEXT =
    "Notification. The System has detected a new hunter. Tap Awaken to begin."
