package com.solotodo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.solotodo.data.local.OpKind
import kotlinx.datetime.Instant

/**
 * `op_log` — local outbox of writes waiting to sync.
 *
 * Every repository write inserts an `OpLogEntity` in the same transaction as the
 * canonical entity write. On foreground, a sync engine (Phase 4) drains rows
 * with `synced_at = null`, pushes them to Supabase, and sets `synced_at`.
 *
 * `fields` is JSON of changed columns for a patch. For create, it's the full
 * row; for delete, null.
 *
 * `field_timestamps` is JSON `{ field: updated_at }` so server can apply per-field
 * LWW even when multiple patches land together.
 */
@Entity(
    tableName = "op_log",
    indices = [
        Index(value = ["synced_at"], name = "op_log_synced"),
        Index(value = ["entity", "entity_id"], name = "op_log_entity"),
    ],
)
data class OpLogEntity(
    @PrimaryKey @ColumnInfo(name = "op_id") val opId: String,
    val entity: String,
    @ColumnInfo(name = "entity_id") val entityId: String,
    val kind: OpKind,
    /** JSON object of changed fields, or null for deletes. */
    val fields: String?,
    /** JSON map `{ field: unix_millis }` per changed field. */
    @ColumnInfo(name = "field_timestamps") val fieldTimestamps: String,
    @ColumnInfo(name = "origin_device_id") val originDeviceId: String,
    @ColumnInfo(name = "applied_at") val appliedAt: Instant,
    @ColumnInfo(name = "synced_at") val syncedAt: Instant?,
)
