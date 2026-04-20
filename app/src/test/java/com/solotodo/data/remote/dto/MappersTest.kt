package com.solotodo.data.remote.dto

import com.solotodo.data.local.FloorState
import com.solotodo.data.local.Rank
import com.solotodo.data.local.StatKind
import com.solotodo.data.local.ThemeAccent
import com.solotodo.data.local.entity.DailyQuestItemEntity
import com.solotodo.data.local.entity.DailyQuestLogEntity
import com.solotodo.data.local.entity.DungeonEntity
import com.solotodo.data.local.entity.DungeonFloorEntity
import com.solotodo.data.local.entity.RankEventEntity
import com.solotodo.data.local.entity.ReflectionEntity
import com.solotodo.data.local.entity.TaskEntity
import com.solotodo.data.local.entity.TaskListEntity
import com.solotodo.data.local.entity.UserSettingsEntity
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MappersTest {

    private val now = Instant.parse("2026-04-20T12:00:00Z")
    private val userId = "fake-user-uuid"
    private val deviceId = "fake-device-uuid"

    @Test fun `TaskEntity round-trips with all fields`() {
        val entity = TaskEntity(
            id = "t1",
            title = "Hunt goblins",
            rawInput = "hunt goblins !!",
            dueAt = now,
            repeat = """{"rule":"weekly","days":[1,3,5]}""",
            stat = StatKind.STR,
            xp = 25,
            listId = "list1",
            priority = 2,
            completedAt = null,
            shadowedAt = null,
            subtasks = """[{"title":"sharpen sword","done":false}]""",
            createdAt = now,
            updatedAt = now,
            originDeviceId = deviceId,
            deletedAt = null,
        )
        val dto = entity.toDto(userId)
        assertEquals(userId, dto.userId)
        val roundTripped = dto.toEntity()
        assertEquals(entity, roundTripped)
    }

    @Test fun `TaskEntity round-trips with null optional fields`() {
        val entity = TaskEntity(
            id = "t2",
            title = "Minimal task",
            rawInput = null,
            dueAt = null,
            repeat = null,
            stat = null,
            xp = 0,
            listId = null,
            priority = 0,
            completedAt = null,
            shadowedAt = null,
            subtasks = null,
            createdAt = now,
            updatedAt = now,
            originDeviceId = deviceId,
            deletedAt = null,
        )
        val dto = entity.toDto(userId)
        assertEquals(entity, dto.toEntity())
    }

    @Test fun `TaskDto serializes with snake_case keys`() {
        val entity = sampleTask()
        val dto = entity.toDto(userId)
        val json = Json.encodeToString(TaskDto.serializer(), dto)
        assert(json.contains("\"user_id\"")) { "expected user_id in json: $json" }
        assert(json.contains("\"due_at\"")) { "expected due_at in json: $json" }
        assert(json.contains("\"origin_device_id\"")) { "expected origin_device_id in json" }
    }

    @Test fun `DailyQuestItem round-trips`() {
        val entity = DailyQuestItemEntity(
            id = "dq1",
            title = "Push-ups",
            target = """{"kind":"count","value":20,"unit":"reps"}""",
            stat = StatKind.STR,
            orderIndex = 0,
            active = true,
            createdAt = now,
            updatedAt = now,
            originDeviceId = deviceId,
        )
        assertEquals(entity, entity.toDto(userId).toEntity())
    }

    @Test fun `DailyQuestLog round-trips`() {
        val entity = DailyQuestLogEntity(
            id = "log1",
            questId = "dq1",
            day = LocalDate(2026, 4, 20),
            progress = 10,
            completedAt = now,
            createdAt = now,
        )
        assertEquals(entity, entity.toDto(userId).toEntity())
    }

    @Test fun `Dungeon round-trips`() {
        val entity = DungeonEntity(
            id = "d1",
            title = "Tower of Focus",
            description = "Multi-floor project",
            rank = Rank.B,
            dueAt = now,
            clearedAt = null,
            abandonedAt = null,
            createdAt = now,
            updatedAt = now,
            originDeviceId = deviceId,
        )
        assertEquals(entity, entity.toDto(userId).toEntity())
    }

    @Test fun `DungeonFloor round-trips`() {
        val entity = DungeonFloorEntity(
            id = "f1",
            dungeonId = "d1",
            title = "Floor 1",
            orderIndex = 0,
            taskIds = """["t1","t2"]""",
            state = FloorState.OPEN,
            clearedAt = null,
            updatedAt = now,
        )
        assertEquals(entity, entity.toDto(userId).toEntity())
    }

    @Test fun `TaskList round-trips`() {
        val entity = TaskListEntity(
            id = "l1",
            name = "Personal",
            colorToken = "cyan",
            orderIndex = 0,
        )
        assertEquals(entity, entity.toDto(userId).toEntity())
    }

    @Test fun `Reflection round-trips`() {
        val entity = ReflectionEntity(
            id = "r1",
            weekStart = LocalDate(2026, 4, 13),
            summary = """{"tasks_done":12,"streak_peak":5}""",
            generatedAt = now,
        )
        assertEquals(entity, entity.toDto(userId).toEntity())
    }

    @Test fun `UserSettings round-trips and forces singleton id`() {
        val entity = UserSettingsEntity(
            id = UserSettingsEntity.SINGLETON_ID,
            designation = "HUNTER",
            theme = ThemeAccent.CYAN,
            haptics = """{"master":true}""",
            notifications = """{"morning":false}""",
            reduceMotion = false,
            vacationUntil = null,
            streakFreezes = 2,
            updatedAt = now,
        )
        val round = entity.toDto(userId).toEntity()
        assertEquals(UserSettingsEntity.SINGLETON_ID, round.id)
        assertEquals(entity, round)
    }

    @Test fun `RankEvent round-trips`() {
        val entity = RankEventEntity(
            id = "re1",
            fromRank = Rank.E,
            toRank = Rank.D,
            consecutiveDays = 7,
            occurredAt = now,
            cinematicPlayed = false,
        )
        assertEquals(entity, entity.toDto(userId).toEntity())
    }

    @Test fun `user_id is stamped on every outbound mapper`() {
        assertEquals(userId, sampleTask().toDto(userId).userId)
        assertEquals(
            userId,
            DailyQuestItemEntity(
                "x", "t", "{}", StatKind.STR, 0, true, now, now, deviceId,
            ).toDto(userId).userId,
        )
        assertEquals(
            userId,
            DailyQuestLogEntity("x", "q", LocalDate(2026, 1, 1), 0, null, now).toDto(userId).userId,
        )
        assertEquals(
            userId,
            DungeonEntity("x", "t", null, Rank.E, null, null, null, now, now, deviceId)
                .toDto(userId).userId,
        )
        assertEquals(
            userId,
            DungeonFloorEntity("x", "d", "t", 0, "[]", FloorState.OPEN, null, now)
                .toDto(userId).userId,
        )
        assertEquals(userId, TaskListEntity("x", "n", null, 0).toDto(userId).userId)
        assertEquals(
            userId,
            ReflectionEntity("x", LocalDate(2026, 1, 1), "{}", now).toDto(userId).userId,
        )
        assertEquals(
            userId,
            UserSettingsEntity(
                UserSettingsEntity.SINGLETON_ID, "H", ThemeAccent.CYAN, "{}", "{}",
                false, null, 0, now,
            ).toDto(userId).userId,
        )
        assertEquals(
            userId,
            RankEventEntity("x", Rank.E, Rank.D, 7, now, false).toDto(userId).userId,
        )
    }

    @Test fun `JsonElement helpers preserve JSON blobs`() {
        val raw = """{"kind":"count","value":20,"unit":"reps"}"""
        val element = raw.toJsonElement()
        assertNotNull(element)
        val reEncoded = element.toRawJsonString()
        // kotlinx.serialization may re-key ordering; round-trip stability checks
        // that the re-encoded string parses to the same element.
        val reParsed = reEncoded.toJsonElement()
        assertEquals(element, reParsed)
    }

    private fun sampleTask() = TaskEntity(
        id = "t-sample",
        title = "Sample",
        rawInput = null,
        dueAt = now,
        repeat = null,
        stat = StatKind.STR,
        xp = 10,
        listId = null,
        priority = 0,
        completedAt = null,
        shadowedAt = null,
        subtasks = null,
        createdAt = now,
        updatedAt = now,
        originDeviceId = deviceId,
        deletedAt = null,
    )
}
