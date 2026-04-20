package com.solotodo.domain.streak

import com.solotodo.data.local.Rank
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Pure logic for streak + rank progression.
 *
 * A "day" counts as COMPLETE when [completeDays] includes it. The streak is
 * the count of consecutive complete days leading up to and including today
 * (if today is complete) or yesterday (if today isn't complete yet).
 *
 * Rank thresholds match `data-state.html` §03 exactly:
 *  - D: 3 consecutive complete
 *  - C: 10 + 1 dungeon
 *  - B: 30 + 3
 *  - A: 75 + 8
 *  - S: 150 + 20 + 1 A→S gate
 *
 * Per plan override, **demotion is disabled**; [RankProgression.rankFor] only
 * moves forward.
 */
object RankProgression {
    private val thresholds = linkedMapOf(
        Rank.D to Threshold(days = 3, dungeons = 0, gates = 0),
        Rank.C to Threshold(days = 10, dungeons = 1, gates = 0),
        Rank.B to Threshold(days = 30, dungeons = 3, gates = 0),
        Rank.A to Threshold(days = 75, dungeons = 8, gates = 0),
        Rank.S to Threshold(days = 150, dungeons = 20, gates = 1),
    )

    data class Threshold(val days: Int, val dungeons: Int, val gates: Int)

    /** Highest rank reached given the supplied counters. */
    fun rankFor(longestStreak: Int, dungeonsCleared: Int, aToSGatesCleared: Int): Rank {
        var current: Rank = Rank.E
        for ((rank, t) in thresholds) {
            if (longestStreak >= t.days &&
                dungeonsCleared >= t.dungeons &&
                aToSGatesCleared >= t.gates
            ) {
                current = rank
            } else {
                break
            }
        }
        return current
    }

    /**
     * Distance (in days) from today's longest streak to the next-rank day
     * requirement. Returns `null` at S.
     */
    fun daysToNextRank(current: Rank, longestStreak: Int): Int? {
        val next = current.next() ?: return null
        val t = thresholds[next] ?: return null
        return (t.days - longestStreak).coerceAtLeast(0)
    }

    /** Threshold lookup for UI labels. */
    fun thresholdFor(rank: Rank): Threshold? = thresholds[rank]
}

object StreakCalculator {

    /**
     * Consecutive-complete-days ending at [reference] (today). If [reference]
     * is itself complete, counts it; otherwise counts backwards from yesterday.
     *
     * [completeDays] should be sorted or unordered — we convert to a Set.
     */
    fun currentStreak(completeDays: Collection<LocalDate>, reference: LocalDate): Int {
        val set = completeDays.toHashSet()
        var cursor = if (set.contains(reference)) reference else reference.minus(1, DateTimeUnit.DAY)
        var count = 0
        while (set.contains(cursor)) {
            count += 1
            cursor = cursor.minus(1, DateTimeUnit.DAY)
        }
        return count
    }

    /**
     * Highest streak ever achieved in the supplied history. Used for rank
     * gating — rank never drops even if the current streak does.
     */
    fun longestStreak(completeDays: Collection<LocalDate>): Int {
        if (completeDays.isEmpty()) return 0
        val sorted = completeDays.toSortedSet().toList()
        var longest = 1
        var run = 1
        for (i in 1 until sorted.size) {
            val prev = sorted[i - 1]
            val curr = sorted[i]
            if (curr == prev.plus(1, DateTimeUnit.DAY)) {
                run += 1
                if (run > longest) longest = run
            } else {
                run = 1
            }
        }
        return longest
    }
}
