package com.solotodo.data.repository

import androidx.room.withTransaction
import com.solotodo.data.DeviceId
import com.solotodo.data.local.OpKind
import com.solotodo.data.local.SoloTodoDb
import com.solotodo.data.local.StatKind
import com.solotodo.data.local.dao.TaskDao
import com.solotodo.data.local.entity.TaskEntity
import com.solotodo.data.sync.OpLogWriter
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin layer over [TaskDao] that also appends to the op-log.
 *
 * Every mutating method runs inside a single Room transaction — so the entity
 * and op-log either both succeed or both roll back. Reads pass through
 * unchanged.
 */
@Singleton
class TaskRepository @Inject constructor(
    private val db: SoloTodoDb,
    private val taskDao: TaskDao,
    private val opLog: OpLogWriter,
    private val deviceId: DeviceId,
    private val clock: Clock = Clock.System,
) {

    // ---- Reads ----
    fun observeOpen(): Flow<List<TaskEntity>> = taskDao.observeOpen()
    fun observeByList(listId: String?): Flow<List<TaskEntity>> = taskDao.observeByList(listId)
    fun observeArchive(): Flow<List<TaskEntity>> = taskDao.observeArchive()
    fun observeById(id: String): Flow<TaskEntity?> = taskDao.observeById(id)
    fun search(query: String): Flow<List<TaskEntity>> = taskDao.search(query)
    fun observeCompletedCount(): Flow<Int> = taskDao.observeCompletedCount()
    fun observeDueBetween(startOfDay: Instant, endOfDay: Instant): Flow<List<TaskEntity>> =
        taskDao.observeDueBetween(startOfDay, endOfDay)

    // ---- Writes ----
    suspend fun create(
        title: String,
        rawInput: String? = null,
        dueAt: Instant? = null,
        stat: StatKind? = null,
        listId: String? = null,
        priority: Int = 0,
        xp: Int = 0,
    ): String {
        val id = UUID.randomUUID().toString()
        val now = clock.now()
        val entity = TaskEntity(
            id = id,
            title = title,
            rawInput = rawInput,
            dueAt = dueAt,
            repeat = null,
            stat = stat,
            xp = xp,
            listId = listId,
            priority = priority,
            completedAt = null,
            shadowedAt = null,
            subtasks = null,
            createdAt = now,
            updatedAt = now,
            originDeviceId = deviceId.value,
            deletedAt = null,
        )
        db.withTransaction {
            taskDao.upsert(entity)
            opLog.record(
                entity = ENTITY,
                entityId = id,
                kind = OpKind.CREATE,
                fieldsJson = null,
                fieldTimestampsJson = "{}",
            )
        }
        return id
    }

    suspend fun complete(id: String) {
        val now = clock.now()
        db.withTransaction {
            taskDao.markComplete(id, now)
            opLog.record(ENTITY, id, OpKind.PATCH, fieldsJson = null, fieldTimestampsJson = "{}")
        }
    }

    suspend fun uncomplete(id: String) {
        val now = clock.now()
        db.withTransaction {
            taskDao.markIncomplete(id, now)
            opLog.record(ENTITY, id, OpKind.PATCH, fieldsJson = null, fieldTimestampsJson = "{}")
        }
    }

    suspend fun softDelete(id: String) {
        val now = clock.now()
        db.withTransaction {
            taskDao.softDelete(id, now)
            opLog.record(ENTITY, id, OpKind.DELETE, fieldsJson = null, fieldTimestampsJson = "{}")
        }
    }

    companion object {
        const val ENTITY = "task"
    }
}
