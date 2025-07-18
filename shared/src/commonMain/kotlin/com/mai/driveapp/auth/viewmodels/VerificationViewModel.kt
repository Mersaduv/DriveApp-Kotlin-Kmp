package com.mai.driveapp.auth.viewmodels

import com.mai.driveapp.auth.AuthRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the verification code input screen
 */
class VerificationViewModel(private val authRepository: AuthRepository) : ViewModel() {
    // UI state
    private val _uiState = MutableStateFlow<VerificationUiState>(VerificationUiState.Initial)
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()
    
    // Input state
    private val _verificationCode = MutableStateFlow("")
    val verificationCode: StateFlow<String> = _verificationCode.asStateFlow()
    
    // Navigation trigger for direct instruction to navigate
    private val _shouldNavigateToRegistration = MutableStateFlow(false)
    val shouldNavigateToRegistration: StateFlow<Boolean> = _shouldNavigateToRegistration.asStateFlow()
    
    /**
     * Update the verification code input
     */
    fun updateVerificationCode(code: String) {
        _verificationCode.value = code
    }
    
    /**
     * Submit the verification code to verify
     */
    fun submitVerificationCode() {
        val code = _verificationCode.value.trim()
        
        if (code.isEmpty()) {
            _uiState.value = VerificationUiState.Error("لطفاً کد تایید را وارد کنید.")
            return
        }
        
        _uiState.value = VerificationUiState.Loading
        
        viewModelScope.launch {
            try {
                val result = authRepository.verifyCode(code)
                
                result.fold(
                    onSuccess = { requiresRegistration ->
                        _uiState.value = VerificationUiState.Success
                        println("[VerificationViewModel] requiresRegistration=$requiresRegistration")
                        // Only navigate to registration if required
                        _shouldNavigateToRegistration.value = requiresRegistration
                    },
                    onFailure = { error ->
                        _uiState.value = VerificationUiState.Error(error.message ?: "خطایی رخ داد. لطفاً دوباره تلاش کنید.")
                        _shouldNavigateToRegistration.value = false
                    }
                )
            } catch (e: Exception) {
                _uiState.value = VerificationUiState.Error(e.message ?: "خطایی رخ داد. لطفاً دوباره تلاش کنید.")
                _shouldNavigateToRegistration.value = false
            }
        }
    }
    
    /**
     * Force navigation to registration screen
     */
    fun forceNavigateToRegistration() {
        _shouldNavigateToRegistration.value = true
    }
    
    /**
     * Reset the UI state back to initial
     */
    fun resetState() {
        _uiState.value = VerificationUiState.Initial
        _shouldNavigateToRegistration.value = false
    }
    
    /**
     * Reset navigation flag after navigation
     */
    fun onNavigationComplete() {
        _shouldNavigateToRegistration.value = false
    }
}

/**
 * UI states for verification code screen
 */
sealed class VerificationUiState {
    object Initial : VerificationUiState()
    object Loading : VerificationUiState()
    object Success : VerificationUiState()
    data class Error(val message: String) : VerificationUiState()
} 