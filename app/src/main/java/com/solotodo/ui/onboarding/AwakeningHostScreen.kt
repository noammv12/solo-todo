package com.solotodo.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.solotodo.designsystem.SoloTokens
import com.solotodo.domain.onboarding.AwakeningEvent
import com.solotodo.domain.onboarding.AwakeningState
import com.solotodo.ui.onboarding.steps.Step1Awaken
import com.solotodo.ui.onboarding.steps.Step2Designate
import com.solotodo.ui.onboarding.steps.Step3DailyQuest
import com.solotodo.ui.onboarding.steps.Step4FirstQuest
import com.solotodo.ui.onboarding.steps.Step5Permissions

/**
 * Root Awakening screen. Routes the current FSM state to the matching step
 * composable. Persists and restores draft state via [AwakeningHostViewModel].
 *
 * State transitions animate as a 220ms crossfade so each step appears as a
 * fresh System panel rather than a jarring swap.
 */
@Composable
fun AwakeningHostScreen(
    viewModel: AwakeningHostViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val draft by viewModel.draft.collectAsState()
    val reduceMotion by viewModel.reduceMotion.collectAsState()
    val isForeground by viewModel.isForeground.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloTokens.Colors.BgVoid),
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                fadeIn(tween()) togetherWith fadeOut(tween())
            },
            label = "awakening-step",
        ) { s ->
            when (s) {
                AwakeningState.Black -> Step1Awaken(
                    reduceMotion = reduceMotion,
                    isForeground = isForeground,
                    haptics = viewModel.haptics,
                    onAwaken = { viewModel.onEvent(AwakeningEvent.AwakenTapped) },
                )
                AwakeningState.Designate -> Step2Designate(
                    value = draft.designation.orEmpty(),
                    onValueChange = viewModel::updateDesignation,
                    onSubmit = { viewModel.onEvent(AwakeningEvent.DesignationSubmitted(it)) },
                    onSkip = { viewModel.onEvent(AwakeningEvent.DesignationSkipped) },
                    haptics = viewModel.haptics,
                )
                AwakeningState.DailyQuest -> Step3DailyQuest(
                    selected = draft.selectedPresetIds,
                    onSelectedChange = viewModel::updateSelection,
                    onCommit = { viewModel.onEvent(AwakeningEvent.DailyQuestCommitted(it)) },
                    haptics = viewModel.haptics,
                )
                AwakeningState.FirstQuest -> Step4FirstQuest(
                    value = draft.firstQuestRaw.orEmpty(),
                    onValueChange = viewModel::updateFirstQuestRaw,
                    onSubmit = { value ->
                        if (value.isBlank()) {
                            viewModel.onEvent(AwakeningEvent.FirstQuestSkipped)
                        } else {
                            viewModel.onEvent(AwakeningEvent.FirstQuestCaptured(value))
                        }
                    },
                    parser = viewModel.nlParser,
                    haptics = viewModel.haptics,
                )
                AwakeningState.Permissions -> Step5Permissions(
                    onResolved = { allowed ->
                        viewModel.onEvent(AwakeningEvent.PermissionsResolved(allowed))
                    },
                    haptics = viewModel.haptics,
                )
                AwakeningState.Complete -> {
                    // Gate flow re-routes SoloTodoNav once Room settings flip.
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

private fun tween() = androidx.compose.animation.core.tween<Float>(durationMillis = 220)
