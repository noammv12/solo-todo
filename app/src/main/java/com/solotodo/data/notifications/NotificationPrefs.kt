package com.solotodo.data.notifications

import com.solotodo.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Typed accessor over the `user_settings.notifications` JSON blob. Hides the
 * serialization detail so step 5 + Protocol UI only see a strongly-typed
 * [Channels] value.
 *
 * Retention-override reminder: defaults are ALL FALSE. Only Awakening Step 5
 * (via [setAll]) or Protocol toggles (future 6.4) flip these on.
 */
@Singleton
class NotificationPrefs @Inject constructor(
    private val settings: SettingsRepository,
) {
    @Serializable
    data class Channels(
        val morning: Boolean = false,
        val evening: Boolean = false,
        @kotlinx.serialization.SerialName("rank_up") val rankUp: Boolean = false,
        val reflection: Boolean = false,
    ) {
        fun toJson(): String = JSON.encodeToString(serializer(), this)

        companion object {
            val OFF = Channels()
            val ON = Channels(morning = true, evening = true, rankUp = true, reflection = true)
            fun fromJsonOrDefault(raw: String?): Channels = raw?.let {
                runCatching { JSON.decodeFromString(serializer(), it) }.getOrNull()
            } ?: OFF
        }
    }

    fun observe(): Flow<Channels> = settings.observe().map {
        Channels.fromJsonOrDefault(it?.notifications)
    }

    suspend fun get(): Channels = Channels.fromJsonOrDefault(settings.get()?.notifications)

    /**
     * Awakening Step 5 commits here: DECLINE → all off, ALLOW (with OS grant)
     * → all on. Called inside the OnboardingCommitter's outer transaction so
     * the write piggy-backs on the same op-log entry as every other settings
     * field flipped at commit time.
     */
    suspend fun setAll(allowed: Boolean) {
        val current = settings.initializeIfMissing()
        val next = if (allowed) Channels.ON else Channels.OFF
        settings.update(current.copy(notifications = next.toJson()))
    }

    suspend fun setChannel(id: ChannelId, enabled: Boolean) {
        val current = settings.initializeIfMissing()
        val channels = Channels.fromJsonOrDefault(current.notifications)
        val next = when (id) {
            ChannelId.MORNING -> channels.copy(morning = enabled)
            ChannelId.EVENING -> channels.copy(evening = enabled)
            ChannelId.RANK_UP -> channels.copy(rankUp = enabled)
            ChannelId.REFLECTION -> channels.copy(reflection = enabled)
        }
        settings.update(current.copy(notifications = next.toJson()))
    }

    enum class ChannelId { MORNING, EVENING, RANK_UP, REFLECTION }

    private companion object {
        val JSON = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    }
}
