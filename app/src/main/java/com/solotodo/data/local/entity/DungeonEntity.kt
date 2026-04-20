package com.solotodo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.solotodo.data.local.Rank
import kotlinx.datetime.Instant

/**
 * `dungeon` — multi-floor project.
 *
 * `rank` is cosmetic only in our build (the gameplay decision — see plan overrides).
 * `cleared_at` is set when the final floor transitions to CLEARED.
 * `abandoned_at` marks a dungeon the user cancelled mid-clear.
 */
@Entity(tableName = "dungeon")
data class DungeonEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val rank: Rank,
    @ColumnInfo(name = "due_at") val dueAt: Instant?,
    @ColumnInfo(name = "cleared_at") val clearedAt: Instant?,
    @ColumnInfo(name = "abandoned_at") val abandonedAt: Instant?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "origin_device_id") val originDeviceId: String,
)
