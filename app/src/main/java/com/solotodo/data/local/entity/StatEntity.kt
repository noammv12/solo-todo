package com.solotodo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.solotodo.data.local.StatKind
import kotlinx.datetime.Instant

/**
 * `stat` — derived cumulative-XP per stat axis.
 *
 * Not directly synced (conflict rule is `RECOMPUTE` per `data-state.html` §05).
 * Cached locally for fast reads; recomputed from Task + DailyQuestLog after every
 * sync cycle.
 */
@Entity(tableName = "stat")
data class StatEntity(
    @PrimaryKey val kind: StatKind,
    val value: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
