package com.solotodo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * `daily_quest_log` — append-only progress record.
 *
 * One row per `(quest_id, day)` pair. `progress` is monotonic — never decreases
 * on sync conflict; MAX() is the reconciliation rule (`data-state.html` §05).
 * `completed_at` is set when progress reaches the item's target.
 */
@Entity(
    tableName = "daily_quest_log",
    indices = [
        Index(value = ["quest_id", "day"], unique = true, name = "dq_log_quest_day"),
    ],
)
data class DailyQuestLogEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "quest_id") val questId: String,
    /** User-local ISO date (yyyy-mm-dd). */
    val day: LocalDate,
    val progress: Int,
    @ColumnInfo(name = "completed_at") val completedAt: Instant?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
)
