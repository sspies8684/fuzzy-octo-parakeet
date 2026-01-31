package net.ixapi.sdk.models

import kotlinx.serialization.*

/**
 * Authentication token response
 */
@Serializable
data class AuthToken(
    @SerialName("access_token")
    val accessToken: String,
    
    @SerialName("refresh_token")
    val refreshToken: String
)

/**
 * Request to create an authentication token
 */
@Serializable
data class AuthTokenRequest(
    @SerialName("api_key")
    val apiKey: String,
    
    @SerialName("api_secret")
    val apiSecret: String
)

/**
 * Request to refresh an authentication token
 */
@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)
