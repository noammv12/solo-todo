package com.solotodo.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Pull-only DTO. Composite server PK is `(user_id, kind)`. */
@Serializable
data class StatDto(
    @SerialName("user_id") val userId: String,
    @SerialName("kind") val kind: String,
    @SerialName("value") val value: Int,
    @SerialName("updated_at") @Serializable(with = InstantIso8601Serializer::class) val updatedAt: Instant,
)
