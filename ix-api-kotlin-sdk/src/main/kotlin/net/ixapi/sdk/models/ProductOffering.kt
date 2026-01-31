package net.ixapi.sdk.models

import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * A ProductOffering is an offer made by an exchange to be consumed by a client.
 */
@Serializable
data class ProductOffering(
    val id: String,
    val type: ProductOfferingType,
    val name: String,
    
    @SerialName("display_name")
    val displayName: String? = null,
    
    @SerialName("service_provider")
    val serviceProvider: String? = null,
    
    val handover: Int? = null,
    
    @SerialName("handover_media_type")
    val handoverMediaType: String? = null,
    
    @SerialName("physical_port_speed")
    val physicalPortSpeed: Int? = null,
    
    @SerialName("service_metro_area")
    val serviceMetroArea: String? = null,
    
    @SerialName("service_metro_area_network")
    val serviceMetroAreaNetwork: String? = null,
    
    @SerialName("bandwidth_min")
    val bandwidthMin: Int? = null,
    
    @SerialName("bandwidth_max")
    val bandwidthMax: Int? = null,
    
    @SerialName("exchange_lan_network_service")
    val exchangeLanNetworkService: String? = null,
    
    @SerialName("downgrade_allowed")
    val downgradeAllowed: Boolean? = null,
    
    @SerialName("upgrade_allowed")
    val upgradeAllowed: Boolean? = null,
    
    @SerialName("delivery_method")
    val deliveryMethod: String? = null,
    
    @SerialName("resource_type")
    val resourceType: String? = null,
    
    @SerialName("provider_vlans")
    val providerVlans: String? = null,
    
    @SerialName("cloud_config")
    val cloudConfig: CloudConfig? = null
)

/**
 * Connection product offering
 */
@Serializable
data class ConnectionProductOffering(
    val id: String,
    val type: String = "connection",
    val name: String,
    
    @SerialName("display_name")
    val displayName: String? = null,
    
    @SerialName("service_provider")
    val serviceProvider: String? = null,
    
    @SerialName("handover_metro_area")
    val handoverMetroArea: String? = null,
    
    @SerialName("handover_metro_area_network")
    val handoverMetroAreaNetwork: String? = null,
    
    @SerialName("physical_port_speed")
    val physicalPortSpeed: Int? = null,
    
    @SerialName("handover_media_type")
    val handoverMediaType: String? = null,
    
    @SerialName("cross_connect_initiator")
    val crossConnectInitiator: String? = null,
    
    @SerialName("handover_pop")
    val handoverPop: String? = null,
    
    @SerialName("max_lag")
    val maxLag: Int? = null,
    
    @SerialName("service_provider_region")
    val serviceProviderRegion: String? = null,
    
    @SerialName("service_provider_pop_id")
    val serviceProviderPopId: String? = null,
    
    @SerialName("service_provider_workflow")
    val serviceProviderWorkflow: String? = null
)

/**
 * Exchange LAN network product offering
 */
@Serializable
data class ExchangeLanNetworkProductOffering(
    val id: String,
    val type: String = "exchange_lan",
    val name: String,
    
    @SerialName("display_name")
    val displayName: String? = null,
    
    @SerialName("exchange_lan_network_service")
    val exchangeLanNetworkService: String? = null,
    
    @SerialName("service_provider")
    val serviceProvider: String? = null,
    
    @SerialName("service_metro_area")
    val serviceMetroArea: String? = null,
    
    @SerialName("service_metro_area_network")
    val serviceMetroAreaNetwork: String? = null,
    
    @SerialName("downgrade_allowed")
    val downgradeAllowed: Boolean? = null,
    
    @SerialName("upgrade_allowed")
    val upgradeAllowed: Boolean? = null,
    
    @SerialName("bandwidth_min")
    val bandwidthMin: Int? = null,
    
    @SerialName("bandwidth_max")
    val bandwidthMax: Int? = null
)

/**
 * P2P Network product offering
 */
@Serializable
data class P2PNetworkProductOffering(
    val id: String,
    val type: String = "p2p_vc",
    val name: String,
    
    @SerialName("display_name")
    val displayName: String? = null,
    
    @SerialName("service_provider")
    val serviceProvider: String? = null,
    
    @SerialName("service_metro_area")
    val serviceMetroArea: String? = null,
    
    @SerialName("service_metro_area_network")
    val serviceMetroAreaNetwork: String? = null,
    
    @SerialName("bandwidth_min")
    val bandwidthMin: Int? = null,
    
    @SerialName("bandwidth_max")
    val bandwidthMax: Int? = null,
    
    @SerialName("downgrade_allowed")
    val downgradeAllowed: Boolean? = null,
    
    @SerialName("upgrade_allowed")
    val upgradeAllowed: Boolean? = null
)

/**
 * P2MP Network product offering
 */
@Serializable
data class P2MPNetworkProductOffering(
    val id: String,
    val type: String = "p2mp_vc",
    val name: String,
    
    @SerialName("display_name")
    val displayName: String? = null,
    
    @SerialName("service_provider")
    val serviceProvider: String? = null,
    
    @SerialName("service_metro_area")
    val serviceMetroArea: String? = null,
    
    @SerialName("service_metro_area_network")
    val serviceMetroAreaNetwork: String? = null,
    
    @SerialName("bandwidth_min")
    val bandwidthMin: Int? = null,
    
    @SerialName("bandwidth_max")
    val bandwidthMax: Int? = null,
    
    @SerialName("downgrade_allowed")
    val downgradeAllowed: Boolean? = null,
    
    @SerialName("upgrade_allowed")
    val upgradeAllowed: Boolean? = null
)

/**
 * MP2MP Network product offering
 */
@Serializable
data class MP2MPNetworkProductOffering(
    val id: String,
    val type: String = "mp2mp_vc",
    val name: String,
    
    @SerialName("display_name")
    val displayName: String? = null,
    
    @SerialName("service_provider")
    val serviceProvider: String? = null,
    
    @SerialName("service_metro_area")
    val serviceMetroArea: String? = null,
    
    @SerialName("service_metro_area_network")
    val serviceMetroAreaNetwork: String? = null,
    
    @SerialName("bandwidth_min")
    val bandwidthMin: Int? = null,
    
    @SerialName("bandwidth_max")
    val bandwidthMax: Int? = null,
    
    @SerialName("downgrade_allowed")
    val downgradeAllowed: Boolean? = null,
    
    @SerialName("upgrade_allowed")
    val upgradeAllowed: Boolean? = null
)

/**
 * Cloud network product offering
 */
@Serializable
data class CloudNetworkProductOffering(
    val id: String,
    val type: String = "cloud_vc",
    val name: String,
    
    @SerialName("display_name")
    val displayName: String? = null,
    
    @SerialName("service_provider")
    val serviceProvider: String? = null,
    
    @SerialName("service_metro_area")
    val serviceMetroArea: String? = null,
    
    @SerialName("service_metro_area_network")
    val serviceMetroAreaNetwork: String? = null,
    
    @SerialName("bandwidth_min")
    val bandwidthMin: Int? = null,
    
    @SerialName("bandwidth_max")
    val bandwidthMax: Int? = null,
    
    @SerialName("downgrade_allowed")
    val downgradeAllowed: Boolean? = null,
    
    @SerialName("upgrade_allowed")
    val upgradeAllowed: Boolean? = null,
    
    @SerialName("provider_vlans")
    val providerVlans: String? = null,
    
    @SerialName("cloud_config")
    val cloudConfig: CloudConfig? = null,
    
    @SerialName("delivery_method")
    val deliveryMethod: String? = null
)

/**
 * Routing function product offering
 */
@Serializable
data class RoutingFunctionProductOffering(
    val id: String,
    val type: String = "routing_function",
    val name: String,
    
    @SerialName("display_name")
    val displayName: String? = null,
    
    @SerialName("service_provider")
    val serviceProvider: String? = null,
    
    @SerialName("service_metro_area")
    val serviceMetroArea: String? = null,
    
    @SerialName("service_metro_area_network")
    val serviceMetroAreaNetwork: String? = null
)

/**
 * Cloud configuration
 */
@Serializable
data class CloudConfig(
    @SerialName("cloud_service_provider")
    val cloudServiceProvider: String? = null,
    
    @SerialName("cloud_service_type")
    val cloudServiceType: String? = null,
    
    @SerialName("cloud_service_region")
    val cloudServiceRegion: String? = null,
    
    @SerialName("cloud_service_regions")
    val cloudServiceRegions: List<String>? = null
)
