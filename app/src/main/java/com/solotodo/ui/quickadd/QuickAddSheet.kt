package com.solotodo.ui.quickadd

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.components.Tag
import com.solotodo.designsystem.theme.SystemMonoLabel
import com.solotodo.domain.nl.NaturalLanguageDateParser

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    viewModel: QuickAddViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.reset()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = SoloTokens.Colors.BgPanelRaised,
        scrimColor = Color.Black.copy(alpha = 0.6f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Tag("QUICK ADD", color = SoloTokens.Colors.Glow)
            OutlinedTextField(
                value = state.input,
                onValueChange = viewModel::onInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("what must be done", color = SoloTokens.Colors.TextMuted) },
                singleLine = false,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.launchSubmit(onDismiss) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SoloTokens.Colors.Text,
                    unfocusedTextColor = SoloTokens.Colors.Text,
                    cursorColor = SoloTokens.Colors.Glow,
                    focusedBorderColor = SoloTokens.Colors.Glow,
                    unfocusedBorderColor = SoloTokens.Colors.Stroke,
                ),
            )

            if (state.parse.tokensFound.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.parse.tokensFound.forEach { token ->
                        Tag(
                            text = "${token.kind.name}: ${token.value}",
                            color = tokenColor(token.kind),
                        )
                    }
                }
            }

            val captureEnabled = state.input.isNotBlank() && !state.submitting
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val captureModifier = if (captureEnabled) {
                    Modifier.clickable { viewModel.launchSubmit(onDismiss) }
                } else {
                    Modifier
                }
                Text(
                    text = "› CAPTURE",
                    color = if (captureEnabled) SoloTokens.Colors.Glow else SoloTokens.Colors.TextDim,
                    style = SystemMonoLabel,
                    modifier = captureModifier.padding(12.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun tokenColor(kind: NaturalLanguageDateParser.Token.Kind): Color = when (kind) {
    NaturalLanguageDateParser.Token.Kind.DATE -> SoloTokens.Colors.Glow
    NaturalLanguageDateParser.Token.Kind.TIME -> SoloTokens.Colors.AccentShadowLift
    NaturalLanguageDateParser.Token.Kind.STAT -> SoloTokens.Colors.AccentGold
    NaturalLanguageDateParser.Token.Kind.LIST -> SoloTokens.Colors.Stroke
    NaturalLanguageDateParser.Token.Kind.PRIORITY -> SoloTokens.Colors.Danger
}
