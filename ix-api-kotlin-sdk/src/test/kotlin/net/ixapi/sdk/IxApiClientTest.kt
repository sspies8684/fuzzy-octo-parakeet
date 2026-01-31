package net.ixapi.sdk

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.ixapi.sdk.client.IxApiConfig
import net.ixapi.sdk.models.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IxApiClientTest {
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }
    
    @Test
    fun `test config creation with api key`() {
        val config = IxApiConfig.withApiKey(
            baseUrl = "https://api.example.com",
            apiKey = "test-key",
            apiSecret = "test-secret"
        )
        
        assertEquals("https://api.example.com", config.baseUrl)
        assertEquals("/api/v2", config.apiPath)
        assertNotNull(config.credentials)
        assertEquals("test-key", config.credentials?.apiKey)
        assertEquals("test-secret", config.credentials?.apiSecret)
    }
    
    @Test
    fun `test config creation with access token`() {
        val config = IxApiConfig.withAccessToken(
            baseUrl = "https://api.example.com",
            accessToken = "my-token"
        )
        
        assertEquals("https://api.example.com", config.baseUrl)
        assertEquals("my-token", config.accessToken)
    }
    
    @Test
    fun `test config api url generation`() {
        val config = IxApiConfig(
            baseUrl = "https://api.example.com/",
            apiPath = "/api/v2"
        )
        
        assertEquals("https://api.example.com/api/v2", config.apiUrl)
    }
    
    @Test
    fun `test resource state serialization`() {
        val states = listOf(
            ResourceState.PRODUCTION,
            ResourceState.ALLOCATED,
            ResourceState.DECOMMISSIONED
        )
        
        states.forEach { state ->
            val serialized = json.encodeToString(state)
            assertTrue(serialized.isNotBlank())
        }
    }
    
    @Test
    fun `test account model serialization`() {
        val account = Account(
            id = "acc-123",
            name = "Test Account",
            metroAreaNetworkPresence = listOf("man-1", "man-2"),
            state = ResourceState.PRODUCTION,
            discoverable = true
        )
        
        val serialized = json.encodeToString(account)
        assertTrue(serialized.contains("acc-123"))
        assertTrue(serialized.contains("Test Account"))
        
        val deserialized = json.decodeFromString<Account>(serialized)
        assertEquals(account.id, deserialized.id)
        assertEquals(account.name, deserialized.name)
        assertEquals(account.metroAreaNetworkPresence, deserialized.metroAreaNetworkPresence)
    }
    
    @Test
    fun `test connection model serialization`() {
        val connection = Connection(
            id = "conn-123",
            state = ResourceState.PRODUCTION,
            name = "My Connection",
            pop = "pop-1",
            productOffering = "po-1",
            mode = ConnectionMode.LAG_LACP,
            portQuantity = 2,
            outerVlanEthertypes = listOf(OuterVlanEthertype.ETHERTYPE_8100),
            vlanTypes = listOf(VlanType.DOT1Q),
            capacityAllocated = 10000,
            capacityAllocationLimit = 100000,
            consumingAccount = "acc-1",
            managingAccount = "acc-1",
            billingAccount = "acc-1",
            roleAssignments = emptyList()
        )
        
        val serialized = json.encodeToString(connection)
        assertTrue(serialized.contains("conn-123"))
        
        val deserialized = json.decodeFromString<Connection>(serialized)
        assertEquals(connection.id, deserialized.id)
        assertEquals(connection.mode, deserialized.mode)
    }
    
    @Test
    fun `test auth token model serialization`() {
        val authToken = AuthToken(
            accessToken = "access-123",
            refreshToken = "refresh-456"
        )
        
        val serialized = json.encodeToString(authToken)
        assertTrue(serialized.contains("access_token"))
        assertTrue(serialized.contains("refresh_token"))
        
        val deserialized = json.decodeFromString<AuthToken>(serialized)
        assertEquals(authToken.accessToken, deserialized.accessToken)
        assertEquals(authToken.refreshToken, deserialized.refreshToken)
    }
    
    @Test
    fun `test facility model serialization`() {
        val facility = Facility(
            id = "fac-123",
            name = "Test DC",
            metroArea = "ma-1",
            addressCountry = "US",
            addressLocality = "New York",
            addressRegion = "NY",
            postalCode = "10001",
            streetAddress = "123 Main St",
            organisationName = "Test Org",
            pops = listOf("pop-1", "pop-2"),
            latitude = 40.7128,
            longitude = -74.0060
        )
        
        val serialized = json.encodeToString(facility)
        assertTrue(serialized.contains("fac-123"))
        assertTrue(serialized.contains("Test DC"))
        
        val deserialized = json.decodeFromString<Facility>(serialized)
        assertEquals(facility.id, deserialized.id)
        assertEquals(facility.latitude, deserialized.latitude)
    }
    
    @Test
    fun `test ip address model serialization`() {
        val ip = IpAddress(
            id = "ip-123",
            address = "192.168.1.1",
            version = 4,
            prefixLength = 24,
            consumingAccount = "acc-1",
            managingAccount = "acc-1",
            fqdn = "host.example.com"
        )
        
        val serialized = json.encodeToString(ip)
        assertTrue(serialized.contains("192.168.1.1"))
        
        val deserialized = json.decodeFromString<IpAddress>(serialized)
        assertEquals(ip.address, deserialized.address)
        assertEquals(ip.version, deserialized.version)
        assertEquals(ip.prefixLength, deserialized.prefixLength)
    }
    
    @Test
    fun `test product offering types`() {
        val types = ProductOfferingType.values()
        assertTrue(types.contains(ProductOfferingType.CONNECTION))
        assertTrue(types.contains(ProductOfferingType.EXCHANGE_LAN))
        assertTrue(types.contains(ProductOfferingType.P2P_VC))
        assertTrue(types.contains(ProductOfferingType.CLOUD_VC))
    }
    
    @Test
    fun `test network service types`() {
        val types = NetworkServiceType.values()
        assertTrue(types.contains(NetworkServiceType.EXCHANGE_LAN))
        assertTrue(types.contains(NetworkServiceType.P2P_VC))
        assertTrue(types.contains(NetworkServiceType.P2MP_VC))
        assertTrue(types.contains(NetworkServiceType.MP2MP_VC))
        assertTrue(types.contains(NetworkServiceType.CLOUD_VC))
    }
    
    @Test
    fun `test problem response serialization`() {
        val problemJson = """
            {
                "type": "https://errors.ix-api.net/v2/validation-error.html",
                "title": "Validation Error",
                "status": 400,
                "detail": "Some fields did not validate",
                "properties": [
                    {"name": "email", "reason": "Invalid email format"}
                ]
            }
        """.trimIndent()
        
        val problem = json.decodeFromString<ProblemResponse>(problemJson)
        assertEquals(400, problem.status)
        assertEquals("Validation Error", problem.title)
        assertEquals(1, problem.properties?.size)
        assertEquals("email", problem.properties?.first()?.name)
    }
    
    @Test
    fun `test health status enum`() {
        val statuses = HealthStatus.values()
        assertTrue(statuses.contains(HealthStatus.PASS))
        assertTrue(statuses.contains(HealthStatus.WARN))
        assertTrue(statuses.contains(HealthStatus.FAIL))
    }
}
