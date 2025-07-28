package com.natrajtechnology.giggo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.natrajtechnology.giggo.data.model.AuthResult
import com.natrajtechnology.giggo.data.model.AuthState
import com.natrajtechnology.giggo.data.model.LoginRequest
import com.natrajtechnology.giggo.data.model.SignUpRequest
import com.natrajtechnology.giggo.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _loginEmail = MutableStateFlow("")
    val loginEmail: StateFlow<String> = _loginEmail.asStateFlow()
    
    private val _loginPassword = MutableStateFlow("")
    val loginPassword: StateFlow<String> = _loginPassword.asStateFlow()
    
    private val _signUpFirstName = MutableStateFlow("")
    val signUpFirstName: StateFlow<String> = _signUpFirstName.asStateFlow()
    
    private val _signUpLastName = MutableStateFlow("")
    val signUpLastName: StateFlow<String> = _signUpLastName.asStateFlow()
    
    private val _signUpEmail = MutableStateFlow("")
    val signUpEmail: StateFlow<String> = _signUpEmail.asStateFlow()
    
    private val _signUpPassword = MutableStateFlow("")
    val signUpPassword: StateFlow<String> = _signUpPassword.asStateFlow()
    
    private val _signUpConfirmPassword = MutableStateFlow("")
    val signUpConfirmPassword: StateFlow<String> = _signUpConfirmPassword.asStateFlow()
    
    // Forgot password state
    private val _forgotPasswordEmail = MutableStateFlow("")
    val forgotPasswordEmail: StateFlow<String> = _forgotPasswordEmail.asStateFlow()
    
    private val _isPasswordResetSent = MutableStateFlow(false)
    val isPasswordResetSent: StateFlow<Boolean> = _isPasswordResetSent.asStateFlow()
    
    init {
        checkAuthStatus()
    }
    
    fun updateLoginEmail(email: String) {
        _loginEmail.value = email
        clearError()
    }
    
    fun updateLoginPassword(password: String) {
        _loginPassword.value = password
        clearError()
    }
    
    fun updateSignUpFirstName(firstName: String) {
        _signUpFirstName.value = firstName
        clearError()
    }
    
    fun updateSignUpLastName(lastName: String) {
        _signUpLastName.value = lastName
        clearError()
    }
    
    fun updateSignUpEmail(email: String) {
        _signUpEmail.value = email
        clearError()
    }
    
    fun updateSignUpPassword(password: String) {
        _signUpPassword.value = password
        clearError()
    }
    
    fun updateSignUpConfirmPassword(confirmPassword: String) {
        _signUpConfirmPassword.value = confirmPassword
        clearError()
    }
    
    fun updateForgotPasswordEmail(email: String) {
        _forgotPasswordEmail.value = email
        clearError()
    }
    
    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val request = LoginRequest(
                email = _loginEmail.value.trim(),
                password = _loginPassword.value
            )
            
            authRepository.login(request).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _authState.value = _authState.value.copy(isLoading = true, error = null)
                    }
                    is AuthResult.Success -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            error = null
                        )
                        // Reload user data
                        loadCurrentUser()
                        onSuccess()
                    }
                    is AuthResult.Error -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
    
    fun signUp(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val request = SignUpRequest(
                firstName = _signUpFirstName.value.trim(),
                lastName = _signUpLastName.value.trim(),
                email = _signUpEmail.value.trim(),
                password = _signUpPassword.value,
                confirmPassword = _signUpConfirmPassword.value
            )
            
            authRepository.signUp(request).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _authState.value = _authState.value.copy(isLoading = true, error = null)
                    }
                    is AuthResult.Success -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            error = null
                        )
                        // Reload user data
                        loadCurrentUser()
                        onSuccess()
                    }
                    is AuthResult.Error -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
    
    fun sendPasswordReset(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val email = _forgotPasswordEmail.value.trim()
            
            authRepository.resetPassword(email).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _authState.value = _authState.value.copy(isLoading = true, error = null)
                    }
                    is AuthResult.Success -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = null
                        )
                        _isPasswordResetSent.value = true
                        onSuccess()
                    }
                    is AuthResult.Error -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
    
    fun resendPasswordReset() {
        _isPasswordResetSent.value = false
        sendPasswordReset { }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState()
            _isPasswordResetSent.value = false
            clearAllFields()
        }
    }
    
    fun reloadUser() {
        viewModelScope.launch {
            authRepository.reloadUser()
            loadCurrentUser()
        }
    }
    
    fun sendEmailVerification() {
        viewModelScope.launch {
            authRepository.sendEmailVerification()
        }
    }
    
    fun updateUserProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            try {
                val success = authRepository.updateUserProfile(firstName, lastName, phoneNumber)
                if (success) {
                    // Reload user data to reflect changes
                    loadCurrentUser()
                    _authState.value = _authState.value.copy(isLoading = false)
                    onSuccess()
                } else {
                    _authState.value = _authState.value.copy(isLoading = false)
                    onError("Failed to update profile")
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false)
                onError(e.message ?: "Failed to update profile")
            }
        }
    }
    
    private fun checkAuthStatus() {
        viewModelScope.launch {
            val isSignedIn = authRepository.isUserSignedIn()
            if (isSignedIn) {
                loadCurrentUser()
            } else {
                _authState.value = _authState.value.copy(isAuthenticated = false)
            }
        }
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _authState.value = _authState.value.copy(
                isAuthenticated = user != null,
                user = user
            )
        }
    }
    
    private fun clearError() {
        if (_authState.value.error != null) {
            _authState.value = _authState.value.copy(error = null)
        }
    }
    
    private fun clearAllFields() {
        _loginEmail.value = ""
        _loginPassword.value = ""
        _signUpFirstName.value = ""
        _signUpLastName.value = ""
        _signUpEmail.value = ""
        _signUpPassword.value = ""
        _signUpConfirmPassword.value = ""
        _forgotPasswordEmail.value = ""
    }
    
    fun resetPasswordResetState() {
        _isPasswordResetSent.value = false
        _forgotPasswordEmail.value = ""
    }
}
