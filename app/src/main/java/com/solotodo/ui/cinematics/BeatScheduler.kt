package com.solotodo.ui.cinematics

import com.solotodo.data.local.Rank

/**
 * Declarative timing tables for each cinematic tier. The host reads these and
 * drives `delay()`-based phase transitions + haptic firings. Unit tests use
 * these tables to assert the beat cadence matches the prototype spec.
 *
 * All offsets are milliseconds from cinematic start.
 */
object BeatScheduler {

    data class Beats(
        val flashEndMs: Long,
        val chargeEndMs: Long,
        val liftEndMs: Long,
        val titleStartMs: Long,
        val titleEndMs: Long,
        val totalMs: Long,
        val hapticAtMs: Long,
        val burstCount: Int,
        val burstStartMs: Long,
        val burstIntervalMs: Long,
    )

    /** D/C/B (standard) — 2800ms. */
    val Standard: Beats = Beats(
        flashEndMs = 400,
        chargeEndMs = 1200,
        liftEndMs = 1800,
        titleStartMs = 1800,
        titleEndMs = 2800,
        totalMs = 2800,
        hapticAtMs = 1200,
        burstCount = 0, // overridden per rank by the cinematic composable
        burstStartMs = 1200,
        burstIntervalMs = 220,
    )

    /** A — 3400ms with extended title phase. */
    val Extended: Beats = Beats(
        flashEndMs = 400,
        chargeEndMs = 1200,
        liftEndMs = 1800,
        titleStartMs = 1800,
        titleEndMs = 3400,
        totalMs = 3400,
        hapticAtMs = 1200,
        burstCount = 3,
        burstStartMs = 1200,
        burstIntervalMs = 220,
    )

    /** S — 5400ms monumental. */
    val Monumental: Beats = Beats(
        flashEndMs = 700,
        chargeEndMs = 2700,
        liftEndMs = 3600,
        titleStartMs = 3600,
        titleEndMs = 5400,
        totalMs = 5400,
        hapticAtMs = 1200,
        burstCount = 5,
        burstStartMs = 2700,
        burstIntervalMs = 220,
    )

    /** Reduce-motion collapsed variant. Single frame, minimum 200ms hold. */
    const val ReducedHoldMs: Long = 200

    /** After tap-to-skip, hold the final frame for this long before dismissing. */
    const val SkipHoldMs: Long = 400

    /** D/C/B burst counts — per spec: D=0, C=1, B=2. */
    fun standardBurstCount(rank: Rank): Int = when (rank) {
        Rank.D -> 0
        Rank.C -> 1
        Rank.B -> 2
        else -> 0
    }
}
