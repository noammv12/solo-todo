package com.solotodo.domain.rank

import com.solotodo.data.local.Rank

/** A single promotion emitted by [RankEvaluator]. */
data class RankTransition(
    val from: Rank,
    val to: Rank,
    val consecutiveDays: Int,
)
