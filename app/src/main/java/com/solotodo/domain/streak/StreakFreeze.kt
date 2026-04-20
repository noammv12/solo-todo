package com.solotodo.domain.streak

/**
 * Streak-freeze logic — the highest-ROI retention mechanic per research
 * (Duolingo A/B tests show ~12% less churn the day after a miss, zero harm
 * to overall retention).
 *
 * Rules:
 *  - User earns **one** freeze each time the running streak crosses a
 *    multiple of 7 (7, 14, 21, …). Stockpile capped at **3**.
 *  - On DQ miss tick, if freezes > 0, one is consumed, streak stays, no
 *    penalty enters.
 *  - On DQ miss with freezes = 0, normal penalty flow runs.
 *  - User sees the consumption on next open: plain "STREAK FREEZE CONSUMED ·
 *    N REMAINING" panel — not shame copy.
 *  - Can be disabled via Settings → Daily Quest → "Play on hard mode".
 */
object StreakFreeze {
    const val MAX_STOCKPILE = 3
    const val EARN_EVERY_DAYS = 7

    /**
     * Returns the freeze count after a streak bump. If the new streak crossed
     * a multiple of [EARN_EVERY_DAYS], [current] increments by one (capped at
     * [MAX_STOCKPILE]). Otherwise unchanged.
     */
    fun afterStreakBump(newStreak: Int, current: Int): Int {
        if (newStreak > 0 && newStreak % EARN_EVERY_DAYS == 0 && current < MAX_STOCKPILE) {
            return current + 1
        }
        return current
    }

    /**
     * Consumes a freeze if available. Returns the new count and a flag
     * indicating whether the streak was saved. When [hardMode] is on,
     * freezes are ignored.
     */
    data class ConsumeResult(val newCount: Int, val streakSaved: Boolean)
    fun consume(current: Int, hardMode: Boolean): ConsumeResult {
        if (hardMode || current <= 0) return ConsumeResult(current, streakSaved = false)
        return ConsumeResult(current - 1, streakSaved = true)
    }
}
