package com.solotodo.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DungeonFloorDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("dungeon_id") val dungeonId: String,
    @SerialName("title") val title: String,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("task_ids") val taskIds: JsonElement,
    @SerialName("state") val state: String,
    @SerialName("cleared_at") @Serializable(with = InstantIso8601Serializer::class) val clearedAt: Instant? = null,
    @SerialName("updated_at") @Serializable(with = InstantIso8601Serializer::class) val updatedAt: Instant,
)
