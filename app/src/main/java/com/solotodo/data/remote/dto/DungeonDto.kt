package com.solotodo.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DungeonDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("rank") val rank: String,
    @SerialName("due_at") @Serializable(with = InstantIso8601Serializer::class) val dueAt: Instant? = null,
    @SerialName("cleared_at") @Serializable(with = InstantIso8601Serializer::class) val clearedAt: Instant? = null,
    @SerialName("abandoned_at") @Serializable(with = InstantIso8601Serializer::class) val abandonedAt: Instant? = null,
    @SerialName("created_at") @Serializable(with = InstantIso8601Serializer::class) val createdAt: Instant,
    @SerialName("updated_at") @Serializable(with = InstantIso8601Serializer::class) val updatedAt: Instant,
    @SerialName("origin_device_id") val originDeviceId: String,
)
