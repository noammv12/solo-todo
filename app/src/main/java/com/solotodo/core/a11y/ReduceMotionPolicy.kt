package com.solotodo.core.a11y

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.solotodo.data.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves the effective "reduce motion" preference by OR'ing the user's
 * in-app toggle with the OS animator-duration-scale accessibility setting.
 *
 * When either is active, cinematics collapse to a 200ms single-frame variant.
 */
@Singleton
class ReduceMotionPolicy @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
) {
    fun observe(): Flow<Boolean> = combine(
        settingsRepository.observe().map { it?.reduceMotion ?: false },
        osAnimatorScaleOffFlow(),
    ) { user, osOff -> user || osOff }.distinctUntilChanged()

    private fun osAnimatorScaleOffFlow(): Flow<Boolean> = callbackFlow {
        val resolver = context.contentResolver
        fun readScaleIsZero(): Boolean =
            Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

        trySend(readScaleIsZero())

        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(readScaleIsZero())
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        awaitClose { resolver.unregisterContentObserver(observer) }
    }
}
