package com.solotodo.ui.quests

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.components.Panel
import com.solotodo.designsystem.theme.SystemMonoLabel
import com.solotodo.ui.common.TaskRow

@Composable
fun QuestsScreen(
    viewModel: QuestsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloTokens.Colors.BgVoid),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FilterTabs(current = state.filter, onFilterSelected = viewModel::setFilter)
            if (state.tasks.isEmpty()) {
                EmptyState(filter = state.filter)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(state.tasks, key = { it.id }) { task ->
                        TaskRow(task = task, onToggleComplete = viewModel::complete)
                    }
                    item { Spacer(Modifier.height(96.dp)) }
                }
            }
        }
    }
}

@Composable
private fun FilterTabs(current: QuestsFilter, onFilterSelected: (QuestsFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        QuestsFilter.entries.forEach { filter ->
            val selected = filter == current
            Text(
                text = filter.name,
                color = if (selected) SoloTokens.Colors.Glow else SoloTokens.Colors.TextMuted,
                style = SystemMonoLabel,
                modifier = Modifier
                    .clickable { onFilterSelected(filter) }
                    .padding(4.dp),
            )
        }
    }
}

@Composable
private fun EmptyState(filter: QuestsFilter) {
    val copy = when (filter) {
        QuestsFilter.TODAY -> "NO QUESTS TODAY. YOUR MOVE, HUNTER."
        QuestsFilter.UPCOMING -> "THE PATH AHEAD IS CLEAR."
        QuestsFilter.ANYTIME -> "INBOX CLEARED."
        QuestsFilter.ARCHIVE -> "THE SHADOWS HOLD NOTHING YET."
    }
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Panel(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = copy,
                color = SoloTokens.Colors.TextMuted,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
