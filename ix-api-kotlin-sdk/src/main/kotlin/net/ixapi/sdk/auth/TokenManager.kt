package net.ixapi.sdk.auth

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import net.ixapi.sdk.models.AuthToken
import java.util.Base64

/**
 * Manages authentication tokens for the IX-API SDK
 */
class TokenManager(
    private val onTokenRefresh: suspend () -> AuthToken
) {
    private val mutex = Mutex()
    private var currentToken: AuthToken? = null
    private var accessTokenExpiry: Long = 0
    private var refreshTokenExpiry: Long = 0
    
    /**
     * Set the current authentication token
     */
    suspend fun setToken(token: AuthToken) {
        mutex.withLock {
            currentToken = token
            accessTokenExpiry = parseTokenExpiry(token.accessToken)
            refreshTokenExpiry = parseTokenExpiry(token.refreshToken)
        }
    }
    
    /**
     * Get a valid access token, refreshing if necessary
     */
    suspend fun getAccessToken(): String? {
        return mutex.withLock {
            val token = currentToken ?: return@withLock null
            
            // Check if access token is still valid (with 60 second buffer)
            val now = System.currentTimeMillis() / 1000
            if (accessTokenExpiry > now + 60) {
                return@withLock token.accessToken
            }
            
            // Check if we can refresh
            if (refreshTokenExpiry > now + 60) {
                try {
                    val newToken = onTokenRefresh()
                    currentToken = newToken
                    accessTokenExpiry = parseTokenExpiry(newToken.accessToken)
                    refreshTokenExpiry = parseTokenExpiry(newToken.refreshToken)
                    return@withLock newToken.accessToken
                } catch (e: Exception) {
                    // Refresh failed, return current token anyway
                    return@withLock token.accessToken
                }
            }
            
            // Both tokens expired
            null
        }
    }
    
    /**
     * Get the current refresh token
     */
    suspend fun getRefreshToken(): String? {
        return mutex.withLock {
            currentToken?.refreshToken
        }
    }
    
    /**
     * Check if we have a valid token
     */
    suspend fun hasValidToken(): Boolean {
        return mutex.withLock {
            val token = currentToken ?: return@withLock false
            val now = System.currentTimeMillis() / 1000
            accessTokenExpiry > now || refreshTokenExpiry > now
        }
    }
    
    /**
     * Check if the access token needs refresh
     */
    suspend fun needsRefresh(): Boolean {
        return mutex.withLock {
            currentToken ?: return@withLock true
            val now = System.currentTimeMillis() / 1000
            accessTokenExpiry <= now + 60 && refreshTokenExpiry > now + 60
        }
    }
    
    /**
     * Clear the current token
     */
    suspend fun clearToken() {
        mutex.withLock {
            currentToken = null
            accessTokenExpiry = 0
            refreshTokenExpiry = 0
        }
    }
    
    /**
     * Parse JWT token to extract expiry time
     */
    private fun parseTokenExpiry(token: String): Long {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return 0
            
            val payload = String(Base64.getUrlDecoder().decode(parts[1]))
            val json = Json.parseToJsonElement(payload).jsonObject
            json["exp"]?.jsonPrimitive?.long ?: 0
        } catch (e: Exception) {
            0
        }
    }
}

/**
 * Credentials for IX-API authentication
 */
data class IxApiCredentials(
    val apiKey: String,
    val apiSecret: String
)

/**
 * OAuth2 credentials
 */
data class OAuth2Credentials(
    val clientId: String,
    val clientSecret: String,
    val tokenUrl: String,
    val scope: String = "ix-api"
)
