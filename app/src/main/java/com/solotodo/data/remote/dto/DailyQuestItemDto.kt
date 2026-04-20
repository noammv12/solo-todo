package com.solotodo.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DailyQuestItemDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("title") val title: String,
    @SerialName("target") val target: JsonElement,
    @SerialName("stat") val stat: String,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("active") val active: Boolean,
    @SerialName("created_at") @Serializable(with = InstantIso8601Serializer::class) val createdAt: Instant,
    @SerialName("updated_at") @Serializable(with = InstantIso8601Serializer::class) val updatedAt: Instant,
    @SerialName("origin_device_id") val originDeviceId: String,
)
