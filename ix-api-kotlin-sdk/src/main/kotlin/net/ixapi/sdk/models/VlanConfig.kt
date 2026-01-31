package net.ixapi.sdk.models

import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * VLAN configuration sealed class - base for different VLAN types
 */
@Serializable
sealed class VlanConfig {
    abstract val vlanType: VlanType
}

/**
 * Port mode VLAN configuration (untagged)
 */
@Serializable
@SerialName("port")
data class VlanConfigPort(
    @SerialName("vlan_type")
    override val vlanType: VlanType = VlanType.PORT
) : VlanConfig()

/**
 * Dot1Q VLAN configuration (single tagged)
 */
@Serializable
@SerialName("dot1q")
data class VlanConfigDot1Q(
    @SerialName("vlan_type")
    override val vlanType: VlanType = VlanType.DOT1Q,
    
    val vlan: Int,
    
    @SerialName("vlan_ethertype")
    val vlanEthertype: OuterVlanEthertype? = null
) : VlanConfig()

/**
 * QinQ VLAN configuration (double tagged)
 */
@Serializable
@SerialName("qinq")
data class VlanConfigQinQ(
    @SerialName("vlan_type")
    override val vlanType: VlanType = VlanType.QINQ,
    
    @SerialName("outer_vlan")
    val outerVlan: Int,
    
    @SerialName("inner_vlan")
    val innerVlan: Int,
    
    @SerialName("outer_vlan_ethertype")
    val outerVlanEthertype: OuterVlanEthertype? = null
) : VlanConfig()
