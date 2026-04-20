package com.solotodo.domain.rank

import com.solotodo.data.local.dao.DailyQuestDao
import com.solotodo.data.local.dao.DungeonDao
import com.solotodo.data.local.entity.DailyQuestItemEntity
import com.solotodo.data.local.entity.DailyQuestLogEntity
import com.solotodo.domain.time.DayBoundary
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads a point-in-time snapshot of streak + dungeon counters for rank
 * evaluation. Intended to be called inside an active `db.withTransaction { }`
 * so the snapshot is consistent with the caller's write.
 *
 * `completeDays` logic mirrors `StatusViewModel.completeDaysFrom` exactly — a
 * day counts as complete when every active Daily Quest item has a log with
 * `completedAt != null` on that day.
 */
@Singleton
class StreakSnapshotReader @Inject constructor(
    private val dqDao: DailyQuestDao,
    private val dungeonDao: DungeonDao,
    private val dayBoundary: DayBoundary,
) {
    suspend fun snapshot(): Snapshot {
        val today = dayBoundary.today()
        val items = dqDao.getActiveItems()
        val from = today.minus(HISTORY_WINDOW_DAYS, DateTimeUnit.DAY)
        val logs = dqDao.getLogsBetween(from, today)
        val completeDays = completeDaysFrom(items, logs)
        val clearedCount = dungeonDao.getClearedCount()
        return Snapshot(
            currentStreak = com.solotodo.domain.streak.StreakCalculator
                .currentStreak(completeDays, today),
            longestStreak = com.solotodo.domain.streak.StreakCalculator
                .longestStreak(completeDays),
            dungeonsCleared = clearedCount,
        )
    }

    data class Snapshot(
        val currentStreak: Int,
        val longestStreak: Int,
        val dungeonsCleared: Int,
    )

    private fun completeDaysFrom(
        items: List<DailyQuestItemEntity>,
        logs: List<DailyQuestLogEntity>,
    ): List<LocalDate> {
        if (items.isEmpty()) return emptyList()
        val activeIds = items.filter { it.active }.map { it.id }.toHashSet()
        val requiredPerDay = items.count { it.active }
        val byDay = logs
            .filter { it.questId in activeIds && it.completedAt != null }
            .groupBy { it.day }
        return byDay.filterValues { it.size >= requiredPerDay }.keys.toList()
    }

    companion object {
        /** Match [StatusViewModel.historyWindow]. Long enough for S rank (150-day streak). */
        const val HISTORY_WINDOW_DAYS = 200
    }
}
