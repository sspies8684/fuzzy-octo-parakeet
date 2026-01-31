package net.ixapi.sdk.models

import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * A NetworkFeature represents additional functionality of a single NetworkService.
 */
@Serializable
data class NetworkFeature(
    val id: String,
    val type: String,
    
    @SerialName("network_service")
    val networkService: String,
    
    val name: String? = null,
    val required: Boolean = false,
    val flags: List<IXPSpecificFeatureFlag>? = null,
    
    // Route Server specific
    val asn: Int? = null,
    val fqdn: String? = null,
    
    @SerialName("looking_glass_url")
    val lookingGlassUrl: String? = null,
    
    @SerialName("address_families")
    val addressFamilies: List<String>? = null,
    
    @SerialName("session_mode")
    val sessionMode: String? = null,
    
    @SerialName("ip_v4")
    val ipV4: String? = null,
    
    @SerialName("ip_v6")
    val ipV6: String? = null,
    
    @SerialName("max_prefix_v4")
    val maxPrefixV4: Int? = null,
    
    @SerialName("max_prefix_v6")
    val maxPrefixV6: Int? = null
)

/**
 * Route Server Network Feature
 */
@Serializable
data class RouteServerNetworkFeature(
    val id: String,
    val type: String = "route_server",
    
    @SerialName("network_service")
    val networkService: String,
    
    val name: String? = null,
    val required: Boolean = false,
    val asn: Int? = null,
    val fqdn: String? = null,
    
    @SerialName("looking_glass_url")
    val lookingGlassUrl: String? = null,
    
    @SerialName("address_families")
    val addressFamilies: List<String>? = null,
    
    @SerialName("session_mode")
    val sessionMode: String? = null,
    
    @SerialName("ip_v4")
    val ipV4: String? = null,
    
    @SerialName("ip_v6")
    val ipV6: String? = null,
    
    @SerialName("max_prefix_v4")
    val maxPrefixV4: Int? = null,
    
    @SerialName("max_prefix_v6")
    val maxPrefixV6: Int? = null
)

/**
 * IXP Specific Feature Flag
 */
@Serializable
data class IXPSpecificFeatureFlag(
    val name: String,
    val mandatory: Boolean = false
)

/**
 * Network Feature Config - configuration for using a NetworkFeature
 */
@Serializable
data class NetworkFeatureConfig(
    val id: String,
    val type: String,
    
    @SerialName("network_feature")
    val networkFeature: String,
    
    @SerialName("network_service_config")
    val networkServiceConfig: String,
    
    val flags: List<IXPSpecificFeatureFlagConfig>? = null,
    
    // Route Server specific
    val asns: List<Int>? = null,
    
    @SerialName("password")
    val password: String? = null,
    
    @SerialName("as_set_v4")
    val asSetV4: String? = null,
    
    @SerialName("as_set_v6")
    val asSetV6: String? = null,
    
    @SerialName("max_prefix_v4")
    val maxPrefixV4: Int? = null,
    
    @SerialName("max_prefix_v6")
    val maxPrefixV6: Int? = null,
    
    @SerialName("ip_v4")
    val ipV4: String? = null,
    
    @SerialName("ip_v6")
    val ipV6: String? = null,
    
    @SerialName("session_mode")
    val sessionMode: String? = null
)

/**
 * Route Server Network Feature Config
 */
@Serializable
data class RouteServerNetworkFeatureConfig(
    val id: String,
    val type: String = "route_server",
    
    @SerialName("network_feature")
    val networkFeature: String,
    
    @SerialName("network_service_config")
    val networkServiceConfig: String,
    
    val asns: List<Int>? = null,
    val password: String? = null,
    
    @SerialName("as_set_v4")
    val asSetV4: String? = null,
    
    @SerialName("as_set_v6")
    val asSetV6: String? = null,
    
    @SerialName("max_prefix_v4")
    val maxPrefixV4: Int? = null,
    
    @SerialName("max_prefix_v6")
    val maxPrefixV6: Int? = null,
    
    @SerialName("ip_v4")
    val ipV4: String? = null,
    
    @SerialName("ip_v6")
    val ipV6: String? = null,
    
    @SerialName("session_mode")
    val sessionMode: String? = null
)

/**
 * IXP Specific Feature Flag Config
 */
@Serializable
data class IXPSpecificFeatureFlagConfig(
    val name: String,
    val enabled: Boolean = false
)

/**
 * Request to create a network feature config
 */
@Serializable
data class NetworkFeatureConfigRequest(
    val type: String,
    
    @SerialName("network_feature")
    val networkFeature: String,
    
    @SerialName("network_service_config")
    val networkServiceConfig: String,
    
    val flags: List<IXPSpecificFeatureFlagConfig>? = null,
    val asns: List<Int>? = null,
    val password: String? = null,
    
    @SerialName("as_set_v4")
    val asSetV4: String? = null,
    
    @SerialName("as_set_v6")
    val asSetV6: String? = null,
    
    @SerialName("max_prefix_v4")
    val maxPrefixV4: Int? = null,
    
    @SerialName("max_prefix_v6")
    val maxPrefixV6: Int? = null,
    
    @SerialName("ip_v4")
    val ipV4: String? = null,
    
    @SerialName("ip_v6")
    val ipV6: String? = null,
    
    @SerialName("session_mode")
    val sessionMode: String? = null
)

/**
 * Request to update a network feature config
 */
@Serializable
data class NetworkFeatureConfigUpdate(
    val flags: List<IXPSpecificFeatureFlagConfig>? = null,
    val asns: List<Int>? = null,
    val password: String? = null,
    
    @SerialName("as_set_v4")
    val asSetV4: String? = null,
    
    @SerialName("as_set_v6")
    val asSetV6: String? = null,
    
    @SerialName("max_prefix_v4")
    val maxPrefixV4: Int? = null,
    
    @SerialName("max_prefix_v6")
    val maxPrefixV6: Int? = null,
    
    @SerialName("ip_v4")
    val ipV4: String? = null,
    
    @SerialName("ip_v6")
    val ipV6: String? = null,
    
    @SerialName("session_mode")
    val sessionMode: String? = null
)

/**
 * Request to patch a network feature config
 */
@Serializable
data class NetworkFeatureConfigPatch(
    val flags: List<IXPSpecificFeatureFlagConfig>? = null,
    val asns: List<Int>? = null,
    val password: String? = null,
    
    @SerialName("as_set_v4")
    val asSetV4: String? = null,
    
    @SerialName("as_set_v6")
    val asSetV6: String? = null,
    
    @SerialName("max_prefix_v4")
    val maxPrefixV4: Int? = null,
    
    @SerialName("max_prefix_v6")
    val maxPrefixV6: Int? = null,
    
    @SerialName("ip_v4")
    val ipV4: String? = null,
    
    @SerialName("ip_v6")
    val ipV6: String? = null,
    
    @SerialName("session_mode")
    val sessionMode: String? = null
)
