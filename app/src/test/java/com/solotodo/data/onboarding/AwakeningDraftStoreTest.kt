package com.solotodo.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AwakeningDraftStoreTest {

    @get:Rule val tmp: TemporaryFolder = TemporaryFolder()

    private lateinit var job: Job
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var store: AwakeningDraftStore

    @Before fun setup() {
        job = SupervisorJob()
        // Use real IO dispatcher for the DataStore scope — `runTest`'s virtual
        // scheduler can't be shared with DataStore's internal dispatcher so
        // we run the store on a normal dispatcher and let `runTest` suspend
        // naturally while we wait for disk reads/writes.
        val scope = CoroutineScope(job + Dispatchers.IO)
        dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { tmp.newFile("awakening_draft.preferences_pb") },
        )
        store = AwakeningDraftStore(dataStore)
    }

    @After fun tearDown() {
        job.cancel()
    }

    @Test fun `default draft is fully empty`() = runTest {
        val draft = store.observe().first()
        assertNull(draft.designation)
        assertTrue(draft.selectedPresetIds.isEmpty())
        assertNull(draft.firstQuestRaw)
    }

    @Test fun `setDesignation round-trip`() = runTest {
        store.setDesignation("SHADOW")
        assertEquals("SHADOW", store.observe().first().designation)
    }

    @Test fun `setDesignation(null) clears the value`() = runTest {
        store.setDesignation("SHADOW")
        store.setDesignation(null)
        assertNull(store.observe().first().designation)
    }

    @Test fun `setDesignation empty string clears the value`() = runTest {
        store.setDesignation("SHADOW")
        store.setDesignation("")
        assertNull(store.observe().first().designation)
    }

    @Test fun `setSelected preserves order`() = runTest {
        val ids = listOf("preset_workout", "preset_water_2l", "preset_read_10p", "preset_journal")
        store.setSelected(ids)
        assertEquals(ids, store.observe().first().selectedPresetIds)
    }

    @Test fun `setSelected empty list clears the value`() = runTest {
        store.setSelected(listOf("a", "b", "c"))
        store.setSelected(emptyList())
        assertTrue(store.observe().first().selectedPresetIds.isEmpty())
    }

    @Test fun `setFirstQuestRaw round-trip`() = runTest {
        store.setFirstQuestRaw("Book flight to Seoul friday 3pm @int #travel")
        assertEquals(
            "Book flight to Seoul friday 3pm @int #travel",
            store.observe().first().firstQuestRaw,
        )
    }

    @Test fun `clear wipes all fields`() = runTest {
        store.setDesignation("SHADOW")
        store.setSelected(listOf("preset_workout", "preset_water_2l", "preset_read_10p"))
        store.setFirstQuestRaw("pay rent tomorrow")
        store.clear()
        val draft = store.observe().first()
        assertNull(draft.designation)
        assertTrue(draft.selectedPresetIds.isEmpty())
        assertNull(draft.firstQuestRaw)
    }

    @Test fun `get() matches observe() first emission`() = runTest {
        store.setDesignation("HUNTER")
        store.setSelected(listOf("preset_water_2l", "preset_read_10p", "preset_workout"))
        val fromObserve = store.observe().first()
        val fromGet = store.get()
        assertEquals(fromObserve, fromGet)
    }
}
