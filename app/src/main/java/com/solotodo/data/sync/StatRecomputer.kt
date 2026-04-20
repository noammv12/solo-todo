package com.solotodo.data.sync

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Placeholder for Phase 5: recompute the local `stat` cache from canonical
 * Task + DailyQuestLog rows after each pull cycle.
 *
 * For Phase 4.5 we rely on the server-provided `stat` rows (pulled in
 * [PullTranslator.pullStats]) as the cache. The Phase 5 rank-progression
 * work will replace this with a real GROUP-BY aggregation over completed
 * tasks and DQ log progress.
 *
 * Kept as an injectable seam so callers can depend on it today and get
 * real behaviour later without changing wiring.
 */
@Singleton
class StatRecomputer @Inject constructor() {
    @Suppress("RedundantSuspendModifier")
    suspend fun recompute() {
        // Intentionally empty — see class docs.
    }
}
