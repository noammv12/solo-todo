package com.solotodo.data.repository

import androidx.room.withTransaction
import com.solotodo.data.local.OpKind
import com.solotodo.data.local.Rank
import com.solotodo.data.local.SoloTodoDb
import com.solotodo.data.local.dao.RankEventDao
import com.solotodo.data.local.entity.RankEventEntity
import com.solotodo.data.sync.OpLogWriter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper over [RankEventDao] that pairs rank-event writes with op-log rows.
 *
 * `rank_event` is append-only. Server-side dedup (5-min bucket on
 * `(user, from_rank, to_rank)`) protects against multi-device duplicates; the
 * client inserts optimistically, and the push path already swallows 409s.
 */
@Singleton
class RankEventRepository @Inject constructor(
    private val db: SoloTodoDb,
    private val dao: RankEventDao,
    private val opLog: OpLogWriter,
    private val clock: Clock = Clock.System,
) {
    fun observePendingCinematics(): Flow<List<RankEventEntity>> = dao.observePending()

    fun observeCurrentRank(): Flow<Rank> =
        dao.observeAll().map { events -> events.firstOrNull()?.toRank ?: Rank.E }

    suspend fun currentRank(): Rank =
        dao.currentRank()?.let { Rank.valueOf(it) } ?: Rank.E

    /** Called by the cinematic host after the user sees (or skips) the cinematic. */
    suspend fun markPlayed(id: String) {
        db.withTransaction {
            dao.markPlayed(id)
            opLog.record(ENTITY, id, OpKind.PATCH, null, "{}")
        }
    }

    /**
     * Inserts a rank-event row + op-log row. **Caller MUST be inside an active
     * `db.withTransaction { }` block** so reads of [currentRank] and the insert
     * stay atomic with the triggering write. Returns the new entity.
     */
    suspend fun insertInCurrentTxn(
        from: Rank,
        to: Rank,
        consecutiveDays: Int,
    ): RankEventEntity {
        val entity = RankEventEntity(
            id = UUID.randomUUID().toString(),
            fromRank = from,
            toRank = to,
            consecutiveDays = consecutiveDays,
            occurredAt = clock.now(),
            cinematicPlayed = false,
        )
        dao.insert(entity)
        opLog.record(ENTITY, entity.id, OpKind.CREATE, null, "{}")
        return entity
    }

    companion object {
        const val ENTITY = "rank_event"
    }
}
