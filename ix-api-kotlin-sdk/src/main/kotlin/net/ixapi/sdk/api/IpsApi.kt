package net.ixapi.sdk.api

import net.ixapi.sdk.client.IxApiHttpClient
import net.ixapi.sdk.models.*
import net.ixapi.sdk.pagination.PaginatedResponse
import net.ixapi.sdk.pagination.PaginationParams
import net.ixapi.sdk.util.query

/**
 * API client for IP Address operations
 */
class IpsApi(private val client: IxApiHttpClient) {
    
    /**
     * List all IP addresses
     */
    suspend fun list(
        version: Int? = null,
        address: String? = null,
        managingAccount: String? = null,
        consumingAccount: String? = null,
        externalRef: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<IpAddress>> {
        val params = query {
            param("version", version)
            param("address", address)
            managingAccount(managingAccount)
            consumingAccount(consumingAccount)
            externalRef(externalRef)
            pagination(pagination)
        }
        return client.getWithPagination("/ips", params)
    }
    
    /**
     * Get an IP address by ID
     */
    suspend fun get(id: String): IpAddress {
        return client.get("/ips/$id")
    }
    
    /**
     * Create a new IP address
     */
    suspend fun create(request: IpAddressRequest): IpAddress {
        return client.post("/ips", request)
    }
    
    /**
     * Update an IP address (full update)
     */
    suspend fun update(id: String, request: IpAddressUpdate): IpAddress {
        return client.put("/ips/$id", request)
    }
    
    /**
     * Patch an IP address (partial update)
     */
    suspend fun patch(id: String, request: IpAddressPatch): IpAddress {
        return client.patch("/ips/$id", request)
    }
    
    /**
     * Delete an IP address
     */
    suspend fun delete(id: String): IpAddress {
        return client.delete("/ips/$id")
    }
    
    /**
     * List IPv4 addresses
     */
    suspend fun listV4(
        address: String? = null,
        managingAccount: String? = null,
        consumingAccount: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<IpAddress>> {
        return list(
            version = 4,
            address = address,
            managingAccount = managingAccount,
            consumingAccount = consumingAccount,
            pagination = pagination
        )
    }
    
    /**
     * List IPv6 addresses
     */
    suspend fun listV6(
        address: String? = null,
        managingAccount: String? = null,
        consumingAccount: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<IpAddress>> {
        return list(
            version = 6,
            address = address,
            managingAccount = managingAccount,
            consumingAccount = consumingAccount,
            pagination = pagination
        )
    }
}

/**
 * API client for MAC Address operations
 */
class MacsApi(private val client: IxApiHttpClient) {
    
    /**
     * List all MAC addresses
     */
    suspend fun list(
        address: String? = null,
        managingAccount: String? = null,
        consumingAccount: String? = null,
        externalRef: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<MacAddress>> {
        val params = query {
            param("address", address)
            managingAccount(managingAccount)
            consumingAccount(consumingAccount)
            externalRef(externalRef)
            pagination(pagination)
        }
        return client.getWithPagination("/macs", params)
    }
    
    /**
     * Get a MAC address by ID
     */
    suspend fun get(id: String): MacAddress {
        return client.get("/macs/$id")
    }
    
    /**
     * Create a new MAC address
     */
    suspend fun create(request: MacAddressRequest): MacAddress {
        return client.post("/macs", request)
    }
    
    /**
     * Delete a MAC address
     */
    suspend fun delete(id: String): MacAddress {
        return client.delete("/macs/$id")
    }
}
