package com.solotodo.data.repository

import androidx.room.withTransaction
import com.solotodo.data.local.OpKind
import com.solotodo.data.local.SoloTodoDb
import com.solotodo.data.local.dao.DungeonDao
import com.solotodo.data.local.entity.DungeonEntity
import com.solotodo.data.local.entity.DungeonFloorEntity
import com.solotodo.data.sync.OpLogWriter
import com.solotodo.domain.rank.RankEvaluator
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DungeonRepository @Inject constructor(
    private val db: SoloTodoDb,
    private val dao: DungeonDao,
    private val opLog: OpLogWriter,
    private val rankEvaluator: RankEvaluator,
    private val clock: Clock = Clock.System,
) {
    fun observeActive(): Flow<List<DungeonEntity>> = dao.observeActive()
    fun observeCleared(): Flow<List<DungeonEntity>> = dao.observeCleared()
    fun observeClearedCount(): Flow<Int> = dao.observeClearedCount()
    fun observeDungeon(id: String): Flow<DungeonEntity?> = dao.observeDungeon(id)
    fun observeFloors(dungeonId: String): Flow<List<DungeonFloorEntity>> = dao.observeFloorsForDungeon(dungeonId)

    suspend fun createDungeon(dungeon: DungeonEntity, floors: List<DungeonFloorEntity>): String {
        db.withTransaction {
            dao.upsertDungeon(dungeon)
            dao.upsertFloors(floors)
            opLog.record(ENTITY_DUNGEON, dungeon.id, OpKind.CREATE, null, "{}")
            floors.forEach { opLog.record(ENTITY_FLOOR, it.id, OpKind.CREATE, null, "{}") }
        }
        return dungeon.id
    }

    suspend fun markCleared(id: String) {
        val now = clock.now()
        db.withTransaction {
            dao.markCleared(id, now)
            opLog.record(ENTITY_DUNGEON, id, OpKind.PATCH, null, "{}")
            rankEvaluator.evaluateAndEmit()
        }
    }

    suspend fun abandon(id: String) {
        val now = clock.now()
        db.withTransaction {
            dao.markAbandoned(id, now)
            opLog.record(ENTITY_DUNGEON, id, OpKind.PATCH, null, "{}")
        }
    }

    companion object {
        const val ENTITY_DUNGEON = "dungeon"
        const val ENTITY_FLOOR = "dungeon_floor"
    }
}
