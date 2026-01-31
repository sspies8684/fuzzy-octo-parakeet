package net.ixapi.sdk.util

import net.ixapi.sdk.models.ResourceState
import net.ixapi.sdk.pagination.PaginationParams

/**
 * Query builder for list endpoints
 */
class QueryBuilder {
    private val params = mutableMapOf<String, String?>()
    
    fun param(key: String, value: String?) = apply {
        params[key] = value
    }
    
    fun param(key: String, value: Int?) = apply {
        params[key] = value?.toString()
    }
    
    fun param(key: String, value: Boolean?) = apply {
        params[key] = value?.toString()
    }
    
    fun param(key: String, value: List<String>?) = apply {
        value?.let { params[key] = it.joinToString(",") }
    }
    
    fun state(value: ResourceState?) = apply {
        params["state"] = value?.name?.lowercase()
    }
    
    fun states(vararg values: ResourceState) = apply {
        params["state"] = values.joinToString(",") { it.name.lowercase() }
    }
    
    fun id(value: String?) = apply {
        params["id"] = value
    }
    
    fun ids(values: List<String>?) = apply {
        values?.let { params["id"] = it.joinToString(",") }
    }
    
    fun externalRef(value: String?) = apply {
        params["external_ref"] = value
    }
    
    fun managingAccount(value: String?) = apply {
        params["managing_account"] = value
    }
    
    fun consumingAccount(value: String?) = apply {
        params["consuming_account"] = value
    }
    
    fun billingAccount(value: String?) = apply {
        params["billing_account"] = value
    }
    
    fun pagination(pagination: PaginationParams?) = apply {
        pagination?.let {
            params.putAll(it.toQueryParams())
        }
    }
    
    fun build(): Map<String, String?> = params.toMap()
}

/**
 * DSL function for building query parameters
 */
fun query(block: QueryBuilder.() -> Unit): Map<String, String?> =
    QueryBuilder().apply(block).build()

/**
 * Filter builder specific for accounts
 */
class AccountFilterBuilder : QueryBuilder() {
    fun name(value: String?) = param("name", value)
    fun discoverable(value: Boolean?) = param("discoverable", value)
    fun metroAreaNetworkPresence(value: String?) = param("metro_area_network_presence", value)
    fun asn(value: Int?) = param("asn", value)
}

/**
 * Filter builder for connections
 */
class ConnectionFilterBuilder : QueryBuilder() {
    fun pop(value: String?) = param("pop", value)
    fun facility(value: String?) = param("facility", value)
    fun metroArea(value: String?) = param("metro_area", value)
    fun metroAreaNetwork(value: String?) = param("metro_area_network", value)
    fun productOffering(value: String?) = param("product_offering", value)
}

/**
 * Filter builder for network services
 */
class NetworkServiceFilterBuilder : QueryBuilder() {
    fun type(value: String?) = param("type", value)
    fun productOffering(value: String?) = param("product_offering", value)
    fun metroAreaNetwork(value: String?) = param("metro_area_network", value)
}

/**
 * Filter builder for network service configs
 */
class NetworkServiceConfigFilterBuilder : QueryBuilder() {
    fun type(value: String?) = param("type", value)
    fun networkService(value: String?) = param("network_service", value)
    fun connection(value: String?) = param("connection", value)
}

/**
 * Filter builder for product offerings
 */
class ProductOfferingFilterBuilder : QueryBuilder() {
    fun type(value: String?) = param("type", value)
    fun serviceMetroArea(value: String?) = param("service_metro_area", value)
    fun serviceMetroAreaNetwork(value: String?) = param("service_metro_area_network", value)
    fun handoverMetroArea(value: String?) = param("handover_metro_area", value)
    fun handoverMetroAreaNetwork(value: String?) = param("handover_metro_area_network", value)
}
