package com.solotodo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.solotodo.data.local.ThemeAccent
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * `user_settings` — singleton (id is always `"me"`).
 *
 * `haptics` + `notifications` stored as JSON strings for flexibility. They are
 * small (≤1 KB each) and not worth splitting into their own tables until they
 * grow substantially.
 *
 * Field order is load-bearing: the Phase 6.1 additions (`onboardingCompleted`,
 * `awakenedAt`, `dailyQuestCount`, `hardMode`) are appended **after**
 * `updatedAt` so existing positional callers (tests, `defaultSettings`) keep
 * working. Room tolerates either order for schema purposes.
 */
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: String = SINGLETON_ID,
    val designation: String,
    val theme: ThemeAccent,
    /** JSON: `{ master, tap, success, threshold, rank_up, warning }`. */
    val haptics: String,
    /** JSON: per-channel on/off + quiet hours. */
    val notifications: String,
    @ColumnInfo(name = "reduce_motion") val reduceMotion: Boolean,
    @ColumnInfo(name = "vacation_until") val vacationUntil: LocalDate?,
    /** Retention-override: earned Duolingo-style grace freezes, stockpiled up to 3. */
    @ColumnInfo(name = "streak_freezes") val streakFreezes: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    /** Phase 6 Awakening — set to true after Step 5 of onboarding commits. */
    @ColumnInfo(name = "onboarding_completed") val onboardingCompleted: Boolean = false,
    /** First time the user cleared Awakening; null until then. */
    @ColumnInfo(name = "awakened_at") val awakenedAt: Instant? = null,
    /** User-picked Daily Quest size, 3..5, chosen in Awakening Step 3. */
    @ColumnInfo(name = "daily_quest_count") val dailyQuestCount: Int = 3,
    /** Opt-in "hard mode": disables Streak Freeze grace. Default off. */
    @ColumnInfo(name = "hard_mode") val hardMode: Boolean = false,
) {
    companion object {
        const val SINGLETON_ID = "me"
    }
}
