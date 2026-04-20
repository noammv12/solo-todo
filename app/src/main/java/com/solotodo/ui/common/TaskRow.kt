package com.solotodo.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.solotodo.data.local.entity.TaskEntity
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.components.DiamondCheck
import com.solotodo.designsystem.theme.SystemMonoLabel

/**
 * Single task list row. Tap toggles completion. Swipe gestures (complete
 * from right, delete from left) land in Phase 3.3.1 — for now the diamond
 * checkbox handles the common case.
 */
@Composable
fun TaskRow(
    task: TaskEntity,
    onToggleComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val done = task.completedAt != null
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleComplete(task.id) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DiamondCheck(checked = done, onCheckedChange = { onToggleComplete(task.id) })
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                color = if (done) SoloTokens.Colors.TextDim else SoloTokens.Colors.Text,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (done) TextDecoration.LineThrough else null,
            )
            if (task.stat != null) {
                Text(
                    text = task.stat.name,
                    color = SoloTokens.Colors.TextMuted,
                    style = SystemMonoLabel,
                )
            }
        }
        if (task.priority > 0) {
            val label = if (task.priority >= 2) "!!!" else "!!"
            val color = if (task.priority >= 2) SoloTokens.Colors.Danger else SoloTokens.Colors.AccentGold
            Text(text = label, color = color, style = SystemMonoLabel)
        }
    }
}
