package com.solotodo.data.repository

import androidx.room.withTransaction
import com.solotodo.data.local.OpKind
import com.solotodo.data.local.SoloTodoDb
import com.solotodo.data.local.ThemeAccent
import com.solotodo.data.local.dao.SettingsDao
import com.solotodo.data.local.entity.UserSettingsEntity
import com.solotodo.data.sync.OpLogWriter
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val db: SoloTodoDb,
    private val dao: SettingsDao,
    private val opLog: OpLogWriter,
    private val clock: Clock = Clock.System,
) {
    fun observe(): Flow<UserSettingsEntity?> = dao.observe()
    suspend fun get(): UserSettingsEntity? = dao.get()

    /** First-run initialiser. Writes a default row if none exists. */
    suspend fun initializeIfMissing(): UserSettingsEntity {
        dao.get()?.let { return it }
        val defaults = defaultSettings(clock.now())
        db.withTransaction {
            dao.upsert(defaults)
            opLog.record(ENTITY, defaults.id, OpKind.CREATE, null, "{}")
        }
        return defaults
    }

    suspend fun update(settings: UserSettingsEntity) {
        db.withTransaction {
            dao.upsert(settings.copy(updatedAt = clock.now()))
            opLog.record(ENTITY, settings.id, OpKind.PATCH, null, "{}")
        }
    }

    /**
     * Phase 6.1 helpers. Each one reads the singleton, patches a single field,
     * and writes through the existing op-log path so changes sync like any
     * other `user_settings` edit.
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        val current = initializeIfMissing()
        update(current.copy(onboardingCompleted = completed))
    }

    suspend fun setAwakenedAt(awakenedAt: kotlinx.datetime.Instant?) {
        val current = initializeIfMissing()
        update(current.copy(awakenedAt = awakenedAt))
    }

    suspend fun setDailyQuestCount(count: Int) {
        require(count in 3..5) { "dailyQuestCount must be 3..5, was $count" }
        val current = initializeIfMissing()
        update(current.copy(dailyQuestCount = count))
    }

    suspend fun setHardMode(hardMode: Boolean) {
        val current = initializeIfMissing()
        update(current.copy(hardMode = hardMode))
    }

    suspend fun setDesignation(name: String) {
        require(name.length in 2..16) { "designation length must be 2..16, was ${name.length}" }
        val current = initializeIfMissing()
        update(current.copy(designation = name))
    }

    /**
     * Awakening-commit helper: flip every onboarding-affected field in a single
     * `update` call, which in turn emits one op-log PATCH row inside the
     * caller's (outer) transaction. The committer is responsible for also
     * writing the daily-quest items + optional first task + clearing the draft
     * inside the same outer transaction.
     *
     * @param notificationsJson the JSON string produced by
     *   `NotificationPrefs.Channels.toJson()` — passed as a string so this
     *   repository stays oblivious to the notifications schema.
     */
    suspend fun finishAwakening(
        designation: String,
        dailyQuestCount: Int,
        notificationsJson: String,
        now: kotlinx.datetime.Instant,
    ) {
        require(designation.length in 2..16) {
            "designation length must be 2..16, was ${designation.length}"
        }
        require(dailyQuestCount in 3..5) {
            "dailyQuestCount must be 3..5, was $dailyQuestCount"
        }
        val current = initializeIfMissing()
        update(
            current.copy(
                designation = designation,
                dailyQuestCount = dailyQuestCount,
                notifications = notificationsJson,
                onboardingCompleted = true,
                awakenedAt = now,
            ),
        )
    }

    companion object {
        const val ENTITY = "user_settings"

        fun defaultSettings(now: kotlinx.datetime.Instant): UserSettingsEntity = UserSettingsEntity(
            designation = "HUNTER",
            theme = ThemeAccent.CYAN,
            haptics = DEFAULT_HAPTICS_JSON,
            notifications = DEFAULT_NOTIFICATIONS_JSON,
            reduceMotion = false,
            vacationUntil = null,
            streakFreezes = 0,
            onboardingCompleted = false,
            awakenedAt = null,
            dailyQuestCount = 3,
            hardMode = false,
            updatedAt = now,
        )

        // Retention overrides: notifications default to off (see plan §deliberate-overrides).
        private const val DEFAULT_HAPTICS_JSON =
            """{"master":true,"tap":true,"success":true,"threshold":true,"rank_up":true,"warning":true}"""
        private const val DEFAULT_NOTIFICATIONS_JSON =
            """{"morning":false,"evening":false,"rank_up":false,"reflection":false}"""
    }
}
