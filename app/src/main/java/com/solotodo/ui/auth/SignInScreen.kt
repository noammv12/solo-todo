package com.solotodo.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.components.Panel
import com.solotodo.designsystem.components.Rank
import com.solotodo.designsystem.components.RankGlow
import com.solotodo.designsystem.components.RankGlyph
import com.solotodo.designsystem.components.Tag
import com.solotodo.designsystem.theme.SystemMonoLabel

/**
 * Shown at app launch when the user isn't signed in.
 *
 * Guest is the hero action — zero-friction path to the aha moment (per plan
 * §retention overrides: "Day 1 retention is driven by the first completed
 * quest + rank progress within 60s"). Google / magic link arrive in Phase 4.5.
 */
@Composable
fun SignInScreen(
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloTokens.Colors.BgVoid)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            RankGlyph(rank = Rank.E, size = 88.dp, glowOption = RankGlow.Cyan)
            Spacer(Modifier.height(24.dp))
            Tag("AWAITING HUNTER")
            Spacer(Modifier.height(8.dp))
            Text(
                text = "SOLO TODO",
                color = SoloTokens.Colors.Text,
                style = MaterialTheme.typography.displaySmall,
            )
            Spacer(Modifier.height(48.dp))

            // Primary action: Guest
            Panel(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (state.signingIn) Modifier
                        else Modifier.clickable { viewModel.signInAsGuest() },
                    ),
                glow = true,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (state.signingIn) "AWAKENING..." else "▸ CONTINUE AS HUNTER",
                        color = SoloTokens.Colors.Glow,
                        style = SystemMonoLabel,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "no account needed",
                        color = SoloTokens.Colors.TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Secondary: Google (stubbed)
            Panel(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SIGN IN WITH GOOGLE",
                        color = SoloTokens.Colors.TextDim,
                        style = SystemMonoLabel,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "coming in phase 4.5",
                        color = SoloTokens.Colors.TextDim,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            if (state.error != null) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = state.error!!,
                    color = SoloTokens.Colors.Danger,
                    style = SystemMonoLabel,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "if the server is not configured yet, see supabase/README.md",
                    color = SoloTokens.Colors.TextDim,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
