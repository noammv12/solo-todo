package com.solotodo.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.solotodo.data.di.AwakeningDraftPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferences-DataStore wrapper for the in-flight [AwakeningDraft].
 *
 * The [DataStore] instance is provided by `OnboardingDataStoreModule` and
 * points at `<filesDir>/datastore/awakening_draft.preferences_pb`. All writes
 * are atomic; `edit { }` is cancel-safe.
 *
 * Preset IDs are stored as a pipe-delimited string to preserve user selection
 * order (Preferences lacks a List primitive; Set would lose order).
 */
@Singleton
class AwakeningDraftStore @Inject constructor(
    @AwakeningDraftPrefs private val dataStore: DataStore<Preferences>,
) {
    fun observe(): Flow<AwakeningDraft> = dataStore.data.map { prefs ->
        AwakeningDraft(
            designation = prefs[KEY_DESIGNATION]?.takeIf { it.isNotEmpty() },
            selectedPresetIds = prefs[KEY_SELECTED]
                ?.split(DELIM)
                ?.filter { it.isNotBlank() }
                .orEmpty(),
            firstQuestRaw = prefs[KEY_FIRST_QUEST_RAW]?.takeIf { it.isNotEmpty() },
        )
    }

    suspend fun get(): AwakeningDraft {
        val prefs = dataStore.data.first()
        return AwakeningDraft(
            designation = prefs[KEY_DESIGNATION]?.takeIf { it.isNotEmpty() },
            selectedPresetIds = prefs[KEY_SELECTED]
                ?.split(DELIM)
                ?.filter { it.isNotBlank() }
                .orEmpty(),
            firstQuestRaw = prefs[KEY_FIRST_QUEST_RAW]?.takeIf { it.isNotEmpty() },
        )
    }

    suspend fun setDesignation(name: String?) {
        dataStore.edit { prefs ->
            if (name.isNullOrEmpty()) prefs.remove(KEY_DESIGNATION)
            else prefs[KEY_DESIGNATION] = name
        }
    }

    suspend fun setSelected(ids: List<String>) {
        dataStore.edit { prefs ->
            if (ids.isEmpty()) prefs.remove(KEY_SELECTED)
            else prefs[KEY_SELECTED] = ids.joinToString(DELIM)
        }
    }

    suspend fun setFirstQuestRaw(raw: String?) {
        dataStore.edit { prefs ->
            if (raw.isNullOrEmpty()) prefs.remove(KEY_FIRST_QUEST_RAW)
            else prefs[KEY_FIRST_QUEST_RAW] = raw
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    companion object {
        const val FILE_NAME = "awakening_draft"
        private const val DELIM = "|"
        val KEY_DESIGNATION = stringPreferencesKey("designation")
        val KEY_SELECTED = stringPreferencesKey("selected_preset_ids")
        val KEY_FIRST_QUEST_RAW = stringPreferencesKey("first_quest_raw")
    }
}
