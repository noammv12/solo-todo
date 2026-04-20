package com.solotodo.ui.status

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solotodo.data.local.entity.DailyQuestItemEntity
import com.solotodo.data.local.entity.TaskEntity
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.components.DiamondCheck
import com.solotodo.designsystem.components.Panel
import com.solotodo.designsystem.components.ProgressBar
import com.solotodo.designsystem.components.Rank
import com.solotodo.designsystem.components.RankGlow
import com.solotodo.designsystem.components.RankGlyph
import com.solotodo.designsystem.components.Tag
import com.solotodo.designsystem.theme.SystemMonoLabel
import com.solotodo.ui.common.TaskRow

@Composable
fun StatusScreen(
    viewModel: StatusViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloTokens.Colors.BgVoid),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { HunterHeader(state) }
            item { DailyQuestCard(state, onToggle = viewModel::toggleDailyItem) }
            if (state.todayTasks.isNotEmpty()) {
                item { Tag("TODAY", color = SoloTokens.Colors.TextMuted) }
                items(state.todayTasks, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        onToggleComplete = { taskId ->
                            if (task.completedAt == null) viewModel.completeTask(taskId)
                            else viewModel.uncompleteTask(taskId)
                        },
                    )
                }
            } else {
                item { EmptyToday() }
            }
            item { Spacer(Modifier.height(96.dp)) } // room for tab bar + FAB
        }
    }
}

@Composable
private fun HunterHeader(state: StatusUiState) {
    val glow = when (state.rank.toDesignRank()) {
        Rank.S -> RankGlow.Gold
        Rank.A -> RankGlow.Red
        Rank.B -> RankGlow.Shadow
        else -> RankGlow.Cyan
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RankGlyph(rank = state.rank.toDesignRank(), size = 72.dp, glowOption = glow)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.designation.uppercase(),
                color = SoloTokens.Colors.Text,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "STREAK · ${state.streak} · LONGEST ${state.longestStreak}",
                color = SoloTokens.Colors.TextMuted,
                style = SystemMonoLabel,
            )
            val nextRankLabel = state.daysToNextRank?.let { "${it} days to ${state.rank.next()?.name ?: "MAX"}" }
                ?: "RANK · MAXED"
            Text(
                text = nextRankLabel,
                color = SoloTokens.Colors.TextDim,
                style = SystemMonoLabel,
            )
        }
    }
}

@Composable
private fun DailyQuestCard(
    state: StatusUiState,
    onToggle: (DailyQuestItemEntity) -> Unit,
) {
    Panel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Tag("DAILY QUEST")
                Text(
                    text = "${state.dqDone} / ${state.dqTarget}",
                    color = SoloTokens.Colors.Glow,
                    style = SystemMonoLabel,
                )
            }
            if (state.dqTarget > 0) {
                ProgressBar(
                    value = state.dqDone,
                    total = state.dqTarget,
                    segmented = state.dqTarget <= 10,
                )
            }
            if (state.dqItems.isEmpty()) {
                Text(
                    text = "NO DAILY ITEMS CONFIGURED",
                    color = SoloTokens.Colors.TextMuted,
                    style = MaterialTheme.typography.labelMedium,
                )
            } else {
                state.dqItems.forEach { item ->
                    val progress = state.dqProgressByItem[item.id] ?: 0
                    val target = parseTargetValueLocal(item.target)
                    val done = progress >= target
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(item) }
                            .padding(vertical = 4.dp),
                    ) {
                        DiamondCheck(checked = done, onCheckedChange = { onToggle(item) })
                        Text(
                            text = item.title.uppercase(),
                            color = if (done) SoloTokens.Colors.TextDim else SoloTokens.Colors.Text,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${item.stat.name}",
                            color = SoloTokens.Colors.TextMuted,
                            style = SystemMonoLabel,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyToday() {
    Panel(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "NO QUESTS TODAY. YOUR MOVE, HUNTER.",
            color = SoloTokens.Colors.TextMuted,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private fun com.solotodo.data.local.Rank.toDesignRank(): Rank = when (this) {
    com.solotodo.data.local.Rank.E -> Rank.E
    com.solotodo.data.local.Rank.D -> Rank.D
    com.solotodo.data.local.Rank.C -> Rank.C
    com.solotodo.data.local.Rank.B -> Rank.B
    com.solotodo.data.local.Rank.A -> Rank.A
    com.solotodo.data.local.Rank.S -> Rank.S
}

private fun parseTargetValueLocal(json: String): Int =
    Regex("""\"value\"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toIntOrNull() ?: 1
