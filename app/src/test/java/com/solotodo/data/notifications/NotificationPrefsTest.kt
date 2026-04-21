package com.solotodo.data.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPrefsTest {

    @Test fun `default json decodes to OFF`() {
        val defaultJson = """{"morning":false,"evening":false,"rank_up":false,"reflection":false}"""
        val ch = NotificationPrefs.Channels.fromJsonOrDefault(defaultJson)
        assertEquals(NotificationPrefs.Channels.OFF, ch)
    }

    @Test fun `ON serializes with snake_case rank_up`() {
        val json = NotificationPrefs.Channels.ON.toJson()
        // Snake-case key matches the SQL column naming convention used everywhere else.
        assertTrue(json.contains("\"rank_up\":true"))
        assertTrue(json.contains("\"morning\":true"))
        assertTrue(json.contains("\"evening\":true"))
        assertTrue(json.contains("\"reflection\":true"))
    }

    @Test fun `round-trip preserves all fields`() {
        val original = NotificationPrefs.Channels(
            morning = true,
            evening = false,
            rankUp = true,
            reflection = false,
        )
        val parsed = NotificationPrefs.Channels.fromJsonOrDefault(original.toJson())
        assertEquals(original, parsed)
    }

    @Test fun `null or invalid json falls back to OFF`() {
        assertEquals(NotificationPrefs.Channels.OFF, NotificationPrefs.Channels.fromJsonOrDefault(null))
        assertEquals(NotificationPrefs.Channels.OFF, NotificationPrefs.Channels.fromJsonOrDefault(""))
        assertEquals(NotificationPrefs.Channels.OFF, NotificationPrefs.Channels.fromJsonOrDefault("not-json"))
    }

    @Test fun `OFF has every channel disabled`() {
        val off = NotificationPrefs.Channels.OFF
        assertFalse(off.morning)
        assertFalse(off.evening)
        assertFalse(off.rankUp)
        assertFalse(off.reflection)
    }

    @Test fun `ON has every channel enabled`() {
        val on = NotificationPrefs.Channels.ON
        assertTrue(on.morning)
        assertTrue(on.evening)
        assertTrue(on.rankUp)
        assertTrue(on.reflection)
    }
}
