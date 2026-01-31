package net.ixapi.sdk.api

import net.ixapi.sdk.client.IxApiHttpClient
import net.ixapi.sdk.models.*
import net.ixapi.sdk.pagination.PaginatedResponse
import net.ixapi.sdk.pagination.PaginationParams
import net.ixapi.sdk.util.query

/**
 * API client for Account operations
 */
class AccountsApi(private val client: IxApiHttpClient) {
    
    /**
     * Get the current account (authenticated user's account)
     */
    suspend fun getCurrent(): Account {
        return client.get("/account")
    }
    
    /**
     * List all accounts
     */
    suspend fun list(
        state: ResourceState? = null,
        managingAccount: String? = null,
        name: String? = null,
        discoverable: Boolean? = null,
        metroAreaNetworkPresence: String? = null,
        asn: Int? = null,
        externalRef: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<Account>> {
        val params = query {
            state(state)
            managingAccount(managingAccount)
            param("name", name)
            param("discoverable", discoverable)
            param("metro_area_network_presence", metroAreaNetworkPresence)
            param("asn", asn)
            externalRef(externalRef)
            pagination(pagination)
        }
        return client.getWithPagination("/accounts", params)
    }
    
    /**
     * Get an account by ID
     */
    suspend fun get(id: String): Account {
        return client.get("/accounts/$id")
    }
    
    /**
     * Create a new account
     */
    suspend fun create(request: AccountRequest): Account {
        return client.post("/accounts", request)
    }
    
    /**
     * Update an account (full update)
     */
    suspend fun update(id: String, request: AccountUpdate): Account {
        return client.put("/accounts/$id", request)
    }
    
    /**
     * Patch an account (partial update)
     */
    suspend fun patch(id: String, request: AccountPatch): Account {
        return client.patch("/accounts/$id", request)
    }
    
    /**
     * Delete an account
     */
    suspend fun delete(id: String): Account {
        return client.delete("/accounts/$id")
    }
}
