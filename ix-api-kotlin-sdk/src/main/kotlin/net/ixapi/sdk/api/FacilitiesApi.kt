package net.ixapi.sdk.api

import net.ixapi.sdk.client.IxApiHttpClient
import net.ixapi.sdk.models.*
import net.ixapi.sdk.pagination.PaginatedResponse
import net.ixapi.sdk.pagination.PaginationParams
import net.ixapi.sdk.util.query

/**
 * API client for Facility operations
 */
class FacilitiesApi(private val client: IxApiHttpClient) {
    
    /**
     * List all facilities
     */
    suspend fun list(
        metroArea: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<Facility>> {
        val params = query {
            param("metro_area", metroArea)
            pagination(pagination)
        }
        return client.getWithPagination("/facilities", params)
    }
    
    /**
     * Get a facility by ID
     */
    suspend fun get(id: String): Facility {
        return client.get("/facilities/$id")
    }
}

/**
 * API client for Metro Area operations
 */
class MetroAreasApi(private val client: IxApiHttpClient) {
    
    /**
     * List all metro areas
     */
    suspend fun list(
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<MetroArea>> {
        val params = query {
            pagination(pagination)
        }
        return client.getWithPagination("/metro-areas", params)
    }
    
    /**
     * Get a metro area by ID
     */
    suspend fun get(id: String): MetroArea {
        return client.get("/metro-areas/$id")
    }
}

/**
 * API client for Metro Area Network operations
 */
class MetroAreaNetworksApi(private val client: IxApiHttpClient) {
    
    /**
     * List all metro area networks
     */
    suspend fun list(
        metroArea: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<MetroAreaNetwork>> {
        val params = query {
            param("metro_area", metroArea)
            pagination(pagination)
        }
        return client.getWithPagination("/metro-area-networks", params)
    }
    
    /**
     * Get a metro area network by ID
     */
    suspend fun get(id: String): MetroAreaNetwork {
        return client.get("/metro-area-networks/$id")
    }
}

/**
 * API client for Point of Presence operations
 */
class PopsApi(private val client: IxApiHttpClient) {
    
    /**
     * List all PoPs
     */
    suspend fun list(
        facility: String? = null,
        metroAreaNetwork: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<PointOfPresence>> {
        val params = query {
            param("facility", facility)
            param("metro_area_network", metroAreaNetwork)
            pagination(pagination)
        }
        return client.getWithPagination("/pops", params)
    }
    
    /**
     * Get a PoP by ID
     */
    suspend fun get(id: String): PointOfPresence {
        return client.get("/pops/$id")
    }
}

/**
 * API client for Availability Zone operations
 */
class AvailabilityZonesApi(private val client: IxApiHttpClient) {
    
    /**
     * List all availability zones
     */
    suspend fun list(
        metroAreaNetwork: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<AvailabilityZone>> {
        val params = query {
            param("metro_area_network", metroAreaNetwork)
            pagination(pagination)
        }
        return client.getWithPagination("/availability-zones", params)
    }
    
    /**
     * Get an availability zone by ID
     */
    suspend fun get(id: String): AvailabilityZone {
        return client.get("/availability-zones/$id")
    }
}

/**
 * API client for Device operations
 */
class DevicesApi(private val client: IxApiHttpClient) {
    
    /**
     * List all devices
     */
    suspend fun list(
        pop: String? = null,
        facility: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<Device>> {
        val params = query {
            param("pop", pop)
            param("facility", facility)
            pagination(pagination)
        }
        return client.getWithPagination("/devices", params)
    }
    
    /**
     * Get a device by ID
     */
    suspend fun get(id: String): Device {
        return client.get("/devices/$id")
    }
}
