package net.ixapi.sdk.client

import net.ixapi.sdk.auth.IxApiCredentials
import net.ixapi.sdk.auth.OAuth2Credentials

/**
 * Configuration for the IX-API client
 */
data class IxApiConfig(
    /**
     * Base URL of the IX-API server (e.g., "https://api.example-ix.net")
     */
    val baseUrl: String,
    
    /**
     * API path prefix (defaults to "/api/v2")
     */
    val apiPath: String = "/api/v2",
    
    /**
     * IX-API credentials (API key/secret)
     */
    val credentials: IxApiCredentials? = null,
    
    /**
     * OAuth2 credentials (alternative to API key/secret)
     */
    val oauthCredentials: OAuth2Credentials? = null,
    
    /**
     * Pre-existing access token (optional)
     */
    val accessToken: String? = null,
    
    /**
     * Request timeout in milliseconds
     */
    val timeout: Long = 30_000,
    
    /**
     * Connection timeout in milliseconds
     */
    val connectTimeout: Long = 10_000,
    
    /**
     * Enable debug logging
     */
    val debug: Boolean = false,
    
    /**
     * Custom headers to include in all requests
     */
    val customHeaders: Map<String, String> = emptyMap(),
    
    /**
     * User agent string
     */
    val userAgent: String = "IX-API-Kotlin-SDK/2.7.1",
    
    /**
     * Maximum number of retries for failed requests
     */
    val maxRetries: Int = 3,
    
    /**
     * Initial retry delay in milliseconds
     */
    val retryDelay: Long = 1000,
    
    /**
     * Enable automatic token refresh
     */
    val autoRefreshToken: Boolean = true
) {
    init {
        require(baseUrl.isNotBlank()) { "Base URL must not be blank" }
        require(timeout > 0) { "Timeout must be positive" }
        require(connectTimeout > 0) { "Connect timeout must be positive" }
    }
    
    /**
     * Get the full API URL
     */
    val apiUrl: String
        get() = baseUrl.trimEnd('/') + apiPath
    
    companion object {
        /**
         * Create a configuration with API key authentication
         */
        fun withApiKey(
            baseUrl: String,
            apiKey: String,
            apiSecret: String,
            apiPath: String = "/api/v2"
        ): IxApiConfig = IxApiConfig(
            baseUrl = baseUrl,
            apiPath = apiPath,
            credentials = IxApiCredentials(apiKey, apiSecret)
        )
        
        /**
         * Create a configuration with OAuth2 authentication
         */
        fun withOAuth2(
            baseUrl: String,
            clientId: String,
            clientSecret: String,
            tokenUrl: String,
            apiPath: String = "/api/v2"
        ): IxApiConfig = IxApiConfig(
            baseUrl = baseUrl,
            apiPath = apiPath,
            oauthCredentials = OAuth2Credentials(clientId, clientSecret, tokenUrl)
        )
        
        /**
         * Create a configuration with a pre-existing access token
         */
        fun withAccessToken(
            baseUrl: String,
            accessToken: String,
            apiPath: String = "/api/v2"
        ): IxApiConfig = IxApiConfig(
            baseUrl = baseUrl,
            apiPath = apiPath,
            accessToken = accessToken
        )
    }
}
