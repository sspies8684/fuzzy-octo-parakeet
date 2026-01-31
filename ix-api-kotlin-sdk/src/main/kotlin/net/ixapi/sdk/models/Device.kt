package net.ixapi.sdk.models

import kotlinx.serialization.*

/**
 * A Device is a network hardware device, typically a switch,
 * which is located at a specified facility and inside a PointOfPresence.
 */
@Serializable
data class Device(
    val id: String,
    val name: String,
    val pop: String,
    val facility: String,
    val capabilities: List<DeviceCapability>? = null
)

/**
 * Device capability information
 */
@Serializable
data class DeviceCapability(
    @SerialName("media_type")
    val mediaType: String,
    
    val speed: Int,
    
    @SerialName("max_lag")
    val maxLag: Int? = null
)
