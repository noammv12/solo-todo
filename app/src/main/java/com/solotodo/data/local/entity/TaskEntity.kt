package com.solotodo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.solotodo.data.local.StatKind
import kotlinx.datetime.Instant

/**
 * `task` — core to-do row.
 *
 * Mirrors the schema specced in `data-state.html` §01 and the `V1__init.sql`
 * in §06. Field-level LWW sync uses `updated_at` + `origin_device_id`.
 *
 * - `completed_at == null` → open task.
 * - `shadowed_at != null`  → archived (Shadow Archive) without completion.
 * - `deleted_at != null`   → soft-deleted; hard-purged 30 days later.
 *
 * `repeat` stored as JSON string: `{ rule, days?, interval? }`.
 * `subtasks` stored as JSON string: `[{ title, done }, …]`.
 */
@Entity(
    tableName = "task",
    indices = [
        Index(value = ["list_id", "due_at"], name = "task_list_due"),
        Index(value = ["updated_at"], name = "task_updated"),
        Index(value = ["deleted_at"], name = "task_deleted"),
    ],
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "raw_input") val rawInput: String?,
    @ColumnInfo(name = "due_at") val dueAt: Instant?,
    val repeat: String?,
    val stat: StatKind?,
    val xp: Int,
    @ColumnInfo(name = "list_id") val listId: String?,
    val priority: Int,
    @ColumnInfo(name = "completed_at") val completedAt: Instant?,
    @ColumnInfo(name = "shadowed_at") val shadowedAt: Instant?,
    val subtasks: String?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "origin_device_id") val originDeviceId: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: Instant?,
)
