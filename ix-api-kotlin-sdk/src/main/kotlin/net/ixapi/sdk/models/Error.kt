package net.ixapi.sdk.models

import kotlinx.serialization.*

/**
 * Problem response following RFC 7807
 */
@Serializable
data class ProblemResponse(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val instance: String? = null,
    val properties: List<ValidationErrorProperty>? = null
)

/**
 * Validation error property details
 */
@Serializable
data class ValidationErrorProperty(
    val name: String,
    val reason: String
)

/**
 * Device connection information
 */
@Serializable
data class DeviceConnection(
    val device: String,
    val pop: String,
    val facility: String,
    
    @SerialName("metro_area_network")
    val metroAreaNetwork: String? = null
)

/**
 * Service Exchange PoP info
 */
@Serializable
data class ServiceExchangePop(
    val pop: String,
    val facility: String,
    val device: String? = null,
    
    @SerialName("cross_connect_id")
    val crossConnectId: String? = null
)

/**
 * L3 Configuration
 */
@Serializable
data class L3Config(
    @SerialName("ip_v4")
    val ipV4: String? = null,
    
    @SerialName("ip_v6")
    val ipV6: String? = null,
    
    @SerialName("subnet_v4")
    val subnetV4: String? = null,
    
    @SerialName("subnet_v6")
    val subnetV6: String? = null,
    
    @SerialName("gateway_v4")
    val gatewayV4: String? = null,
    
    @SerialName("gateway_v6")
    val gatewayV6: String? = null,
    
    val mtu: Int? = null,
    
    @SerialName("bgp_enabled")
    val bgpEnabled: Boolean? = null,
    
    @SerialName("bgp_local_asn")
    val bgpLocalAsn: Int? = null,
    
    @SerialName("bgp_remote_asn")
    val bgpRemoteAsn: Int? = null
)
