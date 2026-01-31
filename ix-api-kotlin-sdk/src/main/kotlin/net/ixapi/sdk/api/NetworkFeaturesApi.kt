package net.ixapi.sdk.api

import net.ixapi.sdk.client.IxApiHttpClient
import net.ixapi.sdk.models.*
import net.ixapi.sdk.pagination.PaginatedResponse
import net.ixapi.sdk.pagination.PaginationParams
import net.ixapi.sdk.util.query

/**
 * API client for Network Feature operations
 */
class NetworkFeaturesApi(private val client: IxApiHttpClient) {
    
    /**
     * List all network features
     */
    suspend fun list(
        type: String? = null,
        networkService: String? = null,
        required: Boolean? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<NetworkFeature>> {
        val params = query {
            param("type", type)
            param("network_service", networkService)
            param("required", required)
            pagination(pagination)
        }
        return client.getWithPagination("/network-features", params)
    }
    
    /**
     * Get a network feature by ID
     */
    suspend fun get(id: String): NetworkFeature {
        return client.get("/network-features/$id")
    }
}

/**
 * API client for Network Feature Config operations
 */
class NetworkFeatureConfigsApi(private val client: IxApiHttpClient) {
    
    /**
     * List all network feature configs
     */
    suspend fun list(
        type: String? = null,
        networkFeature: String? = null,
        networkServiceConfig: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<NetworkFeatureConfig>> {
        val params = query {
            param("type", type)
            param("network_feature", networkFeature)
            param("network_service_config", networkServiceConfig)
            pagination(pagination)
        }
        return client.getWithPagination("/network-feature-configs", params)
    }
    
    /**
     * Get a network feature config by ID
     */
    suspend fun get(id: String): NetworkFeatureConfig {
        return client.get("/network-feature-configs/$id")
    }
    
    /**
     * Create a new network feature config
     */
    suspend fun create(request: NetworkFeatureConfigRequest): NetworkFeatureConfig {
        return client.post("/network-feature-configs", request)
    }
    
    /**
     * Update a network feature config (full update)
     */
    suspend fun update(id: String, request: NetworkFeatureConfigUpdate): NetworkFeatureConfig {
        return client.put("/network-feature-configs/$id", request)
    }
    
    /**
     * Patch a network feature config (partial update)
     */
    suspend fun patch(id: String, request: NetworkFeatureConfigPatch): NetworkFeatureConfig {
        return client.patch("/network-feature-configs/$id", request)
    }
    
    /**
     * Delete a network feature config
     */
    suspend fun delete(id: String): NetworkFeatureConfig {
        return client.delete("/network-feature-configs/$id")
    }
}
