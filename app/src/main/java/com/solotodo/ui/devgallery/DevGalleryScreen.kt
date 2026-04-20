package com.solotodo.ui.devgallery

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
fun DevGalleryScreen() {
    Box(modifier = Modifier.fillMaxSize().background(SoloTokens.Colors.BgVoid)) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
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
