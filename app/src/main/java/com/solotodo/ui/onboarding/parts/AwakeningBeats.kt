package com.solotodo.ui.onboarding.parts

import com.solotodo.ui.cinematics.BeatScheduler

/**
 * Awakening Step 1 timing table. Uses the existing [BeatScheduler.Beats]
 * shape so the host composable can reuse the same wall-clock orchestration
 * loop that drives rank-up cinematics.
 *
 * Phase map (ms from screen mount):
 *   0 – 600     pure black + micro radial flash (flashEnd)
 *   600 – 2200  hex reveal: alpha 0→1, scale 0.8→1.0 (chargeEnd)
 *               haptic `light()` fires at 600
 *   2200 – 4000 kicker + body TypeIn at 38 cps (liftEnd)
 *   4000 – 4800 AWAKEN CTA rises 12dp + fades in (titleStart → titleEnd)
 *   tap       → haptic `rigid()`, advance FSM
 *
 * Total 4800ms exactly, as locked in `onboarding.html`. Reduce-motion
 * collapses to a single-frame render (200ms hold) via the host.
 */
val AwakeningBeats: BeatScheduler.Beats = BeatScheduler.Beats(
    flashEndMs = 600,
    chargeEndMs = 2200,
    liftEndMs = 4000,
    titleStartMs = 4000,
    titleEndMs = 4800,
    totalMs = 4800,
    hapticAtMs = 600,
    burstCount = 0,
    burstStartMs = 4000,
    burstIntervalMs = 200,
)
