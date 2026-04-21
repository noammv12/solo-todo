package com.solotodo.ui.cinematics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solotodo.core.a11y.ReduceMotionPolicy
import com.solotodo.core.haptics.SoloHaptics
import com.solotodo.core.lifecycle.ForegroundObserver
import com.solotodo.data.repository.RankEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CinematicHostViewModel @Inject constructor(
    private val rankEventRepository: RankEventRepository,
    private val haptics: SoloHaptics,
    foregroundObserver: ForegroundObserver,
    reduceMotionPolicy: ReduceMotionPolicy,
) : ViewModel() {

    val state: StateFlow<CinematicState> = combine(
        rankEventRepository.observePendingCinematics(),
        foregroundObserver.isForeground,
        reduceMotionPolicy.observe(),
    ) { pending, foreground, reduceMotion ->
        val highest = pending.maxByOrNull { it.toRank.ordinal }
        if (highest == null || !foreground) {
            CinematicState.Idle
        } else {
            CinematicState.Playing(event = highest, reduceMotion = reduceMotion)
        }
    }
        .distinctUntilChanged { a, b ->
            // Avoid restarting the same cinematic on reduceMotion toggle; only
            // restart when the underlying event changes or idle flips.
            when {
                a is CinematicState.Idle && b is CinematicState.Idle -> true
                a is CinematicState.Playing && b is CinematicState.Playing ->
                    a.event.id == b.event.id && a.reduceMotion == b.reduceMotion
                else -> false
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CinematicState.Idle,
        )

    fun onDismiss(eventId: String) {
        viewModelScope.launch { rankEventRepository.markPlayed(eventId) }
    }

    fun fireRankUpHaptic() {
        viewModelScope.launch { haptics.rankUpPulse() }
    }

    fun fireSuccessTap() {
        viewModelScope.launch { haptics.successTap() }
    }
}
