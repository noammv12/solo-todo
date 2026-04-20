package com.solotodo.ui.cinematics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solotodo.data.local.Rank
import com.solotodo.designsystem.SoloTokens
import kotlinx.coroutines.delay

/**
 * Root cinematic overlay — mount as a sibling above the nav host so it draws
 * above tab bar, FAB, and sheets (Z=200 per `SoloTokens.ZIndex.Cinematic`).
 *
 * Observes the pending rank-event queue, plays the highest-rank cinematic
 * while the app is foregrounded, and marks it played on natural dismiss or
 * tap-to-skip. Backgrounding mid-play drops to Idle; the event stays
 * `cinematic_played = false` and replays on next foreground.
 */
@Composable
fun CinematicHost(
    modifier: Modifier = Modifier,
    vm: CinematicHostViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = state is CinematicState.Playing,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .fillMaxSize()
            .zIndex(SoloTokens.ZIndex.Cinematic),
    ) {
        val playing = state as? CinematicState.Playing ?: return@AnimatedVisibility
        CinematicOverlay(
            playing = playing,
            onFireRankUpHaptic = vm::fireRankUpHaptic,
            onFireSuccessTap = vm::fireSuccessTap,
            onDismiss = { vm.onDismiss(playing.event.id) },
        )
    }
}

@Composable
private fun CinematicOverlay(
    playing: CinematicState.Playing,
    onFireRankUpHaptic: () -> Unit,
    onFireSuccessTap: () -> Unit,
    onDismiss: () -> Unit,
) {
    val event = playing.event
    val reduceMotion = playing.reduceMotion
    val beats = remember(event.id, reduceMotion) {
        if (reduceMotion) null
        else when (event.toRank) {
            Rank.A -> BeatScheduler.Extended
            Rank.S -> BeatScheduler.Monumental
            else -> BeatScheduler.Standard.copy(
                burstCount = BeatScheduler.standardBurstCount(event.toRank),
            )
        }
    }

    var elapsed by remember(event.id) { mutableLongStateOf(0L) }
    var skipping by remember(event.id) { mutableStateOf(false) }
    val updatedDismiss = rememberUpdatedState(onDismiss)

    LaunchedEffect(event.id, reduceMotion, skipping) {
        if (reduceMotion) {
            onFireSuccessTap()
            delay(BeatScheduler.ReducedHoldMs)
            updatedDismiss.value()
            return@LaunchedEffect
        }
        val b = beats ?: return@LaunchedEffect

        if (skipping) {
            elapsed = b.totalMs
            delay(BeatScheduler.SkipHoldMs)
            updatedDismiss.value()
            return@LaunchedEffect
        }

        val start = System.currentTimeMillis()
        var hapticFired = false
        while (true) {
            val e = (System.currentTimeMillis() - start).coerceAtMost(b.totalMs)
            elapsed = e
            if (!hapticFired && e >= b.hapticAtMs) {
                hapticFired = true
                onFireRankUpHaptic()
            }
            if (e >= b.totalMs) break
            delay(16)
        }
        updatedDismiss.value()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloTokens.Colors.BgVoid.copy(alpha = 0.96f))
            .pointerInput(event.id) {
                detectTapGestures(onTap = { skipping = true })
            },
    ) {
        when {
            reduceMotion -> RankUpCinematicReduced(toRank = event.toRank)
            event.toRank == Rank.S -> RankUpCinematicS(
                fromRank = event.fromRank,
                toRank = event.toRank,
                elapsedMs = elapsed,
                beats = beats ?: BeatScheduler.Monumental,
            )
            event.toRank == Rank.A -> RankUpCinematicA(
                fromRank = event.fromRank,
                toRank = event.toRank,
                elapsedMs = elapsed,
                beats = beats ?: BeatScheduler.Extended,
            )
            else -> RankUpCinematicStandard(
                fromRank = event.fromRank,
                toRank = event.toRank,
                elapsedMs = elapsed,
                beats = beats ?: BeatScheduler.Standard,
            )
        }
    }
}
