package net.ixapi.sdk.api

import net.ixapi.sdk.client.IxApiHttpClient
import net.ixapi.sdk.models.*
import net.ixapi.sdk.pagination.PaginatedResponse
import net.ixapi.sdk.pagination.PaginationParams
import net.ixapi.sdk.util.query

/**
 * API client for Contact operations
 */
class ContactsApi(private val client: IxApiHttpClient) {
    
    /**
     * List all contacts
     */
    suspend fun list(
        consumingAccount: String? = null,
        managingAccount: String? = null,
        externalRef: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<Contact>> {
        val params = query {
            consumingAccount(consumingAccount)
            managingAccount(managingAccount)
            externalRef(externalRef)
            pagination(pagination)
        }
        return client.getWithPagination("/contacts", params)
    }
    
    /**
     * Get a contact by ID
     */
    suspend fun get(id: String): Contact {
        return client.get("/contacts/$id")
    }
    
    /**
     * Create a new contact
     */
    suspend fun create(request: ContactRequest): Contact {
        return client.post("/contacts", request)
    }
    
    /**
     * Update a contact (full update)
     */
    suspend fun update(id: String, request: ContactUpdate): Contact {
        return client.put("/contacts/$id", request)
    }
    
    /**
     * Patch a contact (partial update)
     */
    suspend fun patch(id: String, request: ContactPatch): Contact {
        return client.patch("/contacts/$id", request)
    }
    
    /**
     * Delete a contact
     */
    suspend fun delete(id: String): Contact {
        return client.delete("/contacts/$id")
    }
}

/**
 * API client for Role operations
 */
class RolesApi(private val client: IxApiHttpClient) {
    
    /**
     * List all roles
     */
    suspend fun list(
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<Role>> {
        val params = query {
            pagination(pagination)
        }
        return client.getWithPagination("/roles", params)
    }
    
    /**
     * Get a role by ID
     */
    suspend fun get(id: String): Role {
        return client.get("/roles/$id")
    }
}

/**
 * API client for Role Assignment operations
 */
class RoleAssignmentsApi(private val client: IxApiHttpClient) {
    
    /**
     * List all role assignments
     */
    suspend fun list(
        role: String? = null,
        contact: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<RoleAssignment>> {
        val params = query {
            param("role", role)
            param("contact", contact)
            pagination(pagination)
        }
        return client.getWithPagination("/role-assignments", params)
    }
    
    /**
     * Get a role assignment by ID
     */
    suspend fun get(id: String): RoleAssignment {
        return client.get("/role-assignments/$id")
    }
    
    /**
     * Create a new role assignment
     */
    suspend fun create(request: RoleAssignmentRequest): RoleAssignment {
        return client.post("/role-assignments", request)
    }
    
    /**
     * Update a role assignment (full update)
     */
    suspend fun update(id: String, request: RoleAssignmentUpdate): RoleAssignment {
        return client.put("/role-assignments/$id", request)
    }
    
    /**
     * Patch a role assignment (partial update)
     */
    suspend fun patch(id: String, request: RoleAssignmentPatch): RoleAssignment {
        return client.patch("/role-assignments/$id", request)
    }
    
    /**
     * Delete a role assignment
     */
    suspend fun delete(id: String): RoleAssignment {
        return client.delete("/role-assignments/$id")
    }
}
