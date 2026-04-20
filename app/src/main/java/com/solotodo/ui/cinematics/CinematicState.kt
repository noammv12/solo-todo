package com.solotodo.ui.cinematics

import com.solotodo.data.local.entity.RankEventEntity

/**
 * Observable state of the cinematic host. Idle is the resting state; Playing
 * indicates the overlay is currently on screen showing [event].
 */
sealed interface CinematicState {
    data object Idle : CinematicState

    data class Playing(
        val event: RankEventEntity,
        val reduceMotion: Boolean,
    ) : CinematicState
}
