package com.solotodo.data.repository

import androidx.room.withTransaction
import com.solotodo.data.DeviceId
import com.solotodo.data.local.OpKind
import com.solotodo.data.local.SoloTodoDb
import com.solotodo.data.local.dao.DailyQuestDao
import com.solotodo.data.local.entity.DailyQuestItemEntity
import com.solotodo.data.local.entity.DailyQuestLogEntity
import com.solotodo.data.sync.OpLogWriter
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
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
     * Report progress for a quest on a given day. Idempotent and monotonic —
     * if an existing row has higher progress, this is a no-op.
     */
    suspend fun reportProgress(questId: String, day: LocalDate, progress: Int, target: Int) {
        val now = clock.now()
        val completedAt = if (progress >= target) now else null

        db.withTransaction {
            val existing = dao.getLog(questId, day)
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
            } else if (progress > existing.progress) {
                // bumpProgress does a monotonic guarded update (WHERE progress < new).
                val clamped = progress.coerceAtMost(target)
                dao.bumpProgress(questId = questId, day = day, progress = clamped, completedAt = completedAt)
                opLog.record(ENTITY_LOG, existing.id, OpKind.PATCH, null, "{}")
            }
        }
    }

    companion object {
        const val ENTITY_ITEM = "daily_quest_item"
        const val ENTITY_LOG = "daily_quest_log"
    }
}
