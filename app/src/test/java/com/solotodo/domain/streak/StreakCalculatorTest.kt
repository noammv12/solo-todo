package com.solotodo.domain.streak

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCalculatorTest {

    private val day = LocalDate(2026, 4, 20)

    @Test fun `empty history returns zero`() {
        assertEquals(0, StreakCalculator.currentStreak(emptyList(), day))
    }

    @Test fun `today incomplete with yesterday complete counts from yesterday`() {
        val days = listOf(day.minusDay(1), day.minusDay(2), day.minusDay(3))
        assertEquals(3, StreakCalculator.currentStreak(days, day))
    }

    @Test fun `today complete counts today`() {
        val days = listOf(day, day.minusDay(1), day.minusDay(2))
        assertEquals(3, StreakCalculator.currentStreak(days, day))
    }

    @Test fun `gap breaks streak`() {
        val days = listOf(day, day.minusDay(1), day.minusDay(3), day.minusDay(4))
        assertEquals(2, StreakCalculator.currentStreak(days, day))
    }

    @Test fun `longest picks the biggest run in history`() {
        val days = listOf(
            day.minusDay(1), day.minusDay(2), day.minusDay(3), day.minusDay(4), // 4-run
            day.minusDay(10),
            day.minusDay(20), day.minusDay(21),
        )
        assertEquals(4, StreakCalculator.longestStreak(days))
    }

    @Test fun `longest with single day is 1`() {
        assertEquals(1, StreakCalculator.longestStreak(listOf(day)))
    }

    private fun LocalDate.minusDay(n: Int): LocalDate = this.minus(n, DateTimeUnit.DAY)
}
