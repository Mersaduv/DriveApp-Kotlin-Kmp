package com.mai.driveapp.android

import com.mai.driveapp.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Adapter class that provides a simplified interface for the Android app
 * to interact with the authentication system.
 */
class AuthService : KoinComponent {
    
    // Use the shared AuthRepository for API calls
    private val authRepository: AuthRepository by inject()
    
    /**
     * Send phone number and get verification code
     */
    suspend fun sendPhoneNumber(phoneNumber: String): PhoneNumberResponse = withContext(Dispatchers.IO) {
        // Use real API
        val result = authRepository.requestVerificationCode(phoneNumber)
        return@withContext result.fold(
            onSuccess = { code ->
                PhoneNumberResponse(
                    success = true,
                    message = "Verification code sent",
                    sessionId = phoneNumber // Use phone number as session ID for simplicity
                )
            },
            onFailure = { error ->
                PhoneNumberResponse(
                    success = false,
                    message = error.message ?: "Failed to send verification code",
                    sessionId = null
                )
            }
        )
    }
    
    /**
     * Verify the code
     */
    suspend fun verifyCode(sessionId: String, code: String): VerifyResponse = withContext(Dispatchers.IO) {
        // Use real API
        val result = authRepository.verifyCode(code)
        return@withContext result.fold(
            onSuccess = { requiresRegistration ->
                VerifyResponse(
                    success = true,
                    message = "Code verified successfully",
                    requiresRegistration = requiresRegistration
                )
            },
            onFailure = { error ->
                VerifyResponse(
                    success = false,
                    message = error.message ?: "Failed to verify code",
                    requiresRegistration = false
                )
            }
        )
    }
    
    /**
     * Create profile with user's name
     */
    suspend fun createProfile(sessionId: String, fullName: String): CreateProfileResponse = withContext(Dispatchers.IO) {
        // Use real API
        val result = authRepository.completeRegistration(
            fullName = fullName
        )
        
        return@withContext result.fold(
            onSuccess = { success ->
                CreateProfileResponse(
                    success = success,
                    message = "Profile created successfully",
                    token = authRepository.getToken(),
                    expiresIn = 604800 // 7 days in seconds
                )
            },
            onFailure = { error ->
                CreateProfileResponse(
                    success = false,
                    message = error.message ?: "Failed to create profile",
                    token = null,
                    expiresIn = 0
                )
            }
        )
    }
}

/**
 * Response for phone number verification request
 */
data class PhoneNumberResponse(
    val success: Boolean,
    val message: String,
    val sessionId: String? = null
)

/**
 * Response for code verification
 */
data class VerifyResponse(
    val success: Boolean,
    val message: String,
    val requiresRegistration: Boolean = false
)

/**
 * Response for profile creation
 */
data class CreateProfileResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val expiresIn: Int = 0
) 