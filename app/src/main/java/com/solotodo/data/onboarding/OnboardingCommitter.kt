package com.solotodo.data.onboarding

import androidx.room.withTransaction
import com.solotodo.data.local.SoloTodoDb
import com.solotodo.data.local.StatKind
import com.solotodo.data.notifications.NotificationPrefs
import com.solotodo.data.repository.DailyQuestRepository
import com.solotodo.data.repository.SettingsRepository
import com.solotodo.data.repository.TaskRepository
import com.solotodo.domain.nl.NaturalLanguageDateParser
import com.solotodo.domain.onboarding.PresetBank
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single write boundary for Awakening completion.
 *
 * All side effects funnel through [commit] so the UI never reaches around it.
 * The whole set of writes happens inside one Room `withTransaction` — any
 * failure rolls back every entity (and every op-log row) atomically.
 *
 * Writes emitted by one successful commit:
 *  - `user_settings` row: designation / dailyQuestCount / notifications JSON /
 *    onboardingCompleted=true / awakenedAt=now   (1 PATCH op-log row)
 *  - `daily_quest_item` rows: 3..5 active + any prior-active that got
 *    deactivated during Replay (N CREATE + M PATCH op-log rows)
 *  - Optional `task` row if the user typed a first quest (1 CREATE op-log
 *    row). No task is created if the field is blank/whitespace.
 *
 * Fallbacks applied at commit time:
 *  - Null / too-short `designation` → "HUNTER".
 *  - Empty `selectedPresetIds` → [PresetBank.defaultThree]. (UI can't reach
 *    here with an empty list, but the fallback makes the contract total so
 *    Replay + tests can't construct an invalid state.)
 */
@Singleton
class OnboardingCommitter @Inject constructor(
    private val db: SoloTodoDb,
    private val settings: SettingsRepository,
    private val dqRepo: DailyQuestRepository,
    private val taskRepo: TaskRepository,
    private val nlParser: NaturalLanguageDateParser,
    private val clock: Clock = Clock.System,
) {
    suspend fun commit(
        draft: AwakeningDraft,
        notificationsAllowed: Boolean,
    ) = db.withTransaction {
        commitBody(draft, notificationsAllowed)
    }

    /**
     * The actual write sequence, extracted out of [commit] so it can be unit-
     * tested directly without trying to mock [androidx.room.withTransaction].
     * In production this only runs inside the outer `withTransaction` block
     * opened by [commit]; nested calls to repo methods that wrap their own
     * `withTransaction` correctly join the outer transaction.
     */
    internal suspend fun commitBody(
        draft: AwakeningDraft,
        notificationsAllowed: Boolean,
    ) {
        val now = clock.now()
        val designation = draft.designation
            ?.trim()
            ?.takeIf { it.length in 2..16 }
            ?: DEFAULT_DESIGNATION

        val selected = draft.selectedPresetIds
            .filter { PresetBank.byId(it) != null }
            .let { if (it.isEmpty()) PresetBank.defaultThree() else it }
            .take(5)
        check(selected.size in 3..5) {
            "Awakening commit requires 3..5 daily quest presets (got ${selected.size})"
        }

        val notifJson = (
            if (notificationsAllowed) NotificationPrefs.Channels.ON
            else NotificationPrefs.Channels.OFF
            ).toJson()

        settings.finishAwakening(
            designation = designation,
            dailyQuestCount = selected.size,
            notificationsJson = notifJson,
            now = now,
        )

        dqRepo.replaceActiveItemsFromPresets(selected, now)

        val rawFirstQuest = draft.firstQuestRaw?.trim()?.takeUnless { it.isEmpty() }
        if (rawFirstQuest != null) {
            val parsed = nlParser.parse(rawFirstQuest)
            val statKind = parsed.stat
                ?.let { runCatching { StatKind.valueOf(it.uppercase()) }.getOrNull() }
            taskRepo.create(
                title = parsed.title.ifBlank { rawFirstQuest },
                rawInput = rawFirstQuest,
                dueAt = parsed.dueAt,
                stat = statKind,
                listId = parsed.list,
                priority = parsed.priority,
                xp = 0,
            )
        }
    }

    companion object {
        const val DEFAULT_DESIGNATION = "HUNTER"
    }
}
