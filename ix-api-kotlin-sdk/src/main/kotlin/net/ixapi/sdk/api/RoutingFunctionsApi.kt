package net.ixapi.sdk.api

import net.ixapi.sdk.client.IxApiHttpClient
import net.ixapi.sdk.models.*
import net.ixapi.sdk.pagination.PaginatedResponse
import net.ixapi.sdk.pagination.PaginationParams
import net.ixapi.sdk.util.query

/**
 * API client for Routing Function operations
 */
class RoutingFunctionsApi(private val client: IxApiHttpClient) {
    
    /**
     * List all routing functions
     */
    suspend fun list(
        state: ResourceState? = null,
        managingAccount: String? = null,
        consumingAccount: String? = null,
        billingAccount: String? = null,
        externalRef: String? = null,
        productOffering: String? = null,
        metroAreaNetwork: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<RoutingFunction>> {
        val params = query {
            state(state)
            managingAccount(managingAccount)
            consumingAccount(consumingAccount)
            billingAccount(billingAccount)
            externalRef(externalRef)
            param("product_offering", productOffering)
            param("metro_area_network", metroAreaNetwork)
            pagination(pagination)
        }
        return client.getWithPagination("/routing-functions", params)
    }
    
    /**
     * Get a routing function by ID
     */
    suspend fun get(id: String): RoutingFunction {
        return client.get("/routing-functions/$id")
    }
    
    /**
     * Create a new routing function
     */
    suspend fun create(request: RoutingFunctionRequest): RoutingFunction {
        return client.post("/routing-functions", request)
    }
    
    /**
     * Patch a routing function (partial update)
     */
    suspend fun patch(id: String, request: RoutingFunctionPatch): RoutingFunction {
        return client.patch("/routing-functions/$id", request)
    }
    
    /**
     * Delete a routing function
     */
    suspend fun delete(id: String): RoutingFunction {
        return client.delete("/routing-functions/$id")
    }
    
    /**
     * Get the cancellation policy for a routing function
     */
    suspend fun getCancellationPolicy(id: String): CancellationPolicy {
        return client.get("/routing-functions/$id/cancellation-policy")
    }
}
