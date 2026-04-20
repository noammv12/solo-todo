package com.solotodo.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Wire format for the `task` row. Column names snake_case via @SerialName. */
@Serializable
data class TaskDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("title") val title: String,
    @SerialName("raw_input") val rawInput: String? = null,
    @SerialName("due_at") @Serializable(with = InstantIso8601Serializer::class) val dueAt: Instant? = null,
    @SerialName("repeat") val repeat: JsonElement? = null,
    @SerialName("stat") val stat: String? = null,
    @SerialName("xp") val xp: Int = 0,
    @SerialName("list_id") val listId: String? = null,
    @SerialName("priority") val priority: Int = 0,
    @SerialName("completed_at") @Serializable(with = InstantIso8601Serializer::class) val completedAt: Instant? = null,
    @SerialName("shadowed_at") @Serializable(with = InstantIso8601Serializer::class) val shadowedAt: Instant? = null,
    @SerialName("subtasks") val subtasks: JsonElement? = null,
    @SerialName("created_at") @Serializable(with = InstantIso8601Serializer::class) val createdAt: Instant,
    @SerialName("updated_at") @Serializable(with = InstantIso8601Serializer::class) val updatedAt: Instant,
    @SerialName("origin_device_id") val originDeviceId: String,
    @SerialName("deleted_at") @Serializable(with = InstantIso8601Serializer::class) val deletedAt: Instant? = null,
)
