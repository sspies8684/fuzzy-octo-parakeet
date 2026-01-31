package net.ixapi.sdk.models

import kotlinx.serialization.*

/**
 * Aggregate statistics data
 */
@Serializable
data class AggregateStatistics(
    val timestamp: String,
    
    @SerialName("bytes_in")
    val bytesIn: Long? = null,
    
    @SerialName("bytes_out")
    val bytesOut: Long? = null,
    
    @SerialName("packets_in")
    val packetsIn: Long? = null,
    
    @SerialName("packets_out")
    val packetsOut: Long? = null,
    
    @SerialName("errors_in")
    val errorsIn: Long? = null,
    
    @SerialName("errors_out")
    val errorsOut: Long? = null,
    
    @SerialName("discards_in")
    val discardsIn: Long? = null,
    
    @SerialName("discards_out")
    val discardsOut: Long? = null
)

/**
 * Aggregate timeseries data point
 */
@Serializable
data class AggregateTimeseries(
    @SerialName("start")
    val start: String,
    
    @SerialName("end")
    val end: String,
    
    val data: List<AggregateStatistics>
)

/**
 * Network service config aggregate info
 */
@Serializable
data class NetworkServiceConfigAggregate(
    val id: String,
    val aggregate: Aggregate
)

/**
 * Network service config aggregate statistics
 */
@Serializable
data class NetworkServiceConfigAggregateStatistics(
    val id: String,
    val aggregate: Aggregate,
    val statistics: AggregateStatistics
)

/**
 * Peer information for statistics
 */
@Serializable
data class Peer(
    @SerialName("network_service_config")
    val networkServiceConfig: String,
    
    @SerialName("ip_address")
    val ipAddress: String? = null,
    
    @SerialName("mac_address")
    val macAddress: String? = null,
    
    val asn: Int? = null,
    
    @SerialName("account_name")
    val accountName: String? = null
)

/**
 * Peer aggregate data
 */
@Serializable
data class PeerAggregate(
    val peer: Peer,
    val aggregate: Aggregate,
    val timestamp: String,
    
    @SerialName("bytes_in")
    val bytesIn: Long? = null,
    
    @SerialName("bytes_out")
    val bytesOut: Long? = null,
    
    @SerialName("packets_in")
    val packetsIn: Long? = null,
    
    @SerialName("packets_out")
    val packetsOut: Long? = null
)

/**
 * Peer RTT (Round-Trip Time) statistics
 */
@Serializable
data class PeerRTT(
    val peer: Peer,
    val timestamp: String,
    
    @SerialName("rtt_min")
    val rttMin: Double? = null,
    
    @SerialName("rtt_max")
    val rttMax: Double? = null,
    
    @SerialName("rtt_avg")
    val rttAvg: Double? = null,
    
    @SerialName("packet_loss")
    val packetLoss: Double? = null
)

/**
 * Peer timeseries data
 */
@Serializable
data class PeerTimeseries(
    val peer: Peer,
    val aggregate: Aggregate,
    
    @SerialName("start")
    val start: String,
    
    @SerialName("end")
    val end: String,
    
    val data: List<PeerAggregate>
)

/**
 * Shared statistics configuration
 */
@Serializable
sealed class SharedStatisticsConfig {
    abstract val type: String
}

/**
 * Allow statistics sharing
 */
@Serializable
@SerialName("allow")
data class SharedStatisticsConfigAllow(
    override val type: String = "allow",
    
    @SerialName("peer_network_service_config")
    val peerNetworkServiceConfig: String? = null
) : SharedStatisticsConfig()

/**
 * Deny statistics sharing
 */
@Serializable
@SerialName("deny")
data class SharedStatisticsConfigDeny(
    override val type: String = "deny",
    
    @SerialName("peer_network_service_config")
    val peerNetworkServiceConfig: String? = null
) : SharedStatisticsConfig()
