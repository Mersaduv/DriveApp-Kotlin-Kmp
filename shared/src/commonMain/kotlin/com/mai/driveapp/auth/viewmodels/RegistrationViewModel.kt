package com.mai.driveapp.auth.viewmodels

import com.mai.driveapp.auth.AuthRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the registration completion screen
 */
class RegistrationViewModel(private val authRepository: AuthRepository) : ViewModel() {
    // UI state
    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Initial)
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()
    
    // Form state
    private val _firstName = MutableStateFlow("")
    val firstName: StateFlow<String> = _firstName.asStateFlow()
    
    private val _lastName = MutableStateFlow("")
    val lastName: StateFlow<String> = _lastName.asStateFlow()
    
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    /**
     * Update first name input
     */
    fun updateFirstName(name: String) {
        _firstName.value = name
    }
    
    /**
     * Update last name input
     */
    fun updateLastName(name: String) {
        _lastName.value = name
    }
    
    /**
     * Update email input
     */
    fun updateEmail(email: String) {
        _email.value = email
    }
    
    /**
     * Submit the registration form
     */
    fun submitRegistration() {
        val firstName = _firstName.value.trim()
        
        if (firstName.isEmpty()) {
            _uiState.value = RegistrationUiState.Error("نام نمی‌تواند خالی باشد.")
            return
        }
        
        _uiState.value = RegistrationUiState.Loading
        
        viewModelScope.launch {
            val result = authRepository.completeRegistration(
                firstName = firstName,
                lastName = _lastName.value.trim().takeIf { it.isNotEmpty() },
                email = _email.value.trim().takeIf { it.isNotEmpty() && isValidEmail(it) }
            )
            
            result.fold(
                onSuccess = { success ->
                    if (success) {
                        _uiState.value = RegistrationUiState.Success
                    } else {
                        _uiState.value = RegistrationUiState.Error("ثبت اطلاعات با خطا مواجه شد.")
                    }
                },
                onFailure = { error ->
                    _uiState.value = RegistrationUiState.Error(error.message ?: "خطایی رخ داد. لطفاً دوباره تلاش کنید.")
                }
            )
        }
    }
    
    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return email.matches(Regex(emailRegex))
    }
    
    /**
     * Reset the UI state back to initial
     */
    fun resetState() {
        _uiState.value = RegistrationUiState.Initial
    }
}

/**
 * UI states for registration screen
 */
sealed class RegistrationUiState {
    object Initial : RegistrationUiState()
    object Loading : RegistrationUiState()
    object Success : RegistrationUiState()
    data class Error(val message: String) : RegistrationUiState()
} 