package com.solotodo.ui.devgallery

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solotodo.data.auth.AuthRepository
import com.solotodo.data.local.entity.SyncStateEntity
import com.solotodo.data.sync.SyncEngine
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.components.CornerBrackets
import com.solotodo.designsystem.components.DiamondCheck
import com.solotodo.designsystem.components.GateGlyph
import com.solotodo.designsystem.components.Panel
import com.solotodo.designsystem.components.ProgressBar
import com.solotodo.designsystem.components.QuestGlyph
import com.solotodo.designsystem.components.Rank
import com.solotodo.designsystem.components.RankGlow
import com.solotodo.designsystem.components.RankGlyph
import com.solotodo.designsystem.components.ShadowGlyph
import com.solotodo.designsystem.components.Tag
import com.solotodo.designsystem.components.TypeIn
import com.solotodo.designsystem.modifiers.panelIn
import com.solotodo.designsystem.modifiers.scanlineOverlay
import com.solotodo.designsystem.theme.SystemMonoLabel

/**
 * Dev-only visual gallery of every design-system primitive and token.
 *
 * Not user-facing. Entered from the Placeholder screen in DEBUG builds only.
 * Used to visually verify each primitive matches the prototype screenshots.
 */
@Composable
fun DevGalleryScreen(
    viewModel: DevGalleryViewModel = hiltViewModel(),
) {
    val completedCount by viewModel.completedCount.collectAsState()
    val status by viewModel.status.collectAsState()
    val pendingOpCount by viewModel.pendingOpCount.collectAsState()
    val quarantinedCount by viewModel.quarantinedCount.collectAsState()
    val latestQuarantinedError by viewModel.latestQuarantinedError.collectAsState()
    val syncStates by viewModel.syncStates.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val lastSnapshot by viewModel.lastSnapshot.collectAsState()
    Box(modifier = Modifier.fillMaxSize().background(SoloTokens.Colors.BgVoid)) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item { SectionHeader("SYNC · DIAGNOSTICS") }
            item {
                SyncStatusPanel(
                    pendingOpCount = pendingOpCount,
                    quarantinedCount = quarantinedCount,
                    latestQuarantinedError = latestQuarantinedError,
                    syncStates = syncStates,
                    authState = authState,
                    lastSnapshot = lastSnapshot,
                    onSyncNow = viewModel::syncNow,
                    onClearOpLog = viewModel::clearOpLog,
                    onReleaseQuarantine = viewModel::releaseQuarantine,
                )
            }

            item { SectionHeader("DATABASE · DEV TOOLS") }
            item { DbControls(completed = completedCount, status = status, onSeed = viewModel::seed, onWipe = viewModel::wipe) }

            item { SectionHeader("TOKENS · COLORS") }
            item { ColorSwatches() }

            item { SectionHeader("TOKENS · RANK COLORS") }
            item { RankColorSwatches() }

            item { SectionHeader("TOKENS · STAT COLORS") }
            item { StatColorSwatches() }

            item { SectionHeader("PRIMITIVE · PANEL") }
            item { PanelRow() }

            item { SectionHeader("PRIMITIVE · TAG") }
            item { TagRow() }

            item { SectionHeader("PRIMITIVE · RANK GLYPH") }
            item { RankGlyphRow() }

            item { SectionHeader("PRIMITIVE · DIAMOND CHECK") }
            item { DiamondCheckRow() }

            item { SectionHeader("PRIMITIVE · QUEST / GATE / SHADOW GLYPHS") }
            item { IconRow() }

            item { SectionHeader("PRIMITIVE · PROGRESS BARS") }
            item { ProgressBarRow() }

            item { SectionHeader("PRIMITIVE · TYPE-IN") }
            item { TypeInDemo() }

            item { SectionHeader("MODIFIER · PANEL IN") }
            item { PanelInDemo() }

            item { SectionHeader("MODIFIER · SCANLINE OVERLAY") }
            item { ScanlineDemo() }

            item { SectionHeader("MODIFIER · CORNER BRACKETS") }
            item { CornerBracketsDemo() }

            items(listOf("END OF GALLERY")) { marker ->
                Text(
                    text = marker,
                    color = SoloTokens.Colors.TextDim,
                    style = SystemMonoLabel,
                    modifier = Modifier.padding(top = 32.dp, bottom = 64.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Text(
        text = label,
        color = SoloTokens.Colors.Stroke,
        style = SystemMonoLabel,
    )
}

@Composable
private fun SyncStatusPanel(
    pendingOpCount: Int,
    quarantinedCount: Int,
    latestQuarantinedError: String?,
    syncStates: List<SyncStateEntity>,
    authState: AuthRepository.AuthState,
    lastSnapshot: SyncEngine.Snapshot?,
    onSyncNow: () -> Unit,
    onClearOpLog: () -> Unit,
    onReleaseQuarantine: () -> Unit,
) {
    Panel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "auth: ${authStateLabel(authState)}",
                color = SoloTokens.Colors.Text,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "pending op-log: $pendingOpCount",
                color = if (pendingOpCount == 0) SoloTokens.Colors.TextMuted else SoloTokens.Colors.Glow,
                style = SystemMonoLabel,
            )
            if (quarantinedCount > 0) {
                Text(
                    text = "quarantined: $quarantinedCount",
                    color = SoloTokens.Colors.Danger,
                    style = SystemMonoLabel,
                )
                latestQuarantinedError?.let { err ->
                    Text(
                        text = "  last: $err",
                        color = SoloTokens.Colors.Danger,
                        style = SystemMonoLabel,
                    )
                }
            }
            if (lastSnapshot != null) {
                Text(
                    text = "last sync: ${lastSnapshot.ranAt}",
                    color = SoloTokens.Colors.TextMuted,
                    style = SystemMonoLabel,
                )
                lastSnapshot.pushResult?.let {
                    Text(
                        text = "  push → pushed=${it.pushed} failed=${it.failed}",
                        color = if (it.failed == 0) SoloTokens.Colors.TextMuted else SoloTokens.Colors.Danger,
                        style = SystemMonoLabel,
                    )
                }
                lastSnapshot.pullResult?.let {
                    Text(
                        text = "  pull → rows=${it.total} tables=${it.perTable.size}",
                        color = SoloTokens.Colors.TextMuted,
                        style = SystemMonoLabel,
                    )
                }
                lastSnapshot.errorMessage?.let {
                    Text(
                        text = "  error: $it",
                        color = SoloTokens.Colors.Danger,
                        style = SystemMonoLabel,
                    )
                }
            } else {
                Text(
                    text = "last sync: —",
                    color = SoloTokens.Colors.TextDim,
                    style = SystemMonoLabel,
                )
            }
            if (syncStates.isNotEmpty()) {
                Text(
                    text = "per-table cursors:",
                    color = SoloTokens.Colors.TextMuted,
                    style = SystemMonoLabel,
                )
                syncStates.forEach { state ->
                    Text(
                        text = "  ${state.entity}: ${state.lastPulledAt ?: "—"}",
                        color = SoloTokens.Colors.TextDim,
                        style = SystemMonoLabel,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "› SYNC NOW",
                    color = SoloTokens.Colors.Glow,
                    style = SystemMonoLabel,
                    modifier = Modifier
                        .clickable { onSyncNow() }
                        .padding(8.dp),
                )
                Text(
                    text = "› CLEAR OP-LOG",
                    color = SoloTokens.Colors.Danger,
                    style = SystemMonoLabel,
                    modifier = Modifier
                        .clickable { onClearOpLog() }
                        .padding(8.dp),
                )
                if (quarantinedCount > 0) {
                    Text(
                        text = "› RELEASE QUARANTINE",
                        color = SoloTokens.Colors.Glow,
                        style = SystemMonoLabel,
                        modifier = Modifier
                            .clickable { onReleaseQuarantine() }
                            .padding(8.dp),
                    )
                }
            }
        }
    }
}

private fun authStateLabel(state: AuthRepository.AuthState): String = when (state) {
    AuthRepository.AuthState.Loading -> "loading"
    AuthRepository.AuthState.NotAuthed -> "not authed"
    is AuthRepository.AuthState.Guest -> "guest · ${state.userId.take(8)}…"
    is AuthRepository.AuthState.Authenticated -> "authed · ${state.email ?: state.userId.take(8) + "…"}"
}

@Composable
private fun DbControls(
    completed: Int,
    status: String?,
    onSeed: () -> Unit,
    onWipe: () -> Unit,
) {
    Panel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "completed tasks (live from Room): $completed",
                color = SoloTokens.Colors.Text,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (status != null) {
                Text(
                    text = "status: $status",
                    color = SoloTokens.Colors.Glow,
                    style = SystemMonoLabel,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "› SEED",
                    color = SoloTokens.Colors.Glow,
                    style = SystemMonoLabel,
                    modifier = Modifier
                        .clickable { onSeed() }
                        .padding(8.dp),
                )
                Text(
                    text = "› WIPE",
                    color = SoloTokens.Colors.Danger,
                    style = SystemMonoLabel,
                    modifier = Modifier
                        .clickable { onWipe() }
                        .padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun ColorSwatches() {
    val swatches = listOf(
        "BG VOID" to SoloTokens.Colors.BgVoid,
        "BG PANEL" to SoloTokens.Colors.BgPanel,
        "BG PANEL RAISED" to SoloTokens.Colors.BgPanelRaised,
        "BG PANEL DANGER" to SoloTokens.Colors.BgPanelDanger,
        "STROKE" to SoloTokens.Colors.Stroke,
        "GLOW" to SoloTokens.Colors.Glow,
        "TEXT" to SoloTokens.Colors.Text,
        "TEXT MUTED" to SoloTokens.Colors.TextMuted,
        "TEXT DIM" to SoloTokens.Colors.TextDim,
        "ACCENT SHADOW" to SoloTokens.Colors.AccentShadow,
        "ACCENT SHADOW LIFT" to SoloTokens.Colors.AccentShadowLift,
        "ACCENT GOLD" to SoloTokens.Colors.AccentGold,
        "DANGER" to SoloTokens.Colors.Danger,
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        swatches.forEach { (label, color) -> Swatch(label, color) }
    }
}

@Composable
private fun RankColorSwatches() {
    val swatches = listOf(
        "RANK E" to SoloTokens.Colors.RankE,
        "RANK D" to SoloTokens.Colors.RankD,
        "RANK C" to SoloTokens.Colors.RankC,
        "RANK B" to SoloTokens.Colors.RankB,
        "RANK A" to SoloTokens.Colors.RankA,
        "RANK S" to SoloTokens.Colors.RankS,
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        swatches.forEach { (label, color) -> Swatch(label, color) }
    }
}

@Composable
private fun StatColorSwatches() {
    val swatches = listOf(
        "STR" to SoloTokens.Colors.StatStr,
        "INT" to SoloTokens.Colors.StatInt,
        "SEN" to SoloTokens.Colors.StatSen,
        "VIT" to SoloTokens.Colors.StatVit,
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        swatches.forEach { (label, color) -> Swatch(label, color) }
    }
}

@Composable
private fun Swatch(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(28.dp).background(color))
        Spacer(Modifier.width(12.dp))
        Text(label, style = SystemMonoLabel, color = SoloTokens.Colors.TextMuted)
    }
}

@Composable
private fun PanelRow() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Panel(modifier = Modifier.fillMaxWidth()) {
            Text("DEFAULT PANEL", color = SoloTokens.Colors.Text, style = MaterialTheme.typography.titleSmall)
        }
        Panel(modifier = Modifier.fillMaxWidth(), glow = true) {
            Text("GLOW PANEL", color = SoloTokens.Colors.Glow, style = MaterialTheme.typography.titleSmall)
        }
        Panel(modifier = Modifier.fillMaxWidth(), danger = true) {
            Text("DANGER PANEL", color = SoloTokens.Colors.Danger, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun TagRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Tag("DAILY QUEST")
        Tag("RANK E", color = SoloTokens.Colors.RankE)
        Tag("DANGER", color = SoloTokens.Colors.Danger)
    }
}

@Composable
private fun RankGlyphRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        RankGlyph(rank = Rank.E, size = 48.dp, glowOption = RankGlow.None)
        RankGlyph(rank = Rank.D, size = 48.dp)
        RankGlyph(rank = Rank.C, size = 48.dp)
        RankGlyph(rank = Rank.B, size = 48.dp, glowOption = RankGlow.Shadow)
        RankGlyph(rank = Rank.A, size = 48.dp, glowOption = RankGlow.Red)
        RankGlyph(rank = Rank.S, size = 48.dp, glowOption = RankGlow.Gold)
    }
}

@Composable
private fun DiamondCheckRow() {
    var checked1 by remember { mutableStateOf(false) }
    var checked2 by remember { mutableStateOf(true) }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        DiamondCheck(checked = checked1, onCheckedChange = { checked1 = it })
        DiamondCheck(checked = checked2, onCheckedChange = { checked2 = it })
        Text("tap to toggle", color = SoloTokens.Colors.TextMuted, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun IconRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        QuestGlyph(size = 24.dp, color = SoloTokens.Colors.Stroke)
        QuestGlyph(size = 24.dp, filled = true, color = SoloTokens.Colors.Glow)
        QuestGlyph(size = 24.dp, ring = true, color = SoloTokens.Colors.TextMuted)
        GateGlyph(size = 24.dp, color = SoloTokens.Colors.AccentShadow)
        ShadowGlyph(size = 24.dp, color = SoloTokens.Colors.TextMuted)
    }
}

@Composable
private fun ProgressBarRow() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ProgressBar(value = 3, total = 5, segmented = true)
        ProgressBar(value = 42, total = 100)
    }
}

@Composable
private fun TypeInDemo() {
    Panel(modifier = Modifier.fillMaxWidth()) {
        TypeIn(
            text = "PROTOCOL INITIATED. THE SYSTEM AWAKENS.",
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun PanelInDemo() {
    Panel(
        modifier = Modifier
            .fillMaxWidth()
            .panelIn(),
    ) {
        Text("ENTERS AT 220MS EASE-OUT", color = SoloTokens.Colors.Text, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun ScanlineDemo() {
    Panel(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .scanlineOverlay(),
    ) {
        Column {
            Tag("CRT")
            Spacer(Modifier.height(8.dp))
            Text(
                "scanlines drift downward over 20s. opacity 4%.",
                color = SoloTokens.Colors.TextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun CornerBracketsDemo() {
    Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        CornerBrackets(modifier = Modifier.fillMaxSize())
        Text(
            "bracketed",
            modifier = Modifier.align(Alignment.Center),
            color = SoloTokens.Colors.TextMuted,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
