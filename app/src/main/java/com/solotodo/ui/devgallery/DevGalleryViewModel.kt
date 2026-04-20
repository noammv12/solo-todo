package com.solotodo.ui.devgallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solotodo.data.dev.DevSeeder
import com.solotodo.data.local.dao.TaskDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the dev gallery's Seed / Wipe buttons and exposes live task count so we
 * can visually confirm the DB round-trip works.
 */
@HiltViewModel
class DevGalleryViewModel @Inject constructor(
    private val seeder: DevSeeder,
    taskDao: TaskDao,
) : ViewModel() {

    val completedCount: StateFlow<Int> = taskDao.observeCompletedCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

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
}
