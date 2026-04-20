package com.solotodo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solotodo.data.local.StatKind
import com.solotodo.data.local.entity.StatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stat: StatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(stats: List<StatEntity>)

    @Query("SELECT * FROM stat")
    fun observeAll(): Flow<List<StatEntity>>

    @Query("SELECT * FROM stat WHERE kind = :kind")
    suspend fun get(kind: StatKind): StatEntity?
}
