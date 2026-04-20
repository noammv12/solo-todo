package com.solotodo.core.haptics

import com.solotodo.data.repository.SettingsRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads boolean flags from the `user_settings.haptics` JSON string.
 *
 * Shape: `{ "master": bool, "tap": bool, "success": bool, "threshold": bool,
 * "rank_up": bool, "warning": bool }`. Unknown channels default to enabled
 * (fail-open — absent key means "not configured", not "off").
 */
@Singleton
class HapticsPrefs @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend fun isEnabled(channel: String): Boolean {
        val raw = settingsRepository.get()?.haptics ?: return true
        val obj = runCatching { Json.parseToJsonElement(raw).jsonObject }.getOrNull() ?: return true
        val master = obj["master"]?.jsonPrimitive?.booleanOrNull ?: true
        val channelOn = obj[channel]?.jsonPrimitive?.booleanOrNull ?: true
        return master && channelOn
    }

    companion object {
        const val CHANNEL_RANK_UP = "rank_up"
        const val CHANNEL_SUCCESS = "success"
        const val CHANNEL_TAP = "tap"
    }
}
