package com.solotodo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * Per-entity sync bookmark. One row per synced table (entity name matching the
 * remote table). Read by the pull path to build an `updated_at > lastPulledAt`
 * query; written after each successful batch pull.
 *
 * `lastUserId` is checked on bootstrap — if it doesn't match the currently
 * signed-in user, we wipe local state before pulling to avoid cross-user leaks.
 */
@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val entity: String,
    @ColumnInfo(name = "last_pulled_at") val lastPulledAt: Instant?,
    @ColumnInfo(name = "last_user_id") val lastUserId: String?,
)
