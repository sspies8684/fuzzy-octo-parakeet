package net.ixapi.sdk.models

import kotlinx.serialization.*

/**
 * An Account represents an individual customer account, organization
 * or partner involved with the IXP.
 */
@Serializable
data class Account(
    val id: String,
    val name: String,
    
    @SerialName("metro_area_network_presence")
    val metroAreaNetworkPresence: List<String>,
    
    @SerialName("legal_name")
    val legalName: String? = null,
    
    @SerialName("billing_information")
    val billingInformation: BillingInformation? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    val discoverable: Boolean? = null,
    val address: Address? = null,
    val asns: List<Int>? = null,
    val state: ResourceState? = null,
    val status: List<Status>? = null,
    
    @SerialName("managing_account")
    val managingAccount: String? = null
)

/**
 * Request to create a new account
 */
@Serializable
data class AccountRequest(
    val name: String,
    
    @SerialName("metro_area_network_presence")
    val metroAreaNetworkPresence: List<String> = emptyList(),
    
    @SerialName("legal_name")
    val legalName: String? = null,
    
    @SerialName("billing_information")
    val billingInformation: BillingInformation? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    val discoverable: Boolean? = null,
    val address: Address? = null,
    val asns: List<Int>? = null,
    
    @SerialName("managing_account")
    val managingAccount: String? = null
)

/**
 * Request to update an account
 */
@Serializable
data class AccountUpdate(
    val name: String? = null,
    
    @SerialName("metro_area_network_presence")
    val metroAreaNetworkPresence: List<String>? = null,
    
    @SerialName("legal_name")
    val legalName: String? = null,
    
    @SerialName("billing_information")
    val billingInformation: BillingInformation? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    val discoverable: Boolean? = null,
    val address: Address? = null,
    val asns: List<Int>? = null,
    
    @SerialName("managing_account")
    val managingAccount: String? = null
)

/**
 * Request to patch an account
 */
@Serializable
data class AccountPatch(
    val name: String? = null,
    
    @SerialName("metro_area_network_presence")
    val metroAreaNetworkPresence: List<String>? = null,
    
    @SerialName("legal_name")
    val legalName: String? = null,
    
    @SerialName("billing_information")
    val billingInformation: BillingInformation? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    val discoverable: Boolean? = null,
    val address: Address? = null,
    val asns: List<Int>? = null
)
