package com.booking.worktracker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booking.worktracker.data.models.User
import com.booking.worktracker.domain.usecases.GetCurrentUserUseCase
import com.booking.worktracker.domain.usecases.SignInUseCase
import com.booking.worktracker.domain.usecases.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import java.util.prefs.Preferences

data class SignInUiState(
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@Inject
class SignInViewModel(
    private val signInUseCase: SignInUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    private val prefs = Preferences.userNodeForPackage(SignInViewModel::class.java)
    private var currentToken: String? = null

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val savedToken = prefs.get("session_token", null)
            if (savedToken != null) {
                val authState = getCurrentUserUseCase(savedToken)
                if (authState != null) {
                    currentToken = authState.token
                    _uiState.value = SignInUiState(
                        isAuthenticated = true,
                        currentUser = authState.user
                    )
                } else {
                    prefs.remove("session_token")
                }
            }
        }
    }

    fun signIn(email: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = signInUseCase(email, password, rememberMe)) {
                is SignInUseCase.Result.Success -> {
                    currentToken = result.authResult.session.token
                    if (rememberMe) {
                        prefs.put("session_token", result.authResult.session.token)
                    }
                    _uiState.value = SignInUiState(
                        isAuthenticated = true,
                        currentUser = result.authResult.user
                    )
                }
                is SignInUseCase.Result.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            currentToken?.let { signOutUseCase(it) }
            prefs.remove("session_token")
            currentToken = null
            _uiState.value = SignInUiState()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
