package com.booking.worktracker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booking.worktracker.domain.usecases.SignUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

data class SignUpUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@Inject
class SignUpViewModel(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun signUp(
        email: String,
        displayName: String,
        password: String,
        confirmPassword: String,
        onSuccess: (email: String, password: String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = signUpUseCase(email, displayName, password, confirmPassword)) {
                is SignUpUseCase.Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess(email, password)
                }
                is SignUpUseCase.Result.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
