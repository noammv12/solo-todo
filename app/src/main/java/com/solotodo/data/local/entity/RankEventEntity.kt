package com.solotodo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.solotodo.data.local.Rank
import kotlinx.datetime.Instant

/**
 * `rank_event` — append-only rank promotion/demotion log.
 *
 * Server-side dedup key: `(user, from_rank, to_rank, bucket_5min(occurred_at))`.
 * Clients insert optimistically; server reconciles. `cinematic_played = false`
 * until the user has seen or skipped the associated cinematic.
 */
@Entity(
    tableName = "rank_event",
    indices = [Index(value = ["occurred_at"], name = "rank_event_occurred")],
)
data class RankEventEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "from_rank") val fromRank: Rank,
    @ColumnInfo(name = "to_rank") val toRank: Rank,
    @ColumnInfo(name = "consecutive_days") val consecutiveDays: Int,
    @ColumnInfo(name = "occurred_at") val occurredAt: Instant,
    @ColumnInfo(name = "cinematic_played") val cinematicPlayed: Boolean,
)
