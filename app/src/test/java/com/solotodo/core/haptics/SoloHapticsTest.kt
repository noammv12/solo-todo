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

class SoloHapticsTest {

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

    private fun haptics(haptics: String, hasVibrator: Boolean = true): Pair<SoloHaptics, FakeVibrator> {
        val vib = FakeVibrator(hasIt = hasVibrator)
        val h = SoloHaptics(vib, HapticsPrefs(settings(haptics)))
        return h to vib
    }

    @Test
    fun `rankUpPulse passes spec waveform when enabled`() = runTest {
        val (h, vib) = haptics("""{"master":true,"rank_up":true}""")
        h.rankUpPulse()
        assertEquals(1, vib.callCount)
        assertArrayEquals(longArrayOf(0, 60, 80, 60), vib.lastTimings)
        assertArrayEquals(intArrayOf(0, 255, 0, 255), vib.lastAmplitudes)
    }

    @Test
    fun `rankUpPulse is silent when master haptics off`() = runTest {
        val (h, vib) = haptics("""{"master":false,"rank_up":true}""")
        h.rankUpPulse()
        assertEquals(0, vib.callCount)
        assertNull(vib.lastTimings)
    }

    @Test
    fun `rankUpPulse is silent when rank_up channel off`() = runTest {
        val (h, vib) = haptics("""{"master":true,"rank_up":false}""")
        h.rankUpPulse()
        assertEquals(0, vib.callCount)
    }

    @Test
    fun `no-op when device reports no vibrator`() = runTest {
        val (h, vib) = haptics("""{"master":true,"rank_up":true}""", hasVibrator = false)
        h.rankUpPulse()
        assertEquals(0, vib.callCount)
    }

    @Test
    fun `unknown channel defaults to enabled (fail-open)`() = runTest {
        val (h, vib) = haptics("""{"master":true}""")
        h.rankUpPulse()
        assertEquals(1, vib.callCount)
    }

    // Phase 6.1 new patterns — verify spec waveforms + gating via the tap/threshold/
    // warning/success channels.

    @Test
    fun `light plays spec waveform via tap channel`() = runTest {
        val (h, vib) = haptics("""{"master":true,"tap":true}""")
        h.light()
        assertArrayEquals(longArrayOf(0, 20), vib.lastTimings)
        assertArrayEquals(intArrayOf(0, 80), vib.lastAmplitudes)
    }

    @Test
    fun `tap plays spec waveform`() = runTest {
        val (h, vib) = haptics("""{"master":true,"tap":true}""")
        h.tap()
        assertArrayEquals(longArrayOf(0, 10), vib.lastTimings)
        assertArrayEquals(intArrayOf(0, 60), vib.lastAmplitudes)
    }

    @Test
    fun `rigid plays spec waveform`() = runTest {
        val (h, vib) = haptics("""{"master":true,"tap":true}""")
        h.rigid()
        assertArrayEquals(longArrayOf(0, 30), vib.lastTimings)
        assertArrayEquals(intArrayOf(0, 255), vib.lastAmplitudes)
    }

    @Test
    fun `threshold plays spec waveform`() = runTest {
        val (h, vib) = haptics("""{"master":true,"threshold":true}""")
        h.threshold()
        assertArrayEquals(longArrayOf(0, 15, 40, 15), vib.lastTimings)
        assertArrayEquals(intArrayOf(0, 140, 0, 255), vib.lastAmplitudes)
    }

    @Test
    fun `warning plays spec waveform`() = runTest {
        val (h, vib) = haptics("""{"master":true,"warning":true}""")
        h.warning()
        assertArrayEquals(longArrayOf(0, 30, 60, 30, 60, 30), vib.lastTimings)
        assertArrayEquals(intArrayOf(0, 200, 0, 200, 0, 200), vib.lastAmplitudes)
    }

    @Test
    fun `success plays spec waveform via success channel`() = runTest {
        val (h, vib) = haptics("""{"master":true,"success":true}""")
        h.success()
        assertArrayEquals(longArrayOf(0, 20, 30, 40), vib.lastTimings)
        assertArrayEquals(intArrayOf(0, 120, 0, 200), vib.lastAmplitudes)
    }

    @Test
    fun `light rigid and tap all silence when tap channel is off`() = runTest {
        val (h, vib) = haptics("""{"master":true,"tap":false}""")
        h.light(); h.tap(); h.rigid()
        assertEquals(0, vib.callCount)
    }

    @Test
    fun `threshold silenced when threshold channel off`() = runTest {
        val (h, vib) = haptics("""{"master":true,"threshold":false}""")
        h.threshold()
        assertEquals(0, vib.callCount)
    }

    @Test
    fun `warning silenced when warning channel off`() = runTest {
        val (h, vib) = haptics("""{"master":true,"warning":false}""")
        h.warning()
        assertEquals(0, vib.callCount)
    }

    @Test
    fun `successTap is an alias for success`() = runTest {
        val (h, vib) = haptics("""{"master":true,"success":true}""")
        h.successTap()
        assertArrayEquals(longArrayOf(0, 20, 30, 40), vib.lastTimings)
        assertEquals(1, vib.callCount)
    }

    @Test
    fun `every pattern silent when master off`() = runTest {
        val (h, vib) = haptics(
            """{"master":false,"tap":true,"success":true,"threshold":true,"warning":true,"rank_up":true}""",
        )
        h.light(); h.tap(); h.rigid(); h.threshold(); h.warning(); h.success(); h.rankUpPulse()
        assertEquals(0, vib.callCount)
    }
}
