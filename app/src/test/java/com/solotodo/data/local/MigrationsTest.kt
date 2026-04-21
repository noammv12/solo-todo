package com.solotodo.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * Unit-only coverage of Room migrations.
 *
 * A full integration test needs `androidx.room:room-testing` + MigrationTestHelper
 * + an `androidTest` source set, which this project doesn't wire up yet.
 * Until then, we capture every `execSQL(String)` call via a mockk answer so
 * typos in migration SQL (wrong table, wrong default, wrong type) are caught
 * at test time.
 */
class MigrationsTest {

    private fun capturedExecs(): List<String> {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        val captured = mutableListOf<String>()
        every { db.execSQL(any<String>()) } answers {
            captured += firstArg<String>()
        }
        Migrations.MIGRATION_3_4.migrate(db)
        return captured
    }

    @Test
    fun `MIGRATION_3_4 issues four ALTER TABLE statements on user_settings`() {
        val executed = capturedExecs()
        assertEquals(4, executed.size)
        executed.forEach { sql ->
            assertTrue(
                "Expected ALTER TABLE user_settings, got: $sql",
                sql.uppercase(Locale.ROOT).startsWith("ALTER TABLE USER_SETTINGS"),
            )
        }
    }

    @Test
    fun `MIGRATION_3_4 adds onboarding_completed as NOT NULL INTEGER DEFAULT 0`() {
        val sql = capturedExecs().first { it.contains("onboarding_completed") }
        assertTrue(sql.contains("INTEGER"))
        assertTrue(sql.contains("NOT NULL"))
        assertTrue(sql.contains("DEFAULT 0"))
    }

    @Test
    fun `MIGRATION_3_4 adds awakened_at as nullable INTEGER`() {
        val sql = capturedExecs().first { it.contains("awakened_at") }
        assertTrue(sql.contains("INTEGER"))
        assertFalse("awakened_at must be nullable", sql.contains("NOT NULL"))
    }

    @Test
    fun `MIGRATION_3_4 adds daily_quest_count with default 3`() {
        val sql = capturedExecs().first { it.contains("daily_quest_count") }
        assertTrue(sql.contains("INTEGER"))
        assertTrue(sql.contains("NOT NULL"))
        assertTrue(sql.contains("DEFAULT 3"))
    }

    @Test
    fun `MIGRATION_3_4 adds hard_mode as NOT NULL INTEGER DEFAULT 0`() {
        val sql = capturedExecs().first { it.contains("hard_mode") }
        assertTrue(sql.contains("INTEGER"))
        assertTrue(sql.contains("NOT NULL"))
        assertTrue(sql.contains("DEFAULT 0"))
    }

    @Test
    fun `MIGRATION_3_4 hits execSQL exactly four times`() {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        Migrations.MIGRATION_3_4.migrate(db)
        verify(exactly = 4) { db.execSQL(any<String>()) }
    }

    @Test
    fun `Migrations ALL exposes V3 to V4`() {
        val migration = Migrations.ALL.single()
        assertEquals(3, migration.startVersion)
        assertEquals(4, migration.endVersion)
    }
}
