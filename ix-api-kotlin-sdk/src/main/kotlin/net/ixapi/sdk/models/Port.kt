package net.ixapi.sdk.models

import kotlinx.serialization.*

/**
 * A Port is the point at which subscriber and IXP networks meet.
 * A port is always associated with a device and pop, has a speed and a media_type.
 */
@Serializable
data class Port(
    val id: String,
    val state: ResourceState,
    val device: String,
    val pop: String,
    
    @SerialName("media_type")
    val mediaType: String,
    
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
    
    val connection: String? = null,
    val speed: Int? = null,
    val name: String? = null,
    
    @SerialName("operational_state")
    val operationalState: OperationalState? = null
)

/**
 * Port statistics
 */
@Serializable
data class PortStatistics(
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
    val discardsOut: Long? = null,
    
    val timestamp: String? = null
)

/**
 * A PortReservation expresses the intent to include a Port in a connection.
 */
@Serializable
data class PortReservation(
    val id: String,
    val state: ResourceState,
    val connection: String,
    
    @SerialName("subscriber_side_demarc")
    val subscriberSideDemarc: String? = null,
    
    val port: String? = null,
    val status: List<Status>? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("decommission_at")
    val decommissionAt: String? = null,
    
    @SerialName("charged_until")
    val chargedUntil: String? = null
)

/**
 * Request to create a port reservation
 */
@Serializable
data class PortReservationRequest(
    val connection: String,
    
    @SerialName("subscriber_side_demarc")
    val subscriberSideDemarc: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null
)

/**
 * Request to update a port reservation
 */
@Serializable
data class PortReservationUpdate(
    @SerialName("subscriber_side_demarc")
    val subscriberSideDemarc: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null
)

/**
 * Request to patch a port reservation
 */
@Serializable
data class PortReservationPatch(
    @SerialName("subscriber_side_demarc")
    val subscriberSideDemarc: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null
)
