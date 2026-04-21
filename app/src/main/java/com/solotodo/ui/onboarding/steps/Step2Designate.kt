@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.solotodo.ui.onboarding.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.solotodo.core.haptics.SoloHaptics
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.fonts.SystemDisplay
import com.solotodo.designsystem.fonts.SystemMono
import com.solotodo.designsystem.modifiers.glow
import com.solotodo.ui.onboarding.parts.ProgressDots
import com.solotodo.ui.onboarding.parts.UppercaseTransformation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Step 2 · Designate — the user names their Hunter.
 *
 * - Uppercase-forced input, 2..16 chars, regex filter `[A-Z0-9 \-_.]`.
 *   Rejected keystrokes are silently dropped (no error flash on every typo).
 * - Counter "n/16" updates live; threshold haptic fires if user tries to type
 *   past 16 (normally unreachable due to filter).
 * - Autofocus 600ms after mount so the panel-in animation completes first.
 * - `▸ CONFIRM` enabled only when `value.length in 2..16`. Disabled tap fires
 *   [SoloHaptics.threshold]; valid tap fires [SoloHaptics.rigid] + submit.
 * - `SKIP ›` ghost button top-right — calls [onSkip], which in the committer
 *   maps to the default designation "HUNTER".
 * - Every keystroke writes the current value to draft via [onValueChange] so
 *   backgrounding mid-type preserves it.
 */
@Composable
fun Step2Designate(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onSkip: () -> Unit,
    haptics: SoloHaptics,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    var keystrokeHapticJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        delay(600) // wait for panel-in animation before stealing focus
        focusRequester.requestFocus()
        keyboard?.show()
    }

    val length = value.length
    val canSubmit = length in 2..16

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SoloTokens.Colors.BgVoid)
            .padding(24.dp),
    ) {
        // Top bar: progress dots + SKIP
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ProgressDots(current = 2)
            Text(
                text = "SKIP ›",
                color = SoloTokens.Colors.TextMuted,
                fontFamily = SystemMono,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 0.24.em,
                modifier = Modifier
                    .clickable {
                        scope.launch {
                            haptics.tap()
                            onSkip()
                        }
                    }
                    .padding(8.dp),
            )
        }

        // Centerpiece: kicker + title + subtitle + input
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "▸ IDENTITY PROTOCOL",
                color = SoloTokens.Colors.Glow,
                fontFamily = SystemMono,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 0.30.em,
                modifier = Modifier.glow(SoloTokens.Glow.Cyan),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "STATE YOUR DESIGNATION, HUNTER.",
                color = SoloTokens.Colors.Text,
                fontFamily = SystemDisplay,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                letterSpacing = 0.04.em,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "This name will appear on your Profile and in every System notification.",
                color = SoloTokens.Colors.TextMuted,
                fontFamily = SystemDisplay,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))

            // Input label
            Text(
                text = "▸ ENTER NAME",
                color = SoloTokens.Colors.TextMuted,
                fontFamily = SystemMono,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 0.30.em,
                modifier = Modifier.align(Alignment.Start),
            )
            Spacer(Modifier.height(6.dp))

            // Underlined BasicTextField
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.5.dp, color = SoloTokens.Colors.Glow.copy(alpha = 0.6f)),
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = { raw ->
                        val filtered = raw.filter { it.isAllowedDesignationChar() }.take(16)
                        if (filtered.length == 16 && raw.length > filtered.length) {
                            scope.launch { haptics.threshold() }
                        } else if (filtered.length > value.length) {
                            keystrokeHapticJob?.cancel()
                            keystrokeHapticJob = scope.launch {
                                delay(30)
                                haptics.tap()
                            }
                        }
                        onValueChange(filtered)
                    },
                    textStyle = TextStyle(
                        color = SoloTokens.Colors.Text,
                        fontFamily = SystemDisplay,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.08.em,
                        textAlign = TextAlign.Center,
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(SoloTokens.Colors.Glow),
                    visualTransformation = UppercaseTransformation,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (canSubmit) {
                                scope.launch {
                                    haptics.rigid()
                                    onSubmit(value)
                                }
                            } else {
                                scope.launch { haptics.threshold() }
                            }
                        },
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                )
                if (value.isEmpty()) {
                    Text(
                        text = "HUNTER",
                        color = SoloTokens.Colors.TextDim,
                        fontFamily = SystemDisplay,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.08.em,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "MAX 16 CHARACTERS",
                    color = SoloTokens.Colors.TextDim,
                    fontFamily = SystemMono,
                    fontSize = 10.sp,
                    letterSpacing = 0.24.em,
                )
                Text(
                    text = "$length/16",
                    color = if (length > 16) SoloTokens.Colors.Danger else SoloTokens.Colors.TextDim,
                    fontFamily = SystemMono,
                    fontSize = 10.sp,
                    letterSpacing = 0.24.em,
                )
            }
        }

        // CTA at bottom
        ConfirmCta(
            enabled = canSubmit,
            onTap = {
                scope.launch {
                    if (canSubmit) {
                        haptics.rigid()
                        onSubmit(value)
                    } else {
                        haptics.threshold()
                    }
                }
            },
            label = "▸ CONFIRM",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
        )
    }
}

@Composable
internal fun ConfirmCta(
    enabled: Boolean,
    onTap: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val stroke = if (enabled) SoloTokens.Colors.Glow else SoloTokens.Colors.StrokeDim
    val text = if (enabled) SoloTokens.Colors.Glow else SoloTokens.Colors.TextDim
    Box(
        modifier = modifier
            .then(if (enabled) Modifier.glow(SoloTokens.Glow.Cyan) else Modifier)
            .background(
                if (enabled) SoloTokens.Colors.Glow.copy(alpha = 0.08f)
                else SoloTokens.Colors.BgPanel,
            )
            .border(1.5.dp, stroke)
            .clickable(onClick = onTap)
            .padding(horizontal = 36.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = text,
            fontFamily = SystemDisplay,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            letterSpacing = 0.22.em,
        )
    }
}

private fun Char.isAllowedDesignationChar(): Boolean {
    return this.isLetterOrDigit() || this == ' ' || this == '-' || this == '_' || this == '.'
}
