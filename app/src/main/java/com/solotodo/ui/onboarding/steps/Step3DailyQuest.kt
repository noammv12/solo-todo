package com.solotodo.ui.onboarding.steps

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.solotodo.core.haptics.SoloHaptics
import com.solotodo.data.local.StatKind
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.components.DiamondCheck
import com.solotodo.designsystem.components.Panel
import com.solotodo.designsystem.fonts.SystemDisplay
import com.solotodo.designsystem.fonts.SystemMono
import com.solotodo.designsystem.modifiers.glow
import com.solotodo.domain.onboarding.Preset
import com.solotodo.domain.onboarding.PresetBank
import com.solotodo.ui.onboarding.parts.ProgressDots
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Step 3 · Daily Quest Definition — user picks 3..5 presets from a bank of 9.
 *
 * Locked constraints (retention override vs. prototype's 3..7):
 *  - Minimum 3, maximum 5.
 *  - Default pre-checked: first 3 in [PresetBank.ALL].
 *  - Tapping a 6th preset: no state change, `threshold()` haptic, brief red
 *    flash on the counter row.
 *  - No skip button (locked).
 *
 * Every toggle writes the new selection to the draft so backgrounding mid-
 * Step-3 preserves the user's picks.
 */
@Composable
fun Step3DailyQuest(
    selected: List<String>,
    onSelectedChange: (List<String>) -> Unit,
    onCommit: (List<String>) -> Unit,
    haptics: SoloHaptics,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var flashOverMax by remember { mutableStateOf(false) }
    val counterColor by animateColorAsState(
        targetValue = if (flashOverMax) SoloTokens.Colors.Danger else SoloTokens.Colors.TextMuted,
        animationSpec = tween(durationMillis = 220),
        label = "dq-counter-flash",
    )
    LaunchedEffect(flashOverMax) {
        if (flashOverMax) {
            delay(400)
            flashOverMax = false
        }
    }

    val selectedSet = remember(selected) { selected.toSet() }
    val count = selectedSet.size
    val canCommit = count in 3..5

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoloTokens.Colors.BgVoid)
            .padding(24.dp),
    ) {
        ProgressDots(current = 3, modifier = Modifier.padding(top = 16.dp))
        Spacer(Modifier.height(24.dp))
        Text(
            text = "▸ DAILY QUEST · DEFINITION",
            color = SoloTokens.Colors.Glow,
            fontFamily = SystemMono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.30.em,
            modifier = Modifier.glow(SoloTokens.Glow.Cyan),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "DEFINE YOUR DAILY QUEST.",
            color = SoloTokens.Colors.Text,
            fontFamily = SystemDisplay,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            letterSpacing = 0.04.em,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Three to five actions. Completed every day.",
            color = SoloTokens.Colors.TextMuted,
            fontFamily = SystemDisplay,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "$count SELECTED · MIN 3",
                color = counterColor,
                fontFamily = SystemMono,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 0.24.em,
            )
            Text(
                text = "MAX 5",
                color = counterColor,
                fontFamily = SystemMono,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 0.24.em,
            )
        }
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) {
            items(PresetBank.ALL, key = { it.id }) { preset ->
                val isSelected = preset.id in selectedSet
                PresetRow(
                    preset = preset,
                    checked = isSelected,
                    onToggle = {
                        val newSet = if (isSelected) {
                            selectedSet - preset.id
                        } else {
                            if (selectedSet.size >= 5) {
                                scope.launch { haptics.threshold() }
                                flashOverMax = true
                                return@PresetRow
                            }
                            selectedSet + preset.id
                        }
                        scope.launch { haptics.tap() }
                        // preserve insertion order of the original list for stability
                        val nextList = PresetBank.ALL.map { it.id }.filter { it in newSet }
                        onSelectedChange(nextList)
                    },
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        ConfirmCta(
            enabled = canCommit,
            onTap = {
                scope.launch {
                    if (canCommit) {
                        haptics.rigid()
                        onCommit(selected)
                    } else {
                        haptics.threshold()
                    }
                }
            },
            label = "▸ COMMIT",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun PresetRow(
    preset: Preset,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    val border = if (checked) SoloTokens.Colors.Glow else SoloTokens.Colors.StrokeDim
    val tint = if (checked) SoloTokens.Colors.Glow.copy(alpha = 0.08f) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(tint)
            .border(1.dp, border)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle,
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DiamondCheck(
            checked = checked,
            onCheckedChange = { onToggle() },
            size = 18.dp,
        )
        Text(
            text = preset.title,
            color = SoloTokens.Colors.Text,
            fontFamily = SystemDisplay,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            letterSpacing = 0.08.em,
            modifier = Modifier.weight(1f),
        )
        StatBadge(preset.stat, preset.xp)
    }
}

@Composable
private fun StatBadge(stat: StatKind, xp: Int) {
    val color = when (stat) {
        StatKind.STR -> SoloTokens.Colors.StatStr
        StatKind.INT -> SoloTokens.Colors.StatInt
        StatKind.SEN -> SoloTokens.Colors.StatSen
        StatKind.VIT -> SoloTokens.Colors.StatVit
    }
    Row(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "+$xp",
            color = color,
            fontFamily = SystemMono,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            letterSpacing = 0.15.em,
        )
        Text(
            text = stat.name,
            color = color,
            fontFamily = SystemMono,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            letterSpacing = 0.30.em,
        )
    }
}
