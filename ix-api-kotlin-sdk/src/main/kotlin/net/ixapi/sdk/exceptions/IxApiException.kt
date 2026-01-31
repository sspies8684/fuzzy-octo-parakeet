package net.ixapi.sdk.exceptions

import net.ixapi.sdk.models.ProblemResponse
import net.ixapi.sdk.models.ValidationErrorProperty

/**
 * Base exception for all IX-API errors
 */
open class IxApiException(
    message: String,
    val statusCode: Int? = null,
    val problemResponse: ProblemResponse? = null,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    val errorType: String?
        get() = problemResponse?.type
    
    val errorTitle: String?
        get() = problemResponse?.title
    
    val errorDetail: String?
        get() = problemResponse?.detail
}

/**
 * Authentication error - credentials invalid or expired
 */
class AuthenticationException(
    message: String = "Authentication failed",
    statusCode: Int? = 401,
    problemResponse: ProblemResponse? = null,
    cause: Throwable? = null
) : IxApiException(message, statusCode, problemResponse, cause)

/**
 * Authorization error - insufficient permissions
 */
class AuthorizationException(
    message: String = "Permission denied",
    statusCode: Int? = 403,
    problemResponse: ProblemResponse? = null,
    cause: Throwable? = null
) : IxApiException(message, statusCode, problemResponse, cause)

/**
 * Resource not found error
 */
class NotFoundException(
    message: String = "Resource not found",
    statusCode: Int? = 404,
    problemResponse: ProblemResponse? = null,
    cause: Throwable? = null
) : IxApiException(message, statusCode, problemResponse, cause)

/**
 * Validation error - request data invalid
 */
class ValidationException(
    message: String = "Validation failed",
    val validationErrors: List<ValidationErrorProperty> = emptyList(),
    statusCode: Int? = 400,
    problemResponse: ProblemResponse? = null,
    cause: Throwable? = null
) : IxApiException(message, statusCode, problemResponse, cause) {
    
    /**
     * Get validation errors as a map of field name to error reason
     */
    fun getErrorsByField(): Map<String, String> =
        validationErrors.associate { it.name to it.reason }
}

/**
 * Conflict error - resource state conflict
 */
class ConflictException(
    message: String = "Resource conflict",
    statusCode: Int? = 409,
    problemResponse: ProblemResponse? = null,
    cause: Throwable? = null
) : IxApiException(message, statusCode, problemResponse, cause)

/**
 * Rate limiting error
 */
class RateLimitException(
    message: String = "Rate limit exceeded",
    val retryAfter: Int? = null,
    statusCode: Int? = 429,
    problemResponse: ProblemResponse? = null,
    cause: Throwable? = null
) : IxApiException(message, statusCode, problemResponse, cause)

/**
 * Server error
 */
class ServerException(
    message: String = "Server error",
    statusCode: Int? = 500,
    problemResponse: ProblemResponse? = null,
    cause: Throwable? = null
) : IxApiException(message, statusCode, problemResponse, cause)

/**
 * Network/connection error
 */
class NetworkException(
    message: String = "Network error",
    cause: Throwable? = null
) : IxApiException(message, null, null, cause)

/**
 * Configuration error
 */
class ConfigurationException(
    message: String = "Configuration error",
    cause: Throwable? = null
) : IxApiException(message, null, null, cause)
