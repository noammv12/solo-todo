package com.solotodo.domain.onboarding

/**
 * The 5-step Awakening state machine. Pure Kotlin — no Android, no coroutines,
 * no side effects. Write side effects live in `OnboardingCommitter`.
 *
 * Transitions (linear):
 *   Black ──AwakenTapped────────────────────────────→ Designate
 *   Designate ──(Submitted | Skipped)───────────────→ DailyQuest
 *   DailyQuest ──DailyQuestCommitted(3..5 ids)──────→ FirstQuest
 *   FirstQuest ──(Captured | Skipped)───────────────→ Permissions
 *   Permissions ──PermissionsResolved───────────────→ Complete
 *
 * Any event that doesn't match the current state's allowed transitions is a
 * no-op (returns the same state). `DailyQuestCommitted(ids)` with
 * `ids.size !in 3..5` is also a no-op — enforced here so neither UI nor
 * test harness can accidentally bypass the 3..5 constraint.
 */
sealed interface AwakeningState {
    data object Black : AwakeningState
    data object Designate : AwakeningState
    data object DailyQuest : AwakeningState
    data object FirstQuest : AwakeningState
    data object Permissions : AwakeningState
    data object Complete : AwakeningState
}

sealed interface AwakeningEvent {
    data object AwakenTapped : AwakeningEvent
    data class DesignationSubmitted(val name: String) : AwakeningEvent
    data object DesignationSkipped : AwakeningEvent
    data class DailyQuestCommitted(val ids: List<String>) : AwakeningEvent
    data class FirstQuestCaptured(val raw: String?) : AwakeningEvent
    data object FirstQuestSkipped : AwakeningEvent
    data class PermissionsResolved(val allowed: Boolean) : AwakeningEvent
}

object AwakeningFsm {

    fun transition(state: AwakeningState, event: AwakeningEvent): AwakeningState = when (state) {
        AwakeningState.Black -> when (event) {
            AwakeningEvent.AwakenTapped -> AwakeningState.Designate
            else -> state
        }
        AwakeningState.Designate -> when (event) {
            is AwakeningEvent.DesignationSubmitted,
            AwakeningEvent.DesignationSkipped -> AwakeningState.DailyQuest
            else -> state
        }
        AwakeningState.DailyQuest -> when (event) {
            is AwakeningEvent.DailyQuestCommitted ->
                if (event.ids.size in 3..5) AwakeningState.FirstQuest else state
            else -> state
        }
        AwakeningState.FirstQuest -> when (event) {
            is AwakeningEvent.FirstQuestCaptured,
            AwakeningEvent.FirstQuestSkipped -> AwakeningState.Permissions
            else -> state
        }
        AwakeningState.Permissions -> when (event) {
            is AwakeningEvent.PermissionsResolved -> AwakeningState.Complete
            else -> state
        }
        AwakeningState.Complete -> state
    }

    /**
     * Seed a resume-state from a persisted draft. A force-quit mid-flow leaves
     * the draft on disk; the next launch re-enters the furthest step that has
     * captured input.
     *
     *   draft empty                             → Black  (start fresh)
     *   designation drafted                     → Designate (user was typing)
     *   selection drafted (size in 3..5)        → DailyQuest committed view
     *   selection drafted + firstQuestRaw set   → FirstQuest
     *
     * Step 5 (Permissions) has no draftable state; a user who reaches it and
     * kills the app restarts at Step 4 to re-confirm.
     */
    fun resumeFrom(
        hasDesignation: Boolean,
        committedSelection: Boolean,
        hasFirstQuestRaw: Boolean,
    ): AwakeningState = when {
        hasFirstQuestRaw && committedSelection -> AwakeningState.FirstQuest
        committedSelection -> AwakeningState.FirstQuest
        hasDesignation -> AwakeningState.DailyQuest
        else -> AwakeningState.Black
    }
}
