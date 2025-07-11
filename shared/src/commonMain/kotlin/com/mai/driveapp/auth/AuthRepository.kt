package com.mai.driveapp.auth

import com.mai.driveapp.auth.models.VerifyCodeResponse
import com.mai.driveapp.auth.models.UserDto

/**
 * AuthRepository manages authentication operations and coordinates 
 * between AuthService and SessionManager
 */
class AuthRepository(
    private val authService: AuthService,
    private val sessionManager: SessionManager
) {
    /**
     * Request a verification code for the given phone number
     * 
     * @param phoneNumber The user's phone number
     * @param userType The type of user ("passenger" or "driver")
     * @return A result containing success or failure
     */
    suspend fun requestVerificationCode(phoneNumber: String, userType: String = "passenger"): Result<String> {
        // Validate phone number format
        val formattedPhone = formatPhoneNumber(phoneNumber)
        if (!isValidPhoneNumber(formattedPhone)) {
            return Result.failure(Exception("شماره تلفن نامعتبر است. لطفاً شماره تلفن صحیح وارد کنید."))
        }
        
        return try {
            val response = authService.requestVerificationCode(formattedPhone, userType).getOrThrow()
            
            if (response.success) {
                // Save the phone number for verification
                sessionManager.saveVerificationData(formattedPhone, null)
                Result.success(response.verification_code)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verify the code that was sent to the user's phone
     * 
     * @param code The verification code
     * @return A result indicating success or failure
     */
    suspend fun verifyCode(code: String): Result<Boolean> {
        val phoneNumber = sessionManager.getVerificationPhoneNumber()
            ?: return Result.failure(Exception("شماره تلفن یافت نشد. لطفاً دوباره تلاش کنید."))
            
        // Validate code format
        if (!isValidVerificationCode(code)) {
            return Result.failure(Exception("کد تایید نامعتبر است. لطفاً کد 6 رقمی را وارد کنید."))
        }
        
        return try {
            val response = authService.verifyCode(phoneNumber, code).getOrThrow()
            
            if (response.success) {
                // Store auth data in session manager
                sessionManager.saveAuthData(
                    response.token,
                    response.user,
                    response.userType
                )
                
                // Store verification data for registration completion
                sessionManager.saveVerificationData(phoneNumber, response.userId)
                
                // Return whether registration is required
                Result.success(response.requiresRegistration)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Complete the registration by providing user details
     * 
     * @param firstName User's first name
     * @param lastName User's last name (optional)
     * @param email User's email (optional)
     * @return A result indicating success or failure
     */
    suspend fun completeRegistration(
        firstName: String,
        lastName: String? = null,
        email: String? = null
    ): Result<Boolean> {
        val userId = sessionManager.getVerificationUserId()
            ?: return Result.failure(Exception("اطلاعات کاربر یافت نشد. لطفاً دوباره وارد شوید."))
            
        val phoneNumber = sessionManager.getVerificationPhoneNumber()
            ?: return Result.failure(Exception("شماره تلفن یافت نشد. لطفاً دوباره وارد شوید."))
        
        // Validate first name
        if (firstName.isBlank()) {
            return Result.failure(Exception("نام نمی‌تواند خالی باشد."))
        }
        
        return try {
            val response = authService.completeRegistration(
                userId = userId,
                phoneNumber = phoneNumber,
                firstName = firstName,
                lastName = lastName,
                email = email
            ).getOrThrow()
            
            Result.success(response.success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get the current authentication token
     * 
     * @return The authentication token or null if not authenticated
     */
    fun getToken(): String? {
        return sessionManager.getToken()
    }
    
    /**
     * Format phone number to the standard format
     * 
     * @param phoneNumber The input phone number
     * @return Formatted phone number with country code
     */
    private fun formatPhoneNumber(phoneNumber: String): String {
        val digitsOnly = phoneNumber.replace(Regex("[^0-9+]"), "")
        
        return when {
            digitsOnly.startsWith("+93") -> digitsOnly
            digitsOnly.startsWith("93") -> "+$digitsOnly"
            digitsOnly.startsWith("0") -> "+93${digitsOnly.substring(1)}"
            else -> "+93$digitsOnly"
        }
    }
    
    /**
     * Validate if the phone number format is correct
     * 
     * @param phoneNumber The phone number to validate
     * @return True if valid, false otherwise
     */
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Check if phone number has the correct format (Afghanistan number)
        // +93 followed by 9-10 digits
        return phoneNumber.matches(Regex("^\\+93[0-9]{9,10}$"))
    }
    
    /**
     * Validate if the verification code format is correct
     * 
     * @param code The verification code to validate
     * @return True if valid, false otherwise
     */
    private fun isValidVerificationCode(code: String): Boolean {
        // Verify code is 6 digits
        return code.matches(Regex("^[0-9]{6}$"))
    }
    
    /**
     * Log out the current user
     */
    fun logout() {
        sessionManager.logout()
    }
} 