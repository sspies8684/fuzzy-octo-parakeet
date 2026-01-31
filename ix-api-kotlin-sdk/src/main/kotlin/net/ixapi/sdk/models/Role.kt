package net.ixapi.sdk.models

import kotlinx.serialization.*

/**
 * A Role enables a Contact to act for a specific purpose.
 * 
 * Well-defined roles:
 * - legal: signing terms and conditions
 * - implementation: technical contact for deployment
 * - noc: technical contact for troubleshooting
 * - peering: contact authorized to accept peering requests
 * - billing: contact that receives invoices
 */
@Serializable
data class Role(
    val id: String,
    val name: String,
    
    @SerialName("required_fields")
    val requiredFields: List<String>
)

/**
 * Request to create a new role
 */
@Serializable
data class RoleRequest(
    val name: String,
    
    @SerialName("required_fields")
    val requiredFields: List<String> = emptyList()
)

/**
 * Request to update a role
 */
@Serializable
data class RoleUpdate(
    val name: String? = null,
    
    @SerialName("required_fields")
    val requiredFields: List<String>? = null
)

/**
 * Request to patch a role
 */
@Serializable
data class RolePatch(
    val name: String? = null,
    
    @SerialName("required_fields")
    val requiredFields: List<String>? = null
)

/**
 * A Contact can be assigned to many Roles
 */
@Serializable
data class RoleAssignment(
    val id: String,
    val role: String,
    val contact: String
)

/**
 * Request to create a role assignment
 */
@Serializable
data class RoleAssignmentRequest(
    val role: String,
    val contact: String
)

/**
 * Request to update a role assignment
 */
@Serializable
data class RoleAssignmentUpdate(
    val role: String? = null,
    val contact: String? = null
)

/**
 * Request to patch a role assignment
 */
@Serializable
data class RoleAssignmentPatch(
    val role: String? = null,
    val contact: String? = null
)
