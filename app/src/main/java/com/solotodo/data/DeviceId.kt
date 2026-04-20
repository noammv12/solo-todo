package com.solotodo.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stable per-install UUID used as the `origin_device_id` on every entity and
 * op-log row. Generated on first access and persisted in a dedicated
 * SharedPreferences file so it's independent of the main DataStore.
 */
@Singleton
class DeviceId @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Lazily generates and caches a per-install UUID. */
    val value: String by lazy {
        prefs.getString(KEY, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY, it).apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "solotodo.device"
        private const val KEY = "device_id"
    }
}
