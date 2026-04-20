package com.solotodo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solotodo.data.local.entity.ReflectionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface ReflectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reflection: ReflectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(reflections: List<ReflectionEntity>)

    @Query("SELECT * FROM reflection WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ReflectionEntity?

    @Query("SELECT * FROM reflection ORDER BY week_start DESC")
    fun observeAll(): Flow<List<ReflectionEntity>>

    @Query("SELECT * FROM reflection WHERE week_start = :weekStart LIMIT 1")
    suspend fun getForWeek(weekStart: LocalDate): ReflectionEntity?
}
