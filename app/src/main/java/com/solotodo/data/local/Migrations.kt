package com.solotodo.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room migrations, numbered N → N+1. Room applies them in sequence; a user on
 * V1 upgrading to V4 will get 1→2, 2→3, 3→4 played in order.
 *
 * Earlier phases shipped V1–V3 as destructive-fallback only (no stored
 * migrations), so this file starts at 3→4. If we ever need to handle a V1 or
 * V2 device in the wild we'll add those back; for now they're covered by the
 * destructive-downgrade fallback in DataModule.
 */
object Migrations {

    /**
     * V3 → V4 — Phase 6.1 Awakening:
     * add `onboarding_completed`, `awakened_at`, `daily_quest_count`, `hard_mode`
     * to `user_settings`. Defaults preserve the V3 contract (user not yet onboarded).
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE user_settings ADD COLUMN onboarding_completed INTEGER NOT NULL DEFAULT 0",
            )
            db.execSQL(
                "ALTER TABLE user_settings ADD COLUMN awakened_at INTEGER",
            )
            db.execSQL(
                "ALTER TABLE user_settings ADD COLUMN daily_quest_count INTEGER NOT NULL DEFAULT 3",
            )
            db.execSQL(
                "ALTER TABLE user_settings ADD COLUMN hard_mode INTEGER NOT NULL DEFAULT 0",
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_3_4)
}
