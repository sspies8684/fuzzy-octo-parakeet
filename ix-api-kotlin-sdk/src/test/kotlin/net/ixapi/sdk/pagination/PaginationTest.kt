package net.ixapi.sdk.pagination

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PaginationTest {
    
    @Test
    fun `test pagination info has next page`() {
        val info = PaginationInfo(
            limit = 50,
            offset = 0,
            totalItems = 100,
            totalPages = 2,
            currentPage = 1,
            token = "abc123",
            nextPath = "/items?page_offset=50"
        )
        
        assertTrue(info.hasNextPage)
        assertFalse(info.hasPreviousPage)
        assertEquals(50, info.nextOffset)
    }
    
    @Test
    fun `test pagination info last page`() {
        val info = PaginationInfo(
            limit = 50,
            offset = 50,
            totalItems = 100,
            totalPages = 2,
            currentPage = 2,
            token = "abc123",
            nextPath = null
        )
        
        assertFalse(info.hasNextPage)
        assertTrue(info.hasPreviousPage)
        assertEquals(0, info.previousOffset)
    }
    
    @Test
    fun `test pagination params to query`() {
        val params = PaginationParams(
            limit = 25,
            offset = 50,
            token = "mytoken"
        )
        
        val query = params.toQueryParams()
        assertEquals("25", query["page_limit"])
        assertEquals("50", query["page_offset"])
        assertEquals("mytoken", query["page_token"])
    }
    
    @Test
    fun `test pagination builder`() {
        val params = pagination {
            limit(100)
            offset(200)
            token("test-token")
        }
        
        assertEquals(100, params.limit)
        assertEquals(200, params.offset)
        assertEquals("test-token", params.token)
    }
    
    @Test
    fun `test paginated response`() {
        val paginationInfo = PaginationInfo(
            limit = 50,
            offset = 0,
            totalItems = 150,
            totalPages = 3,
            currentPage = 1,
            token = null,
            nextPath = "/next"
        )
        
        val response = PaginatedResponse(
            data = listOf("item1", "item2", "item3"),
            pagination = paginationInfo
        )
        
        assertTrue(response.isPaginated)
        assertTrue(response.hasNextPage)
        assertFalse(response.hasPreviousPage)
        assertEquals(3, response.data.size)
    }
    
    @Test
    fun `test non-paginated response`() {
        val response = PaginatedResponse(
            data = listOf("item1"),
            pagination = null
        )
        
        assertFalse(response.isPaginated)
        assertFalse(response.hasNextPage)
        assertFalse(response.hasPreviousPage)
    }
}
