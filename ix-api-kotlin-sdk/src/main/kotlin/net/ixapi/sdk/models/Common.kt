package net.ixapi.sdk.models

import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * Common enums and types used across the IX-API SDK
 */

/**
 * State of a resource
 */
@Serializable
enum class ResourceState {
    @SerialName("requested") REQUESTED,
    @SerialName("allocated") ALLOCATED,
    @SerialName("testing") TESTING,
    @SerialName("production") PRODUCTION,
    @SerialName("production_change_pending") PRODUCTION_CHANGE_PENDING,
    @SerialName("decommission_requested") DECOMMISSION_REQUESTED,
    @SerialName("decommissioned") DECOMMISSIONED,
    @SerialName("archived") ARCHIVED,
    @SerialName("error") ERROR,
    @SerialName("cancelled") CANCELLED,
    @SerialName("operator") OPERATOR,
    @SerialName("scheduled") SCHEDULED
}

/**
 * Status message for a resource
 */
@Serializable
data class Status(
    val severity: StatusSeverity,
    val tag: String,
    val message: String? = null,
    val attrs: JsonObject? = null,
    val timestamp: String? = null
)

@Serializable
enum class StatusSeverity {
    @SerialName("info") INFO,
    @SerialName("warning") WARNING,
    @SerialName("error") ERROR
}

/**
 * VLAN configuration types
 */
@Serializable
enum class VlanType {
    @SerialName("dot1q") DOT1Q,
    @SerialName("qinq") QINQ,
    @SerialName("port") PORT
}

/**
 * LACP timeout values
 */
@Serializable
enum class LacpTimeout {
    @SerialName("slow") SLOW,
    @SerialName("fast") FAST
}

/**
 * Connection mode
 */
@Serializable
enum class ConnectionMode {
    @SerialName("lag_lacp") LAG_LACP,
    @SerialName("lag_static") LAG_STATIC,
    @SerialName("standalone") STANDALONE
}

/**
 * Operational state of a port
 */
@Serializable
enum class OperationalState {
    @SerialName("up") UP,
    @SerialName("down") DOWN
}

/**
 * IP address version
 */
@Serializable
enum class IpVersion(val value: Int) {
    @SerialName("4") V4(4),
    @SerialName("6") V6(6)
}

/**
 * Aggregate time period for statistics
 */
@Serializable
enum class Aggregate {
    @SerialName("fivemin") FIVE_MIN,
    @SerialName("hourly") HOURLY,
    @SerialName("daily") DAILY,
    @SerialName("weekly") WEEKLY,
    @SerialName("monthly") MONTHLY
}

/**
 * Product offering types
 */
@Serializable
enum class ProductOfferingType {
    @SerialName("connection") CONNECTION,
    @SerialName("exchange_lan") EXCHANGE_LAN,
    @SerialName("p2p_vc") P2P_VC,
    @SerialName("p2mp_vc") P2MP_VC,
    @SerialName("mp2mp_vc") MP2MP_VC,
    @SerialName("cloud_vc") CLOUD_VC,
    @SerialName("routing_function") ROUTING_FUNCTION
}

/**
 * Network service types
 */
@Serializable
enum class NetworkServiceType {
    @SerialName("exchange_lan") EXCHANGE_LAN,
    @SerialName("p2p_vc") P2P_VC,
    @SerialName("p2mp_vc") P2MP_VC,
    @SerialName("mp2mp_vc") MP2MP_VC,
    @SerialName("cloud_vc") CLOUD_VC
}

/**
 * Network service config types
 */
@Serializable
enum class NetworkServiceConfigType {
    @SerialName("exchange_lan") EXCHANGE_LAN,
    @SerialName("p2p_vc") P2P_VC,
    @SerialName("p2mp_vc") P2MP_VC,
    @SerialName("mp2mp_vc") MP2MP_VC,
    @SerialName("cloud_vc") CLOUD_VC
}

/**
 * Member joining rule types
 */
@Serializable
enum class MemberJoiningRuleType {
    @SerialName("allow") ALLOW,
    @SerialName("deny") DENY
}

/**
 * Outer VLAN ethertypes
 */
@Serializable
enum class OuterVlanEthertype {
    @SerialName("0x8100") ETHERTYPE_8100,
    @SerialName("0x88a8") ETHERTYPE_88A8
}

/**
 * Health check status
 */
@Serializable
enum class HealthStatus {
    @SerialName("pass") PASS,
    @SerialName("warn") WARN,
    @SerialName("fail") FAIL
}
