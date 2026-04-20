package com.solotodo.data.auth

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 4 auth: Guest (anonymous) for now. Google/magic-link sign-in land
 * in Phase 4.5.
 *
 * [AuthState] exposed as a Flow — UI observes and flips between sign-in screen
 * and main app automatically. Every repository that writes to Supabase pulls
 * the current user_id from here.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val auth: Auth,
) {
    sealed interface AuthState {
        data object Loading : AuthState
        data object NotAuthed : AuthState
        data class Guest(val userId: String) : AuthState
        data class Authenticated(val userId: String, val email: String?) : AuthState
    }

    val state: Flow<AuthState> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Initializing -> AuthState.Loading
            is SessionStatus.NotAuthenticated -> AuthState.NotAuthed
            is SessionStatus.RefreshFailure -> AuthState.NotAuthed
            is SessionStatus.Authenticated -> status.session.user?.toAuthState() ?: AuthState.NotAuthed
        }
    }

    suspend fun signInAsGuest() {
        auth.signInAnonymously()
    }

    suspend fun signOut() {
        auth.signOut()
    }

    /** Quick accessor for other repositories that need the current user_id. */
    fun currentUserId(): String? = auth.currentSessionOrNull()?.user?.id

    private fun UserInfo.toAuthState(): AuthState {
        // Anonymous users have no email and no verified identities attached.
        // Once they link an identity (Google, magic-link, etc.) email populates.
        val isAnonymous = email.isNullOrBlank()
        return if (isAnonymous) {
            AuthState.Guest(userId = id)
        } else {
            AuthState.Authenticated(userId = id, email = email)
        }
    }
}
