package com.example.oncall.application.port.`in`

import com.example.oncall.domain.model.AcknowledgementOutcome
import com.example.oncall.domain.model.Alert
import com.example.oncall.domain.model.AlertPriority
import com.example.oncall.domain.model.AlertStatus
import java.time.Instant
import java.util.UUID

interface OnCallUseCase {
    fun raiseAlert(message: String, priority: AlertPriority, createdAt: Instant = Instant.now()): Alert
    fun listAlerts(status: AlertStatus? = null): List<Alert>
    fun getAlert(id: UUID): Alert?
    fun acknowledge(alertId: UUID, responderId: UUID, at: Instant = Instant.now()): AcknowledgementOutcome
    fun acknowledgeByToken(alertId: UUID, token: UUID, at: Instant = Instant.now()): AcknowledgementOutcome
    fun advance(now: Instant = Instant.now()): List<Alert>
}
