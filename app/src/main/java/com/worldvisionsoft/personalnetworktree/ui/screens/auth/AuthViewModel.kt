package com.worldvisionsoft.personalnetworktree.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldvisionsoft.personalnetworktree.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.fold

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            val result = authRepository.signUp(email, password)

            result.fold(
                onSuccess = {
                    _authState.value = AuthState(isSuccess = true)
                },
                onFailure = { exception ->
                    _authState.value = AuthState(error = exception.message ?: "Sign up failed")
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
                    _authState.value = AuthState(error = exception.message ?: "Sign in failed")
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
                    _authState.value = AuthState(error = "Password reset email sent!")
                },
                onFailure = { exception ->
                    _authState.value = AuthState(error = exception.message ?: "Password reset failed")
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

