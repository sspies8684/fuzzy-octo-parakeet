package com.example.oncall.domain.model

import java.time.Instant
import java.util.UUID

enum class AlertPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class AlertStatus {
    PENDING,
    ACKNOWLEDGED,
    EXHAUSTED
}

data class Assignment(
    val id: UUID = UUID.randomUUID(),
    val target: OnCallTarget,
    val levelIndex: Int,
    val dispatchedAt: Instant,
    val deadline: Instant,
    val acknowledgementToken: UUID = UUID.randomUUID(),
    var acknowledgedAt: Instant? = null
) {
    val isAcknowledged: Boolean
        get() = acknowledgedAt != null

    fun acknowledge(at: Instant) {
        acknowledgedAt = at
    }
}

data class Alert(
    val id: UUID = UUID.randomUUID(),
    val message: String,
    val priority: AlertPriority,
    val createdAt: Instant,
    val policy: EscalationPolicy,
    val assignments: MutableList<Assignment> = mutableListOf(),
    var status: AlertStatus = AlertStatus.PENDING,
    var currentLevelIndex: Int = 0,
    var acknowledgedBy: Responder? = null,
    var acknowledgedAt: Instant? = null
) {
    init {
        require(message.isNotBlank()) { "Alert message must be provided" }
    }

    fun assignmentsForLevel(levelIndex: Int): List<Assignment> =
        assignments.filter { it.levelIndex == levelIndex }
}
