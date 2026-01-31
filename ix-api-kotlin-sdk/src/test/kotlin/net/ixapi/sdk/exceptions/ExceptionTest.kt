package net.ixapi.sdk.exceptions

import net.ixapi.sdk.models.ProblemResponse
import net.ixapi.sdk.models.ValidationErrorProperty
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExceptionTest {
    
    @Test
    fun `test validation exception with errors`() {
        val errors = listOf(
            ValidationErrorProperty("email", "Invalid format"),
            ValidationErrorProperty("name", "Required field")
        )
        
        val exception = ValidationException(
            message = "Validation failed",
            validationErrors = errors,
            statusCode = 400
        )
        
        assertEquals(400, exception.statusCode)
        assertEquals(2, exception.validationErrors.size)
        
        val errorMap = exception.getErrorsByField()
        assertEquals("Invalid format", errorMap["email"])
        assertEquals("Required field", errorMap["name"])
    }
    
    @Test
    fun `test authentication exception`() {
        val exception = AuthenticationException(
            message = "Invalid credentials",
            statusCode = 401
        )
        
        assertEquals(401, exception.statusCode)
        assertTrue(exception.message!!.contains("Invalid credentials"))
    }
    
    @Test
    fun `test not found exception`() {
        val problemResponse = ProblemResponse(
            type = "https://errors.ix-api.net/v2/not-found.html",
            title = "Not Found",
            status = 404,
            detail = "Resource with ID 'xyz' not found"
        )
        
        val exception = NotFoundException(
            message = "Resource not found",
            statusCode = 404,
            problemResponse = problemResponse
        )
        
        assertEquals(404, exception.statusCode)
        assertNotNull(exception.problemResponse)
        assertEquals("https://errors.ix-api.net/v2/not-found.html", exception.errorType)
        assertEquals("Not Found", exception.errorTitle)
        assertEquals("Resource with ID 'xyz' not found", exception.errorDetail)
    }
    
    @Test
    fun `test rate limit exception`() {
        val exception = RateLimitException(
            message = "Too many requests",
            retryAfter = 60,
            statusCode = 429
        )
        
        assertEquals(429, exception.statusCode)
        assertEquals(60, exception.retryAfter)
    }
    
    @Test
    fun `test server exception`() {
        val exception = ServerException(
            message = "Internal server error",
            statusCode = 500
        )
        
        assertEquals(500, exception.statusCode)
    }
    
    @Test
    fun `test conflict exception`() {
        val exception = ConflictException(
            message = "Resource state conflict",
            statusCode = 409
        )
        
        assertEquals(409, exception.statusCode)
    }
    
    @Test
    fun `test network exception`() {
        val cause = java.net.ConnectException("Connection refused")
        val exception = NetworkException(
            message = "Failed to connect",
            cause = cause
        )
        
        assertNotNull(exception.cause)
        assertTrue(exception.cause is java.net.ConnectException)
    }
    
    @Test
    fun `test configuration exception`() {
        val exception = ConfigurationException(
            message = "Missing API credentials"
        )
        
        assertTrue(exception.message!!.contains("credentials"))
    }
}
