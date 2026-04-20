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
import com.solotodo.data.local.entity.StatEntity
import com.solotodo.data.local.entity.TaskEntity
import com.solotodo.data.local.entity.TaskListEntity
import com.solotodo.data.local.entity.UserSettingsEntity
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * Entity ↔ DTO mappers. Single site where `user_id` is stamped on outbound
 * rows. Inbound rows drop the `user_id` — it's not stored in Room (see plan
 * decision #1).
 *
 * JSON-blob Room columns (`repeat`, `subtasks`, `target`, `task_ids`, `summary`,
 * `haptics`, `notifications`) are stored locally as `String` but travel the
 * wire as `jsonb`. Parse/render with [SyncJson] via the helper extensions.
 */

// ---- Task ----

fun TaskEntity.toDto(userId: String): TaskDto = TaskDto(
    id = id,
    userId = userId,
    title = title,
    rawInput = rawInput,
    dueAt = dueAt,
    repeat = repeat?.toJsonElement(),
    stat = stat?.name,
    xp = xp,
    listId = listId,
    priority = priority,
    completedAt = completedAt,
    shadowedAt = shadowedAt,
    subtasks = subtasks?.toJsonElement(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    originDeviceId = originDeviceId,
    deletedAt = deletedAt,
)

fun TaskDto.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    rawInput = rawInput,
    dueAt = dueAt,
    repeat = repeat?.toRawJsonString(),
    stat = stat?.let { StatKind.valueOf(it) },
    xp = xp,
    listId = listId,
    priority = priority,
    completedAt = completedAt,
    shadowedAt = shadowedAt,
    subtasks = subtasks?.toRawJsonString(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    originDeviceId = originDeviceId,
    deletedAt = deletedAt,
)

// ---- DailyQuestItem ----

fun DailyQuestItemEntity.toDto(userId: String): DailyQuestItemDto = DailyQuestItemDto(
    id = id,
    userId = userId,
    title = title,
    target = target.toJsonElement(),
    stat = stat.name,
    orderIndex = orderIndex,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt,
    originDeviceId = originDeviceId,
)

fun DailyQuestItemDto.toEntity(): DailyQuestItemEntity = DailyQuestItemEntity(
    id = id,
    title = title,
    target = target.toRawJsonString(),
    stat = StatKind.valueOf(stat),
    orderIndex = orderIndex,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt,
    originDeviceId = originDeviceId,
)

// ---- DailyQuestLog ----

fun DailyQuestLogEntity.toDto(userId: String): DailyQuestLogDto = DailyQuestLogDto(
    id = id,
    userId = userId,
    questId = questId,
    day = day,
    progress = progress,
    completedAt = completedAt,
    createdAt = createdAt,
)

fun DailyQuestLogDto.toEntity(): DailyQuestLogEntity = DailyQuestLogEntity(
    id = id,
    questId = questId,
    day = day,
    progress = progress,
    completedAt = completedAt,
    createdAt = createdAt,
)

// ---- Dungeon ----

fun DungeonEntity.toDto(userId: String): DungeonDto = DungeonDto(
    id = id,
    userId = userId,
    title = title,
    description = description,
    rank = rank.name,
    dueAt = dueAt,
    clearedAt = clearedAt,
    abandonedAt = abandonedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    originDeviceId = originDeviceId,
)

fun DungeonDto.toEntity(): DungeonEntity = DungeonEntity(
    id = id,
    title = title,
    description = description,
    rank = Rank.valueOf(rank),
    dueAt = dueAt,
    clearedAt = clearedAt,
    abandonedAt = abandonedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    originDeviceId = originDeviceId,
)

// ---- DungeonFloor ----

fun DungeonFloorEntity.toDto(userId: String): DungeonFloorDto = DungeonFloorDto(
    id = id,
    userId = userId,
    dungeonId = dungeonId,
    title = title,
    orderIndex = orderIndex,
    taskIds = taskIds.toJsonElement(),
    state = state.name,
    clearedAt = clearedAt,
    updatedAt = updatedAt,
)

fun DungeonFloorDto.toEntity(): DungeonFloorEntity = DungeonFloorEntity(
    id = id,
    dungeonId = dungeonId,
    title = title,
    orderIndex = orderIndex,
    taskIds = taskIds.toRawJsonString(),
    state = FloorState.valueOf(state),
    clearedAt = clearedAt,
    updatedAt = updatedAt,
)

// ---- TaskList ----

fun TaskListEntity.toDto(userId: String): TaskListDto = TaskListDto(
    id = id,
    userId = userId,
    name = name,
    colorToken = colorToken,
    orderIndex = orderIndex,
)

fun TaskListDto.toEntity(): TaskListEntity = TaskListEntity(
    id = id,
    name = name,
    colorToken = colorToken,
    orderIndex = orderIndex,
)

// ---- Reflection ----

fun ReflectionEntity.toDto(userId: String): ReflectionDto = ReflectionDto(
    id = id,
    userId = userId,
    weekStart = weekStart,
    summary = summary.toJsonElement(),
    generatedAt = generatedAt,
)

fun ReflectionDto.toEntity(): ReflectionEntity = ReflectionEntity(
    id = id,
    weekStart = weekStart,
    summary = summary.toRawJsonString(),
    generatedAt = generatedAt,
)

// ---- Stat (pull-only) ----

fun StatDto.toEntity(): StatEntity = StatEntity(
    kind = StatKind.valueOf(kind),
    value = value,
    updatedAt = updatedAt,
)

// ---- UserSettings ----

fun UserSettingsEntity.toDto(userId: String): UserSettingsDto = UserSettingsDto(
    userId = userId,
    designation = designation,
    theme = theme.name,
    haptics = haptics.toJsonElement(),
    notifications = notifications.toJsonElement(),
    reduceMotion = reduceMotion,
    vacationUntil = vacationUntil,
    streakFreezes = streakFreezes,
    updatedAt = updatedAt,
)

fun UserSettingsDto.toEntity(): UserSettingsEntity = UserSettingsEntity(
    id = UserSettingsEntity.SINGLETON_ID,
    designation = designation,
    theme = ThemeAccent.valueOf(theme),
    haptics = haptics.toRawJsonString(),
    notifications = notifications.toRawJsonString(),
    reduceMotion = reduceMotion,
    vacationUntil = vacationUntil,
    streakFreezes = streakFreezes,
    updatedAt = updatedAt,
)

// ---- RankEvent ----

fun RankEventEntity.toDto(userId: String): RankEventDto = RankEventDto(
    id = id,
    userId = userId,
    fromRank = fromRank.name,
    toRank = toRank.name,
    consecutiveDays = consecutiveDays,
    occurredAt = occurredAt,
    cinematicPlayed = cinematicPlayed,
)

fun RankEventDto.toEntity(): RankEventEntity = RankEventEntity(
    id = id,
    fromRank = Rank.valueOf(fromRank),
    toRank = Rank.valueOf(toRank),
    consecutiveDays = consecutiveDays,
    occurredAt = occurredAt,
    cinematicPlayed = cinematicPlayed,
)

// ---- JSON helpers ----

/**
 * Parse a raw JSON string stored in a Room column into a [JsonElement] so it
 * travels the wire as `jsonb` (not a double-encoded string). Returns
 * [JsonNull] on parse failure so the row still round-trips.
 */
internal fun String.toJsonElement(): JsonElement =
    runCatching { SyncJson.parseToJsonElement(this) }.getOrElse {
        JsonPrimitive(this)
    }

/**
 * Inverse of [toJsonElement]: render a [JsonElement] back to its canonical
 * string form for Room. We always store the compact JSON string; the app
 * layer parses it on read.
 */
internal fun JsonElement.toRawJsonString(): String =
    SyncJson.encodeToString(JsonElement.serializer(), this)
