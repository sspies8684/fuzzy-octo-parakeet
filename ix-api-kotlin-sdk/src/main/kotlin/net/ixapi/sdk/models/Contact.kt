package net.ixapi.sdk.models

import kotlinx.serialization.*

/**
 * A Contact is a role undertaking a specific responsibility
 * within an account, typically a department or agent of the customer company.
 */
@Serializable
data class Contact(
    val id: String,
    
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    @SerialName("managing_account")
    val managingAccount: String,
    
    val name: String? = null,
    val telephone: String? = null,
    val email: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null
)

/**
 * Request to create a new contact
 */
@Serializable
data class ContactRequest(
    @SerialName("consuming_account")
    val consumingAccount: String,
    
    val name: String? = null,
    val telephone: String? = null,
    val email: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("managing_account")
    val managingAccount: String? = null
)

/**
 * Request to update a contact
 */
@Serializable
data class ContactUpdate(
    val name: String? = null,
    val telephone: String? = null,
    val email: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null,
    
    @SerialName("managing_account")
    val managingAccount: String? = null
)

/**
 * Request to patch a contact
 */
@Serializable
data class ContactPatch(
    val name: String? = null,
    val telephone: String? = null,
    val email: String? = null,
    
    @SerialName("external_ref")
    val externalRef: String? = null
)
