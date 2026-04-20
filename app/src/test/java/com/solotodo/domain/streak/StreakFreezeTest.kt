package com.solotodo.domain.streak

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakFreezeTest {

    @Test fun `bump at 7 grants a freeze`() {
        assertEquals(1, StreakFreeze.afterStreakBump(newStreak = 7, current = 0))
    }

    @Test fun `bump at 14 grants another up to cap`() {
        assertEquals(2, StreakFreeze.afterStreakBump(14, 1))
    }

    @Test fun `cap enforced at MAX_STOCKPILE`() {
        assertEquals(StreakFreeze.MAX_STOCKPILE, StreakFreeze.afterStreakBump(28, StreakFreeze.MAX_STOCKPILE))
    }

    @Test fun `bumps on non-7 days do not grant freezes`() {
        assertEquals(0, StreakFreeze.afterStreakBump(6, 0))
        assertEquals(1, StreakFreeze.afterStreakBump(8, 1))
    }

    @Test fun `consume decrements and flags streak saved`() {
        val result = StreakFreeze.consume(current = 2, hardMode = false)
        assertEquals(1, result.newCount)
        assertTrue(result.streakSaved)
    }

    @Test fun `consume with zero freezes is a no-op`() {
        val result = StreakFreeze.consume(current = 0, hardMode = false)
        assertEquals(0, result.newCount)
        assertFalse(result.streakSaved)
    }

    @Test fun `hard mode ignores freezes`() {
        val result = StreakFreeze.consume(current = 3, hardMode = true)
        assertEquals(3, result.newCount)
        assertFalse(result.streakSaved)
    }
}
