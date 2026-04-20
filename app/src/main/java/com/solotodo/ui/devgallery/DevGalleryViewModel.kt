package com.solotodo.ui.devgallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solotodo.data.auth.AuthRepository
import com.solotodo.data.dev.DevSeeder
import com.solotodo.data.local.dao.OpLogDao
import com.solotodo.data.local.dao.SyncStateDao
import com.solotodo.data.local.dao.TaskDao
import com.solotodo.data.local.entity.OpLogEntity
import com.solotodo.data.local.entity.SyncStateEntity
import com.solotodo.data.sync.SyncEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the dev gallery's controls:
 *  - Seed / Wipe (existing)
 *  - Sync diagnostics + "Sync now" / "Clear op-log" (Phase 4.5b)
 *
 * All flows are eager-started so the panel shows live values the moment the
 * user opens the gallery.
 */
@HiltViewModel
class DevGalleryViewModel @Inject constructor(
    private val seeder: DevSeeder,
    private val syncEngine: SyncEngine,
    authRepository: AuthRepository,
    opLogDao: OpLogDao,
    syncStateDao: SyncStateDao,
    taskDao: TaskDao,
) : ViewModel() {

    val completedCount: StateFlow<Int> = taskDao.observeCompletedCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val pendingOpCount: StateFlow<Int> = opLogDao.observePendingCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val quarantinedCount: StateFlow<Int> = opLogDao
        .observeQuarantinedCount(OpLogEntity.QUARANTINE_SENTINEL)
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val latestQuarantinedError: StateFlow<String?> = opLogDao
        .observeQuarantinedCount(OpLogEntity.QUARANTINE_SENTINEL)
        .map { count ->
            if (count > 0) opLogDao.latestQuarantinedError(OpLogEntity.QUARANTINE_SENTINEL)
            else null
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val syncStates: StateFlow<List<SyncStateEntity>> = syncStateDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val authState: StateFlow<AuthRepository.AuthState> = authRepository.state
        .stateIn(viewModelScope, SharingStarted.Eagerly, AuthRepository.AuthState.Loading)

    val lastSnapshot: StateFlow<SyncEngine.Snapshot?> = syncEngine.lastSnapshot

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    fun seed() {
        viewModelScope.launch {
            _status.value = "seeding…"
            runCatching { seeder.seed() }
                .onSuccess { _status.value = "seeded" }
                .onFailure { _status.value = "seed failed: ${it.message}" }
        }
    }

    fun wipe() {
        viewModelScope.launch {
            _status.value = "wiping…"
            runCatching { seeder.wipe() }
                .onSuccess { _status.value = "wiped" }
                .onFailure { _status.value = "wipe failed: ${it.message}" }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _status.value = "syncing…"
            syncEngine.runSyncNow()
            _status.value = "sync complete"
        }
    }

    fun clearOpLog() {
        viewModelScope.launch {
            _status.value = "clearing op-log…"
            val removed = syncEngine.clearOpLog()
            _status.value = "op-log cleared ($removed rows)"
        }
    }

    fun releaseQuarantine() {
        viewModelScope.launch {
            _status.value = "releasing quarantined ops…"
            val released = syncEngine.releaseQuarantine()
            _status.value = "released $released op(s) for retry"
        }
    }
}
