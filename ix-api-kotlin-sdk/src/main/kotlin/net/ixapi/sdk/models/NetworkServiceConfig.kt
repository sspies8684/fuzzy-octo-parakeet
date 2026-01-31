package net.ixapi.sdk.models

import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * A NetworkServiceConfig is a customer's configuration for usage of a NetworkService.
 */
@Serializable
data class NetworkServiceConfig(
    val id: String,
    val type: NetworkServiceConfigType,
    val state: ResourceState,
    
    @SerialName("network_service")
    val networkService: String,
    
    val connection: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
    @SerialName("role_assignments")
    val roleAssignments: List<String>,
    
    val status: List<Status>? = null,
    
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
    
    @SerialName("vlan_config")
    val vlanConfig: JsonObject? = null,
    
    // Exchange LAN specific
    val asns: List<Int>? = null,
    
    @SerialName("mac_addresses")
    val macAddresses: List<String>? = null,
    
    val ips: List<String>? = null,
    
    @SerialName("listed")
    val listed: Boolean? = null,
    
    @SerialName("peeringdb_netixlan_id")
    val peeringdbNetixlanId: Int? = null,
    
    // P2P/P2MP/MP2MP/Cloud specific
    val capacity: Int? = null,
    
    @SerialName("capacity_min")
    val capacityMin: Int? = null,
    
    @SerialName("capacity_max")
    val capacityMax: Int? = null,
    
    // P2P specific
    @SerialName("role")
    val role: String? = null,
    
    // Cloud specific  
    @SerialName("cloud_vlan")
    val cloudVlan: Int? = null,
    
    @SerialName("handover")
    val handover: Int? = null,
    
    @SerialName("handover_metro_area")
    val handoverMetroArea: String? = null,
    
    @SerialName("handover_metro_area_network")
    val handoverMetroAreaNetwork: String? = null
)

/**
 * Exchange LAN Network Service Config
 */
@Serializable
data class ExchangeLanNetworkServiceConfig(
    val id: String,
    val type: String = "exchange_lan",
    val state: ResourceState,
    
    @SerialName("network_service")
    val networkService: String,
    
    val connection: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
    @SerialName("role_assignments")
    val roleAssignments: List<String>,
    
    @SerialName("vlan_config")
    val vlanConfig: JsonObject? = null,
    
    val asns: List<Int>? = null,
    
    @SerialName("mac_addresses")
    val macAddresses: List<String>? = null,
    
    val ips: List<String>? = null,
    
    val listed: Boolean? = null,
    
    @SerialName("peeringdb_netixlan_id")
    val peeringdbNetixlanId: Int? = null,
    
    val status: List<Status>? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null
)

/**
 * P2P Network Service Config
 */
@Serializable
data class P2PNetworkServiceConfig(
    val id: String,
    val type: String = "p2p_vc",
    val state: ResourceState,
    
    @SerialName("network_service")
    val networkService: String,
    
    val connection: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
    @SerialName("role_assignments")
    val roleAssignments: List<String>,
    
    @SerialName("vlan_config")
    val vlanConfig: JsonObject? = null,
    
    val capacity: Int? = null,
    val role: String? = null,
    
    val status: List<Status>? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null
)

/**
 * MP2MP Network Service Config
 */
@Serializable
data class MP2MPNetworkServiceConfig(
    val id: String,
    val type: String = "mp2mp_vc",
    val state: ResourceState,
    
    @SerialName("network_service")
    val networkService: String,
    
    val connection: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
    @SerialName("role_assignments")
    val roleAssignments: List<String>,
    
    @SerialName("vlan_config")
    val vlanConfig: JsonObject? = null,
    
    val capacity: Int? = null,
    
    val status: List<Status>? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null
)

/**
 * Cloud Network Service Config
 */
@Serializable
data class CloudNetworkServiceConfig(
    val id: String,
    val type: String = "cloud_vc",
    val state: ResourceState,
    
    @SerialName("network_service")
    val networkService: String,
    
    val connection: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
    @SerialName("role_assignments")
    val roleAssignments: List<String>,
    
    @SerialName("vlan_config")
    val vlanConfig: JsonObject? = null,
    
    val capacity: Int? = null,
    
    @SerialName("cloud_vlan")
    val cloudVlan: Int? = null,
    
    val handover: Int? = null,
    
    @SerialName("handover_metro_area")
    val handoverMetroArea: String? = null,
    
    @SerialName("handover_metro_area_network")
    val handoverMetroAreaNetwork: String? = null,
    
    val status: List<Status>? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null
)

/**
 * Request to create a network service config
 */
@Serializable
data class NetworkServiceConfigRequest(
    val type: NetworkServiceConfigType,
    
    @SerialName("network_service")
    val networkService: String,
    
    val connection: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String? = null,
    
    @SerialName("billing_account")
    val billingAccount: String,
    
    @SerialName("vlan_config")
    val vlanConfig: JsonObject? = null,
    
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
    
    // Exchange LAN specific
    val asns: List<Int>? = null,
    val listed: Boolean? = null,
    
    // P2P/P2MP/MP2MP/Cloud specific
    val capacity: Int? = null,
    val role: String? = null,
    
    // Cloud specific
    @SerialName("cloud_vlan")
    val cloudVlan: Int? = null,
    
    val handover: Int? = null
)

/**
 * Request to update a network service config
 */
@Serializable
data class NetworkServiceConfigUpdate(
    @SerialName("vlan_config")
    val vlanConfig: JsonObject? = null,
    
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
    
    // Exchange LAN specific
    val asns: List<Int>? = null,
    val listed: Boolean? = null,
    
    // P2P/P2MP/MP2MP/Cloud specific  
    val capacity: Int? = null,
    
    // Cloud specific
    @SerialName("cloud_vlan")
    val cloudVlan: Int? = null,
    
    val handover: Int? = null
)

/**
 * Request to patch a network service config
 */
@Serializable
data class NetworkServiceConfigPatch(
    @SerialName("vlan_config")
    val vlanConfig: JsonObject? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("purchase_order")
    val purchaseOrder: String? = null,
    
    @SerialName("contract_ref")
    val contractRef: String? = null,
    
    val asns: List<Int>? = null,
    val listed: Boolean? = null,
    val capacity: Int? = null,
    
    @SerialName("cloud_vlan")
    val cloudVlan: Int? = null,
    
    val handover: Int? = null
)
