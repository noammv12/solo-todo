package com.solotodo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.solotodo.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE task SET deleted_at = :at, updated_at = :at WHERE id = :id")
    suspend fun softDelete(id: String, at: Instant)

    @Query("UPDATE task SET completed_at = :at, updated_at = :at WHERE id = :id")
    suspend fun markComplete(id: String, at: Instant)

    @Query("UPDATE task SET completed_at = NULL, updated_at = :at WHERE id = :id")
    suspend fun markIncomplete(id: String, at: Instant)

    @Query("SELECT * FROM task WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Query("SELECT * FROM task WHERE id = :id")
    fun observeById(id: String): Flow<TaskEntity?>

    @Query(
        """
        SELECT * FROM task
        WHERE deleted_at IS NULL AND completed_at IS NULL AND shadowed_at IS NULL
        ORDER BY priority DESC, due_at ASC, created_at ASC
        """,
    )
    fun observeOpen(): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM task
        WHERE deleted_at IS NULL AND shadowed_at IS NULL
          AND due_at IS NOT NULL
          AND due_at >= :startOfDay AND due_at < :endOfDay
        ORDER BY due_at ASC, priority DESC
        """,
    )
    fun observeDueBetween(startOfDay: Instant, endOfDay: Instant): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM task
        WHERE deleted_at IS NULL AND list_id = :listId
        ORDER BY completed_at IS NULL DESC, due_at ASC, created_at DESC
        """,
    )
    fun observeByList(listId: String?): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM task
        WHERE deleted_at IS NULL AND shadowed_at IS NOT NULL
        ORDER BY shadowed_at DESC
        """,
    )
    fun observeArchive(): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM task
        WHERE deleted_at IS NULL
          AND (title LIKE '%' || :query || '%' OR raw_input LIKE '%' || :query || '%')
        ORDER BY completed_at IS NULL DESC, updated_at DESC
        LIMIT 200
        """,
    )
    fun search(query: String): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM task WHERE deleted_at IS NULL AND completed_at IS NOT NULL")
    fun observeCompletedCount(): Flow<Int>

    /** Hard-purges soft-deleted rows older than [cutoff]. Run periodically. */
    @Query("DELETE FROM task WHERE deleted_at IS NOT NULL AND deleted_at < :cutoff")
    suspend fun purgeDeletedBefore(cutoff: Instant): Int
}
