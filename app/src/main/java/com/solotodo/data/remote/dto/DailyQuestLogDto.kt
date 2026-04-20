package com.solotodo.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyQuestLogDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("quest_id") val questId: String,
    @SerialName("day") @Serializable(with = LocalDateIso8601Serializer::class) val day: LocalDate,
    @SerialName("progress") val progress: Int,
    @SerialName("completed_at") @Serializable(with = InstantIso8601Serializer::class) val completedAt: Instant? = null,
    @SerialName("created_at") @Serializable(with = InstantIso8601Serializer::class) val createdAt: Instant,
)
