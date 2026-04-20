package com.solotodo.data.sync

import androidx.room.withTransaction
import com.solotodo.data.local.SoloTodoDb
import com.solotodo.data.local.dao.DailyQuestDao
import com.solotodo.data.local.dao.DungeonDao
import com.solotodo.data.local.dao.RankEventDao
import com.solotodo.data.local.dao.ReflectionDao
import com.solotodo.data.local.dao.SettingsDao
import com.solotodo.data.local.dao.StatDao
import com.solotodo.data.local.dao.SyncStateDao
import com.solotodo.data.local.dao.TaskDao
import com.solotodo.data.local.dao.TaskListDao
import com.solotodo.data.local.entity.SyncStateEntity
import com.solotodo.data.remote.dto.DailyQuestItemDto
import com.solotodo.data.remote.dto.DailyQuestLogDto
import com.solotodo.data.remote.dto.DungeonDto
import com.solotodo.data.remote.dto.DungeonFloorDto
import com.solotodo.data.remote.dto.RankEventDto
import com.solotodo.data.remote.dto.ReflectionDto
import com.solotodo.data.remote.dto.StatDto
import com.solotodo.data.remote.dto.TaskDto
import com.solotodo.data.remote.dto.TaskListDto
import com.solotodo.data.remote.dto.UserSettingsDto
import com.solotodo.data.remote.dto.toEntity
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pulls remote rows newer than each table's `last_pulled_at` bookmark and
 * writes them to Room **without** going through repositories — so the pull
 * path never emits op-log rows.
 *
 * Pagination: each table caps at [PAGE_LIMIT] rows per cycle. If a table
 * returns exactly the cap, the next periodic cycle advances the cursor
 * naturally and picks up the rest.
 */
@Singleton
class PullTranslator @Inject constructor(
    private val db: SoloTodoDb,
    private val postgrest: Postgrest,
    private val syncStateDao: SyncStateDao,
    private val taskDao: TaskDao,
    private val taskListDao: TaskListDao,
    private val dailyQuestDao: DailyQuestDao,
    private val dungeonDao: DungeonDao,
    private val reflectionDao: ReflectionDao,
    private val statDao: StatDao,
    private val settingsDao: SettingsDao,
    private val rankEventDao: RankEventDao,
) {

    data class TableResult(val entity: String, val rows: Int)

    suspend fun pullAll(userId: String): List<TableResult> {
        val results = mutableListOf<TableResult>()
        // FK / domain ordering — parents before children, core before leaves.
        results += pullTaskLists(userId)
        results += pullTasks(userId)
        results += pullDailyQuestItems(userId)
        results += pullDailyQuestLogs(userId)
        results += pullDungeons(userId)
        results += pullDungeonFloors(userId)
        results += pullReflections(userId)
        results += pullStats(userId)
        results += pullUserSettings(userId)
        results += pullRankEvents(userId)
        return results
    }

    private suspend fun pullTaskLists(userId: String) = pullTable("task_list", userId) { since ->
        // task_list has no updated_at column — full select each cycle (tiny table).
        val rows = postgrest.from("task_list").select {
            filter { eq("user_id", userId) }
            limit(PAGE_LIMIT.toLong())
        }.decodeList<TaskListDto>()
        if (rows.isNotEmpty()) {
            db.withTransaction { taskListDao.upsertAll(rows.map { it.toEntity() }) }
        }
        rows.size to since
    }

    private suspend fun pullTasks(userId: String) = pullTable("task", userId) { since ->
        val rows = selectNewer<TaskDto>("task", userId, since)
        val latest = rows.maxOfOrNull { it.updatedAt } ?: since
        if (rows.isNotEmpty()) {
            db.withTransaction { taskDao.upsertAll(rows.map { it.toEntity() }) }
        }
        rows.size to latest
    }

    private suspend fun pullDailyQuestItems(userId: String) = pullTable("daily_quest_item", userId) { since ->
        val rows = selectNewer<DailyQuestItemDto>("daily_quest_item", userId, since)
        val latest = rows.maxOfOrNull { it.updatedAt } ?: since
        if (rows.isNotEmpty()) {
            db.withTransaction { dailyQuestDao.upsertItems(rows.map { it.toEntity() }) }
        }
        rows.size to latest
    }

    private suspend fun pullDailyQuestLogs(userId: String) = pullTable("daily_quest_log", userId) { since ->
        val rows = selectNewer<DailyQuestLogDto>("daily_quest_log", userId, since, updatedAtColumn = "created_at")
        val latest = rows.maxOfOrNull { it.createdAt } ?: since
        if (rows.isNotEmpty()) {
            db.withTransaction { dailyQuestDao.upsertLogs(rows.map { it.toEntity() }) }
        }
        rows.size to latest
    }

    private suspend fun pullDungeons(userId: String) = pullTable("dungeon", userId) { since ->
        val rows = selectNewer<DungeonDto>("dungeon", userId, since)
        val latest = rows.maxOfOrNull { it.updatedAt } ?: since
        if (rows.isNotEmpty()) {
            db.withTransaction { dungeonDao.upsertDungeons(rows.map { it.toEntity() }) }
        }
        rows.size to latest
    }

    private suspend fun pullDungeonFloors(userId: String) = pullTable("dungeon_floor", userId) { since ->
        val rows = selectNewer<DungeonFloorDto>("dungeon_floor", userId, since)
        val latest = rows.maxOfOrNull { it.updatedAt } ?: since
        if (rows.isNotEmpty()) {
            db.withTransaction { dungeonDao.upsertFloors(rows.map { it.toEntity() }) }
        }
        rows.size to latest
    }

    private suspend fun pullReflections(userId: String) = pullTable("reflection", userId) { since ->
        val rows = selectNewer<ReflectionDto>("reflection", userId, since, updatedAtColumn = "generated_at")
        val latest = rows.maxOfOrNull { it.generatedAt } ?: since
        if (rows.isNotEmpty()) {
            db.withTransaction { reflectionDao.upsertAll(rows.map { it.toEntity() }) }
        }
        rows.size to latest
    }

    private suspend fun pullStats(userId: String) = pullTable("stat", userId) { since ->
        val rows = selectNewer<StatDto>("stat", userId, since)
        val latest = rows.maxOfOrNull { it.updatedAt } ?: since
        if (rows.isNotEmpty()) {
            db.withTransaction { statDao.upsertAll(rows.map { it.toEntity() }) }
        }
        rows.size to latest
    }

    private suspend fun pullUserSettings(userId: String) = pullTable("user_settings", userId) { since ->
        val rows = selectNewer<UserSettingsDto>("user_settings", userId, since)
        val latest = rows.maxOfOrNull { it.updatedAt } ?: since
        if (rows.isNotEmpty()) {
            db.withTransaction { settingsDao.upsert(rows.first().toEntity()) }
        }
        rows.size to latest
    }

    private suspend fun pullRankEvents(userId: String) = pullTable("rank_event", userId) { since ->
        val rows = selectNewer<RankEventDto>("rank_event", userId, since, updatedAtColumn = "occurred_at")
        val latest = rows.maxOfOrNull { it.occurredAt } ?: since
        if (rows.isNotEmpty()) {
            db.withTransaction {
                rows.forEach { rankEventDao.insert(it.toEntity()) }
            }
        }
        rows.size to latest
    }

    // ---- Internals ----

    private suspend fun pullTable(
        entity: String,
        userId: String,
        block: suspend (since: Instant) -> Pair<Int, Instant>,
    ): TableResult {
        val state = syncStateDao.get(entity)
        val since = state?.lastPulledAt ?: EPOCH
        val (count, latest) = block(since)
        db.withTransaction {
            syncStateDao.upsert(
                SyncStateEntity(
                    entity = entity,
                    lastPulledAt = latest,
                    lastUserId = userId,
                ),
            )
        }
        return TableResult(entity, count)
    }

    private suspend inline fun <reified T : Any> selectNewer(
        table: String,
        userId: String,
        since: Instant,
        updatedAtColumn: String = "updated_at",
    ): List<T> = postgrest.from(table).select {
        filter {
            eq("user_id", userId)
            gt(updatedAtColumn, since.toString())
        }
        order(updatedAtColumn, Order.ASCENDING)
        limit(PAGE_LIMIT.toLong())
    }.decodeList<T>()

    companion object {
        const val PAGE_LIMIT = 1000
        private val EPOCH: Instant = Instant.fromEpochMilliseconds(0)
    }
}
