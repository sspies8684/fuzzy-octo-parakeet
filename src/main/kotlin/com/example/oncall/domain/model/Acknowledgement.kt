package com.example.oncall.domain.model

import java.time.Instant

enum class AcknowledgementStatus {
    ACKNOWLEDGED,
    ALREADY_ACKNOWLEDGED,
    ALERT_NOT_FOUND,
    ASSIGNMENT_NOT_FOUND,
    TOKEN_NOT_FOUND
}

data class AcknowledgementOutcome(
    val status: AcknowledgementStatus,
    val responder: Responder? = null,
    val acknowledgedAt: Instant? = null
)
