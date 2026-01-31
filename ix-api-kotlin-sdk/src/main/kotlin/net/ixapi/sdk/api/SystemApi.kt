package net.ixapi.sdk.api

import net.ixapi.sdk.client.IxApiHttpClient
import net.ixapi.sdk.models.*

/**
 * API client for System/Health operations
 */
class HealthApi(private val client: IxApiHttpClient) {
    
    /**
     * Get the API health status
     */
    suspend fun get(): ApiHealth {
        return client.get("/health")
    }
    
    /**
     * Check if the API is healthy
     */
    suspend fun isHealthy(): Boolean {
        return try {
            val health = get()
            health.status == HealthStatus.PASS
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * API client for Implementation info
 */
class ImplementationApi(private val client: IxApiHttpClient) {
    
    /**
     * Get API implementation details
     */
    suspend fun get(): ApiImplementation {
        return client.get("/implementation")
    }
    
    /**
     * Get the API version
     */
    suspend fun getVersion(): String {
        return get().version
    }
    
    /**
     * Get the schema version
     */
    suspend fun getSchemaVersion(): String {
        return get().schemaVersion
    }
    
    /**
     * Check if a specific feature is supported
     */
    suspend fun isFeatureSupported(feature: String): Boolean {
        val impl = get()
        return when (feature.lowercase()) {
            "pagination" -> impl.features?.pagination ?: false
            "filtering" -> impl.features?.filtering ?: false
            "sorting" -> impl.features?.sorting ?: false
            "account_hierarchy" -> impl.features?.accountHierarchy ?: false
            "managing_accounts" -> impl.features?.managingAccounts ?: false
            "consuming_accounts" -> impl.features?.consumingAccounts ?: false
            "billing_accounts" -> impl.features?.billingAccounts ?: false
            "role_assignments" -> impl.features?.roleAssignments ?: false
            else -> false
        }
    }
    
    /**
     * Check if a specific operation is supported
     */
    suspend fun isOperationSupported(operation: String): Boolean {
        val impl = get()
        return impl.operations?.contains(operation) ?: false
    }
    
    /**
     * Get supported product offering types
     */
    suspend fun getSupportedProductOfferingTypes(): List<String> {
        return get().productOfferingTypes ?: emptyList()
    }
    
    /**
     * Get supported network service types
     */
    suspend fun getSupportedNetworkServiceTypes(): List<String> {
        return get().networkServiceTypes ?: emptyList()
    }
}

/**
 * API client for Extensions
 */
class ExtensionsApi(private val client: IxApiHttpClient) {
    
    /**
     * List all API extensions
     */
    suspend fun list(): List<ApiExtension> {
        return client.get("/extensions")
    }
    
    /**
     * Check if a specific extension is available
     */
    suspend fun hasExtension(name: String): Boolean {
        return list().any { it.name.equals(name, ignoreCase = true) }
    }
    
    /**
     * Get a specific extension by name
     */
    suspend fun getByName(name: String): ApiExtension? {
        return list().find { it.name.equals(name, ignoreCase = true) }
    }
}
