package com.solotodo.domain.streak

import com.solotodo.data.local.Rank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RankProgressionTest {

    @Test fun `default is E`() {
        assertEquals(Rank.E, RankProgression.rankFor(longestStreak = 0, dungeonsCleared = 0, aToSGatesCleared = 0))
    }

    @Test fun `3 consecutive days reaches D`() {
        assertEquals(Rank.D, RankProgression.rankFor(3, 0, 0))
    }

    @Test fun `C requires 10 days AND 1 dungeon`() {
        assertEquals(Rank.D, RankProgression.rankFor(10, 0, 0)) // missing dungeon
        assertEquals(Rank.C, RankProgression.rankFor(10, 1, 0))
    }

    @Test fun `B requires 30 days and 3 dungeons`() {
        assertEquals(Rank.C, RankProgression.rankFor(30, 1, 0))
        assertEquals(Rank.B, RankProgression.rankFor(30, 3, 0))
    }

    @Test fun `S requires 150 days 20 dungeons and 1 AS gate`() {
        assertEquals(Rank.A, RankProgression.rankFor(150, 20, 0))
        assertEquals(Rank.S, RankProgression.rankFor(150, 20, 1))
    }

    @Test fun `daysToNextRank counts down to the next threshold`() {
        assertEquals(3, RankProgression.daysToNextRank(Rank.E, 0))
        assertEquals(0, RankProgression.daysToNextRank(Rank.E, 3))
        assertEquals(7, RankProgression.daysToNextRank(Rank.D, 3))
        assertNull(RankProgression.daysToNextRank(Rank.S, 150))
    }
}
