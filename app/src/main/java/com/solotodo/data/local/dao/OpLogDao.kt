package com.solotodo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solotodo.data.local.entity.OpLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface OpLogDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(op: OpLogEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(ops: List<OpLogEntity>)

    /** Drain unsynced ops in insertion order. */
    @Query("SELECT * FROM op_log WHERE synced_at IS NULL ORDER BY applied_at ASC LIMIT :limit")
    suspend fun pending(limit: Int = 100): List<OpLogEntity>

    @Query("UPDATE op_log SET synced_at = :at WHERE op_id IN (:opIds)")
    suspend fun markSynced(opIds: List<String>, at: Instant)

    @Query("DELETE FROM op_log WHERE synced_at IS NOT NULL AND synced_at < :cutoff")
    suspend fun purgeAckedBefore(cutoff: Instant): Int

    /** Live count of unsynced ops — drives the expedited-push debouncer. */
    @Query("SELECT COUNT(*) FROM op_log WHERE synced_at IS NULL")
    fun observePendingCount(): Flow<Int>

    /** Debug-only: wipe every op-log row. Used by the dev-gallery reset button. */
    @Query("DELETE FROM op_log")
    suspend fun clearAll(): Int

    /**
     * Record a push failure for [opId]. Bumps `retry_count` and stores the
     * truncated [errorMessage]. If the new count reaches [threshold], stamps
     * `synced_at` with [quarantineSentinel] so `pending()` skips the row.
     */
    @Query(
        """
        UPDATE op_log
        SET retry_count = retry_count + 1,
            last_error = :errorMessage,
            synced_at = CASE
                WHEN retry_count + 1 >= :threshold THEN :quarantineSentinel
                ELSE NULL
            END
        WHERE op_id = :opId
        """,
    )
    suspend fun incrementRetry(
        opId: String,
        errorMessage: String?,
        threshold: Int,
        quarantineSentinel: Instant,
    )

    /** Live count of quarantined ops (past the retry threshold). */
    @Query("SELECT COUNT(*) FROM op_log WHERE synced_at = :quarantineSentinel")
    fun observeQuarantinedCount(quarantineSentinel: Instant): Flow<Int>

    /** Most recent quarantined op's last error message, for debugging. */
    @Query(
        """
        SELECT last_error FROM op_log
        WHERE synced_at = :quarantineSentinel
        ORDER BY applied_at DESC LIMIT 1
        """,
    )
    suspend fun latestQuarantinedError(quarantineSentinel: Instant): String?

    /**
     * Reset every quarantined op so it retries. Clears `synced_at` and
     * `retry_count` but keeps `last_error` as a breadcrumb.
     */
    @Query(
        """
        UPDATE op_log
        SET synced_at = NULL, retry_count = 0
        WHERE synced_at = :quarantineSentinel
        """,
    )
    suspend fun releaseQuarantine(quarantineSentinel: Instant): Int
}
