package com.mai.driveapp.auth

import com.mai.driveapp.auth.models.*
import com.mai.driveapp.network.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class AuthService(private val apiClient: ApiClient) {
    
    // Configure JSON parser to be lenient
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true // Handle missing fields
    }
    
    /**
     * Request a verification code to be sent to the user's phone
     */
    suspend fun requestVerificationCode(
        phoneNumber: String,
        userType: String = "passenger"
    ): Result<RequestVerificationResponse> {
        return try {
            val request = RequestVerificationRequest(phoneNumber, userType)
            val response = apiClient.client.post {
                url("auth/custom/request-code")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            if (response.status.isSuccess()) {
                val responseText = response.bodyAsText()
                println("Request verification response: $responseText") // Debug log
                val responseObj = json.decodeFromString<RequestVerificationResponse>(responseText)
                Result.success(responseObj)
            } else {
                val error = response.bodyAsText()
                println("Request verification error: $error") // Debug log
                val errorObj = json.decodeFromString<ErrorResponse>(error)
                Result.failure(Exception(errorObj.message))
            }
        } catch (e: Exception) {
            println("Request verification exception: ${e.message}") // Debug log
            Result.failure(e)
        }
    }
    
    /**
     * Verify the code that was sent to the user's phone
     */
    suspend fun verifyCode(
        phoneNumber: String,
        code: String
    ): Result<VerifyCodeResponse> {
        return try {
            val request = VerifyCodeRequest(phoneNumber, code)
            val response = apiClient.client.post {
                url("auth/custom/verify-code")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            if (response.status.isSuccess()) {
                val responseText = response.bodyAsText()
                println("Verify code response: $responseText") // Debug log
                val responseObj = json.decodeFromString<VerifyCodeResponse>(responseText)
                Result.success(responseObj)
            } else {
                val error = response.bodyAsText()
                println("Verify code error: $error") // Debug log
                val errorObj = json.decodeFromString<ErrorResponse>(error)
                Result.failure(Exception(errorObj.message))
            }
        } catch (e: Exception) {
            println("Verify code exception: ${e.message}") // Debug log
            Result.failure(e)
        }
    }
    
    /**
     * Complete the registration process by providing user details
     */
    suspend fun completeRegistration(
        userId: String,
        phoneNumber: String,
        firstName: String,
        lastName: String? = null,
        email: String? = null
    ): Result<CompleteRegistrationResponse> {
        return try {
            val request = CompleteRegistrationRequest(
                userId = userId,
                phoneNumber = phoneNumber,
                firstName = firstName,
                lastName = lastName,
                email = email
            )
            
            val response = apiClient.client.post {
                url("auth/custom/complete-registration")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            if (response.status.isSuccess()) {
                val responseText = response.bodyAsText()
                println("Complete registration response: $responseText") // Debug log
                val responseObj = json.decodeFromString<CompleteRegistrationResponse>(responseText)
                Result.success(responseObj)
            } else {
                val error = response.bodyAsText()
                println("Complete registration error: $error") // Debug log
                val errorObj = json.decodeFromString<ErrorResponse>(error)
                Result.failure(Exception(errorObj.message))
            }
        } catch (e: Exception) {
            println("Complete registration exception: ${e.message}") // Debug log
            Result.failure(e)
        }
    }
} 