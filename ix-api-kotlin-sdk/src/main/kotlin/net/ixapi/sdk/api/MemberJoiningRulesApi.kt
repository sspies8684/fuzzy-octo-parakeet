package net.ixapi.sdk.api

import net.ixapi.sdk.client.IxApiHttpClient
import net.ixapi.sdk.models.*
import net.ixapi.sdk.pagination.PaginatedResponse
import net.ixapi.sdk.pagination.PaginationParams
import net.ixapi.sdk.util.query

/**
 * API client for Member Joining Rule operations
 */
class MemberJoiningRulesApi(private val client: IxApiHttpClient) {
    
    /**
     * List all member joining rules
     */
    suspend fun list(
        type: MemberJoiningRuleType? = null,
        networkService: String? = null,
        account: String? = null,
        pagination: PaginationParams? = null
    ): PaginatedResponse<List<MemberJoiningRule>> {
        val params = query {
            param("type", type?.name?.lowercase())
            param("network_service", networkService)
            param("account", account)
            pagination(pagination)
        }
        return client.getWithPagination("/member-joining-rules", params)
    }
    
    /**
     * Get a member joining rule by ID
     */
    suspend fun get(id: String): MemberJoiningRule {
        return client.get("/member-joining-rules/$id")
    }
    
    /**
     * Create a new member joining rule
     */
    suspend fun create(request: MemberJoiningRuleRequest): MemberJoiningRule {
        return client.post("/member-joining-rules", request)
    }
    
    /**
     * Create an allow rule
     */
    suspend fun createAllowRule(request: AllowMemberJoiningRuleRequest): MemberJoiningRule {
        return client.post("/member-joining-rules", request)
    }
    
    /**
     * Create a deny rule
     */
    suspend fun createDenyRule(request: DenyMemberJoiningRuleRequest): MemberJoiningRule {
        return client.post("/member-joining-rules", request)
    }
    
    /**
     * Update a member joining rule (full update)
     */
    suspend fun update(id: String, request: MemberJoiningRuleUpdate): MemberJoiningRule {
        return client.put("/member-joining-rules/$id", request)
    }
    
    /**
     * Patch a member joining rule (partial update)
     */
    suspend fun patch(id: String, request: MemberJoiningRulePatch): MemberJoiningRule {
        return client.patch("/member-joining-rules/$id", request)
    }
    
    /**
     * Delete a member joining rule
     */
    suspend fun delete(id: String): MemberJoiningRule {
        return client.delete("/member-joining-rules/$id")
    }
}
