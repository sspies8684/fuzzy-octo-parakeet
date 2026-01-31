# IX-API Kotlin SDK

A full-featured Kotlin client SDK for the [IX-API](https://ix-api.net) (Internet Exchange API) version 2.7.1.

## Features

- **Complete API Coverage**: All 69 endpoints from the IX-API specification
- **Type-Safe Models**: 166+ Kotlin data classes with proper serialization
- **Coroutine-Based**: Built with Kotlin coroutines for async operations
- **Authentication**: JWT-based authentication with automatic token refresh
- **Pagination Support**: Built-in pagination helpers
- **Error Handling**: Comprehensive exception hierarchy
- **Retry Logic**: Automatic retries with exponential backoff
- **Type-Safe Builders**: DSL-style query builders

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("net.ixapi:ix-api-kotlin-sdk:2.7.1")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'net.ixapi:ix-api-kotlin-sdk:2.7.1'
}
```

### Maven

```xml
<dependency>
    <groupId>net.ixapi</groupId>
    <artifactId>ix-api-kotlin-sdk</artifactId>
    <version>2.7.1</version>
</dependency>
```

## Quick Start

### Basic Usage

```kotlin
import net.ixapi.sdk.IxApiClient
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // Create client with API key authentication
    val client = IxApiClient.create(
        baseUrl = "https://api.example-ix.net",
        apiKey = "your-api-key",
        apiSecret = "your-api-secret"
    )
    
    // Authenticate
    client.authenticate()
    
    // List accounts
    val accounts = client.accounts.list()
    accounts.data.forEach { account ->
        println("Account: ${account.name} (${account.id})")
    }
    
    // Get current account
    val myAccount = client.accounts.getCurrent()
    println("My account: ${myAccount.name}")
    
    // Close the client when done
    client.close()
}
```

### Using Pre-existing Token

```kotlin
val client = IxApiClient.createWithToken(
    baseUrl = "https://api.example-ix.net",
    accessToken = "your-existing-token"
)
```

### Custom Configuration

```kotlin
import net.ixapi.sdk.client.IxApiConfig

val config = IxApiConfig(
    baseUrl = "https://api.example-ix.net",
    apiPath = "/api/v2",
    credentials = IxApiCredentials("api-key", "api-secret"),
    timeout = 60_000,
    debug = true,
    maxRetries = 5,
    autoRefreshToken = true
)

val client = IxApiClient.create(config)
```

## API Resources

The SDK provides access to all IX-API resources:

### Infrastructure
- `client.facilities` - Data center facilities
- `client.metroAreas` - Metropolitan areas
- `client.metroAreaNetworks` - Metro area networks
- `client.pops` - Points of Presence
- `client.availabilityZones` - Availability zones
- `client.devices` - Network devices

### Customer Management
- `client.accounts` - Customer accounts
- `client.contacts` - Account contacts
- `client.roles` - Contact roles
- `client.roleAssignments` - Role assignments

### Connectivity
- `client.connections` - Physical connections/LAGs
- `client.ports` - Physical ports
- `client.portReservations` - Port reservations

### Services
- `client.networkServices` - Network services (Exchange LAN, P2P, Cloud, etc.)
- `client.networkServiceConfigs` - Service configurations
- `client.networkFeatures` - Service features (Route Servers, etc.)
- `client.networkFeatureConfigs` - Feature configurations
- `client.routingFunctions` - Routing functions (VPRNs)

### Addressing
- `client.ips` - IP addresses
- `client.macs` - MAC addresses

### Products
- `client.productOfferings` - Available product offerings
- `client.memberJoiningRules` - Network access rules

### System
- `client.health` - API health status
- `client.implementation` - API implementation info
- `client.extensions` - API extensions

## Examples

### Working with Connections

```kotlin
// List all production connections
val connections = client.connections.list(
    state = ResourceState.PRODUCTION
)

// Create a new connection
val newConnection = client.connections.create(ConnectionRequest(
    productOffering = "offering-id",
    consumingAccount = "account-id",
    billingAccount = "billing-account-id",
    portQuantity = 2,
    mode = ConnectionMode.LAG_LACP,
    name = "My Connection"
))

// Get connection statistics
val stats = client.connections.getStatistics(newConnection.id)
println("Bytes in: ${stats.bytesIn}, Bytes out: ${stats.bytesOut}")

// Download LOA document
val loa = client.connections.downloadLoa(newConnection.id)
File("loa.pdf").writeBytes(loa)
```

### Working with Network Services

```kotlin
// List Exchange LAN services
val exchangeLans = client.networkServices.list(
    type = NetworkServiceType.EXCHANGE_LAN
)

// Create a network service config
val nsc = client.networkServiceConfigs.create(NetworkServiceConfigRequest(
    type = NetworkServiceConfigType.EXCHANGE_LAN,
    networkService = "service-id",
    connection = "connection-id",
    billingAccount = "billing-account-id",
    vlanConfig = buildJsonObject {
        put("vlan_type", "dot1q")
        put("vlan", 100)
    },
    asns = listOf(65001)
))
```

### Pagination

```kotlin
import net.ixapi.sdk.pagination.pagination

// Use pagination
val page1 = client.accounts.list(
    pagination = pagination {
        limit(50)
        offset(0)
    }
)

println("Total items: ${page1.pagination?.totalItems}")
println("Has next page: ${page1.hasNextPage}")

// Get next page
if (page1.hasNextPage) {
    val page2 = client.accounts.list(
        pagination = pagination {
            limit(50)
            offset(page1.pagination?.nextOffset ?: 0)
            token(page1.pagination?.token)
        }
    )
}
```

### Error Handling

```kotlin
import net.ixapi.sdk.exceptions.*

try {
    val account = client.accounts.get("non-existent-id")
} catch (e: NotFoundException) {
    println("Account not found: ${e.message}")
} catch (e: ValidationException) {
    println("Validation errors:")
    e.validationErrors.forEach { error ->
        println("  ${error.name}: ${error.reason}")
    }
} catch (e: AuthenticationException) {
    println("Authentication failed - need to re-authenticate")
} catch (e: RateLimitException) {
    println("Rate limited - retry after ${e.retryAfter} seconds")
} catch (e: IxApiException) {
    println("API error: ${e.message} (status: ${e.statusCode})")
}
```

### Filtering with Query Builder

```kotlin
import net.ixapi.sdk.util.query

val connections = client.connections.list(
    state = ResourceState.PRODUCTION,
    metroAreaNetwork = "man-123",
    pagination = pagination { limit(100) }
)
```

## Models

The SDK includes comprehensive data models for all IX-API resources:

### Core Types
- `Account`, `Contact`, `Role`, `RoleAssignment`
- `Facility`, `MetroArea`, `MetroAreaNetwork`, `PointOfPresence`
- `Connection`, `Port`, `PortReservation`
- `NetworkService`, `NetworkServiceConfig`
- `NetworkFeature`, `NetworkFeatureConfig`
- `ProductOffering`, `RoutingFunction`
- `IpAddress`, `MacAddress`

### Enumerations
- `ResourceState` - Resource lifecycle states
- `ConnectionMode` - LAG/standalone modes
- `VlanType` - VLAN configuration types
- `NetworkServiceType` - Service types
- `ProductOfferingType` - Product types

## Configuration Options

| Option | Default | Description |
|--------|---------|-------------|
| `baseUrl` | Required | IX-API server URL |
| `apiPath` | `/api/v2` | API path prefix |
| `timeout` | 30000ms | Request timeout |
| `connectTimeout` | 10000ms | Connection timeout |
| `maxRetries` | 3 | Max retry attempts |
| `retryDelay` | 1000ms | Initial retry delay |
| `autoRefreshToken` | true | Auto-refresh JWT tokens |
| `debug` | false | Enable debug logging |

## Requirements

- Kotlin 1.9+
- Java 17+
- Ktor Client 2.3+

## License

Apache License 2.0

## Links

- [IX-API Documentation](https://docs.ix-api.net)
- [IX-API Specification](https://ix-api.net)
