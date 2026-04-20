package com.solotodo.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solotodo.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val status: AuthRepository.AuthState = AuthRepository.AuthState.Loading,
    val signingIn: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
) : ViewModel() {

    private val _signingIn = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val state: StateFlow<AuthUiState> = combine(repo.state, _signingIn, _error) { status, signingIn, error ->
        AuthUiState(status = status, signingIn = signingIn, error = error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AuthUiState(),
    )

    fun signInAsGuest() {
        if (_signingIn.value) return
        _signingIn.value = true
        _error.value = null
        viewModelScope.launch {
            runCatching { repo.signInAsGuest() }
                .onFailure { _error.value = it.message ?: "sign-in failed" }
            _signingIn.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
