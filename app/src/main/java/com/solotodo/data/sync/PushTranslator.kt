package com.solotodo.data.sync

import com.solotodo.data.local.OpKind
import com.solotodo.data.local.dao.DailyQuestDao
import com.solotodo.data.local.dao.DungeonDao
import com.solotodo.data.local.dao.RankEventDao
import com.solotodo.data.local.dao.ReflectionDao
import com.solotodo.data.local.dao.SettingsDao
import com.solotodo.data.local.dao.TaskDao
import com.solotodo.data.local.dao.TaskListDao
import com.solotodo.data.local.entity.OpLogEntity
import com.solotodo.data.remote.dto.toDto
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Translates a single [OpLogEntity] into one Postgrest call and executes it.
 *
 *  - CREATE / PATCH → re-read the current Room row (op-log `fields` is always
 *    null today), map to DTO with `user_id` stamped, upsert with the correct
 *    `onConflict`.
 *  - DELETE → Postgrest delete filtered by id + user_id.
 *  - rank_event → insert-only; server trigger dedupes, so we treat any
 *    post-insert uniqueness conflict as idempotent success.
 *  - If a local row has been deleted by the time we drain (rare race), the
 *    upsert is a no-op.
 */
@Singleton
class PushTranslator @Inject constructor(
    private val postgrest: Postgrest,
    private val taskDao: TaskDao,
    private val dailyQuestDao: DailyQuestDao,
    private val dungeonDao: DungeonDao,
    private val taskListDao: TaskListDao,
    private val reflectionDao: ReflectionDao,
    private val settingsDao: SettingsDao,
    private val rankEventDao: RankEventDao,
) {

    /**
     * Push a single op to Supabase. Throws on unexpected failure; the engine
     * is responsible for catching and quarantining. A successful return means
     * the op is acknowledged and can be marked synced.
     */
    suspend fun push(op: OpLogEntity, userId: String) {
        when (op.kind) {
            OpKind.DELETE -> pushDelete(op.entity, op.entityId, userId)
            OpKind.CREATE, OpKind.PATCH -> pushUpsert(op.entity, op.entityId, userId)
        }
    }

    private suspend fun pushUpsert(entity: String, entityId: String, userId: String) {
        when (entity) {
            "task" -> {
                val row = taskDao.getById(entityId) ?: return
                postgrest.from("task").upsert(row.toDto(userId))
            }
            "daily_quest_item" -> {
                val row = dailyQuestDao.getItem(entityId) ?: return
                postgrest.from("daily_quest_item").upsert(row.toDto(userId))
            }
            "daily_quest_log" -> {
                val row = dailyQuestDao.getLogById(entityId) ?: return
                postgrest.from("daily_quest_log").upsert(row.toDto(userId)) {
                    onConflict = "user_id,quest_id,day"
                }
            }
            "dungeon" -> {
                val row = dungeonDao.getDungeon(entityId) ?: return
                postgrest.from("dungeon").upsert(row.toDto(userId))
            }
            "dungeon_floor" -> {
                val row = dungeonDao.getFloor(entityId) ?: return
                postgrest.from("dungeon_floor").upsert(row.toDto(userId))
            }
            "task_list" -> {
                val row = taskListDao.getById(entityId) ?: return
                postgrest.from("task_list").upsert(row.toDto(userId))
            }
            "reflection" -> {
                val row = reflectionDao.getById(entityId) ?: return
                postgrest.from("reflection").upsert(row.toDto(userId)) {
                    onConflict = "user_id,week_start"
                }
            }
            "user_settings" -> {
                val row = settingsDao.get() ?: return
                postgrest.from("user_settings").upsert(row.toDto(userId)) {
                    onConflict = "user_id"
                }
            }
            "rank_event" -> {
                val row = rankEventDao.getById(entityId) ?: return
                try {
                    postgrest.from("rank_event").insert(row.toDto(userId))
                } catch (e: Exception) {
                    if (!isDedupError(e)) throw e
                }
            }
            else -> {
                // Unknown entity — skip (engine will mark synced anyway so the
                // op drains instead of jamming the queue indefinitely).
            }
        }
    }

    private suspend fun pushDelete(entity: String, entityId: String, userId: String) {
        postgrest.from(entity).delete {
            filter {
                eq("id", entityId)
                eq("user_id", userId)
            }
        }
    }

    private fun isDedupError(e: Exception): Boolean {
        val msg = e.message.orEmpty()
        return msg.contains("duplicate", ignoreCase = true) ||
            msg.contains("unique", ignoreCase = true) ||
            msg.contains("23505") ||
            msg.contains("409")
    }
}
