package net.ixapi.sdk.models

import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * Base Network Service - A NetworkService represents an instance of a ProductOffering.
 */
@Serializable
data class NetworkService(
    val id: String,
    val type: NetworkServiceType,
    val state: ResourceState,
    
    @SerialName("product_offering")
    val productOffering: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
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
    
    @SerialName("network_features")
    val networkFeatures: List<String>? = null,
    
    @SerialName("metro_area_network")
    val metroAreaNetwork: String? = null,
    
    @SerialName("nsc_required_contact_roles")
    val nscRequiredContactRoles: List<String>? = null,
    
    // Exchange LAN specific
    @SerialName("peeringdb_ixid")
    val peeringdbIxid: Int? = null,
    
    @SerialName("ixfdb_ixid")
    val ixfdbIxid: Int? = null,
    
    @SerialName("public_ip_address_assignment_mode")
    val publicIpAddressAssignmentMode: String? = null,
    
    // P2P/P2MP/MP2MP specific
    val capacity: Int? = null,
    
    @SerialName("capacity_min")
    val capacityMin: Int? = null,
    
    @SerialName("capacity_max")
    val capacityMax: Int? = null,
    
    // Cloud specific
    @SerialName("cloud_key")
    val cloudKey: String? = null,
    
    val diversity: Int? = null
)

/**
 * Exchange LAN Network Service
 */
@Serializable
data class ExchangeLanNetworkService(
    val id: String,
    val type: String = "exchange_lan",
    val state: ResourceState,
    
    @SerialName("product_offering")
    val productOffering: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    @SerialName("metro_area_network")
    val metroAreaNetwork: String,
    
    @SerialName("network_features")
    val networkFeatures: List<String>,
    
    @SerialName("peeringdb_ixid")
    val peeringdbIxid: Int? = null,
    
    @SerialName("ixfdb_ixid")
    val ixfdbIxid: Int? = null,
    
    val status: List<Status>? = null,
    val name: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("nsc_required_contact_roles")
    val nscRequiredContactRoles: List<String>? = null
)

/**
 * P2P (Point-to-Point) Network Service
 */
@Serializable
data class P2PNetworkService(
    val id: String,
    val type: String = "p2p_vc",
    val state: ResourceState,
    
    @SerialName("product_offering")
    val productOffering: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
    val capacity: Int? = null,
    
    @SerialName("capacity_min")
    val capacityMin: Int? = null,
    
    @SerialName("capacity_max")
    val capacityMax: Int? = null,
    
    val status: List<Status>? = null,
    val name: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null,
    
    @SerialName("network_features")
    val networkFeatures: List<String>? = null,
    
    @SerialName("metro_area_network")
    val metroAreaNetwork: String? = null,
    
    @SerialName("nsc_required_contact_roles")
    val nscRequiredContactRoles: List<String>? = null
)

/**
 * P2MP (Point-to-Multipoint) Network Service  
 */
@Serializable
data class P2MPNetworkService(
    val id: String,
    val type: String = "p2mp_vc",
    val state: ResourceState,
    
    @SerialName("product_offering")
    val productOffering: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
    val capacity: Int? = null,
    
    @SerialName("capacity_min")
    val capacityMin: Int? = null,
    
    @SerialName("capacity_max")
    val capacityMax: Int? = null,
    
    val status: List<Status>? = null,
    val name: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null,
    
    @SerialName("network_features")
    val networkFeatures: List<String>? = null,
    
    @SerialName("metro_area_network")
    val metroAreaNetwork: String? = null,
    
    @SerialName("nsc_required_contact_roles")
    val nscRequiredContactRoles: List<String>? = null,
    
    val public: Boolean? = null,
    
    @SerialName("member_joining_rules")
    val memberJoiningRules: List<String>? = null
)

/**
 * MP2MP (Multipoint-to-Multipoint) Network Service
 */
@Serializable
data class MP2MPNetworkService(
    val id: String,
    val type: String = "mp2mp_vc",
    val state: ResourceState,
    
    @SerialName("product_offering")
    val productOffering: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
    val capacity: Int? = null,
    
    @SerialName("capacity_min")
    val capacityMin: Int? = null,
    
    @SerialName("capacity_max")
    val capacityMax: Int? = null,
    
    val status: List<Status>? = null,
    val name: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null,
    
    @SerialName("network_features")
    val networkFeatures: List<String>? = null,
    
    @SerialName("metro_area_network")
    val metroAreaNetwork: String? = null,
    
    @SerialName("nsc_required_contact_roles")
    val nscRequiredContactRoles: List<String>? = null,
    
    val public: Boolean? = null,
    
    @SerialName("member_joining_rules")
    val memberJoiningRules: List<String>? = null
)

/**
 * Cloud Network Service
 */
@Serializable
data class CloudNetworkService(
    val id: String,
    val type: String = "cloud_vc",
    val state: ResourceState,
    
    @SerialName("product_offering")
    val productOffering: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
    @SerialName("cloud_key")
    val cloudKey: String? = null,
    
    val capacity: Int? = null,
    
    @SerialName("capacity_min")
    val capacityMin: Int? = null,
    
    @SerialName("capacity_max")
    val capacityMax: Int? = null,
    
    val diversity: Int? = null,
    
    val status: List<Status>? = null,
    val name: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null,
    
    @SerialName("network_features")
    val networkFeatures: List<String>? = null,
    
    @SerialName("metro_area_network")
    val metroAreaNetwork: String? = null,
    
    @SerialName("nsc_required_contact_roles")
    val nscRequiredContactRoles: List<String>? = null
)

/**
 * Request to create a network service
 */
@Serializable
data class NetworkServiceRequest(
    val type: NetworkServiceType,
    
    @SerialName("product_offering")
    val productOffering: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
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
    val roleAssignments: List<RoleAssignmentRequest>? = null,
    
    // Type-specific fields
    val capacity: Int? = null,
    val public: Boolean? = null,
    
    @SerialName("cloud_key")
    val cloudKey: String? = null,
    
    val diversity: Int? = null
)

/**
 * Request to update a network service
 */
@Serializable
data class NetworkServiceUpdate(
    val name: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null,
    
    @SerialName("managing_account")
    val managingAccount: String? = null,
    
    @SerialName("billing_account")
    val billingAccount: String? = null,
    
    val capacity: Int? = null,
    val public: Boolean? = null
)

/**
 * Request to patch a network service
 */
@Serializable
data class NetworkServicePatch(
    val name: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null,
    
    val capacity: Int? = null,
    val public: Boolean? = null
)

/**
 * Response when deleting a network service
 */
@Serializable
data class NetworkServiceDeleteResponse(
    val id: String,
    val state: ResourceState,
    
    @SerialName("decommission_at")
    val decommissionAt: String? = null
)

/**
 * Network service change request
 */
@Serializable
data class NetworkServiceChangeRequest(
    val capacity: Int? = null,
    val public: Boolean? = null
)
