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
    /**
     * Failed-push counter. Bumped each time the sync engine throws while
     * pushing this op. When it crosses [QUARANTINE_THRESHOLD], `synced_at` is
     * set to [QUARANTINE_SENTINEL] so `pending()` skips it.
     */
    @ColumnInfo(name = "retry_count", defaultValue = "0")
    val retryCount: Int = 0,
    /** Truncated error message from the most recent push failure, for debugging. */
    @ColumnInfo(name = "last_error")
    val lastError: String? = null,
) {
    companion object {
        /** After this many failures, an op is quarantined. */
        const val QUARANTINE_THRESHOLD: Int = 5

        /**
         * Sentinel stamped into `synced_at` when an op is quarantined.
         * `Instant.DISTANT_PAST` is safely before any real clock value, so
         * `pending()` (which filters `synced_at IS NULL`) naturally skips it,
         * and `purgeAckedBefore(cutoff)` eventually cleans it up.
         */
        val QUARANTINE_SENTINEL: Instant = Instant.DISTANT_PAST
    }
}
