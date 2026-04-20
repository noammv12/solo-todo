package com.solotodo.ui.quests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solotodo.data.local.entity.TaskEntity
import com.solotodo.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class QuestsFilter { TODAY, UPCOMING, ANYTIME, ARCHIVE }

data class QuestsUiState(
    val filter: QuestsFilter = QuestsFilter.TODAY,
    val tasks: List<TaskEntity> = emptyList(),
    val searchQuery: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class QuestsViewModel @Inject constructor(
    private val repo: TaskRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(QuestsFilter.TODAY)
    val filter: StateFlow<QuestsFilter> = _filter.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val state: StateFlow<QuestsUiState> = _filter.flatMapLatest { filter ->
        when (filter) {
            QuestsFilter.TODAY, QuestsFilter.UPCOMING, QuestsFilter.ANYTIME -> repo.observeOpen()
            QuestsFilter.ARCHIVE -> repo.observeArchive()
        }.map { tasks -> QuestsUiState(filter = filter, tasks = tasks, searchQuery = _query.value) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = QuestsUiState(),
    )

    fun setFilter(filter: QuestsFilter) {
        _filter.value = filter
    }

    fun setQuery(query: String) {
        _query.value = query
    }

    fun complete(taskId: String) {
        viewModelScope.launch { repo.complete(taskId) }
    }

    fun uncomplete(taskId: String) {
        viewModelScope.launch { repo.uncomplete(taskId) }
    }

    fun delete(taskId: String) {
        viewModelScope.launch { repo.softDelete(taskId) }
    }
}
