package com.solotodo.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ReflectionDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("week_start") @Serializable(with = LocalDateIso8601Serializer::class) val weekStart: LocalDate,
    @SerialName("summary") val summary: JsonElement,
    @SerialName("generated_at") @Serializable(with = InstantIso8601Serializer::class) val generatedAt: Instant,
)
