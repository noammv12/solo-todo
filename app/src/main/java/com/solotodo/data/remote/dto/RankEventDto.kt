package com.solotodo.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RankEventDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("from_rank") val fromRank: String,
    @SerialName("to_rank") val toRank: String,
    @SerialName("consecutive_days") val consecutiveDays: Int,
    @SerialName("occurred_at") @Serializable(with = InstantIso8601Serializer::class) val occurredAt: Instant,
    @SerialName("cinematic_played") val cinematicPlayed: Boolean,
)
