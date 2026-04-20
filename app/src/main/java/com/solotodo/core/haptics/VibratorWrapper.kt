package com.solotodo.core.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin interface around platform vibration so tests can fake the Vibrator
 * without bringing in Robolectric.
 */
interface VibratorWrapper {
    fun hasVibrator(): Boolean

    /**
     * Plays a one-shot waveform. Waveform is (timings, amplitudes) arrays with
     * matched lengths. Silently no-ops when the device reports no vibrator.
     */
    fun vibrateWaveform(timings: LongArray, amplitudes: IntArray)
}

@Singleton
class AndroidVibratorWrapper @Inject constructor(
    @ApplicationContext private val context: Context,
) : VibratorWrapper {

    private val vibrator: Vibrator? by lazy { resolve() }

    override fun hasVibrator(): Boolean = vibrator?.hasVibrator() == true

    override fun vibrateWaveform(timings: LongArray, amplitudes: IntArray) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        if (v.hasAmplitudeControl()) {
            v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(timings, -1)
        }
    }

    private fun resolve(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
