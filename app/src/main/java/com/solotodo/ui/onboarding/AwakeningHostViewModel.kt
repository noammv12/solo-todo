package com.solotodo.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solotodo.core.a11y.ReduceMotionPolicy
import com.solotodo.core.haptics.SoloHaptics
import com.solotodo.core.lifecycle.ForegroundObserver
import com.solotodo.data.onboarding.AwakeningDraft
import com.solotodo.data.onboarding.AwakeningDraftStore
import com.solotodo.data.onboarding.OnboardingCommitter
import com.solotodo.domain.nl.NaturalLanguageDateParser
import com.solotodo.domain.onboarding.AwakeningEvent
import com.solotodo.domain.onboarding.AwakeningFsm
import com.solotodo.domain.onboarding.AwakeningState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

/**
 * Hosts the Awakening FSM, the draft store, and the commit boundary.
 *
 * Responsibilities:
 *  - Seed the FSM state from any persisted [AwakeningDraft] on init
 *    (resume-where-you-were semantics per locked spec).
 *  - Expose flows for state, draft, reduce-motion, and foreground to the
 *    hosted step composables.
 *  - Translate step callbacks into FSM transitions + draft writes.
 *  - Fire the single atomic commit on `PermissionsResolved`, then clear the
 *    draft. Guarded by a [Mutex] so double-tap on ALLOW/DECLINE can't start
 *    two commits.
 *
 * Haptics are exposed directly — step composables call them for edge-
 * triggered feedback (tap, rigid, threshold) that isn't well modelled as
 * state transitions.
 */
@HiltViewModel
class AwakeningHostViewModel @Inject constructor(
    private val draftStore: AwakeningDraftStore,
    private val committer: OnboardingCommitter,
    val haptics: SoloHaptics,
    val nlParser: NaturalLanguageDateParser,
    reduceMotionPolicy: ReduceMotionPolicy,
    foregroundObserver: ForegroundObserver,
) : ViewModel() {

    private val _state = MutableStateFlow<AwakeningState>(AwakeningState.Black)
    val state: StateFlow<AwakeningState> = _state.asStateFlow()

    val draft: StateFlow<AwakeningDraft> = draftStore.observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AwakeningDraft.EMPTY)

    val reduceMotion: StateFlow<Boolean> = reduceMotionPolicy.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isForeground: StateFlow<Boolean> = foregroundObserver.isForeground

    private val commitMutex = Mutex()

    init {
        viewModelScope.launch {
            val d = draftStore.get()
            _state.value = AwakeningFsm.resumeFrom(
                hasDesignation = !d.designation.isNullOrEmpty(),
                committedSelection = d.selectedPresetIds.size in 3..5,
                hasFirstQuestRaw = !d.firstQuestRaw.isNullOrEmpty(),
            )
        }
    }

    fun onEvent(event: AwakeningEvent) {
        _state.value = AwakeningFsm.transition(_state.value, event)
        when (event) {
            is AwakeningEvent.DesignationSubmitted ->
                viewModelScope.launch { draftStore.setDesignation(event.name) }
            AwakeningEvent.DesignationSkipped ->
                viewModelScope.launch { draftStore.setDesignation(HUNTER) }
            is AwakeningEvent.DailyQuestCommitted ->
                if (event.ids.size in 3..5) {
                    viewModelScope.launch { draftStore.setSelected(event.ids) }
                }
            is AwakeningEvent.FirstQuestCaptured ->
                viewModelScope.launch { draftStore.setFirstQuestRaw(event.raw) }
            AwakeningEvent.FirstQuestSkipped ->
                viewModelScope.launch { draftStore.setFirstQuestRaw(null) }
            is AwakeningEvent.PermissionsResolved -> commit(event.allowed)
            AwakeningEvent.AwakenTapped -> Unit
        }
    }

    /** Called inline from Step 2/3/4 on every edit so the draft stays fresh. */
    fun updateDesignation(value: String) {
        viewModelScope.launch { draftStore.setDesignation(value) }
    }
    fun updateSelection(ids: List<String>) {
        viewModelScope.launch { draftStore.setSelected(ids) }
    }
    fun updateFirstQuestRaw(raw: String) {
        viewModelScope.launch { draftStore.setFirstQuestRaw(raw) }
    }

    private fun commit(allowed: Boolean) {
        viewModelScope.launch {
            if (!commitMutex.tryLock()) return@launch
            try {
                val current = draftStore.get()
                committer.commit(current, allowed)
                draftStore.clear()
                // Once SettingsRepository flips onboardingCompleted=true, the
                // AwakeningGateViewModel's flow re-routes SoloTodoNav to the
                // tab NavHost automatically — no manual navigation here.
            } finally {
                commitMutex.unlock()
            }
        }
    }

    private companion object {
        const val HUNTER = "HUNTER"
    }
}
