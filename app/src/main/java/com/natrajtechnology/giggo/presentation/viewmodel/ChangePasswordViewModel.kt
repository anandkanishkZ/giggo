package com.natrajtechnology.giggo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.natrajtechnology.giggo.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isFormValid: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordChanged: Boolean = false
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()
    
    init {
        // Combine all form fields to validate form
        viewModelScope.launch {
            combine(
                _uiState
            ) { states ->
                val state = states[0]
                validateForm(state)
            }.collect { updatedState ->
                _uiState.value = updatedState
            }
        }
    }
    
    fun updateCurrentPassword(password: String) {
        _uiState.value = _uiState.value.copy(
            currentPassword = password,
            currentPasswordError = validateCurrentPassword(password),
            errorMessage = null
        )
    }
    
    fun updateNewPassword(password: String) {
        _uiState.value = _uiState.value.copy(
            newPassword = password,
            newPasswordError = validateNewPassword(password),
            confirmPasswordError = if (_uiState.value.confirmPassword.isNotEmpty()) {
                validateConfirmPassword(password, _uiState.value.confirmPassword)
            } else null,
            errorMessage = null
        )
    }
    
    fun updateConfirmPassword(password: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = password,
            confirmPasswordError = validateConfirmPassword(_uiState.value.newPassword, password),
            errorMessage = null
        )
    }
    
    fun changePassword() {
        if (!_uiState.value.isFormValid) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                val user = firebaseAuth.currentUser
                if (user?.email == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User not found. Please log in again."
                    )
                    return@launch
                }
                
                // Re-authenticate user with current password
                val credential = EmailAuthProvider.getCredential(
                    user.email!!,
                    _uiState.value.currentPassword
                )
                
                user.reauthenticate(credential).await()
                
                // Update password
                user.updatePassword(_uiState.value.newPassword).await()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isPasswordChanged = true,
                    errorMessage = null
                )
                
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("wrong-password") == true ||
                    e.message?.contains("invalid-credential") == true -> 
                        "Current password is incorrect"
                    e.message?.contains("weak-password") == true -> 
                        "New password is too weak"
                    e.message?.contains("network") == true -> 
                        "Network error. Please check your connection."
                    else -> "Failed to change password: ${e.message}"
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
        }
    }
    
    private fun validateCurrentPassword(password: String): String? {
        return when {
            password.isBlank() -> "Current password is required"
            else -> null
        }
    }
    
    private fun validateNewPassword(password: String): String? {
        return when {
            password.isBlank() -> "New password is required"
            password.length < 8 -> "Password must be at least 8 characters"
            !password.any { it.isUpperCase() } -> "Password must contain at least one uppercase letter"
            !password.any { it.isLowerCase() } -> "Password must contain at least one lowercase letter"
            !password.any { it.isDigit() } -> "Password must contain at least one number"
            else -> null
        }
    }
    
    private fun validateConfirmPassword(newPassword: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Please confirm your new password"
            newPassword != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }
    
    private fun validateForm(state: ChangePasswordUiState): ChangePasswordUiState {
        val isValid = state.currentPasswordError == null &&
                state.newPasswordError == null &&
                state.confirmPasswordError == null &&
                state.currentPassword.isNotBlank() &&
                state.newPassword.isNotBlank() &&
                state.confirmPassword.isNotBlank()
        
        return state.copy(isFormValid = isValid)
    }
}
