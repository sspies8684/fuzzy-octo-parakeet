package net.ixapi.sdk.models

import kotlinx.serialization.*

/**
 * Physical address
 */
@Serializable
data class Address(
    @SerialName("country")
    val country: String? = null,
    
    @SerialName("locality")
    val locality: String? = null,
    
    @SerialName("region")
    val region: String? = null,
    
    @SerialName("postal_code")
    val postalCode: String? = null,
    
    @SerialName("street_address")
    val streetAddress: String? = null,
    
    @SerialName("post_office_box_number")
    val postOfficeBoxNumber: String? = null
)

/**
 * Billing information
 */
@Serializable
data class BillingInformation(
    @SerialName("name")
    val name: String? = null,
    
    @SerialName("address")
    val address: Address? = null,
    
    @SerialName("vat_number")
    val vatNumber: String? = null
)
