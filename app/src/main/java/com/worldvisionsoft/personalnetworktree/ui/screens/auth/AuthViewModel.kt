package com.worldvisionsoft.personalnetworktree.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.worldvisionsoft.personalnetworktree.R
import com.worldvisionsoft.personalnetworktree.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authRepository: AuthRepository = AuthRepository()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Expose current user for UI layer
    val currentUser: FirebaseUser?
        get() = authRepository.currentUser

    private fun getString(resId: Int): String {
        return getApplication<Application>().getString(resId)
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            val result = authRepository.signUp(email, password)

            result.fold(
                onSuccess = {
                    _authState.value = AuthState(isSuccess = true)
                },
                onFailure = { exception ->
                    _authState.value = AuthState(error = exception.message ?: getString(R.string.error_sign_up_failed))
                }
            )
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            val result = authRepository.signIn(email, password)

            result.fold(
                onSuccess = {
                    _authState.value = AuthState(isSuccess = true)
                },
                onFailure = { exception ->
                    _authState.value = AuthState(error = exception.message ?: getString(R.string.error_sign_in_failed))
                }
            )
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            val result = authRepository.resetPassword(email)

            result.fold(
                onSuccess = {
                    _authState.value = AuthState(error = getString(R.string.success_password_reset_email_sent))
                },
                onFailure = { exception ->
                    _authState.value = AuthState(error = exception.message ?: getString(R.string.error_password_reset_failed))
                }
            )
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState()
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}

