package com.solotodo.data.onboarding

/**
 * Ephemeral draft captured during the 5-step Awakening. Persists across
 * process death for Designate / Daily Quest / First Quest so a user who
 * backgrounds mid-flow can resume. Step 1 (pure animation) and Step 5
 * (OS-derived permission result) have no draft.
 */
data class AwakeningDraft(
    /** null = user hasn't typed a name. "" also treated as unset. */
    val designation: String? = null,
    /** Empty list = nothing committed yet; on commit must satisfy 3..5. */
    val selectedPresetIds: List<String> = emptyList(),
    /** null / blank = user didn't type a first quest. */
    val firstQuestRaw: String? = null,
) {
    companion object {
        val EMPTY: AwakeningDraft = AwakeningDraft()
    }
}
