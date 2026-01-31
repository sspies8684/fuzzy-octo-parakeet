package net.ixapi.sdk.models

import kotlinx.serialization.*

/**
 * A MemberJoiningRule defines a rule to allow or deny access for
 * an Account to access a NetworkService.
 */
@Serializable
data class MemberJoiningRule(
    val id: String,
    val type: MemberJoiningRuleType,
    
    @SerialName("network_service")
    val networkService: String,
    
    val account: String? = null,
    
    @SerialName("capacity_min")
    val capacityMin: Int? = null,
    
    @SerialName("capacity_max")
    val capacityMax: Int? = null
)

/**
 * Allow member joining rule
 */
@Serializable
data class AllowMemberJoiningRule(
    val id: String,
    val type: String = "allow",
    
    @SerialName("network_service")
    val networkService: String,
    
    val account: String? = null,
    
    @SerialName("capacity_min")
    val capacityMin: Int? = null,
    
    @SerialName("capacity_max")
    val capacityMax: Int? = null
)

/**
 * Deny member joining rule
 */
@Serializable
data class DenyMemberJoiningRule(
    val id: String,
    val type: String = "deny",
    
    @SerialName("network_service")
    val networkService: String,
    
    val account: String? = null
)

/**
 * Request to create a member joining rule
 */
@Serializable
data class MemberJoiningRuleRequest(
    val type: MemberJoiningRuleType,
    
    @SerialName("network_service")
    val networkService: String,
    
    val account: String? = null,
    
    @SerialName("capacity_min")
    val capacityMin: Int? = null,
    
    @SerialName("capacity_max")
    val capacityMax: Int? = null
)

/**
 * Allow member joining rule request
 */
@Serializable
data class AllowMemberJoiningRuleRequest(
    val type: String = "allow",
    
    @SerialName("network_service")
    val networkService: String,
    
    val account: String? = null,
    
    @SerialName("capacity_min")
    val capacityMin: Int? = null,
    
    @SerialName("capacity_max")
    val capacityMax: Int? = null
)

/**
 * Deny member joining rule request
 */
@Serializable
data class DenyMemberJoiningRuleRequest(
    val type: String = "deny",
    
    @SerialName("network_service")
    val networkService: String,
    
    val account: String? = null
)

/**
 * Request to update a member joining rule
 */
@Serializable
data class MemberJoiningRuleUpdate(
    val account: String? = null,
    
    @SerialName("capacity_min")
    val capacityMin: Int? = null,
    
    @SerialName("capacity_max")
    val capacityMax: Int? = null
)

/**
 * Request to patch a member joining rule
 */
@Serializable
data class MemberJoiningRulePatch(
    val account: String? = null,
    
    @SerialName("capacity_min")
    val capacityMin: Int? = null,
    
    @SerialName("capacity_max")
    val capacityMax: Int? = null
)

/**
 * Allow member joining rule update
 */
@Serializable
data class AllowMemberJoiningRuleUpdate(
    val account: String? = null,
    
    @SerialName("capacity_min")
    val capacityMin: Int? = null,
    
    @SerialName("capacity_max")
    val capacityMax: Int? = null
)

/**
 * Allow member joining rule patch
 */
@Serializable
data class AllowMemberJoiningRulePatch(
    val account: String? = null,
    
    @SerialName("capacity_min")
    val capacityMin: Int? = null,
    
    @SerialName("capacity_max")
    val capacityMax: Int? = null
)

/**
 * Deny member joining rule update
 */
@Serializable
data class DenyMemberJoiningRuleUpdate(
    val account: String? = null
)

/**
 * Deny member joining rule patch
 */
@Serializable
data class DenyMemberJoiningRulePatch(
    val account: String? = null
)
