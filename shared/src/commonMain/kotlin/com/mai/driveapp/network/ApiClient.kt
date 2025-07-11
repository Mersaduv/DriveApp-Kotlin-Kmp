package com.mai.driveapp.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * API Client for handling network requests to the backend
 */
class ApiClient(
    private val baseUrl: String,
    private val tokenProvider: TokenProvider? = null
) {
    val client = HttpClient {
        // Setup JSON serialization
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        
        // Configure logging
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL // Log everything for debugging
        }
        
        // Configure default request parameters
        install(DefaultRequest) {
            // Set the base URL for all requests
            url {
                // Parse the baseUrl properly
                val fullUrl = if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
                    "http://$baseUrl"
                } else {
                    baseUrl
                }
                
                try {
                    val urlObj = URLBuilder(fullUrl)
                    protocol = urlObj.protocol
                    host = urlObj.host
                    port = urlObj.port
                    
                    // Add api path to the base URL
                    path("api/")
                } catch (e: Exception) {
                    // Fallback for older method in case URLBuilder fails
                    val urlPattern = "^(https?)://([^:/]+)(?::([0-9]+))?.*$".toRegex()
                    val match = urlPattern.matchEntire(fullUrl)
                    
                    if (match != null) {
                        val (proto, hostName, portStr) = match.destructured
                        protocol = if (proto == "https") URLProtocol.HTTPS else URLProtocol.HTTP
                        host = hostName
                        if (!portStr.isNullOrEmpty()) {
                            port = portStr.toInt()
                        }
                    } else {
                        // Default to HTTP protocol and parse as host:port
                        protocol = URLProtocol.HTTP
                        val hostPort = fullUrl.replace("http://", "").replace("https://", "").split(":")
                        host = hostPort[0]
                        if (hostPort.size > 1) {
                            port = hostPort[1].toInt()
                        }
                    }
                    
                    path("api/")
                }
            }
            
            // Add authorization header if token is available
            headers {
                tokenProvider?.getToken()?.let { token ->
                    if (token.isNotEmpty()) {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
            }
            
            // Set default content type
            contentType(ContentType.Application.Json)
        }
    }
}

/**
 * Interface for providing authentication token
 */
interface TokenProvider {
    fun getToken(): String?
}

/**
 * Implementation of TokenProvider that stores and retrieves token
 */
class DefaultTokenProvider : TokenProvider {
    private var token: String? = null
    
    fun saveToken(newToken: String) {
        token = newToken
    }
    
    fun clearToken() {
        token = null
    }
    
    override fun getToken(): String? = token
} 