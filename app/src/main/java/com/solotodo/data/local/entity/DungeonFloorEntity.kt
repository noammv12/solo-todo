package com.solotodo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.solotodo.data.local.FloorState
import kotlinx.datetime.Instant

/**
 * `dungeon_floor` — ordered step within a [DungeonEntity].
 *
 * `task_ids` is a JSON array of Task IDs — order matters (display) but
 * clearance is all-or-nothing ("floor clears when all referenced tasks
 * close", spec §04).
 *
 * `cleared_at` is cached — derivable from task state but kept for fast lookup.
 */
@Entity(
    tableName = "dungeon_floor",
    foreignKeys = [
        ForeignKey(
            entity = DungeonEntity::class,
            parentColumns = ["id"],
            childColumns = ["dungeon_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["dungeon_id"], name = "floor_dungeon")],
)
data class DungeonFloorEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "dungeon_id") val dungeonId: String,
    val title: String,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    /** JSON array of task IDs. */
    @ColumnInfo(name = "task_ids") val taskIds: String,
    val state: FloorState,
    @ColumnInfo(name = "cleared_at") val clearedAt: Instant?,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
