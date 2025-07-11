package com.mai.driveapp.auth

import com.mai.driveapp.auth.models.UserDto
import com.mai.driveapp.network.DefaultTokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SessionManager manages user authentication state and user data
 */
class SessionManager(private val tokenProvider: DefaultTokenProvider) {
    private val _isLoggedIn = MutableStateFlow(false)
    private val _currentUser = MutableStateFlow<UserDto?>(null)
    private val _userType = MutableStateFlow<String?>(null)
    private val _needsRegistration = MutableStateFlow(false)
    
    // Public immutable flows
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()
    val userType: StateFlow<String?> = _userType.asStateFlow()
    val needsRegistration: StateFlow<Boolean> = _needsRegistration.asStateFlow()
    
    // Temporary verification state
    private var verificationPhoneNumber: String? = null
    private var verificationUserId: String? = null
    
    init {
        // Check if we have a saved token
        checkLoginStatus()
    }
    
    /**
     * Checks if the user is logged in based on token availability
     */
    private fun checkLoginStatus() {
        val hasToken = tokenProvider.getToken() != null
        _isLoggedIn.value = hasToken
        
        // اگر توکن داریم اما اطلاعات کاربر کامل نیست، نیاز به تکمیل ثبت‌نام داریم
        _needsRegistration.value = hasToken && _currentUser.value?.firstName == null
    }
    
    /**
     * Saves authentication data after successful login
     */
    fun saveAuthData(token: String, user: UserDto?, userType: String) {
        tokenProvider.saveToken(token)
        _currentUser.value = user
        _userType.value = userType
        _isLoggedIn.value = true
        
        // تعیین وضعیت نیاز به تکمیل ثبت‌نام
        _needsRegistration.value = (user == null || user.firstName == null)
    }
    
    /**
     * Gets the current authentication token
     * 
     * @return The authentication token or null if not authenticated
     */
    fun getToken(): String? {
        return tokenProvider.getToken()
    }
    
    /**
     * Saves verification data for the registration process
     */
    fun saveVerificationData(phoneNumber: String, userId: String?) {
        this.verificationPhoneNumber = phoneNumber
        if (userId != null) {
            this.verificationUserId = userId
            // نیاز به تکمیل ثبت‌نام داریم
            _needsRegistration.value = true
        }
    }
    
    /**
     * بررسی می‌کند آیا کاربر نیاز به تکمیل ثبت‌نام دارد یا خیر
     */
    fun requiresRegistration(): Boolean {
        return _needsRegistration.value || verificationUserId != null
    }
    
    /**
     * Gets the saved verification phone number
     */
    fun getVerificationPhoneNumber(): String? = verificationPhoneNumber
    
    /**
     * Gets the saved user ID from verification
     */
    fun getVerificationUserId(): String? = verificationUserId
    
    /**
     * Updates user information after profile completion
     */
    fun updateUserInfo(user: UserDto) {
        _currentUser.value = user
        _needsRegistration.value = false
    }
    
    /**
     * تکمیل فرآیند ثبت‌نام
     */
    fun completeRegistration() {
        _needsRegistration.value = false
    }
    
    /**
     * Logs out the user
     */
    fun logout() {
        tokenProvider.clearToken()
        _currentUser.value = null
        _userType.value = null
        _isLoggedIn.value = false
        _needsRegistration.value = false
        verificationPhoneNumber = null
        verificationUserId = null
    }
} 