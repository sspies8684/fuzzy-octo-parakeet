package com.example.oncall.domain.model

import java.time.Duration

/**
 * Escalation level describing who to notify and how long to wait for acknowledgement.
 */
data class EscalationLevel(
    val targets: List<OnCallTarget>,
    val acknowledgementTimeout: Duration
) {
    init {
        require(targets.isNotEmpty()) { "Escalation level requires at least one target" }
        require(!acknowledgementTimeout.isNegative && !acknowledgementTimeout.isZero) {
            "Acknowledgement timeout must be positive"
        }
    }
}

/**
 * Policy describing escalation steps for a priority bucket.
 */
data class EscalationPolicy(
    val levels: List<EscalationLevel>
) {
    init {
        require(levels.isNotEmpty()) { "Escalation policy requires at least one level" }
    }
}
