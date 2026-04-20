package com.solotodo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.solotodo.data.local.StatKind
import kotlinx.datetime.Instant

/**
 * `daily_quest_item` — one row per configured daily item.
 *
 * Every daily item maps to exactly one [StatKind]. `target` JSON shape:
 * `{ kind: 'count'|'duration'|'boolean', value: n, unit?: 'reps'|'min'|'km' }`.
 *
 * `active = false` means retired — preserved for historical logs but not shown
 * in today's Daily Quest list.
 */
@Entity(tableName = "daily_quest_item")
data class DailyQuestItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    /** JSON: `{ kind, value, unit? }`. */
    val target: String,
    val stat: StatKind,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    val active: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "origin_device_id") val originDeviceId: String,
)
