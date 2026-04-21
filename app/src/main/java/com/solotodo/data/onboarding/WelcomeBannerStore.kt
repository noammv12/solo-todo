package com.solotodo.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.solotodo.data.di.WelcomeBannerPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local-only flag for the "▸ SYSTEM · AWAKENED" banner rendered once on the
 * Status screen immediately after the Awakening commit.
 *
 * Lives in a dedicated Preferences DataStore (not in [com.solotodo.data.local.entity.UserSettingsEntity])
 * because the banner is a per-device one-shot UX affordance that has no
 * reason to round-trip through Supabase.
 */
@Singleton
class WelcomeBannerStore @Inject constructor(
    @WelcomeBannerPrefs private val ds: DataStore<Preferences>,
) {
    val shown: Flow<Boolean> = ds.data.map { it[KEY] == true }

    suspend fun markShown() {
        ds.edit { it[KEY] = true }
    }

    /** Only the locked test matrix's "replay preserves flag" case reads this. */
    suspend fun reset() {
        ds.edit { it[KEY] = false }
    }

    private companion object {
        val KEY = booleanPreferencesKey("welcome_banner_shown")
    }
}
