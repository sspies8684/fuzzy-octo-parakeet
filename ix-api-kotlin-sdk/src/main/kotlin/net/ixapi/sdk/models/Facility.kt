package net.ixapi.sdk.models

import kotlinx.serialization.*

/**
 * A Facility is a data centre with a determined physical address,
 * from which a defined set of PoPs can be accessed.
 */
@Serializable
data class Facility(
    val id: String,
    val name: String,
    
    @SerialName("metro_area")
    val metroArea: String,
    
    @SerialName("address_country")
    val addressCountry: String,
    
    @SerialName("address_locality")
    val addressLocality: String,
    
    @SerialName("address_region")
    val addressRegion: String,
    
    @SerialName("postal_code")
    val postalCode: String,
    
    @SerialName("street_address")
    val streetAddress: String,
    
    @SerialName("organisation_name")
    val organisationName: String,
    
    val pops: List<String>,
    
    @SerialName("peeringdb_facility_id")
    val peeringdbFacilityId: Int? = null,
    
    val latitude: Double? = null,
    val longitude: Double? = null
)

/**
 * A MetroArea exists if a MetroAreaNetwork or Facility is present in it.
 */
@Serializable
data class MetroArea(
    val id: String,
    
    @SerialName("display_name")
    val displayName: String,
    
    @SerialName("un_locode")
    val unLocode: String,
    
    @SerialName("iata_code")
    val iataCode: String,
    
    val facilities: List<String>,
    
    @SerialName("metro_area_networks")
    val metroAreaNetworks: List<String>
)

/**
 * Services are provided directly on or can be consumed from inside a MetroAreaNetwork.
 */
@Serializable
data class MetroAreaNetwork(
    val id: String,
    val name: String,
    
    @SerialName("metro_area")
    val metroArea: String,
    
    @SerialName("service_provider")
    val serviceProvider: String? = null,
    
    @SerialName("peeringdb_ixid")
    val peeringdbIxid: Int? = null,
    
    val pops: List<String>? = null
)

/**
 * A PointOfPresence is a technical installation within a Facility
 * which is connected to a single MetroAreaNetwork.
 */
@Serializable
data class PointOfPresence(
    val id: String,
    val name: String,
    val facility: String,
    
    @SerialName("metro_area_network")
    val metroAreaNetwork: String,
    
    val devices: List<String>,
    
    @SerialName("availability_zone")
    val availabilityZone: String? = null
)

/**
 * An AvailabilityZone is a grouping of network resources that have
 * the same maintenance scheduling.
 */
@Serializable
data class AvailabilityZone(
    val id: String,
    val name: String,
    
    @SerialName("metro_area_network")
    val metroAreaNetwork: String
)
