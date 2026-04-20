package com.solotodo.domain.rank

import com.solotodo.data.local.Rank
import com.solotodo.data.repository.RankEventRepository
import com.solotodo.domain.streak.RankProgression
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Decides whether a promotion should happen and, if so, emits the rank event.
 *
 * **MUST be called from inside an active `db.withTransaction { }` block.** The
 * read of `currentRank` and the insert must be atomic: SQLite's write lock
 * serialises concurrent txns so two parallel `reportProgress` callers cannot
 * both observe "prev = D" and both insert a D→C event.
 *
 * Demotion is disabled per plan override — the evaluator only moves forward.
 *
 * A→S gate: the real gate entity lands in Phase 7 (Dungeons). Until then, the
 * evaluator treats the gate as implicitly cleared once the user is at A and
 * has met the 150-day / 20-dungeon thresholds — matches the retention goal of
 * letting S rank be reachable through normal play.
 */
@Singleton
class RankEvaluator @Inject constructor(
    private val rankEventRepository: RankEventRepository,
    private val snapshotReader: StreakSnapshotReader,
) {
    /**
     * Reads the current snapshot + prev rank and inserts a [RankEventEntity]
     * when a promotion is warranted. Returns the emitted transition or `null`
     * if no promotion.
     */
    suspend fun evaluateAndEmit(): RankTransition? {
        val prev = rankEventRepository.currentRank()
        val snap = snapshotReader.snapshot()
        val implicitGate = if (
            prev == Rank.A &&
            snap.longestStreak >= S_RANK_DAYS &&
            snap.dungeonsCleared >= S_RANK_DUNGEONS
        ) 1 else 0

        val target = RankProgression.rankFor(
            longestStreak = snap.longestStreak,
            dungeonsCleared = snap.dungeonsCleared,
            aToSGatesCleared = implicitGate,
        )

        if (target.ordinal <= prev.ordinal) return null

        // Promote one step at a time. If the user somehow jumps two ranks in one
        // evaluation (synthetic seed, catch-up after a sync gap), we still emit
        // a single event — ordinal strictly increases, and the next evaluator
        // call (fired by any future completion) will pick up the remainder.
        val next = prev.next() ?: return null
        val to = if (target.ordinal > next.ordinal) next else target

        rankEventRepository.insertInCurrentTxn(
            from = prev,
            to = to,
            consecutiveDays = snap.currentStreak,
        )
        return RankTransition(from = prev, to = to, consecutiveDays = snap.currentStreak)
    }

    private companion object {
        const val S_RANK_DAYS = 150
        const val S_RANK_DUNGEONS = 20
    }
}
