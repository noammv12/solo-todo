package com.solotodo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * `task_list` — user-created list (Work, Personal, Health, …).
 *
 * The spec calls this entity `List`, but that collides with `kotlin.collections.List`;
 * we prefix with `Task` in the codebase. Table name stays `task_list`.
 *
 * Inbox is synthetic — represented by `list_id = null` on Task, not by a row here.
 */
@Entity(tableName = "task_list")
data class TaskListEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "color_token") val colorToken: String?,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
)
