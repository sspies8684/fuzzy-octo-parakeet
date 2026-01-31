package net.ixapi.sdk.api

import net.ixapi.sdk.client.IxApiHttpClient
import net.ixapi.sdk.models.*
import net.ixapi.sdk.pagination.PaginatedResponse
import net.ixapi.sdk.pagination.PaginationParams
import net.ixapi.sdk.util.query

/**
 * API client for Connection operations
 */
class ConnectionsApi(private val client: IxApiHttpClient) {
    
    /**
     * List all connections
     */
    suspend fun list(
        state: ResourceState? = null,
        managingAccount: String? = null,
        consumingAccount: String? = null,
        billingAccount: String? = null,
        externalRef: String? = null,
        pop: String? = null,
        facility: String? = null,
        metroArea: String? = null,
        metroAreaNetwork: String? = null,
        productOffering: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<Connection>> {
        val params = query {
            state(state)
            managingAccount(managingAccount)
            consumingAccount(consumingAccount)
            billingAccount(billingAccount)
            externalRef(externalRef)
            param("pop", pop)
            param("facility", facility)
            param("metro_area", metroArea)
            param("metro_area_network", metroAreaNetwork)
            param("product_offering", productOffering)
            pagination(pagination)
        }
        return client.getWithPagination("/connections", params)
    }
    
    /**
     * Get a connection by ID
     */
    suspend fun get(id: String): Connection {
        return client.get("/connections/$id")
    }
    
    /**
     * Create a new connection
     */
    suspend fun create(request: ConnectionRequest): Connection {
        return client.post("/connections", request)
    }
    
    /**
     * Update a connection (full update)
     */
    suspend fun update(id: String, request: ConnectionUpdate): Connection {
        return client.put("/connections/$id", request)
    }
    
    /**
     * Patch a connection (partial update)
     */
    suspend fun patch(id: String, request: ConnectionPatch): Connection {
        return client.patch("/connections/$id", request)
    }
    
    /**
     * Delete a connection
     */
    suspend fun delete(id: String): Connection {
        return client.delete("/connections/$id")
    }
    
    /**
     * Get the cancellation policy for a connection
     */
    suspend fun getCancellationPolicy(id: String): CancellationPolicy {
        return client.get("/connections/$id/cancellation-policy")
    }
    
    /**
     * Download the Letter of Authorization (LOA) document
     */
    suspend fun downloadLoa(id: String): ByteArray {
        return client.downloadBinary("/connections/$id/loa")
    }
    
    /**
     * Get statistics for a connection
     */
    suspend fun getStatistics(id: String): AggregateStatistics {
        return client.get("/connections/$id/statistics")
    }
    
    /**
     * Get timeseries statistics for a connection
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
        return client.get("/connections/$id/statistics/${aggregate.name.lowercase()}/timeseries", params)
    }
}

/**
 * API client for Port operations
 */
class PortsApi(private val client: IxApiHttpClient) {
    
    /**
     * List all ports
     */
    suspend fun list(
        state: ResourceState? = null,
        connection: String? = null,
        device: String? = null,
        pop: String? = null,
        managingAccount: String? = null,
        consumingAccount: String? = null,
        externalRef: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<Port>> {
        val params = query {
            state(state)
            param("connection", connection)
            param("device", device)
            param("pop", pop)
            managingAccount(managingAccount)
            consumingAccount(consumingAccount)
            externalRef(externalRef)
            pagination(pagination)
        }
        return client.getWithPagination("/ports", params)
    }
    
    /**
     * Get a port by ID
     */
    suspend fun get(id: String): Port {
        return client.get("/ports/$id")
    }
    
    /**
     * Get statistics for a port
     */
    suspend fun getStatistics(id: String): PortStatistics {
        return client.get("/ports/$id/statistics")
    }
    
    /**
     * Get timeseries statistics for a port
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
        return client.get("/ports/$id/statistics/${aggregate.name.lowercase()}/timeseries", params)
    }
}

/**
 * API client for Port Reservation operations
 */
class PortReservationsApi(private val client: IxApiHttpClient) {
    
    /**
     * List all port reservations
     */
    suspend fun list(
        state: ResourceState? = null,
        connection: String? = null,
        externalRef: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<PortReservation>> {
        val params = query {
            state(state)
            param("connection", connection)
            externalRef(externalRef)
            pagination(pagination)
        }
        return client.getWithPagination("/port-reservations", params)
    }
    
    /**
     * Get a port reservation by ID
     */
    suspend fun get(id: String): PortReservation {
        return client.get("/port-reservations/$id")
    }
    
    /**
     * Create a new port reservation
     */
    suspend fun create(request: PortReservationRequest): PortReservation {
        return client.post("/port-reservations", request)
    }
    
    /**
     * Update a port reservation (full update)
     */
    suspend fun update(id: String, request: PortReservationUpdate): PortReservation {
        return client.put("/port-reservations/$id", request)
    }
    
    /**
     * Patch a port reservation (partial update)
     */
    suspend fun patch(id: String, request: PortReservationPatch): PortReservation {
        return client.patch("/port-reservations/$id", request)
    }
    
    /**
     * Delete a port reservation
     */
    suspend fun delete(id: String): PortReservation {
        return client.delete("/port-reservations/$id")
    }
    
    /**
     * Get the cancellation policy for a port reservation
     */
    suspend fun getCancellationPolicy(id: String): CancellationPolicy {
        return client.get("/port-reservations/$id/cancellation-policy")
    }
    
    /**
     * Download the Letter of Authorization (LOA) document
     */
    suspend fun downloadLoa(id: String): ByteArray {
        return client.downloadBinary("/port-reservations/$id/loa")
    }
}
