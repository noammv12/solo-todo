package com.solotodo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.solotodo.data.local.entity.DailyQuestItemEntity
import com.solotodo.data.local.entity.DailyQuestLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface DailyQuestDao {

    // ---- Items ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: DailyQuestItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<DailyQuestItemEntity>)

    @Update
    suspend fun updateItem(item: DailyQuestItemEntity)

    @Query("SELECT * FROM daily_quest_item WHERE active = 1 ORDER BY order_index ASC")
    fun observeActiveItems(): Flow<List<DailyQuestItemEntity>>

    @Query("SELECT * FROM daily_quest_item WHERE id = :id")
    suspend fun getItem(id: String): DailyQuestItemEntity?

    @Query("SELECT COUNT(*) FROM daily_quest_item WHERE active = 1")
    fun observeActiveItemCount(): Flow<Int>

    // ---- Logs (append-only, progress monotonic) ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: DailyQuestLogEntity)

    @Query("SELECT * FROM daily_quest_log WHERE day = :day ORDER BY quest_id")
    fun observeLogsForDay(day: LocalDate): Flow<List<DailyQuestLogEntity>>

    @Query("SELECT * FROM daily_quest_log WHERE day BETWEEN :from AND :to ORDER BY day, quest_id")
    fun observeLogsBetween(from: LocalDate, to: LocalDate): Flow<List<DailyQuestLogEntity>>

    @Query("SELECT * FROM daily_quest_log WHERE quest_id = :questId AND day = :day LIMIT 1")
    suspend fun getLog(questId: String, day: LocalDate): DailyQuestLogEntity?

    /**
     * Bump progress for today's log if it already exists, else caller inserts.
     * Progress is clamped monotonically by the caller (repository).
     */
    @Query(
        """
        UPDATE daily_quest_log
        SET progress = :progress,
            completed_at = :completedAt
        WHERE quest_id = :questId AND day = :day AND progress < :progress
        """,
    )
    suspend fun bumpProgress(
        questId: String,
        day: LocalDate,
        progress: Int,
        completedAt: kotlinx.datetime.Instant?,
    ): Int
}
