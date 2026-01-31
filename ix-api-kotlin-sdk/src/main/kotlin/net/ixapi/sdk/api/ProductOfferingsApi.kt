package net.ixapi.sdk.api

import net.ixapi.sdk.client.IxApiHttpClient
import net.ixapi.sdk.models.*
import net.ixapi.sdk.pagination.PaginatedResponse
import net.ixapi.sdk.pagination.PaginationParams
import net.ixapi.sdk.util.query

/**
 * API client for Product Offering operations
 */
class ProductOfferingsApi(private val client: IxApiHttpClient) {
    
    /**
     * List all product offerings
     */
    suspend fun list(
        type: ProductOfferingType? = null,
        serviceMetroArea: String? = null,
        serviceMetroAreaNetwork: String? = null,
        handoverMetroArea: String? = null,
        handoverMetroAreaNetwork: String? = null,
        handoverPop: String? = null,
        serviceProvider: String? = null,
        deliveryMethod: String? = null,
        physicalPortSpeed: Int? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<ProductOffering>> {
        val params = query {
            param("type", type?.name?.lowercase())
            param("service_metro_area", serviceMetroArea)
            param("service_metro_area_network", serviceMetroAreaNetwork)
            param("handover_metro_area", handoverMetroArea)
            param("handover_metro_area_network", handoverMetroAreaNetwork)
            param("handover_pop", handoverPop)
            param("service_provider", serviceProvider)
            param("delivery_method", deliveryMethod)
            param("physical_port_speed", physicalPortSpeed)
            pagination(pagination)
        }
        return client.getWithPagination("/product-offerings", params)
    }
    
    /**
     * Get a product offering by ID
     */
    suspend fun get(id: String): ProductOffering {
        return client.get("/product-offerings/$id")
    }
    
    /**
     * List connection product offerings
     */
    suspend fun listConnectionOfferings(
        handoverMetroArea: String? = null,
        handoverMetroAreaNetwork: String? = null,
        handoverPop: String? = null,
        physicalPortSpeed: Int? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<ProductOffering>> {
        return list(
            type = ProductOfferingType.CONNECTION,
            handoverMetroArea = handoverMetroArea,
            handoverMetroAreaNetwork = handoverMetroAreaNetwork,
            handoverPop = handoverPop,
            physicalPortSpeed = physicalPortSpeed,
            pagination = pagination
        )
    }
    
    /**
     * List exchange LAN product offerings
     */
    suspend fun listExchangeLanOfferings(
        serviceMetroArea: String? = null,
        serviceMetroAreaNetwork: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<ProductOffering>> {
        return list(
            type = ProductOfferingType.EXCHANGE_LAN,
            serviceMetroArea = serviceMetroArea,
            serviceMetroAreaNetwork = serviceMetroAreaNetwork,
            pagination = pagination
        )
    }
    
    /**
     * List P2P VC product offerings
     */
    suspend fun listP2POfferings(
        serviceMetroArea: String? = null,
        serviceMetroAreaNetwork: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<ProductOffering>> {
        return list(
            type = ProductOfferingType.P2P_VC,
            serviceMetroArea = serviceMetroArea,
            serviceMetroAreaNetwork = serviceMetroAreaNetwork,
            pagination = pagination
        )
    }
    
    /**
     * List cloud VC product offerings
     */
    suspend fun listCloudOfferings(
        serviceMetroArea: String? = null,
        serviceMetroAreaNetwork: String? = null,
        serviceProvider: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<ProductOffering>> {
        return list(
            type = ProductOfferingType.CLOUD_VC,
            serviceMetroArea = serviceMetroArea,
            serviceMetroAreaNetwork = serviceMetroAreaNetwork,
            serviceProvider = serviceProvider,
            pagination = pagination
        )
    }
}
