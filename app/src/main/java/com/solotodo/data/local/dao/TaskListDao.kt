package com.solotodo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solotodo.data.local.entity.TaskListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(list: TaskListEntity)

    @Query("SELECT * FROM task_list ORDER BY order_index ASC")
    fun observeAll(): Flow<List<TaskListEntity>>

    @Query("SELECT * FROM task_list WHERE id = :id")
    suspend fun getById(id: String): TaskListEntity?

    @Query("DELETE FROM task_list WHERE id = :id")
    suspend fun delete(id: String)
}
