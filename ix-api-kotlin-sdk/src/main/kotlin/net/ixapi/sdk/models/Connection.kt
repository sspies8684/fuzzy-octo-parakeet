package net.ixapi.sdk.models

import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * A Connection is a functional group of physical ports collected together into a LAG (aka trunk).
 */
@Serializable
data class Connection(
    val id: String,
    val state: ResourceState,
    val name: String,
    val pop: String,
    
    @SerialName("product_offering")
    val productOffering: String,
    
    val mode: ConnectionMode,
    
    @SerialName("port_quantity")
    val portQuantity: Int,
    
    @SerialName("outer_vlan_ethertypes")
    val outerVlanEthertypes: List<OuterVlanEthertype>,
    
    @SerialName("vlan_types")
    val vlanTypes: List<VlanType>,
    
    @SerialName("capacity_allocated")
    val capacityAllocated: Int,
    
    @SerialName("capacity_allocation_limit")
    val capacityAllocationLimit: Int,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
    @SerialName("role_assignments")
    val roleAssignments: List<String>,
    
    val status: List<Status>? = null,
    
    @SerialName("decommission_at")
    val decommissionAt: String? = null,
    
    @SerialName("charged_until")
    val chargedUntil: String? = null,
    
    @SerialName("current_billing_start_date")
    val currentBillingStartDate: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null,
    
    @SerialName("lacp_timeout")
    val lacpTimeout: LacpTimeout? = null,
    
    val ports: List<String>? = null,
    
    @SerialName("port_reservations")
    val portReservations: List<String>? = null,
    
    val speed: Int? = null,
    
    @SerialName("subscriber_side_demarcs")
    val subscriberSideDemarcs: List<String>? = null,
    
    @SerialName("metro_area")
    val metroArea: String? = null,
    
    @SerialName("metro_area_network")
    val metroAreaNetwork: String? = null
)

/**
 * Request to create a new connection
 */
@Serializable
data class ConnectionRequest(
    @SerialName("product_offering")
    val productOffering: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
    @SerialName("port_quantity")
    val portQuantity: Int = 1,
    
    val mode: ConnectionMode = ConnectionMode.LAG_LACP,
    
    val name: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null,
    
    @SerialName("role_assignments")
    val roleAssignments: List<RoleAssignmentRequest>? = null,
    
    @SerialName("lacp_timeout")
    val lacpTimeout: LacpTimeout? = null,
    
    @SerialName("managing_account")
    val managingAccount: String? = null,
    
    @SerialName("subscriber_side_demarcs")
    val subscriberSideDemarcs: List<String>? = null
)

/**
 * Request to update a connection
 */
@Serializable
data class ConnectionUpdate(
    val name: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null,
    
    val mode: ConnectionMode? = null,
    
    @SerialName("lacp_timeout")
    val lacpTimeout: LacpTimeout? = null,
    
    @SerialName("managing_account")
    val managingAccount: String? = null,
    
    @SerialName("billing_account")
    val billingAccount: String? = null
)

/**
 * Request to patch a connection
 */
@Serializable
data class ConnectionPatch(
    val name: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null,
    
    val mode: ConnectionMode? = null,
    
    @SerialName("lacp_timeout")
    val lacpTimeout: LacpTimeout? = null
)
