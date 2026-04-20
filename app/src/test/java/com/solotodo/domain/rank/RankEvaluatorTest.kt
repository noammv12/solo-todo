package com.solotodo.domain.rank

import com.solotodo.data.local.Rank
import com.solotodo.data.local.entity.RankEventEntity
import com.solotodo.data.repository.RankEventRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RankEvaluatorTest {

    private fun evaluator(
        currentRank: Rank = Rank.E,
        snapshot: StreakSnapshotReader.Snapshot,
        onInsert: ((Rank, Rank, Int) -> Unit)? = null,
    ): Pair<RankEvaluator, RankEventRepository> {
        val repo = mockk<RankEventRepository>()
        coEvery { repo.currentRank() } returns currentRank
        coEvery { repo.insertInCurrentTxn(any(), any(), any()) } answers {
            val from = firstArg<Rank>()
            val to = secondArg<Rank>()
            val days = thirdArg<Int>()
            onInsert?.invoke(from, to, days)
            RankEventEntity(
                id = "id-$from-$to",
                fromRank = from,
                toRank = to,
                consecutiveDays = days,
                occurredAt = Clock.System.now(),
                cinematicPlayed = false,
            )
        }
        val snapshotReader = mockk<StreakSnapshotReader>()
        coEvery { snapshotReader.snapshot() } returns snapshot
        return RankEvaluator(repo, snapshotReader) to repo
    }

    @Test
    fun `E to D emits D transition on 3-day streak`() = runTest {
        val (evaluator, _) = evaluator(
            currentRank = Rank.E,
            snapshot = StreakSnapshotReader.Snapshot(currentStreak = 3, longestStreak = 3, dungeonsCleared = 0),
        )
        val transition = evaluator.evaluateAndEmit()
        assertEquals(Rank.E, transition?.from)
        assertEquals(Rank.D, transition?.to)
        assertEquals(3, transition?.consecutiveDays)
    }

    @Test
    fun `D to C requires both 10 days and 1 dungeon`() = runTest {
        val (evaluatorNoDungeon, _) = evaluator(
            currentRank = Rank.D,
            snapshot = StreakSnapshotReader.Snapshot(currentStreak = 10, longestStreak = 10, dungeonsCleared = 0),
        )
        assertNull(evaluatorNoDungeon.evaluateAndEmit())

        val (evaluatorWithDungeon, _) = evaluator(
            currentRank = Rank.D,
            snapshot = StreakSnapshotReader.Snapshot(currentStreak = 10, longestStreak = 10, dungeonsCleared = 1),
        )
        assertEquals(Rank.C, evaluatorWithDungeon.evaluateAndEmit()?.to)
    }

    @Test
    fun `A to S fires implicit gate at 150 days and 20 dungeons`() = runTest {
        val (evaluator, repo) = evaluator(
            currentRank = Rank.A,
            snapshot = StreakSnapshotReader.Snapshot(currentStreak = 200, longestStreak = 200, dungeonsCleared = 20),
        )
        val transition = evaluator.evaluateAndEmit()
        assertEquals(Rank.S, transition?.to)
        coVerify(exactly = 1) { repo.insertInCurrentTxn(Rank.A, Rank.S, 200) }
    }

    @Test
    fun `no-op when already at target rank`() = runTest {
        val (evaluator, repo) = evaluator(
            currentRank = Rank.C,
            snapshot = StreakSnapshotReader.Snapshot(currentStreak = 10, longestStreak = 15, dungeonsCleared = 1),
        )
        assertNull(evaluator.evaluateAndEmit())
        coVerify(exactly = 0) { repo.insertInCurrentTxn(any(), any(), any()) }
    }

    @Test
    fun `promotes one step at a time even if thresholds skip multiple ranks`() = runTest {
        // Synthetic: user at E somehow has 30 days + 3 dungeons. Should emit D, not B.
        val (evaluator, repo) = evaluator(
            currentRank = Rank.E,
            snapshot = StreakSnapshotReader.Snapshot(currentStreak = 30, longestStreak = 30, dungeonsCleared = 3),
        )
        val transition = evaluator.evaluateAndEmit()
        assertEquals(Rank.D, transition?.to)
        coVerify(exactly = 1) { repo.insertInCurrentTxn(Rank.E, Rank.D, 30) }
    }

    @Test
    fun `never demotes even if longest streak falls short`() = runTest {
        // prev rank is B but current streak doesn't meet B threshold — stays B, no event.
        val (evaluator, repo) = evaluator(
            currentRank = Rank.B,
            snapshot = StreakSnapshotReader.Snapshot(currentStreak = 0, longestStreak = 5, dungeonsCleared = 3),
        )
        assertNull(evaluator.evaluateAndEmit())
        coVerify(exactly = 0) { repo.insertInCurrentTxn(any(), any(), any()) }
    }
}
