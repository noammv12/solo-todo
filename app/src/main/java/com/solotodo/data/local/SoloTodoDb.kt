package com.solotodo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.solotodo.data.local.dao.DailyQuestDao
import com.solotodo.data.local.dao.DungeonDao
import com.solotodo.data.local.dao.OpLogDao
import com.solotodo.data.local.dao.RankEventDao
import com.solotodo.data.local.dao.ReflectionDao
import com.solotodo.data.local.dao.SettingsDao
import com.solotodo.data.local.dao.StatDao
import com.solotodo.data.local.dao.TaskDao
import com.solotodo.data.local.dao.TaskListDao
import com.solotodo.data.local.entity.DailyQuestItemEntity
import com.solotodo.data.local.entity.DailyQuestLogEntity
import com.solotodo.data.local.entity.DungeonEntity
import com.solotodo.data.local.entity.DungeonFloorEntity
import com.solotodo.data.local.entity.OpLogEntity
import com.solotodo.data.local.entity.RankEventEntity
import com.solotodo.data.local.entity.ReflectionEntity
import com.solotodo.data.local.entity.StatEntity
import com.solotodo.data.local.entity.TaskEntity
import com.solotodo.data.local.entity.TaskListEntity
import com.solotodo.data.local.entity.UserSettingsEntity

/**
 * Room database for all local persistence.
 *
 * Schema version **1**. Migrations are explicitly disabled for now (no production
 * users). The migration plumbing lives in `Migrations.kt` and schema history is
 * exported to `app/schemas/` so the V1 → V2 transition is safe when it comes.
 *
 * Pass `@AutoMigration` or explicit `Migration` implementations here when we
 * change schema in Phase 3+.
 */
@Database(
    entities = [
        TaskEntity::class,
        DailyQuestItemEntity::class,
        DailyQuestLogEntity::class,
        RankEventEntity::class,
        DungeonEntity::class,
        DungeonFloorEntity::class,
        TaskListEntity::class,
        ReflectionEntity::class,
        StatEntity::class,
        UserSettingsEntity::class,
        OpLogEntity::class,
    ],
    version = SoloTodoDb.SCHEMA_VERSION,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SoloTodoDb : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun dailyQuestDao(): DailyQuestDao
    abstract fun rankEventDao(): RankEventDao
    abstract fun dungeonDao(): DungeonDao
    abstract fun taskListDao(): TaskListDao
    abstract fun reflectionDao(): ReflectionDao
    abstract fun statDao(): StatDao
    abstract fun settingsDao(): SettingsDao
    abstract fun opLogDao(): OpLogDao

    companion object {
        const val SCHEMA_VERSION = 1
        const val DATABASE_NAME = "solotodo.db"
    }
}
