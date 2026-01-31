package net.ixapi.sdk

import net.ixapi.sdk.api.*
import net.ixapi.sdk.client.IxApiConfig
import net.ixapi.sdk.client.IxApiHttpClient
import net.ixapi.sdk.models.AuthToken

/**
 * IX-API Kotlin SDK Client
 * 
 * The main entry point for interacting with the IX-API.
 * Provides access to all API resources and handles authentication.
 * 
 * Example usage:
 * ```kotlin
 * val client = IxApiClient.create(
 *     baseUrl = "https://api.example-ix.net",
 *     apiKey = "your-api-key",
 *     apiSecret = "your-api-secret"
 * )
 * 
 * // Authenticate
 * client.authenticate()
 * 
 * // Use the API
 * val accounts = client.accounts.list()
 * val connections = client.connections.list()
 * ```
 */
class IxApiClient private constructor(
    private val config: IxApiConfig,
    private val httpClient: IxApiHttpClient
) : AutoCloseable {
    
    // API Resources
    
    /** Account management operations */
    val accounts: AccountsApi = AccountsApi(httpClient)
    
    /** Contact management operations */
    val contacts: ContactsApi = ContactsApi(httpClient)
    
    /** Role management operations */
    val roles: RolesApi = RolesApi(httpClient)
    
    /** Role assignment operations */
    val roleAssignments: RoleAssignmentsApi = RoleAssignmentsApi(httpClient)
    
    /** Facility information */
    val facilities: FacilitiesApi = FacilitiesApi(httpClient)
    
    /** Metro area information */
    val metroAreas: MetroAreasApi = MetroAreasApi(httpClient)
    
    /** Metro area network information */
    val metroAreaNetworks: MetroAreaNetworksApi = MetroAreaNetworksApi(httpClient)
    
    /** Point of Presence (PoP) information */
    val pops: PopsApi = PopsApi(httpClient)
    
    /** Availability zone information */
    val availabilityZones: AvailabilityZonesApi = AvailabilityZonesApi(httpClient)
    
    /** Device information */
    val devices: DevicesApi = DevicesApi(httpClient)
    
    /** Connection management operations */
    val connections: ConnectionsApi = ConnectionsApi(httpClient)
    
    /** Port information */
    val ports: PortsApi = PortsApi(httpClient)
    
    /** Port reservation operations */
    val portReservations: PortReservationsApi = PortReservationsApi(httpClient)
    
    /** Network service operations */
    val networkServices: NetworkServicesApi = NetworkServicesApi(httpClient)
    
    /** Network service config operations */
    val networkServiceConfigs: NetworkServiceConfigsApi = NetworkServiceConfigsApi(httpClient)
    
    /** Network feature information */
    val networkFeatures: NetworkFeaturesApi = NetworkFeaturesApi(httpClient)
    
    /** Network feature config operations */
    val networkFeatureConfigs: NetworkFeatureConfigsApi = NetworkFeatureConfigsApi(httpClient)
    
    /** Product offering information */
    val productOfferings: ProductOfferingsApi = ProductOfferingsApi(httpClient)
    
    /** IP address operations */
    val ips: IpsApi = IpsApi(httpClient)
    
    /** MAC address operations */
    val macs: MacsApi = MacsApi(httpClient)
    
    /** Member joining rule operations */
    val memberJoiningRules: MemberJoiningRulesApi = MemberJoiningRulesApi(httpClient)
    
    /** Routing function operations */
    val routingFunctions: RoutingFunctionsApi = RoutingFunctionsApi(httpClient)
    
    /** API health check */
    val health: HealthApi = HealthApi(httpClient)
    
    /** API implementation info */
    val implementation: ImplementationApi = ImplementationApi(httpClient)
    
    /** API extensions */
    val extensions: ExtensionsApi = ExtensionsApi(httpClient)
    
    /**
     * Authenticate with the IX-API using configured credentials.
     * This is required before making API calls unless you provided
     * a pre-existing access token.
     * 
     * @return The authentication token
     */
    suspend fun authenticate(): AuthToken {
        return httpClient.authenticate()
    }
    
    /**
     * Refresh the access token using the refresh token.
     * This is typically handled automatically if autoRefreshToken is enabled.
     * 
     * @return The new authentication token
     */
    suspend fun refreshToken(): AuthToken {
        return httpClient.refreshToken()
    }
    
    /**
     * Check if the API is healthy and reachable.
     * 
     * @return true if the API is healthy
     */
    suspend fun isHealthy(): Boolean {
        return health.isHealthy()
    }
    
    /**
     * Get API version information.
     * 
     * @return The API implementation version
     */
    suspend fun getVersion(): String {
        return implementation.getVersion()
    }
    
    /**
     * Close the client and release resources.
     */
    override fun close() {
        httpClient.close()
    }
    
    companion object {
        /**
         * Create an IX-API client with API key authentication.
         * 
         * @param baseUrl The base URL of the IX-API server
         * @param apiKey Your API key
         * @param apiSecret Your API secret
         * @param apiPath The API path (defaults to "/api/v2")
         * @return A new IxApiClient instance
         */
        fun create(
            baseUrl: String,
            apiKey: String,
            apiSecret: String,
            apiPath: String = "/api/v2"
        ): IxApiClient {
            val config = IxApiConfig.withApiKey(baseUrl, apiKey, apiSecret, apiPath)
            val httpClient = IxApiHttpClient(config)
            return IxApiClient(config, httpClient)
        }
        
        /**
         * Create an IX-API client with a pre-existing access token.
         * 
         * @param baseUrl The base URL of the IX-API server
         * @param accessToken Your access token
         * @param apiPath The API path (defaults to "/api/v2")
         * @return A new IxApiClient instance
         */
        fun createWithToken(
            baseUrl: String,
            accessToken: String,
            apiPath: String = "/api/v2"
        ): IxApiClient {
            val config = IxApiConfig.withAccessToken(baseUrl, accessToken, apiPath)
            val httpClient = IxApiHttpClient(config)
            return IxApiClient(config, httpClient)
        }
        
        /**
         * Create an IX-API client with custom configuration.
         * 
         * @param config The client configuration
         * @return A new IxApiClient instance
         */
        fun create(config: IxApiConfig): IxApiClient {
            val httpClient = IxApiHttpClient(config)
            return IxApiClient(config, httpClient)
        }
    }
}
