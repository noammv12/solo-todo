package com.solotodo.data.sync

import com.solotodo.data.local.dao.DailyQuestDao
import com.solotodo.data.local.dao.DungeonDao
import com.solotodo.data.local.dao.RankEventDao
import com.solotodo.data.local.dao.ReflectionDao
import com.solotodo.data.local.dao.SettingsDao
import com.solotodo.data.local.dao.StatDao
import com.solotodo.data.local.dao.TaskDao
import com.solotodo.data.local.dao.TaskListDao
import com.solotodo.data.remote.dto.DailyQuestItemDto
import com.solotodo.data.remote.dto.DailyQuestLogDto
import com.solotodo.data.remote.dto.DungeonDto
import com.solotodo.data.remote.dto.DungeonFloorDto
import com.solotodo.data.remote.dto.RankEventDto
import com.solotodo.data.remote.dto.ReflectionDto
import com.solotodo.data.remote.dto.StatDto
import com.solotodo.data.remote.dto.SyncJson
import com.solotodo.data.remote.dto.TaskDto
import com.solotodo.data.remote.dto.TaskListDto
import com.solotodo.data.remote.dto.UserSettingsDto
import com.solotodo.data.remote.dto.toEntity
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Opens a Supabase Realtime channel for the signed-in user and streams row
 * changes for every synced table into Room silently (bypassing repositories
 * so no op-log is emitted).
 *
 * Phase 4.5b.4 scope: all synced tables — task, daily_quest_item,
 * daily_quest_log, dungeon, dungeon_floor, task_list, reflection, stat,
 * user_settings, rank_event.
 *
 * Lifecycle: [start] is called by [SyncBootstrapper] on auth transitions into
 * Guest/Authenticated. [stop] closes the channel on NotAuthed. Safe to call
 * [start] repeatedly — it's a no-op while the same user is already subscribed.
 *
 * Fallback: the 15-minute [PeriodicSyncWorker] still runs. Realtime is a
 * latency optimiser, not a replacement — if the WebSocket drops silently
 * behind a corporate firewall or aggressive battery optimiser, periodic
 * polling catches us up.
 */
@Singleton
class RealtimeSubscriber @Inject constructor(
    private val realtime: Realtime,
    private val taskDao: TaskDao,
    private val dailyQuestDao: DailyQuestDao,
    private val dungeonDao: DungeonDao,
    private val taskListDao: TaskListDao,
    private val reflectionDao: ReflectionDao,
    private val statDao: StatDao,
    private val settingsDao: SettingsDao,
    private val rankEventDao: RankEventDao,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentChannel: RealtimeChannel? = null
    private var currentUserId: String? = null
    private val flowJobs = mutableListOf<Job>()

    /**
     * Open a per-user Realtime channel on every synced table. Idempotent:
     * re-calling with the same user is a no-op. Calling with a different
     * user closes the old channel first.
     */
    suspend fun start(userId: String) {
        if (currentUserId == userId && currentChannel != null) return
        stop()
        currentUserId = userId

        val channel = realtime.channel("user:$userId")
        currentChannel = channel

        // One postgresChangeFlow per table. Each one is a narrow, self-healing
        // subscription; isolating tables means a decode error on one payload
        // can't poison the rest.
        subscribe(channel, "task", userId, TaskDto.serializer()) { action, dto, recordId ->
            when (action) {
                is PostgresAction.Insert, is PostgresAction.Update -> dto?.let { taskDao.upsert(it.toEntity()) }
                is PostgresAction.Delete -> recordId?.let { taskDao.deleteById(it) }
                else -> Unit
            }
        }
        subscribe(channel, "daily_quest_item", userId, DailyQuestItemDto.serializer()) { action, dto, recordId ->
            when (action) {
                is PostgresAction.Insert, is PostgresAction.Update -> dto?.let { dailyQuestDao.upsertItem(it.toEntity()) }
                is PostgresAction.Delete -> recordId?.let { dailyQuestDao.deleteItemById(it) }
                else -> Unit
            }
        }
        subscribe(channel, "daily_quest_log", userId, DailyQuestLogDto.serializer()) { action, dto, recordId ->
            when (action) {
                is PostgresAction.Insert, is PostgresAction.Update -> dto?.let { dailyQuestDao.upsertLog(it.toEntity()) }
                is PostgresAction.Delete -> recordId?.let { dailyQuestDao.deleteLogById(it) }
                else -> Unit
            }
        }
        subscribe(channel, "dungeon", userId, DungeonDto.serializer()) { action, dto, recordId ->
            when (action) {
                is PostgresAction.Insert, is PostgresAction.Update -> dto?.let { dungeonDao.upsertDungeon(it.toEntity()) }
                is PostgresAction.Delete -> recordId?.let { dungeonDao.deleteDungeonById(it) }
                else -> Unit
            }
        }
        subscribe(channel, "dungeon_floor", userId, DungeonFloorDto.serializer()) { action, dto, recordId ->
            when (action) {
                is PostgresAction.Insert, is PostgresAction.Update -> dto?.let { dungeonDao.upsertFloor(it.toEntity()) }
                is PostgresAction.Delete -> recordId?.let { dungeonDao.deleteFloorById(it) }
                else -> Unit
            }
        }
        subscribe(channel, "task_list", userId, TaskListDto.serializer()) { action, dto, recordId ->
            when (action) {
                is PostgresAction.Insert, is PostgresAction.Update -> dto?.let { taskListDao.upsert(it.toEntity()) }
                is PostgresAction.Delete -> recordId?.let { taskListDao.delete(it) }
                else -> Unit
            }
        }
        subscribe(channel, "reflection", userId, ReflectionDto.serializer()) { action, dto, recordId ->
            when (action) {
                is PostgresAction.Insert, is PostgresAction.Update -> dto?.let { reflectionDao.upsert(it.toEntity()) }
                is PostgresAction.Delete -> recordId?.let { reflectionDao.deleteById(it) }
                else -> Unit
            }
        }
        subscribe(channel, "stat", userId, StatDto.serializer()) { action, dto, _ ->
            when (action) {
                is PostgresAction.Insert, is PostgresAction.Update -> dto?.let { statDao.upsert(it.toEntity()) }
                else -> Unit // Deletes on stat rows aren't expected in the app flow.
            }
        }
        subscribe(channel, "user_settings", userId, UserSettingsDto.serializer()) { action, dto, _ ->
            when (action) {
                is PostgresAction.Insert, is PostgresAction.Update -> dto?.let { settingsDao.upsert(it.toEntity()) }
                else -> Unit // Singleton row per user; no delete path.
            }
        }
        subscribe(channel, "rank_event", userId, RankEventDto.serializer()) { action, dto, recordId ->
            when (action) {
                is PostgresAction.Insert, is PostgresAction.Update -> dto?.let { rankEventDao.insert(it.toEntity()) }
                is PostgresAction.Delete -> recordId?.let { rankEventDao.deleteById(it) }
                else -> Unit
            }
        }

        channel.subscribe()
    }

    /** Close the active channel. Safe to call when nothing is subscribed. */
    suspend fun stop() {
        flowJobs.forEach { it.cancel() }
        flowJobs.clear()
        currentChannel?.let { ch ->
            runCatching { ch.unsubscribe() }
            runCatching { realtime.removeChannel(ch) }
        }
        currentChannel = null
        currentUserId = null
    }

    /**
     * Attach a postgresChangeFlow for a single table and route each event
     * through [handler]. Decode errors are swallowed per-event so one bad
     * payload can't kill the stream.
     */
    private fun <T : Any> subscribe(
        channel: RealtimeChannel,
        tableName: String,
        userId: String,
        serializer: DeserializationStrategy<T>,
        handler: suspend (action: PostgresAction, dto: T?, recordId: String?) -> Unit,
    ) {
        val flow: Flow<PostgresAction> = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = tableName
            filter("user_id", FilterOperator.EQ, userId)
        }
        val job = flow
            .onEach { action ->
                val record = recordFor(action)
                val dto = record?.let {
                    runCatching { SyncJson.decodeFromJsonElement(serializer, it) }.getOrNull()
                }
                val recordId = record?.let {
                    runCatching { it.jsonObject["id"]?.jsonPrimitive?.content }.getOrNull()
                }
                runCatching { handler(action, dto, recordId) }
            }
            .launchIn(scope)
        flowJobs += job
    }

    private fun recordFor(action: PostgresAction): JsonElement? = when (action) {
        is PostgresAction.Insert -> action.record
        is PostgresAction.Update -> action.record
        is PostgresAction.Delete -> action.oldRecord
        is PostgresAction.Select -> action.record
    }

    /**
     * Fire-and-forget wrapper used from callers that can't easily bridge to
     * the suspend [start]. Launches on the subscriber's own scope.
     */
    fun startAsync(userId: String) {
        scope.launch { start(userId) }
    }

    /** Fire-and-forget counterpart of [stop]. */
    fun stopAsync() {
        scope.launch { stop() }
    }
}
