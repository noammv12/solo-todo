package com.solotodo.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

/**
 * The 4 notification channels the System can speak through. Created at
 * application start so user-visible names/descriptions already exist by the
 * time the user toggles permissions in Awakening Step 5 or in Protocol.
 *
 * Channel importance maps to the Android IMPORTANCE_* scale:
 *   - daily_reminder — DEFAULT (sound, no heads-up)
 *   - evening_nudge  — HIGH (heads-up; nudge only fires if Daily Quest incomplete)
 *   - rank_up        — HIGH (heads-up; user asked to be notified of rank changes)
 *   - reflection     — LOW (silent; weekly summary, not time-critical)
 *
 * minSdk 26 so channels are always available. All copy uppercase to match the
 * System voice.
 */
object NotificationChannels {

    const val ID_DAILY_REMINDER = "daily_reminder"
    const val ID_EVENING_NUDGE = "evening_nudge"
    const val ID_RANK_UP = "rank_up"
    const val ID_REFLECTION = "reflection"

    fun createChannels(context: Context) {
        val mgr = context.getSystemService<NotificationManager>() ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        mgr.createNotificationChannels(
            listOf(
                NotificationChannel(
                    ID_DAILY_REMINDER,
                    "DAILY REMINDER",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = "Morning summons at 09:00." },

                NotificationChannel(
                    ID_EVENING_NUDGE,
                    "EVENING NUDGE",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply { description = "Fires only if Daily Quest is incomplete." },

                NotificationChannel(
                    ID_RANK_UP,
                    "RANK UP",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply { description = "System rank ascension." },

                NotificationChannel(
                    ID_REFLECTION,
                    "WEEKLY REFLECTION",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply { description = "Monarch's summary every Sunday at 19:00." },
            ),
        )
    }
}
