package com.solotodo.data.dev

import com.solotodo.data.DeviceId
import com.solotodo.data.local.DailyQuestTargetKind
import com.solotodo.data.local.FloorState
import com.solotodo.data.local.Rank
import com.solotodo.data.local.SoloTodoDb
import com.solotodo.data.local.StatKind
import com.solotodo.data.local.entity.DailyQuestItemEntity
import com.solotodo.data.local.entity.DungeonEntity
import com.solotodo.data.local.entity.DungeonFloorEntity
import com.solotodo.data.repository.DailyQuestRepository
import com.solotodo.data.repository.DungeonRepository
import com.solotodo.data.repository.SettingsRepository
import com.solotodo.data.repository.TaskRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debug-only helper that seeds realistic sample data or wipes everything.
 * Not referenced from release builds (the UI entry points are gated on
 * [com.solotodo.BuildConfig.DEBUG]).
 */
@Singleton
class DevSeeder @Inject constructor(
    private val db: SoloTodoDb,
    private val tasks: TaskRepository,
    private val dailyQuests: DailyQuestRepository,
    private val dungeons: DungeonRepository,
    private val settings: SettingsRepository,
    private val deviceId: DeviceId,
    private val clock: Clock = Clock.System,
) {

    suspend fun wipe() {
        db.clearAllTables()
    }

    suspend fun seed() {
        settings.initializeIfMissing()

        // 3 Daily Quest items — simple count/duration/boolean mix.
        val now = clock.now()
        val dq1Id = "dq-workout"
        val dq2Id = "dq-reading"
        val dq3Id = "dq-hydrate"

        dailyQuests.upsertItems(
            listOf(
                DailyQuestItemEntity(
                    id = dq1Id,
                    title = "WORKOUT",
                    target = """{"kind":"DURATION","value":20,"unit":"min"}""",
                    stat = StatKind.STR,
                    orderIndex = 0,
                    active = true,
                    createdAt = now,
                    updatedAt = now,
                    originDeviceId = deviceId.value,
                ),
                DailyQuestItemEntity(
                    id = dq2Id,
                    title = "READ",
                    target = """{"kind":"COUNT","value":20,"unit":"min"}""",
                    stat = StatKind.INT,
                    orderIndex = 1,
                    active = true,
                    createdAt = now,
                    updatedAt = now,
                    originDeviceId = deviceId.value,
                ),
                DailyQuestItemEntity(
                    id = dq3Id,
                    title = "HYDRATE",
                    target = """{"kind":"BOOLEAN","value":1}""",
                    stat = StatKind.VIT,
                    orderIndex = 2,
                    active = true,
                    createdAt = now,
                    updatedAt = now,
                    originDeviceId = deviceId.value,
                ),
            ),
        )

        // 7 days of history — last 7 days marked complete, today in progress.
        val tz = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(tz).date
        for (daysAgo in 1..7) {
            val day: LocalDate = today.minus(daysAgo, DateTimeUnit.DAY)
            listOf(dq1Id to 20, dq2Id to 20, dq3Id to 1).forEach { (qid, target) ->
                dailyQuests.reportProgress(questId = qid, day = day, progress = target, target = target)
            }
        }

        // 10 sample tasks — mix of open, completed, with stat + priority variance.
        tasks.create(title = "Plan the week", stat = StatKind.INT, priority = 1, xp = 30)
        tasks.create(title = "Call mom", stat = StatKind.SEN, xp = 20)
        tasks.create(title = "Groceries", stat = StatKind.VIT, xp = 15)
        tasks.create(title = "Finish report draft", stat = StatKind.INT, priority = 2, xp = 50)
        tasks.create(title = "Gym — legs", stat = StatKind.STR, xp = 40)
        val t6 = tasks.create(title = "Reply to emails", xp = 10)
        tasks.complete(t6)
        val t7 = tasks.create(title = "Journal 10 min", stat = StatKind.SEN, xp = 15)
        tasks.complete(t7)
        tasks.create(title = "Read one chapter", stat = StatKind.INT, xp = 20)
        tasks.create(title = "Meditate", stat = StatKind.SEN, xp = 15)
        tasks.create(title = "Take out the trash", xp = 5)

        // Sample Dungeon "Launch v1" with 3 floors.
        val dungeonId = "dg-launch-v1"
        val floor1 = "dg-launch-v1-floor1"
        val floor2 = "dg-launch-v1-floor2"
        val floor3 = "dg-launch-v1-floor3"
        dungeons.createDungeon(
            dungeon = DungeonEntity(
                id = dungeonId,
                title = "Launch v1",
                description = "First production release",
                rank = Rank.C,
                dueAt = null,
                clearedAt = null,
                abandonedAt = null,
                createdAt = now,
                updatedAt = now,
                originDeviceId = deviceId.value,
            ),
            floors = listOf(
                DungeonFloorEntity(
                    id = floor1,
                    dungeonId = dungeonId,
                    title = "Foundations",
                    orderIndex = 0,
                    taskIds = "[]",
                    state = FloorState.OPEN,
                    clearedAt = null,
                    updatedAt = now,
                ),
                DungeonFloorEntity(
                    id = floor2,
                    dungeonId = dungeonId,
                    title = "Core loop",
                    orderIndex = 1,
                    taskIds = "[]",
                    state = FloorState.LOCKED,
                    clearedAt = null,
                    updatedAt = now,
                ),
                DungeonFloorEntity(
                    id = floor3,
                    dungeonId = dungeonId,
                    title = "Polish & ship",
                    orderIndex = 2,
                    taskIds = "[]",
                    state = FloorState.LOCKED,
                    clearedAt = null,
                    updatedAt = now,
                ),
            ),
        )
    }

}
