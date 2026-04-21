package com.solotodo.data.di

import android.content.Context
import androidx.room.Room
import com.solotodo.data.local.Migrations
import com.solotodo.data.local.SoloTodoDb
import com.solotodo.data.local.dao.DailyQuestDao
import com.solotodo.data.local.dao.DungeonDao
import com.solotodo.data.local.dao.OpLogDao
import com.solotodo.data.local.dao.RankEventDao
import com.solotodo.data.local.dao.ReflectionDao
import com.solotodo.data.local.dao.SettingsDao
import com.solotodo.data.local.dao.StatDao
import com.solotodo.data.local.dao.SyncStateDao
import com.solotodo.data.local.dao.TaskDao
import com.solotodo.data.local.dao.TaskListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SoloTodoDb {
        return Room.databaseBuilder(
            context,
            SoloTodoDb::class.java,
            SoloTodoDb.DATABASE_NAME,
        )
            .addMigrations(*Migrations.ALL)
            // Downgrades (rare — only when reverting to an older build) still
            // wipe cleanly; upgrades use the explicit migrations above.
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides fun provideTaskDao(db: SoloTodoDb): TaskDao = db.taskDao()
    @Provides fun provideDailyQuestDao(db: SoloTodoDb): DailyQuestDao = db.dailyQuestDao()
    @Provides fun provideRankEventDao(db: SoloTodoDb): RankEventDao = db.rankEventDao()
    @Provides fun provideDungeonDao(db: SoloTodoDb): DungeonDao = db.dungeonDao()
    @Provides fun provideTaskListDao(db: SoloTodoDb): TaskListDao = db.taskListDao()
    @Provides fun provideReflectionDao(db: SoloTodoDb): ReflectionDao = db.reflectionDao()
    @Provides fun provideStatDao(db: SoloTodoDb): StatDao = db.statDao()
    @Provides fun provideSettingsDao(db: SoloTodoDb): SettingsDao = db.settingsDao()
    @Provides fun provideOpLogDao(db: SoloTodoDb): OpLogDao = db.opLogDao()
    @Provides fun provideSyncStateDao(db: SoloTodoDb): SyncStateDao = db.syncStateDao()
}
