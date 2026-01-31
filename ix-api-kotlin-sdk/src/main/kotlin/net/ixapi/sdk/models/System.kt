package net.ixapi.sdk.models

import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * API Health status response implementing RFC draft health check.
 */
@Serializable
data class ApiHealth(
    val status: HealthStatus,
    val version: String? = null,
    
    @SerialName("release_id")
    val releaseId: String? = null,
    
    val notes: List<String>? = null,
    val output: String? = null,
    
    @SerialName("service_id")
    val serviceId: String? = null,
    
    val description: String? = null,
    val checks: JsonObject? = null,
    val links: JsonObject? = null
)

/**
 * API Implementation information
 */
@Serializable
data class ApiImplementation(
    val version: String,
    
    @SerialName("schema_version")
    val schemaVersion: String,
    
    @SerialName("schema_revision")
    val schemaRevision: String? = null,
    
    val features: ApiFeatures? = null,
    val operations: List<String>? = null,
    
    @SerialName("product_offering_types")
    val productOfferingTypes: List<String>? = null,
    
    @SerialName("network_service_types")
    val networkServiceTypes: List<String>? = null,
    
    @SerialName("network_service_config_types")
    val networkServiceConfigTypes: List<String>? = null,
    
    @SerialName("network_feature_types")
    val networkFeatureTypes: List<String>? = null,
    
    @SerialName("network_feature_config_types")
    val networkFeatureConfigTypes: List<String>? = null
)

/**
 * API Features supported
 */
@Serializable
data class ApiFeatures(
    val pagination: Boolean = false,
    val filtering: Boolean = false,
    val sorting: Boolean = false,
    
    @SerialName("account_hierarchy")
    val accountHierarchy: Boolean = false,
    
    @SerialName("managing_accounts")
    val managingAccounts: Boolean = false,
    
    @SerialName("consuming_accounts")
    val consumingAccounts: Boolean = false,
    
    @SerialName("billing_accounts")
    val billingAccounts: Boolean = false,
    
    @SerialName("role_assignments")
    val roleAssignments: Boolean = false
)

/**
 * API Extension information
 */
@Serializable
data class ApiExtension(
    val name: String,
    val version: String,
    val description: String? = null,
    val url: String? = null
)

/**
 * Cancellation policy for a resource
 */
@Serializable
data class CancellationPolicy(
    @SerialName("decommission_at")
    val decommissionAt: String? = null,
    
    @SerialName("charged_until")
    val chargedUntil: String? = null
)

/**
 * Request to cancel/decommission a resource
 */
@Serializable
data class CancellationRequest(
    @SerialName("decommission_at")
    val decommissionAt: String? = null
)

/**
 * Event/webhook payload
 */
@Serializable
data class Event(
    val id: String,
    val type: String,
    val timestamp: String,
    val resource: JsonObject? = null,
    
    @SerialName("resource_type")
    val resourceType: String? = null,
    
    @SerialName("resource_id")
    val resourceId: String? = null
)

/**
 * Conflict error response
 */
@Serializable
data class Conflict(
    val id: String? = null,
    val type: String? = null,
    val message: String? = null
)
