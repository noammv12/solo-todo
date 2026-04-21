package com.solotodo.core.haptics

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Semantic haptic patterns for the whole app.
 *
 * Patterns map to the haptic table in `interactions.html` and the plan's
 * retention-override haptic matrix. Each method:
 *   1. Checks the relevant channel in `HapticsPrefs` (gated by `master` too).
 *   2. Checks `vibrator.hasVibrator()`.
 *   3. Plays the waveform.
 *
 * If any gate fails, the call is silent. Callers never need to branch.
 *
 * Channel mapping:
 *   light/tap/rigid   → CHANNEL_TAP      (keystrokes, toggles, CTA confirms)
 *   success           → CHANNEL_SUCCESS  (task complete, daily-quest-complete)
 *   threshold         → CHANNEL_THRESHOLD(swipe edge, max-selection hit)
 *   warning           → CHANNEL_WARNING  (validation error)
 *   rankUpPulse       → CHANNEL_RANK_UP  (cinematic beat at t=1200ms)
 */
@Singleton
class SoloHaptics @Inject constructor(
    private val vibrator: VibratorWrapper,
    private val prefs: HapticsPrefs,
) {

    /** Barely-there bump — Awakening hex reveal, toggle-on confirmation. */
    suspend fun light() {
        if (!gated(HapticsPrefs.CHANNEL_TAP)) return
        vibrator.vibrateWaveform(LIGHT_TIMINGS, LIGHT_AMPLITUDES)
    }

    /** Keystroke / per-item diamond toggle. */
    suspend fun tap() {
        if (!gated(HapticsPrefs.CHANNEL_TAP)) return
        vibrator.vibrateWaveform(TAP_TIMINGS, TAP_AMPLITUDES)
    }

    /** CTA confirm — AWAKEN, CONFIRM, COMMIT, CAPTURE. */
    suspend fun rigid() {
        if (!gated(HapticsPrefs.CHANNEL_TAP)) return
        vibrator.vibrateWaveform(RIGID_TIMINGS, RIGID_AMPLITUDES)
    }

    /** Swipe-threshold crossed, max-selection attempted. */
    suspend fun threshold() {
        if (!gated(HapticsPrefs.CHANNEL_THRESHOLD)) return
        vibrator.vibrateWaveform(THRESHOLD_TIMINGS, THRESHOLD_AMPLITUDES)
    }

    /** Validation error — "designation too short", "can't save". */
    suspend fun warning() {
        if (!gated(HapticsPrefs.CHANNEL_WARNING)) return
        vibrator.vibrateWaveform(WARNING_TIMINGS, WARNING_AMPLITUDES)
    }

    /** Task complete, Daily Quest complete. */
    suspend fun success() {
        if (!gated(HapticsPrefs.CHANNEL_SUCCESS)) return
        vibrator.vibrateWaveform(SUCCESS_TIMINGS, SUCCESS_AMPLITUDES)
    }

    /**
     * Cinematic rank-up beat (Phase 5). Heavy × 2, 80ms apart.
     * Timings `[0, 60, 80, 60]` / amplitudes `[0, 255, 0, 255]` — leading
     * `0/0` pair is required by `VibrationEffect.createWaveform` when there
     * is no initial delay.
     */
    suspend fun rankUpPulse() {
        if (!gated(HapticsPrefs.CHANNEL_RANK_UP)) return
        vibrator.vibrateWaveform(RANK_UP_TIMINGS, RANK_UP_AMPLITUDES)
    }

    /**
     * Legacy Phase 5 alias for `success()` — kept so existing call sites don't
     * need to change. Mirrors `CinematicHaptics.successTap()`.
     */
    suspend fun successTap() = success()

    private suspend fun gated(channel: String): Boolean =
        prefs.isEnabled(channel) && vibrator.hasVibrator()

    companion object {
        val LIGHT_TIMINGS = longArrayOf(0L, 20L)
        val LIGHT_AMPLITUDES = intArrayOf(0, 80)

        val TAP_TIMINGS = longArrayOf(0L, 10L)
        val TAP_AMPLITUDES = intArrayOf(0, 60)

        val RIGID_TIMINGS = longArrayOf(0L, 30L)
        val RIGID_AMPLITUDES = intArrayOf(0, 255)

        val THRESHOLD_TIMINGS = longArrayOf(0L, 15L, 40L, 15L)
        val THRESHOLD_AMPLITUDES = intArrayOf(0, 140, 0, 255)

        val WARNING_TIMINGS = longArrayOf(0L, 30L, 60L, 30L, 60L, 30L)
        val WARNING_AMPLITUDES = intArrayOf(0, 200, 0, 200, 0, 200)

        val SUCCESS_TIMINGS = longArrayOf(0L, 20L, 30L, 40L)
        val SUCCESS_AMPLITUDES = intArrayOf(0, 120, 0, 200)

        val RANK_UP_TIMINGS = longArrayOf(0L, 60L, 80L, 60L)
        val RANK_UP_AMPLITUDES = intArrayOf(0, 255, 0, 255)
    }
}
