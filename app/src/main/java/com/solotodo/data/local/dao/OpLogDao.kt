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
}
