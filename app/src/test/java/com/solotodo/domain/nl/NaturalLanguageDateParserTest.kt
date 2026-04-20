package com.solotodo.domain.nl

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NaturalLanguageDateParserTest {

    private val fixed = Instant.parse("2026-04-20T12:00:00Z")
    private val zone = TimeZone.of("UTC")
    private val fixedClock = object : Clock { override fun now(): Instant = fixed }
    private val parser = NaturalLanguageDateParser(clock = fixedClock, zone = zone)

    @Test fun `no tokens means plain title`() {
        val p = parser.parse("buy milk")
        assertEquals("buy milk", p.title)
        assertNull(p.dueAt)
        assertTrue(p.tokensFound.isEmpty())
    }

    @Test fun `today resolves to today`() {
        val p = parser.parse("write report today")
        assertEquals("write report", p.title)
        val expected = fixed.toLocalDateTime(zone).date
        val actual = p.dueAt!!.toLocalDateTime(zone).date
        assertEquals(expected, actual)
    }

    @Test fun `tomorrow resolves to next day`() {
        val p = parser.parse("gym tomorrow")
        assertEquals("gym", p.title)
        val expected = fixed.toLocalDateTime(zone).date.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
        val actual = p.dueAt!!.toLocalDateTime(zone).date
        assertEquals(expected, actual)
    }

    @Test fun `stat tag is extracted`() {
        val p = parser.parse("pushups @str")
        assertEquals("pushups", p.title)
        assertEquals("str", p.stat)
    }

    @Test fun `list tag is extracted`() {
        val p = parser.parse("stand-up #work")
        assertEquals("stand-up", p.title)
        assertEquals("work", p.list)
    }

    @Test fun `priority is extracted`() {
        val p1 = parser.parse("ship it !!!")
        assertEquals(2, p1.priority)
        val p2 = parser.parse("review PR !!")
        assertEquals(1, p2.priority)
        val p3 = parser.parse("email !")
        assertEquals(0, p3.priority)
    }

    @Test fun `combined tags extract all`() {
        val p = parser.parse("run 5k tomorrow morning @str #health !!")
        assertEquals("run 5k", p.title)
        assertEquals("str", p.stat)
        assertEquals("health", p.list)
        assertEquals(1, p.priority)
        assertNotNull(p.dueAt)
    }

    @Test fun `in N days advances date`() {
        val p = parser.parse("call dentist in 3 days")
        val expected = fixed.toLocalDateTime(zone).date.plus(3, kotlinx.datetime.DateTimeUnit.DAY)
        val actual = p.dueAt!!.toLocalDateTime(zone).date
        assertEquals(expected, actual)
    }
}
