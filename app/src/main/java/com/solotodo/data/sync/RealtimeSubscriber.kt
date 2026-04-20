package com.solotodo.data.sync

import com.solotodo.data.local.dao.TaskDao
import com.solotodo.data.remote.dto.SyncJson
import com.solotodo.data.remote.dto.TaskDto
import com.solotodo.data.remote.dto.toEntity
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Opens a Supabase Realtime channel for the signed-in user and streams row
 * changes for the `task` table into Room silently (bypassing repositories so
 * no op-log is emitted).
 *
 * Phase 4.5b.3 scope: `task` only — the highest-churn table, used to prove
 * the infrastructure works end-to-end. Phase 4.5b.4 will add the remaining
 * tables once this is stable.
 *
 * Lifecycle: [start] is called by [SyncBootstrapper] on auth transitions into
 * Guest/Authenticated. [stop] closes the channel on NotAuthed. Safe to call
 * [start] repeatedly — it's a no-op while the same user is already subscribed.
 *
 * Fallback: the 15-minute `PeriodicSyncWorker` still runs. Realtime is a
 * latency optimiser, not a replacement — if the WebSocket drops silently
 * behind a corporate firewall or aggressive battery optimiser, periodic
 * polling catches us up.
 */
@Singleton
class RealtimeSubscriber @Inject constructor(
    private val realtime: Realtime,
    private val taskDao: TaskDao,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentChannel: RealtimeChannel? = null
    private var currentUserId: String? = null
    private var flowJob: Job? = null

    /**
     * Open a per-user Realtime channel. Idempotent: re-calling with the same
     * user is a no-op. Calling with a different user closes the old channel
     * first.
     */
    suspend fun start(userId: String) {
        if (currentUserId == userId && currentChannel != null) return
        stop()
        currentUserId = userId

        val channel = realtime.channel("user:$userId")
        currentChannel = channel

        val taskChanges = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "task"
            filter = "user_id=eq.$userId"
        }

        flowJob = taskChanges
            .onEach { action -> handleTaskChange(action) }
            .launchIn(scope)

        channel.subscribe()
    }

    /** Close the active channel. Safe to call when nothing is subscribed. */
    suspend fun stop() {
        flowJob?.cancel()
        flowJob = null
        currentChannel?.let { ch ->
            runCatching { ch.unsubscribe() }
            runCatching { realtime.removeChannel(ch) }
        }
        currentChannel = null
        currentUserId = null
    }

    private suspend fun handleTaskChange(action: PostgresAction) {
        when (action) {
            is PostgresAction.Insert -> applyTaskUpsert(action.record)
            is PostgresAction.Update -> applyTaskUpsert(action.record)
            is PostgresAction.Delete -> applyTaskDelete(action.oldRecord)
            is PostgresAction.Select -> Unit // not requested; ignore
        }
    }

    private suspend fun applyTaskUpsert(record: JsonElement) {
        val dto = runCatching {
            SyncJson.decodeFromJsonElement(TaskDto.serializer(), record)
        }.getOrNull() ?: return
        taskDao.upsert(dto.toEntity())
    }

    private suspend fun applyTaskDelete(oldRecord: JsonElement) {
        val id = runCatching {
            oldRecord.jsonObject["id"]?.jsonPrimitive?.content
        }.getOrNull() ?: return
        taskDao.deleteById(id)
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
