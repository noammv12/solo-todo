@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.solotodo.ui.onboarding.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.solotodo.core.haptics.SoloHaptics
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.components.Panel
import com.solotodo.designsystem.fonts.SystemBody
import com.solotodo.designsystem.fonts.SystemDisplay
import com.solotodo.designsystem.fonts.SystemMono
import com.solotodo.designsystem.modifiers.glow
import com.solotodo.domain.nl.NaturalLanguageDateParser
import com.solotodo.ui.onboarding.parts.ProgressDots
import com.solotodo.ui.onboarding.parts.TokenChip
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Step 4 · First Quest — capture one real task, live-parsing NL hints.
 *
 * - Input is lowercase body font (only lowercase affordance in the app — a
 *   visual cue that this is user prose, not System voice).
 * - NL parser runs with a 120ms debounce; parsed tokens render as colored
 *   chips beneath the input. Chips are display-only.
 * - CTA morphs: empty → `▸ SKIP FOR NOW` (ghost, no glow); text → `▸ CAPTURE`
 *   (glow). Both route through [onSubmit]. An empty/blank value is treated
 *   as skip by the host.
 * - Haptics: `tap()` whenever the parsed-chip count increases; `rigid()` on
 *   CTA tap.
 */
@Composable
fun Step4FirstQuest(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    parser: NaturalLanguageDateParser,
    haptics: SoloHaptics,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var parsed by remember { mutableStateOf<NaturalLanguageDateParser.Parse?>(null) }
    var lastChipCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        delay(600)
        focusRequester.requestFocus()
        keyboard?.show()
    }

    // Debounced NL parse.
    LaunchedEffect(value) {
        if (value.isBlank()) {
            parsed = null
            lastChipCount = 0
            return@LaunchedEffect
        }
        delay(120)
        val next = parser.parse(value)
        val count = next.tokensFound.size
        if (count > lastChipCount) {
            scope.launch { haptics.tap() }
        }
        lastChipCount = count
        parsed = next
    }

    val hasText = value.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoloTokens.Colors.BgVoid)
            .padding(24.dp),
    ) {
        ProgressDots(current = 4, modifier = Modifier.padding(top = 16.dp))
        Spacer(Modifier.height(24.dp))
        Text(
            text = "▸ FIRST QUEST",
            color = SoloTokens.Colors.Glow,
            fontFamily = SystemMono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.30.em,
            modifier = Modifier.glow(SoloTokens.Glow.Cyan),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "ADD YOUR FIRST QUEST.",
            color = SoloTokens.Colors.Text,
            fontFamily = SystemDisplay,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            letterSpacing = 0.04.em,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Something real. Something you will do today, tomorrow, or this week.",
            color = SoloTokens.Colors.TextMuted,
            fontFamily = SystemDisplay,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(24.dp))

        Text(
            text = "▸ QUEST TITLE",
            color = SoloTokens.Colors.TextMuted,
            fontFamily = SystemMono,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            letterSpacing = 0.30.em,
        )
        Spacer(Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, SoloTokens.Colors.Glow.copy(alpha = 0.6f)),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = SoloTokens.Colors.Text,
                    fontFamily = SystemBody,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                ),
                singleLine = true,
                cursorBrush = SolidColor(SoloTokens.Colors.Glow),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        scope.launch {
                            haptics.rigid()
                            onSubmit(value)
                        }
                    },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
            )
            if (value.isEmpty()) {
                Text(
                    text = "what must be done",
                    color = SoloTokens.Colors.TextDim,
                    fontFamily = SystemBody,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 14.dp),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (parsed?.tokensFound?.isNotEmpty() == true) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                parsed!!.tokensFound.forEach { token -> TokenChip(token = token) }
            }
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.weight(1f))

        Panel(
            modifier = Modifier.fillMaxWidth(),
            inset = true,
        ) {
            Text(
                text = "▸ TIP: SAY \"TOMORROW\" OR \"FRIDAY 3PM\" — THE SYSTEM PARSES IT.",
                color = SoloTokens.Colors.TextMuted,
                fontFamily = SystemMono,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 0.24.em,
            )
        }

        Spacer(Modifier.height(16.dp))

        val ctaLabel = if (hasText) "▸ CAPTURE" else "▸ SKIP FOR NOW"
        ConfirmCta(
            enabled = true, // always tappable — empty → skip, non-empty → capture
            onTap = {
                scope.launch {
                    haptics.rigid()
                    onSubmit(value)
                }
            },
            label = ctaLabel,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        )
    }
}
