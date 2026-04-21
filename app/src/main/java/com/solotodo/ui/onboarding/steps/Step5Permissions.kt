package com.solotodo.ui.onboarding.steps

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.solotodo.core.haptics.SoloHaptics
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.components.Panel
import com.solotodo.designsystem.fonts.SystemDisplay
import com.solotodo.designsystem.fonts.SystemMono
import com.solotodo.designsystem.modifiers.glow
import com.solotodo.ui.onboarding.parts.ProgressDots
import kotlinx.coroutines.launch

/**
 * Step 5 · Permissions — grant or decline notification access.
 *
 * **Retention override (locked):** DECLINE is the primary visual CTA; ALLOW
 * is the ghost secondary. Ordered DECLINE first. Rationale: default-on
 * notifications are the #1 uninstall driver for habit apps. See
 * `jazzy-lake.md` §34-46.
 *
 * ALLOW → launches the POST_NOTIFICATIONS permission dialog on API 33+. On
 * API <33 the dialog doesn't exist, so we flip notifications on immediately.
 * OS denial at any level is treated identically to DECLINE.
 *
 * Emits [onResolved] with `allowed = true/false` so the host can fire the
 * final commit transaction.
 */
@Composable
fun Step5Permissions(
    onResolved: (allowed: Boolean) -> Unit,
    haptics: SoloHaptics,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onResolved(granted)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoloTokens.Colors.BgVoid)
            .padding(24.dp),
    ) {
        ProgressDots(current = 5, modifier = Modifier.padding(top = 16.dp))
        Spacer(Modifier.height(24.dp))
        Text(
            text = "▸ SYSTEM SIGNAL",
            color = SoloTokens.Colors.Glow,
            fontFamily = SystemMono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.30.em,
            modifier = Modifier.glow(SoloTokens.Glow.Cyan),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "ALLOW THE SYSTEM TO NOTIFY YOU.",
            color = SoloTokens.Colors.Text,
            fontFamily = SystemDisplay,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            letterSpacing = 0.04.em,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Morning reminder. Evening nudge. Rank-up. Nothing else.",
            color = SoloTokens.Colors.TextMuted,
            fontFamily = SystemDisplay,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(24.dp))

        Panel(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ChannelRow("DAILY REMINDER", "09:00")
                ChannelRow("EVENING NUDGE", "IF INCOMPLETE")
                ChannelRow("RANK-UP", "ALWAYS")
                ChannelRow("REFLECTION", "SUN 19:00")
            }
        }

        Spacer(Modifier.weight(1f))

        // DECLINE — PRIMARY (52dp, glow).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glow(SoloTokens.Glow.Cyan)
                .background(SoloTokens.Colors.Glow.copy(alpha = 0.08f))
                .border(1.5.dp, SoloTokens.Colors.Glow)
                .clickable {
                    scope.launch {
                        haptics.rigid()
                        onResolved(false)
                    }
                }
                .padding(horizontal = 36.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "DECLINE · SYSTEM SILENT",
                color = SoloTokens.Colors.Glow,
                fontFamily = SystemDisplay,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                letterSpacing = 0.22.em,
            )
        }

        Spacer(Modifier.height(12.dp))

        // ALLOW — ghost secondary (40dp, muted stroke).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SoloTokens.Colors.StrokeDim)
                .clickable {
                    scope.launch { haptics.rigid() }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        // pre-API-33: no runtime prompt. Honour the user's tap.
                        onResolved(true)
                    }
                }
                .padding(horizontal = 32.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "▸ ALLOW",
                color = SoloTokens.Colors.TextMuted,
                fontFamily = SystemDisplay,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                letterSpacing = 0.22.em,
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ChannelRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = SoloTokens.Colors.Text,
            fontFamily = SystemMono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.28.em,
        )
        Text(
            text = value,
            color = SoloTokens.Colors.TextMuted,
            fontFamily = SystemMono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.20.em,
        )
    }
}
