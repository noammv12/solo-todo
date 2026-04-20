package com.solotodo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solotodo.data.local.entity.SyncStateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface SyncStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: SyncStateEntity)

    @Query("SELECT * FROM sync_state WHERE entity = :entity LIMIT 1")
    suspend fun get(entity: String): SyncStateEntity?

    @Query("SELECT * FROM sync_state")
    suspend fun getAll(): List<SyncStateEntity>

    @Query("SELECT * FROM sync_state ORDER BY entity ASC")
    fun observeAll(): Flow<List<SyncStateEntity>>

    @Query("UPDATE sync_state SET last_pulled_at = :at WHERE entity = :entity")
    suspend fun setLastPulledAt(entity: String, at: Instant)

    @Query("SELECT last_user_id FROM sync_state LIMIT 1")
    suspend fun anyLastUserId(): String?

    @Query("DELETE FROM sync_state")
    suspend fun clear()
}
