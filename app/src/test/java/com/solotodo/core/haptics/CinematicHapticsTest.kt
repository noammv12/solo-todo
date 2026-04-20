package com.solotodo.core.haptics

import com.solotodo.data.local.ThemeAccent
import com.solotodo.data.local.entity.UserSettingsEntity
import com.solotodo.data.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CinematicHapticsTest {

    private class FakeVibrator(val hasIt: Boolean = true) : VibratorWrapper {
        var lastTimings: LongArray? = null
        var lastAmplitudes: IntArray? = null
        var callCount = 0
        override fun hasVibrator(): Boolean = hasIt
        override fun vibrateWaveform(timings: LongArray, amplitudes: IntArray) {
            lastTimings = timings
            lastAmplitudes = amplitudes
            callCount++
        }
    }

    private fun settings(haptics: String): SettingsRepository {
        val repo = mockk<SettingsRepository>()
        coEvery { repo.get() } returns UserSettingsEntity(
            designation = "HUNTER",
            theme = ThemeAccent.CYAN,
            haptics = haptics,
            notifications = """{"morning":false}""",
            reduceMotion = false,
            vacationUntil = null,
            streakFreezes = 0,
            updatedAt = Clock.System.now(),
        )
        return repo
    }

    @Test
    fun `rankUpPulse passes spec waveform when enabled`() = runTest {
        val vib = FakeVibrator()
        val haptics = CinematicHaptics(vib, HapticsPrefs(settings("""{"master":true,"rank_up":true}""")))
        haptics.rankUpPulse()
        assertEquals(1, vib.callCount)
        assertArrayEquals(longArrayOf(0, 60, 80, 60), vib.lastTimings)
        assertArrayEquals(intArrayOf(0, 255, 0, 255), vib.lastAmplitudes)
    }

    @Test
    fun `rankUpPulse is silent when master haptics off`() = runTest {
        val vib = FakeVibrator()
        val haptics = CinematicHaptics(vib, HapticsPrefs(settings("""{"master":false,"rank_up":true}""")))
        haptics.rankUpPulse()
        assertEquals(0, vib.callCount)
        assertNull(vib.lastTimings)
    }

    @Test
    fun `rankUpPulse is silent when rank_up channel off`() = runTest {
        val vib = FakeVibrator()
        val haptics = CinematicHaptics(vib, HapticsPrefs(settings("""{"master":true,"rank_up":false}""")))
        haptics.rankUpPulse()
        assertEquals(0, vib.callCount)
    }

    @Test
    fun `no-op when device reports no vibrator`() = runTest {
        val vib = FakeVibrator(hasIt = false)
        val haptics = CinematicHaptics(vib, HapticsPrefs(settings("""{"master":true,"rank_up":true}""")))
        haptics.rankUpPulse()
        assertEquals(0, vib.callCount)
    }

    @Test
    fun `unknown channel defaults to enabled (fail-open)`() = runTest {
        val vib = FakeVibrator()
        val haptics = CinematicHaptics(vib, HapticsPrefs(settings("""{"master":true}""")))
        haptics.rankUpPulse()
        assertEquals(1, vib.callCount)
    }
}
