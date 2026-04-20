package com.solotodo.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Server PK is `user_id`. Room's `id` column (always `"me"`) is not sent over
 * the wire — the mapper reconstitutes it when pulling.
 */
@Serializable
data class UserSettingsDto(
    @SerialName("user_id") val userId: String,
    @SerialName("designation") val designation: String,
    @SerialName("theme") val theme: String,
    @SerialName("haptics") val haptics: JsonElement,
    @SerialName("notifications") val notifications: JsonElement,
    @SerialName("reduce_motion") val reduceMotion: Boolean,
    @SerialName("vacation_until") @Serializable(with = LocalDateIso8601Serializer::class) val vacationUntil: LocalDate? = null,
    @SerialName("streak_freezes") val streakFreezes: Int,
    @SerialName("updated_at") @Serializable(with = InstantIso8601Serializer::class) val updatedAt: Instant,
)
