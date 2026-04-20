package com.solotodo.ui.quickadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solotodo.data.local.StatKind
import com.solotodo.data.repository.TaskRepository
import com.solotodo.domain.nl.NaturalLanguageDateParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickAddUiState(
    val input: String = "",
    val parse: NaturalLanguageDateParser.Parse = NaturalLanguageDateParser.Parse(title = ""),
    val submitting: Boolean = false,
)

@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val taskRepo: TaskRepository,
    private val parser: NaturalLanguageDateParser,
) : ViewModel() {

    private val _state = MutableStateFlow(QuickAddUiState())
    val state: StateFlow<QuickAddUiState> = _state.asStateFlow()

    fun onInputChange(input: String) {
        val parse = if (input.isBlank()) {
            NaturalLanguageDateParser.Parse(title = "")
        } else {
            parser.parse(input)
        }
        _state.value = _state.value.copy(input = input, parse = parse)
    }

    /** Returns true once the task is saved; caller closes the sheet. */
    suspend fun submit(): Boolean {
        val parse = _state.value.parse
        val title = parse.title.trim().ifBlank { return false }
        _state.value = _state.value.copy(submitting = true)
        taskRepo.create(
            title = title,
            rawInput = _state.value.input,
            dueAt = parse.dueAt,
            stat = parse.stat?.let { runCatching { StatKind.valueOf(it.uppercase()) }.getOrNull() },
            priority = parse.priority,
            xp = 0,
        )
        _state.value = QuickAddUiState() // reset
        return true
    }

    fun reset() {
        _state.value = QuickAddUiState()
    }

    fun launchSubmit(onDone: () -> Unit) {
        viewModelScope.launch {
            if (submit()) onDone()
        }
    }
}
