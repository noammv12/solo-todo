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
import com.solotodo.data.repository.RankEventRepository
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
    rankEventRepo: RankEventRepository,
    private val dayBoundary: DayBoundary,
    private val clock: Clock,
) : ViewModel() {

    init {
        // Ensure settings row exists so the observe() Flow has something to emit.
        viewModelScope.launch { settingsRepo.initializeIfMissing() }
    }

    private val today: LocalDate get() = dayBoundary.today()

    private val historyWindow = 60 // days — enough to compute longest streak for rank calc

    private val baseInputs = combine(
        settingsRepo.observe(),
        dqRepo.observeActiveItems(),
        dqRepo.observeLogsBetween(today.minus(historyWindow, DateTimeUnit.DAY), today),
        taskRepo.observeDueBetween(dayBoundary.todayWindow().first, dayBoundary.todayWindow().second),
        dungeonRepo.observeClearedCount(),
    ) { settings: UserSettingsEntity?, items, logs, tasks, clearedCount ->
        BaseInputs(settings, items, logs, tasks, clearedCount)
    }

    val state: StateFlow<StatusUiState> = combine(
        baseInputs,
        rankEventRepo.observeCurrentRank(),
    ) { inputs, rank ->
        val completeDays = completeDaysFrom(inputs.items, inputs.logs)
        val streak = StreakCalculator.currentStreak(completeDays, today)
        val longest = StreakCalculator.longestStreak(completeDays)
        val dqTarget = inputs.items.size
        val todaysLogs = inputs.logs.filter { it.day == today }
        val progressByItem = buildProgressMap(inputs.items, todaysLogs)
        val dqDone = progressByItem.count { (id, progress) ->
            val target = inputs.items.find { it.id == id }?.let { parseTargetValue(it.target) } ?: 1
            progress >= target
        }
        StatusUiState(
            designation = inputs.settings?.designation ?: "HUNTER",
            rank = rank,
            streak = streak,
            longestStreak = longest,
            dungeonsCleared = inputs.clearedCount,
            daysToNextRank = RankProgression.daysToNextRank(rank, longest),
            dqItems = inputs.items,
            dqProgressByItem = progressByItem,
            dqTarget = dqTarget,
            dqDone = dqDone,
            todayTasks = inputs.tasks,
            freezes = inputs.settings?.streakFreezes ?: 0,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatusUiState(),
    )

    private data class BaseInputs(
        val settings: UserSettingsEntity?,
        val items: List<DailyQuestItemEntity>,
        val logs: List<DailyQuestLogEntity>,
        val tasks: List<TaskEntity>,
        val clearedCount: Int,
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
