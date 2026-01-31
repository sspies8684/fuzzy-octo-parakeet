package net.ixapi.sdk.pagination

/**
 * Pagination information from response headers
 */
data class PaginationInfo(
    val limit: Int,
    val offset: Int,
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int,
    val token: String?,
    val nextPath: String?
) {
    /**
     * Check if there is a next page
     */
    val hasNextPage: Boolean
        get() = nextPath != null || (currentPage < totalPages)
    
    /**
     * Check if there is a previous page
     */
    val hasPreviousPage: Boolean
        get() = currentPage > 1
    
    /**
     * Get the offset for the next page
     */
    val nextOffset: Int
        get() = offset + limit
    
    /**
     * Get the offset for the previous page
     */
    val previousOffset: Int
        get() = maxOf(0, offset - limit)
}

/**
 * Paginated response wrapper
 */
data class PaginatedResponse<T>(
    val data: T,
    val pagination: PaginationInfo?
) {
    val isPaginated: Boolean
        get() = pagination != null
    
    val hasNextPage: Boolean
        get() = pagination?.hasNextPage ?: false
    
    val hasPreviousPage: Boolean
        get() = pagination?.hasPreviousPage ?: false
}

/**
 * Pagination request parameters
 */
data class PaginationParams(
    val limit: Int? = null,
    val offset: Int? = null,
    val token: String? = null
) {
    fun toQueryParams(): Map<String, String?> = mapOf(
        "page_limit" to limit?.toString(),
        "page_offset" to offset?.toString(),
        "page_token" to token
    )
}

/**
 * Builder for pagination parameters
 */
class PaginationBuilder {
    private var limit: Int? = null
    private var offset: Int? = null
    private var token: String? = null
    
    fun limit(value: Int) = apply { limit = value }
    fun offset(value: Int) = apply { offset = value }
    fun token(value: String) = apply { token = value }
    
    fun build() = PaginationParams(limit, offset, token)
}

/**
 * DSL function for building pagination parameters
 */
fun pagination(block: PaginationBuilder.() -> Unit): PaginationParams =
    PaginationBuilder().apply(block).build()
