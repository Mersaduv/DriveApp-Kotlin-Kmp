package com.mai.driveapp.auth.viewmodels

import com.mai.driveapp.auth.AuthRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the phone number input screen
 */
class PhoneNumberViewModel(private val authRepository: AuthRepository) : ViewModel() {
    // UI state
    private val _uiState = MutableStateFlow<PhoneNumberUiState>(PhoneNumberUiState.Initial)
    val uiState: StateFlow<PhoneNumberUiState> = _uiState.asStateFlow()
    
    // Input state
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()
    
    // User type (passenger or driver)
    private val _userType = MutableStateFlow("passenger")
    val userType: StateFlow<String> = _userType.asStateFlow()
    
    /**
     * Update the phone number input
     */
    fun updatePhoneNumber(phone: String) {
        _phoneNumber.value = phone
    }
    
    /**
     * Set user type (passenger or driver)
     */
    fun setUserType(type: String) {
        if (type == "passenger" || type == "driver") {
            _userType.value = type
        }
    }
    
    /**
     * Submit the phone number to request verification code
     */
    fun submitPhoneNumber() {
        val phone = _phoneNumber.value.trim()
        
        if (phone.isEmpty()) {
            _uiState.value = PhoneNumberUiState.Error("لطفاً شماره تلفن خود را وارد کنید.")
            return
        }
        
        _uiState.value = PhoneNumberUiState.Loading
        
        viewModelScope.launch {
            val result = authRepository.requestVerificationCode(phone, _userType.value)
            
            result.fold(
                onSuccess = { code ->
                    // In production, the code would be sent via SMS
                    // For testing, we return it in the success state
                    _uiState.value = PhoneNumberUiState.Success(code)
                },
                onFailure = { error ->
                    _uiState.value = PhoneNumberUiState.Error(error.message ?: "خطایی رخ داد. لطفاً دوباره تلاش کنید.")
                }
            )
        }
    }
    
    /**
     * Reset the UI state back to initial
     */
    fun resetState() {
        _uiState.value = PhoneNumberUiState.Initial
    }
}

/**
 * UI states for phone number screen
 */
sealed class PhoneNumberUiState {
    object Initial : PhoneNumberUiState()
    object Loading : PhoneNumberUiState()
    data class Success(val verificationCode: String) : PhoneNumberUiState()
    data class Error(val message: String) : PhoneNumberUiState()
} 