package com.solotodo.ui.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solotodo.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Observes `user_settings.onboarding_completed` and makes sure a settings row
 * exists so first-launch users have a concrete value to branch on.
 *
 * The gate has three UI-relevant states:
 *   - `null`   — still loading the settings row (brief, ~1 frame)
 *   - `false`  — user hasn't cleared Awakening yet → show placeholder / flow
 *   - `true`   — drop through to the Status tab
 */
@HiltViewModel
class AwakeningGateViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    val onboardingCompleted: StateFlow<Boolean?> = settingsRepo.observe()
        .map { it?.onboardingCompleted }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    init {
        // Ensure a row exists so the Flow above emits a real value even on
        // the very first authenticated launch. Idempotent.
        viewModelScope.launch { settingsRepo.initializeIfMissing() }
    }
}
