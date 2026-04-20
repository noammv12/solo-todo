package com.solotodo.core.haptics

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Haptic patterns tied to cinematic beats.
 *
 * Rank-up: heavy × 2, 80ms apart (interactions.html "RANK-UP" row).
 * Timings array is `[wait, on, off, on] = [0, 60, 80, 60]`, amplitudes are
 * `[0, max, 0, max]` — the leading 0/0 pair is required by
 * VibrationEffect.createWaveform when there is no initial delay.
 */
@Singleton
class CinematicHaptics @Inject constructor(
    private val vibrator: VibratorWrapper,
    private val prefs: HapticsPrefs,
) {
    suspend fun rankUpPulse() {
        if (!prefs.isEnabled(HapticsPrefs.CHANNEL_RANK_UP)) return
        if (!vibrator.hasVibrator()) return
        vibrator.vibrateWaveform(RANK_UP_TIMINGS, RANK_UP_AMPLITUDES)
    }

    suspend fun successTap() {
        if (!prefs.isEnabled(HapticsPrefs.CHANNEL_SUCCESS)) return
        if (!vibrator.hasVibrator()) return
        vibrator.vibrateWaveform(SUCCESS_TIMINGS, SUCCESS_AMPLITUDES)
    }

    companion object {
        val RANK_UP_TIMINGS = longArrayOf(0L, 60L, 80L, 60L)
        val RANK_UP_AMPLITUDES = intArrayOf(0, 255, 0, 255)

        val SUCCESS_TIMINGS = longArrayOf(0L, 40L)
        val SUCCESS_AMPLITUDES = intArrayOf(0, 180)
    }
}
