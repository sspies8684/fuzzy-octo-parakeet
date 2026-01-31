package net.ixapi.sdk.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import net.ixapi.sdk.auth.TokenManager
import net.ixapi.sdk.exceptions.*
import net.ixapi.sdk.models.AuthToken
import net.ixapi.sdk.models.AuthTokenRequest
import net.ixapi.sdk.models.ProblemResponse
import net.ixapi.sdk.models.RefreshTokenRequest
import net.ixapi.sdk.pagination.PaginatedResponse
import net.ixapi.sdk.pagination.PaginationInfo
import org.slf4j.LoggerFactory

/**
 * HTTP client for IX-API requests
 */
class IxApiHttpClient(
    private val config: IxApiConfig
) {
    private val logger = LoggerFactory.getLogger(IxApiHttpClient::class.java)
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        explicitNulls = false
        coerceInputValues = true
    }
    
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        
        if (config.debug) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = config.timeout
            connectTimeoutMillis = config.connectTimeout
        }
        
        defaultRequest {
            config.customHeaders.forEach { (key, value) ->
                header(key, value)
            }
            header(HttpHeaders.UserAgent, config.userAgent)
        }
    }
    
    private var tokenManager: TokenManager? = null
    
    init {
        if (config.credentials != null && config.autoRefreshToken) {
            tokenManager = TokenManager {
                refreshToken()
            }
        }
    }
    
    /**
     * Authenticate with API key/secret and store the token
     */
    suspend fun authenticate(): AuthToken {
        val credentials = config.credentials
            ?: throw ConfigurationException("API credentials not configured")
        
        val response = httpClient.post("${config.apiUrl}/auth/token") {
            contentType(ContentType.Application.Json)
            setBody(AuthTokenRequest(credentials.apiKey, credentials.apiSecret))
        }
        
        handleErrorResponse(response)
        
        val token = response.body<AuthToken>()
        tokenManager?.setToken(token)
        return token
    }
    
    /**
     * Refresh the access token using the refresh token
     */
    suspend fun refreshToken(): AuthToken {
        val refreshToken = tokenManager?.getRefreshToken()
            ?: throw AuthenticationException("No refresh token available")
        
        val response = httpClient.post("${config.apiUrl}/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(refreshToken))
        }
        
        handleErrorResponse(response)
        
        val token = response.body<AuthToken>()
        tokenManager?.setToken(token)
        return token
    }
    
    /**
     * Perform a GET request
     */
    suspend inline fun <reified T> get(
        path: String,
        queryParams: Map<String, String?> = emptyMap()
    ): T {
        return executeWithRetry {
            val response = httpClient.get("${config.apiUrl}$path") {
                applyAuth()
                queryParams.forEach { (key, value) ->
                    value?.let { parameter(key, it) }
                }
            }
            handleErrorResponse(response)
            response.body()
        }
    }
    
    /**
     * Perform a GET request with pagination info
     */
    suspend inline fun <reified T> getWithPagination(
        path: String,
        queryParams: Map<String, String?> = emptyMap()
    ): PaginatedResponse<T> {
        return executeWithRetry {
            val response = httpClient.get("${config.apiUrl}$path") {
                applyAuth()
                queryParams.forEach { (key, value) ->
                    value?.let { parameter(key, it) }
                }
            }
            handleErrorResponse(response)
            
            val data: T = response.body()
            val paginationInfo = extractPaginationInfo(response)
            PaginatedResponse(data, paginationInfo)
        }
    }
    
    /**
     * Perform a POST request
     */
    suspend inline fun <reified T, reified R> post(
        path: String,
        body: T
    ): R {
        return executeWithRetry {
            val response = httpClient.post("${config.apiUrl}$path") {
                applyAuth()
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            handleErrorResponse(response)
            response.body()
        }
    }
    
    /**
     * Perform a POST request without response body
     */
    suspend inline fun <reified T> postNoResponse(
        path: String,
        body: T
    ) {
        executeWithRetry<Unit> {
            val response = httpClient.post("${config.apiUrl}$path") {
                applyAuth()
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            handleErrorResponse(response)
        }
    }
    
    /**
     * Perform a PUT request
     */
    suspend inline fun <reified T, reified R> put(
        path: String,
        body: T
    ): R {
        return executeWithRetry {
            val response = httpClient.put("${config.apiUrl}$path") {
                applyAuth()
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            handleErrorResponse(response)
            response.body()
        }
    }
    
    /**
     * Perform a PATCH request
     */
    suspend inline fun <reified T, reified R> patch(
        path: String,
        body: T
    ): R {
        return executeWithRetry {
            val response = httpClient.patch("${config.apiUrl}$path") {
                applyAuth()
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            handleErrorResponse(response)
            response.body()
        }
    }
    
    /**
     * Perform a DELETE request
     */
    suspend inline fun <reified R> delete(
        path: String
    ): R {
        return executeWithRetry {
            val response = httpClient.delete("${config.apiUrl}$path") {
                applyAuth()
            }
            handleErrorResponse(response)
            response.body()
        }
    }
    
    /**
     * Perform a DELETE request without response body
     */
    suspend fun deleteNoResponse(path: String) {
        executeWithRetry<Unit> {
            val response = httpClient.delete("${config.apiUrl}$path") {
                applyAuth()
            }
            handleErrorResponse(response)
        }
    }
    
    /**
     * Download binary content (e.g., LOA document)
     */
    suspend fun downloadBinary(path: String): ByteArray {
        return executeWithRetry {
            val response = httpClient.get("${config.apiUrl}$path") {
                applyAuth()
            }
            handleErrorResponse(response)
            response.body()
        }
    }
    
    /**
     * Apply authentication to the request
     */
    private suspend fun HttpRequestBuilder.applyAuth() {
        val token = when {
            config.accessToken != null -> config.accessToken
            tokenManager != null -> tokenManager?.getAccessToken()
            else -> null
        }
        
        token?.let {
            header(HttpHeaders.Authorization, "Bearer $it")
        }
    }
    
    /**
     * Extract pagination information from response headers
     */
    private fun extractPaginationInfo(response: HttpResponse): PaginationInfo? {
        val limit = response.headers["X-Pagination-Limit"]?.toIntOrNull()
        val offset = response.headers["X-Pagination-Offset"]?.toIntOrNull()
        val items = response.headers["X-Pagination-Items"]?.toIntOrNull()
        val pages = response.headers["X-Pagination-Pages"]?.toIntOrNull()
        val page = response.headers["X-Pagination-Page"]?.toIntOrNull()
        val token = response.headers["X-Pagination-Token"]
        val next = response.headers["X-Pagination-Next"]
        
        return if (limit != null || items != null) {
            PaginationInfo(
                limit = limit ?: 0,
                offset = offset ?: 0,
                totalItems = items ?: 0,
                totalPages = pages ?: 0,
                currentPage = page ?: 0,
                token = token,
                nextPath = next
            )
        } else null
    }
    
    /**
     * Handle error responses
     */
    private suspend fun handleErrorResponse(response: HttpResponse) {
        if (response.status.isSuccess()) return
        
        val problemResponse = try {
            response.body<ProblemResponse>()
        } catch (e: Exception) {
            null
        }
        
        val message = problemResponse?.detail 
            ?: problemResponse?.title 
            ?: response.status.description
        
        when (response.status.value) {
            400 -> throw ValidationException(
                message = message,
                validationErrors = problemResponse?.properties ?: emptyList(),
                statusCode = 400,
                problemResponse = problemResponse
            )
            401 -> throw AuthenticationException(
                message = message,
                statusCode = 401,
                problemResponse = problemResponse
            )
            403 -> throw AuthorizationException(
                message = message,
                statusCode = 403,
                problemResponse = problemResponse
            )
            404 -> throw NotFoundException(
                message = message,
                statusCode = 404,
                problemResponse = problemResponse
            )
            409 -> throw ConflictException(
                message = message,
                statusCode = 409,
                problemResponse = problemResponse
            )
            429 -> {
                val retryAfter = response.headers["Retry-After"]?.toIntOrNull()
                throw RateLimitException(
                    message = message,
                    retryAfter = retryAfter,
                    statusCode = 429,
                    problemResponse = problemResponse
                )
            }
            in 500..599 -> throw ServerException(
                message = message,
                statusCode = response.status.value,
                problemResponse = problemResponse
            )
            else -> throw IxApiException(
                message = message,
                statusCode = response.status.value,
                problemResponse = problemResponse
            )
        }
    }
    
    /**
     * Execute a request with retry logic
     */
    private suspend inline fun <T> executeWithRetry(
        crossinline block: suspend () -> T
    ): T {
        var lastException: Exception? = null
        var currentDelay = config.retryDelay
        
        repeat(config.maxRetries) { attempt ->
            try {
                return block()
            } catch (e: RateLimitException) {
                lastException = e
                val delayMs = (e.retryAfter?.times(1000L)) ?: currentDelay
                logger.warn("Rate limited, retrying in ${delayMs}ms (attempt ${attempt + 1}/${config.maxRetries})")
                delay(delayMs)
                currentDelay *= 2
            } catch (e: ServerException) {
                lastException = e
                logger.warn("Server error, retrying in ${currentDelay}ms (attempt ${attempt + 1}/${config.maxRetries})")
                delay(currentDelay)
                currentDelay *= 2
            } catch (e: Exception) {
                throw e
            }
        }
        
        throw lastException ?: IxApiException("Request failed after ${config.maxRetries} retries")
    }
    
    /**
     * Close the HTTP client
     */
    fun close() {
        httpClient.close()
    }
}
