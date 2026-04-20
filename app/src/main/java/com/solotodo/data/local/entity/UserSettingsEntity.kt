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
) {
    companion object {
        const val SINGLETON_ID = "me"
    }
}
