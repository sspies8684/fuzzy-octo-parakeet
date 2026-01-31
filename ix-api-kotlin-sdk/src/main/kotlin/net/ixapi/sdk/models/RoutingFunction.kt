package net.ixapi.sdk.models

import kotlinx.serialization.*

/**
 * Routing functions instances add routing functionality implemented as VPRNs at the IXP.
 * A routing function instance joins two or more independent services into a single routing domain.
 */
@Serializable
data class RoutingFunction(
    val id: String,
    val state: ResourceState,
    
    @SerialName("product_offering")
    val productOffering: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
    @SerialName("role_assignments")
    val roleAssignments: List<String>,
    
    val status: List<Status>? = null,
    val name: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null,
    
    @SerialName("decommission_at")
    val decommissionAt: String? = null,
    
    @SerialName("charged_until")
    val chargedUntil: String? = null,
    
    @SerialName("current_billing_start_date")
    val currentBillingStartDate: String? = null,
    
    @SerialName("network_service_configs")
    val networkServiceConfigs: List<String>? = null,
    
    @SerialName("metro_area_network")
    val metroAreaNetwork: String? = null
)

/**
 * Request to create a routing function
 */
@Serializable
data class RoutingFunctionRequest(
    @SerialName("product_offering")
    val productOffering: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
    @SerialName("network_service_configs")
    val networkServiceConfigs: List<String>,
    
    val name: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null,
    
    @SerialName("managing_account")
    val managingAccount: String? = null,
    
    @SerialName("role_assignments")
    val roleAssignments: List<RoleAssignmentRequest>? = null
)

/**
 * Request to patch a routing function
 */
@Serializable
data class RoutingFunctionPatch(
    val name: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null,
    
    @SerialName("network_service_configs")
    val networkServiceConfigs: List<String>? = null
)
