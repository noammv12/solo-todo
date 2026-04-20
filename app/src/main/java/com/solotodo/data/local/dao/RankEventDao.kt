package com.solotodo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solotodo.data.local.entity.RankEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RankEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: RankEventEntity)

    @Query("SELECT * FROM rank_event ORDER BY occurred_at DESC")
    fun observeAll(): Flow<List<RankEventEntity>>

    @Query("SELECT * FROM rank_event WHERE cinematic_played = 0 ORDER BY occurred_at ASC")
    fun observePending(): Flow<List<RankEventEntity>>

    @Query("UPDATE rank_event SET cinematic_played = 1 WHERE id = :id")
    suspend fun markPlayed(id: String)

    @Query("SELECT to_rank FROM rank_event ORDER BY occurred_at DESC LIMIT 1")
    suspend fun currentRank(): String?
}
