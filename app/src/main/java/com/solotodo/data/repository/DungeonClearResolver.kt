package com.solotodo.data.repository

import androidx.room.withTransaction
import com.solotodo.data.local.OpKind
import com.solotodo.data.local.SoloTodoDb
import com.solotodo.data.local.dao.DungeonDao
import com.solotodo.data.local.dao.TaskDao
import com.solotodo.data.sync.OpLogWriter
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Propagates task completion up through the Dungeon → Floor cleared-state
 * cache. When a task's completion finishes every task on a floor, that floor
 * is marked cleared; when every floor on a dungeon is cleared, the dungeon is
 * too. Called by [TaskRepository.complete] after the task-write txn commits.
 *
 * Runs inside its own transaction so failures here can't roll back the
 * original task mutation. Returns `true` when a dungeon just cleared — the
 * caller then triggers rank evaluation.
 */
@Singleton
class DungeonClearResolver @Inject constructor(
    private val db: SoloTodoDb,
    private val taskDao: TaskDao,
    private val dungeonDao: DungeonDao,
    private val opLog: OpLogWriter,
    private val clock: Clock = Clock.System,
) {
    suspend fun onTaskCompleted(taskId: String): Boolean {
        return db.withTransaction {
            val floors = dungeonDao.findFloorsContainingTaskId(taskId)
            if (floors.isEmpty()) return@withTransaction false

            var dungeonCleared = false
            val now = clock.now()

            for (floor in floors) {
                if (floor.clearedAt != null) continue
                val ids = parseTaskIds(floor.taskIds) ?: continue
                val allDone = ids.all { id -> taskDao.getById(id)?.completedAt != null }
                if (!allDone) continue

                dungeonDao.markFloorCleared(floor.id, now)
                opLog.record(
                    DungeonRepository.ENTITY_FLOOR,
                    floor.id,
                    OpKind.PATCH,
                    null,
                    "{}",
                )

                if (dungeonDao.countUnclearedFloors(floor.dungeonId) == 0) {
                    dungeonDao.markCleared(floor.dungeonId, now)
                    opLog.record(
                        DungeonRepository.ENTITY_DUNGEON,
                        floor.dungeonId,
                        OpKind.PATCH,
                        null,
                        "{}",
                    )
                    dungeonCleared = true
                }
            }
            dungeonCleared
        }
    }

    private fun parseTaskIds(raw: String): List<String>? =
        runCatching { Json.decodeFromString<List<String>>(raw) }.getOrNull()
}
