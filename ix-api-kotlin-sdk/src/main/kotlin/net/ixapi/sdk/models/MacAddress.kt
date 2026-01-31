package net.ixapi.sdk.models

import kotlinx.serialization.*

/**
 * A MAC is a MAC address with a given validity period.
 * Some services require MAC addresses to work.
 * Only unicast MAC addresses are allowed.
 */
@Serializable
data class MacAddress(
    val id: String,
    val address: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    @SerialName("valid_not_before")
    val validNotBefore: String? = null,
    
    @SerialName("valid_not_after")
    val validNotAfter: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null
)

/**
 * Request to create a MAC address
 */
@Serializable
data class MacAddressRequest(
    val address: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("valid_not_before")
    val validNotBefore: String? = null,
    
    @SerialName("valid_not_after")
    val validNotAfter: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("managing_account")
    val managingAccount: String? = null
)
