package com.solotodo.data.repository

import androidx.room.withTransaction
import com.solotodo.data.DeviceId
import com.solotodo.data.local.OpKind
import com.solotodo.data.local.SoloTodoDb
import com.solotodo.data.local.dao.DailyQuestDao
import com.solotodo.data.local.entity.DailyQuestItemEntity
import com.solotodo.data.local.entity.DailyQuestLogEntity
import com.solotodo.data.sync.OpLogWriter
import com.solotodo.domain.onboarding.PresetBank
import com.solotodo.domain.rank.RankEvaluator
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyQuestRepository @Inject constructor(
    private val db: SoloTodoDb,
    private val dao: DailyQuestDao,
    private val opLog: OpLogWriter,
    private val deviceId: DeviceId,
    private val rankEvaluator: RankEvaluator,
    private val clock: Clock = Clock.System,
) {
    fun observeActiveItems(): Flow<List<DailyQuestItemEntity>> = dao.observeActiveItems()
    fun observeLogsForDay(day: LocalDate): Flow<List<DailyQuestLogEntity>> = dao.observeLogsForDay(day)
    fun observeLogsBetween(from: LocalDate, to: LocalDate): Flow<List<DailyQuestLogEntity>> = dao.observeLogsBetween(from, to)
    fun observeActiveItemCount(): Flow<Int> = dao.observeActiveItemCount()

    suspend fun createItem(item: DailyQuestItemEntity): String {
        db.withTransaction {
            dao.upsertItem(item)
            opLog.record(ENTITY_ITEM, item.id, OpKind.CREATE, null, "{}")
        }
        return item.id
    }

    suspend fun upsertItems(items: List<DailyQuestItemEntity>) {
        db.withTransaction {
            dao.upsertItems(items)
            items.forEach { opLog.record(ENTITY_ITEM, it.id, OpKind.CREATE, null, "{}") }
        }
    }

    /**
     * Awakening-commit helper. Replaces the current "active today" set with
     * the user's Step 3 selection.
     *
     * Behaviour, in a single transaction (joins the outer committer txn):
     *  1. Read the currently-active items.
     *  2. For each active item NOT in the new selection → upsert with
     *     `active = false` (row stays in DB for historical logs) + PATCH op-log.
     *  3. Upsert every preset in the new selection with `active = true` and the
     *     selection order as `orderIndex` → CREATE op-log.
     *     (Preset IDs are stable, so on Replay Awakening the same ID round-
     *     trips: REPLACE conflict strategy rewrites the row in-place.)
     *
     * Preserves `createdAt` for presets that were previously active, so rank
     * / streak history isn't forged.
     */
    suspend fun replaceActiveItemsFromPresets(
        presetIds: List<String>,
        now: Instant,
    ) {
        require(presetIds.size in 3..5) {
            "presetIds must have 3..5 entries, was ${presetIds.size}"
        }
        val newIds = presetIds.toSet()
        db.withTransaction {
            val currentlyActive = dao.getActiveItems()
            val toDeactivate = currentlyActive.filter { it.id !in newIds }
            toDeactivate.forEach { existing ->
                dao.upsertItem(existing.copy(active = false, updatedAt = now))
                opLog.record(ENTITY_ITEM, existing.id, OpKind.PATCH, null, "{}")
            }
            val priorById: Map<String, DailyQuestItemEntity> =
                currentlyActive.associateBy { it.id }
            val newEntities = presetIds.mapIndexed { idx, id ->
                val base = PresetBank.buildEntity(
                    presetId = id,
                    orderIndex = idx,
                    now = now,
                    deviceId = deviceId.value,
                )
                // Preserve createdAt if this preset already existed.
                priorById[id]?.let { base.copy(createdAt = it.createdAt) } ?: base
            }
            dao.upsertItems(newEntities)
            newEntities.forEach {
                opLog.record(ENTITY_ITEM, it.id, OpKind.CREATE, null, "{}")
            }
        }
    }

    /**
     * Report progress for a quest on a given day. Idempotent and monotonic —
     * if an existing row has higher progress, this is a no-op.
     */
    suspend fun reportProgress(questId: String, day: LocalDate, progress: Int, target: Int) {
        val now = clock.now()
        val completedAt = if (progress >= target) now else null

        db.withTransaction {
            val existing = dao.getLog(questId, day)
            val justCompleted: Boolean
            if (existing == null) {
                val id = UUID.randomUUID().toString()
                dao.upsertLog(
                    DailyQuestLogEntity(
                        id = id,
                        questId = questId,
                        day = day,
                        progress = progress.coerceAtMost(target),
                        completedAt = completedAt,
                        createdAt = now,
                    ),
                )
                opLog.record(ENTITY_LOG, id, OpKind.CREATE, null, "{}")
                justCompleted = completedAt != null
            } else if (progress > existing.progress) {
                val clamped = progress.coerceAtMost(target)
                dao.bumpProgress(questId = questId, day = day, progress = clamped, completedAt = completedAt)
                opLog.record(ENTITY_LOG, existing.id, OpKind.PATCH, null, "{}")
                justCompleted = completedAt != null && existing.completedAt == null
            } else {
                justCompleted = false
            }

            // Rank evaluation fires only when a quest just crossed its target —
            // avoids running on every progress tick. Evaluator is a no-op if
            // the day's other quests aren't also complete yet.
            if (justCompleted) rankEvaluator.evaluateAndEmit()
        }
    }

    companion object {
        const val ENTITY_ITEM = "daily_quest_item"
        const val ENTITY_LOG = "daily_quest_log"
    }
}
