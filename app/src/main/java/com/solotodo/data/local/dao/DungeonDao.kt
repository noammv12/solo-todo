package com.solotodo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.solotodo.data.local.entity.DungeonEntity
import com.solotodo.data.local.entity.DungeonFloorEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface DungeonDao {

    // ---- Dungeons ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDungeon(dungeon: DungeonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDungeons(dungeons: List<DungeonEntity>)

    @Update
    suspend fun updateDungeon(dungeon: DungeonEntity)

    @Query("SELECT * FROM dungeon WHERE id = :id")
    suspend fun getDungeon(id: String): DungeonEntity?

    @Query("SELECT * FROM dungeon WHERE id = :id")
    fun observeDungeon(id: String): Flow<DungeonEntity?>

    @Query("SELECT * FROM dungeon WHERE cleared_at IS NULL AND abandoned_at IS NULL ORDER BY created_at DESC")
    fun observeActive(): Flow<List<DungeonEntity>>

    @Query("SELECT * FROM dungeon WHERE cleared_at IS NOT NULL ORDER BY cleared_at DESC")
    fun observeCleared(): Flow<List<DungeonEntity>>

    @Query("SELECT COUNT(*) FROM dungeon WHERE cleared_at IS NOT NULL")
    fun observeClearedCount(): Flow<Int>

    @Query("UPDATE dungeon SET cleared_at = :at, updated_at = :at WHERE id = :id")
    suspend fun markCleared(id: String, at: Instant)

    @Query("UPDATE dungeon SET abandoned_at = :at, updated_at = :at WHERE id = :id")
    suspend fun markAbandoned(id: String, at: Instant)

    // ---- Floors ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFloor(floor: DungeonFloorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFloors(floors: List<DungeonFloorEntity>)

    @Query("SELECT * FROM dungeon_floor WHERE dungeon_id = :dungeonId ORDER BY order_index ASC")
    fun observeFloorsForDungeon(dungeonId: String): Flow<List<DungeonFloorEntity>>

    @Query("SELECT * FROM dungeon_floor WHERE id = :id")
    suspend fun getFloor(id: String): DungeonFloorEntity?

    /** Used only by the Realtime subscriber when a DELETE event arrives. */
    @Query("DELETE FROM dungeon WHERE id = :id")
    suspend fun deleteDungeonById(id: String)

    /** Used only by the Realtime subscriber when a DELETE event arrives. */
    @Query("DELETE FROM dungeon_floor WHERE id = :id")
    suspend fun deleteFloorById(id: String)
}
