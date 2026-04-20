package com.solotodo.ui.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solotodo.data.local.Rank
import com.solotodo.data.local.entity.DailyQuestItemEntity
import com.solotodo.data.local.entity.DailyQuestLogEntity
import com.solotodo.data.local.entity.TaskEntity
import com.solotodo.data.local.entity.UserSettingsEntity
import com.solotodo.data.repository.DailyQuestRepository
import com.solotodo.data.repository.DungeonRepository
import com.solotodo.data.repository.SettingsRepository
import com.solotodo.data.repository.TaskRepository
import com.solotodo.domain.streak.RankProgression
import com.solotodo.domain.streak.StreakCalculator
import com.solotodo.domain.time.DayBoundary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import javax.inject.Inject

data class StatusUiState(
    val designation: String = "HUNTER",
    val rank: Rank = Rank.E,
    val streak: Int = 0,
    val longestStreak: Int = 0,
    val dungeonsCleared: Int = 0,
    val daysToNextRank: Int? = null,
    val dqItems: List<DailyQuestItemEntity> = emptyList(),
    val dqProgressByItem: Map<String, Int> = emptyMap(),
    val dqTarget: Int = 0,
    val dqDone: Int = 0,
    val todayTasks: List<TaskEntity> = emptyList(),
    val freezes: Int = 0,
)

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val taskRepo: TaskRepository,
    private val dqRepo: DailyQuestRepository,
    settingsRepo: SettingsRepository,
    dungeonRepo: DungeonRepository,
    private val dayBoundary: DayBoundary,
    private val clock: Clock,
) : ViewModel() {

    init {
        // Ensure settings row exists so the observe() Flow has something to emit.
        viewModelScope.launch { settingsRepo.initializeIfMissing() }
    }

    private val today: LocalDate get() = dayBoundary.today()

    private val historyWindow = 60 // days — enough to compute longest streak for rank calc

    val state: StateFlow<StatusUiState> = combine(
        settingsRepo.observe(),
        dqRepo.observeActiveItems(),
        dqRepo.observeLogsBetween(today.minus(historyWindow, DateTimeUnit.DAY), today),
        taskRepo.observeDueBetween(dayBoundary.todayWindow().first, dayBoundary.todayWindow().second),
        dungeonRepo.observeClearedCount(),
    ) { settings: UserSettingsEntity?, items, logs, tasks, clearedCount ->
        val completeDays = completeDaysFrom(items, logs)
        val streak = StreakCalculator.currentStreak(completeDays, today)
        val longest = StreakCalculator.longestStreak(completeDays)
        val rank = RankProgression.rankFor(longest, clearedCount, aToSGatesCleared = 0)
        val dqTarget = items.size
        val todaysLogs = logs.filter { it.day == today }
        val progressByItem = buildProgressMap(items, todaysLogs)
        val dqDone = progressByItem.count { (id, progress) ->
            val target = items.find { it.id == id }?.let { parseTargetValue(it.target) } ?: 1
            progress >= target
        }
        StatusUiState(
            designation = settings?.designation ?: "HUNTER",
            rank = rank,
            streak = streak,
            longestStreak = longest,
            dungeonsCleared = clearedCount,
            daysToNextRank = RankProgression.daysToNextRank(rank, longest),
            dqItems = items,
            dqProgressByItem = progressByItem,
            dqTarget = dqTarget,
            dqDone = dqDone,
            todayTasks = tasks,
            freezes = settings?.streakFreezes ?: 0,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatusUiState(),
    )

    fun toggleDailyItem(item: DailyQuestItemEntity) {
        viewModelScope.launch {
            val currentProgress = state.value.dqProgressByItem[item.id] ?: 0
            val target = parseTargetValue(item.target)
            val newProgress = if (currentProgress >= target) 0 else target
            dqRepo.reportProgress(questId = item.id, day = today, progress = newProgress, target = target)
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch { taskRepo.complete(taskId) }
    }

    fun uncompleteTask(taskId: String) {
        viewModelScope.launch { taskRepo.uncomplete(taskId) }
    }

    private fun completeDaysFrom(
        items: List<DailyQuestItemEntity>,
        logs: List<DailyQuestLogEntity>,
    ): List<LocalDate> {
        if (items.isEmpty()) return emptyList()
        val activeIds = items.filter { it.active }.map { it.id }.toHashSet()
        val requiredPerDay = items.count { it.active }
        // A day is COMPLETE when every active item has a log with completed_at != null on that day.
        val byDay = logs
            .filter { it.questId in activeIds && it.completedAt != null }
            .groupBy { it.day }
        return byDay.filterValues { it.size >= requiredPerDay }.keys.toList()
    }

    private fun buildProgressMap(
        items: List<DailyQuestItemEntity>,
        todaysLogs: List<DailyQuestLogEntity>,
    ): Map<String, Int> {
        val m = HashMap<String, Int>()
        items.forEach { m[it.id] = 0 }
        todaysLogs.forEach { m[it.questId] = it.progress }
        return m
    }

    private fun parseTargetValue(targetJson: String): Int {
        // Cheap integer extraction to avoid pulling kotlinx.serialization into the VM.
        val m = Regex("""\"value\"\s*:\s*(\d+)""").find(targetJson)
        return m?.groupValues?.get(1)?.toIntOrNull() ?: 1
    }
}
