package net.ixapi.sdk.api

import net.ixapi.sdk.client.IxApiHttpClient
import net.ixapi.sdk.models.*
import net.ixapi.sdk.pagination.PaginatedResponse
import net.ixapi.sdk.pagination.PaginationParams
import net.ixapi.sdk.util.query

/**
 * API client for Network Service operations
 */
class NetworkServicesApi(private val client: IxApiHttpClient) {
    
    /**
     * List all network services
     */
    suspend fun list(
        state: ResourceState? = null,
        type: NetworkServiceType? = null,
        managingAccount: String? = null,
        consumingAccount: String? = null,
        billingAccount: String? = null,
        externalRef: String? = null,
        productOffering: String? = null,
        metroAreaNetwork: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<NetworkService>> {
        val params = query {
            state(state)
            param("type", type?.name?.lowercase())
            managingAccount(managingAccount)
            consumingAccount(consumingAccount)
            billingAccount(billingAccount)
            externalRef(externalRef)
            param("product_offering", productOffering)
            param("metro_area_network", metroAreaNetwork)
            pagination(pagination)
        }
        return client.getWithPagination("/network-services", params)
    }
    
    /**
     * Get a network service by ID
     */
    suspend fun get(id: String): NetworkService {
        return client.get("/network-services/$id")
    }
    
    /**
     * Create a new network service
     */
    suspend fun create(request: NetworkServiceRequest): NetworkService {
        return client.post("/network-services", request)
    }
    
    /**
     * Update a network service (full update)
     */
    suspend fun update(id: String, request: NetworkServiceUpdate): NetworkService {
        return client.put("/network-services/$id", request)
    }
    
    /**
     * Patch a network service (partial update)
     */
    suspend fun patch(id: String, request: NetworkServicePatch): NetworkService {
        return client.patch("/network-services/$id", request)
    }
    
    /**
     * Delete a network service
     */
    suspend fun delete(id: String): NetworkServiceDeleteResponse {
        return client.delete("/network-services/$id")
    }
    
    /**
     * Get the cancellation policy for a network service
     */
    suspend fun getCancellationPolicy(id: String): CancellationPolicy {
        return client.get("/network-services/$id/cancellation-policy")
    }
    
    /**
     * Create a change request for a network service
     */
    suspend fun createChangeRequest(id: String, request: NetworkServiceChangeRequest): NetworkService {
        return client.post("/network-services/$id/change-request", request)
    }
    
    /**
     * Get statistics for a network service
     */
    suspend fun getStatistics(id: String): AggregateStatistics {
        return client.get("/network-services/$id/statistics")
    }
    
    /**
     * Get timeseries statistics for a network service
     */
    suspend fun getTimeseries(
        id: String,
        aggregate: Aggregate,
        start: String? = null,
        end: String? = null
    ): AggregateTimeseries {
        val params = query {
            param("start", start)
            param("end", end)
        }
        return client.get("/network-services/$id/statistics/${aggregate.name.lowercase()}/timeseries", params)
    }
    
    /**
     * Get RTT statistics for a network service
     */
    suspend fun getRttStatistics(id: String): List<PeerRTT> {
        return client.get("/network-services/$id/rtt-statistics")
    }
}

/**
 * API client for Network Service Config operations
 */
class NetworkServiceConfigsApi(private val client: IxApiHttpClient) {
    
    /**
     * List all network service configs
     */
    suspend fun list(
        state: ResourceState? = null,
        type: NetworkServiceConfigType? = null,
        networkService: String? = null,
        connection: String? = null,
        managingAccount: String? = null,
        consumingAccount: String? = null,
        billingAccount: String? = null,
        externalRef: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<NetworkServiceConfig>> {
        val params = query {
            state(state)
            param("type", type?.name?.lowercase())
            param("network_service", networkService)
            param("connection", connection)
            managingAccount(managingAccount)
            consumingAccount(consumingAccount)
            billingAccount(billingAccount)
            externalRef(externalRef)
            pagination(pagination)
        }
        return client.getWithPagination("/network-service-configs", params)
    }
    
    /**
     * Get a network service config by ID
     */
    suspend fun get(id: String): NetworkServiceConfig {
        return client.get("/network-service-configs/$id")
    }
    
    /**
     * Create a new network service config
     */
    suspend fun create(request: NetworkServiceConfigRequest): NetworkServiceConfig {
        return client.post("/network-service-configs", request)
    }
    
    /**
     * Update a network service config (full update)
     */
    suspend fun update(id: String, request: NetworkServiceConfigUpdate): NetworkServiceConfig {
        return client.put("/network-service-configs/$id", request)
    }
    
    /**
     * Patch a network service config (partial update)
     */
    suspend fun patch(id: String, request: NetworkServiceConfigPatch): NetworkServiceConfig {
        return client.patch("/network-service-configs/$id", request)
    }
    
    /**
     * Delete a network service config
     */
    suspend fun delete(id: String): NetworkServiceConfig {
        return client.delete("/network-service-configs/$id")
    }
    
    /**
     * Get the cancellation policy for a network service config
     */
    suspend fun getCancellationPolicy(id: String): CancellationPolicy {
        return client.get("/network-service-configs/$id/cancellation-policy")
    }
    
    /**
     * Get statistics for a network service config
     */
    suspend fun getStatistics(id: String): NetworkServiceConfigAggregateStatistics {
        return client.get("/network-service-configs/$id/statistics")
    }
    
    /**
     * Get timeseries statistics for a network service config
     */
    suspend fun getTimeseries(
        id: String,
        aggregate: Aggregate,
        start: String? = null,
        end: String? = null
    ): AggregateTimeseries {
        val params = query {
            param("start", start)
            param("end", end)
        }
        return client.get("/network-service-configs/$id/statistics/${aggregate.name.lowercase()}/timeseries", params)
    }
    
    /**
     * Get peer statistics for a network service config
     */
    suspend fun getPeerStatistics(id: String): List<PeerAggregate> {
        return client.get("/network-service-configs/$id/peer-statistics")
    }
    
    /**
     * Get peer timeseries statistics for a network service config
     */
    suspend fun getPeerTimeseries(
        id: String,
        aggregate: Aggregate,
        start: String? = null,
        end: String? = null
    ): List<PeerTimeseries> {
        val params = query {
            param("start", start)
            param("end", end)
        }
        return client.get("/network-service-configs/$id/peer-statistics/${aggregate.name.lowercase()}/timeseries", params)
    }
}
