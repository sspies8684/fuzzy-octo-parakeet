package net.ixapi.sdk.models

import kotlinx.serialization.*

/**
 * An IP is a IPv4 or 6 address, with a given validity period.
 * Some services require IP addresses to work.
 */
@Serializable
data class IpAddress(
    val id: String,
    val address: String,
    val version: Int,
    
    @SerialName("prefix_length")
    val prefixLength: Int,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    val fqdn: String? = null,
    
    @SerialName("valid_not_before")
    val validNotBefore: String? = null,
    
    @SerialName("valid_not_after")
    val validNotAfter: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null
)

/**
 * Shortened IP address representation
 */
@Serializable
data class IpAddressShort(
    val id: String,
    val address: String,
    val version: Int,
    
    @SerialName("prefix_length")
    val prefixLength: Int
)

/**
 * Request to create an IP address
 */
@Serializable
data class IpAddressRequest(
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    val fqdn: String? = null,
    
    @SerialName("valid_not_before")
    val validNotBefore: String? = null,
    
    @SerialName("valid_not_after")
    val validNotAfter: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("managing_account")
    val managingAccount: String? = null,
    
    val version: Int? = null
)

/**
 * Request to update an IP address
 */
@Serializable
data class IpAddressUpdate(
    val fqdn: String? = null,
    
    @SerialName("valid_not_before")
    val validNotBefore: String? = null,
    
    @SerialName("valid_not_after")
    val validNotAfter: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("managing_account")
    val managingAccount: String? = null
)

/**
 * Request to patch an IP address
 */
@Serializable
data class IpAddressPatch(
    val fqdn: String? = null,
    
    @SerialName("valid_not_before")
    val validNotBefore: String? = null,
    
    @SerialName("valid_not_after")
    val validNotAfter: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null
)
