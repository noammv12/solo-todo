package com.solotodo.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solotodo.data.repository.SettingsRepository
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.components.Panel
import com.solotodo.designsystem.components.Tag
import com.solotodo.designsystem.theme.SystemMonoLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Phase 6.1 placeholder — the real 5-step Awakening lands in Phase 6.2.
 *
 * Ships so the navigation gate, schema migration, and deep-link infrastructure
 * can be verified end-to-end on an installable APK before the real flow is
 * written. The only user-visible affordance is a dev button that flips
 * `onboarding_completed` to true so the user can still reach Status.
 */
@Composable
fun AwakeningPlaceholderScreen(
    viewModel: AwakeningPlaceholderViewModel = hiltViewModel(),
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloTokens.Colors.BgVoid),
        contentAlignment = Alignment.Center,
    ) {
        Panel(
            modifier = Modifier.padding(horizontal = 32.dp),
            chamfer = SoloTokens.Shape.ChamferMd,
            glow = true,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            ) {
                Tag(text = "▸ AWAKENING · PENDING", color = SoloTokens.Colors.Glow)
                Text(
                    text = "THE SYSTEM IS CALIBRATING.\nPHASE 6.2 ARRIVES SOON.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SoloTokens.Colors.Text,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "▸ SKIP · ENTER STATUS (DEV)",
                    style = SystemMonoLabel,
                    color = SoloTokens.Colors.Glow,
                    modifier = Modifier
                        .border(1.dp, SoloTokens.Colors.Glow)
                        .clickable { viewModel.skip() }
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                )
                Text(
                    text = "This skip button is debug-only; it goes away when Phase 6.2 ships.",
                    style = SystemMonoLabel,
                    color = SoloTokens.Colors.TextDim,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Text(
            text = "PHASE 6.1",
            style = SystemMonoLabel,
            color = SoloTokens.Colors.TextDim,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        )
    }
}

@HiltViewModel
class AwakeningPlaceholderViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
) : ViewModel() {
    fun skip() {
        viewModelScope.launch {
            settingsRepo.initializeIfMissing()
            settingsRepo.setAwakenedAt(kotlinx.datetime.Clock.System.now())
            settingsRepo.setOnboardingCompleted(true)
        }
    }
}
