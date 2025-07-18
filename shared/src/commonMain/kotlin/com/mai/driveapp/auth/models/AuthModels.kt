package com.mai.driveapp.auth.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestVerificationRequest(
    val phoneNumber: String,
    val userType: String = "passenger" // "passenger" or "driver"
)

@Serializable
data class RequestVerificationResponse(
    val message: String,
    val code: String,
    val verification_code: String,
    val notification_text: String,
    val success: Boolean,
    val phoneNumber: String,
    val userType: String
)

@Serializable
data class VerifyCodeRequest(
    val phoneNumber: String,
    val code: String
)

/**
 * مدل پاسخ برای تایید کد
 * This model matches the backend response structure
 */
@Serializable
data class VerifyCodeResponse(
    val success: Boolean,
    val message: String,
    val token: String = "",
    val userId: String = "",
    val isNewUser: Boolean = false,
    val user: UserDto? = null,
    val userType: String = "passenger",
    val requiresRegistration: Boolean = false
)

@Serializable
data class CompleteRegistrationRequest(
    val userId: String,
    val phoneNumber: String,
    val fullName: String,
    val email: String? = null
)

@Serializable
data class CompleteRegistrationResponse(
    val message: String,
    val passengerId: String,
    val userId: String,
    val success: Boolean
)

@Serializable
data class UserDto(
    val id: String,
    val phoneNumber: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val isPhoneVerified: Boolean = false
)

@Serializable
data class ErrorResponse(
    val message: String,
    val success: Boolean = false
) 