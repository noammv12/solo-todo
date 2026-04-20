package com.solotodo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * `reflection` — weekly rollup produced every Sunday 20:00 local by a cron
 * Edge Function on the server. `week_start` is the ISO Monday date.
 *
 * `summary` JSON shape:
 * `{ tasks_done, streak_peak, rank_events, top_stat, shadowed_count }`.
 */
@Entity(
    tableName = "reflection",
    indices = [Index(value = ["week_start"], unique = true, name = "reflection_week")],
)
data class ReflectionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "week_start") val weekStart: LocalDate,
    val summary: String,
    @ColumnInfo(name = "generated_at") val generatedAt: Instant,
)
