package com.solotodo.ui.cinematics

import com.solotodo.data.local.Rank
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Sanity checks against the prototype spec. These values are load-bearing —
 * the haptic fires at `hapticAtMs`, bursts at `burstStartMs`, title phase at
 * `titleStartMs`..`titleEndMs`. Regressions here mean the cinematic feel has
 * drifted from `interactions.html` / `sheets.jsx`.
 */
class BeatSchedulerTest {

    @Test
    fun `Standard beats total 2800ms with haptic at 1200`() {
        val b = BeatScheduler.Standard
        assertEquals(2800L, b.totalMs)
        assertEquals(1200L, b.hapticAtMs)
        assertEquals(400L, b.flashEndMs)
        assertEquals(1200L, b.chargeEndMs)
        assertEquals(1800L, b.liftEndMs)
        assertEquals(1800L, b.titleStartMs)
        assertEquals(2800L, b.titleEndMs)
    }

    @Test
    fun `Extended A beats total 3400ms with extended title phase`() {
        val b = BeatScheduler.Extended
        assertEquals(3400L, b.totalMs)
        assertEquals(1800L, b.titleStartMs)
        assertEquals(3400L, b.titleEndMs)
        assertEquals(3, b.burstCount)
    }

    @Test
    fun `Monumental S beats total 5400ms with 5 bursts at 2700ms`() {
        val b = BeatScheduler.Monumental
        assertEquals(5400L, b.totalMs)
        assertEquals(700L, b.flashEndMs)
        assertEquals(2700L, b.chargeEndMs)
        assertEquals(3600L, b.liftEndMs)
        assertEquals(3600L, b.titleStartMs)
        assertEquals(5400L, b.titleEndMs)
        assertEquals(5, b.burstCount)
        assertEquals(2700L, b.burstStartMs)
        assertEquals(220L, b.burstIntervalMs)
    }

    @Test
    fun `burst counts per D C B match spec`() {
        assertEquals(0, BeatScheduler.standardBurstCount(Rank.D))
        assertEquals(1, BeatScheduler.standardBurstCount(Rank.C))
        assertEquals(2, BeatScheduler.standardBurstCount(Rank.B))
    }

    @Test
    fun `reduced-motion hold is at least 200ms`() {
        assertEquals(200L, BeatScheduler.ReducedHoldMs)
    }
}
