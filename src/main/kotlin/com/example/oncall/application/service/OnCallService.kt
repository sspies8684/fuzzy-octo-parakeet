package com.example.oncall.application.service

import com.example.oncall.application.port.`in`.OnCallUseCase
import com.example.oncall.application.port.out.AlertRepository
import com.example.oncall.application.port.out.NotificationPort
import com.example.oncall.domain.model.AcknowledgementOutcome
import com.example.oncall.domain.model.AcknowledgementStatus
import com.example.oncall.domain.model.Alert
import com.example.oncall.domain.model.AlertPriority
import com.example.oncall.domain.model.AlertStatus
import com.example.oncall.domain.model.Assignment
import com.example.oncall.domain.model.EscalationPolicy
import java.time.Instant
import java.util.UUID

class OnCallService(
    private val policiesByPriority: Map<AlertPriority, EscalationPolicy>,
    private val notificationPort: NotificationPort,
    private val alertRepository: AlertRepository
) : OnCallUseCase {

    override fun raiseAlert(
        message: String,
        priority: AlertPriority,
        createdAt: Instant
    ): Alert {
        val policy = policiesByPriority[priority]
            ?: error("No escalation policy configured for $priority")

        val alert = Alert(
            message = message,
            priority = priority,
            createdAt = createdAt,
            policy = policy
        )

        val persisted = alertRepository.save(alert)
        dispatchLevel(persisted, levelIndex = 0, dispatchedAt = createdAt)

        return alertRepository.save(persisted)
    }

    override fun listAlerts(status: AlertStatus?): List<Alert> =
        alertRepository.findAll(status).sortedBy { it.createdAt }

    override fun getAlert(id: UUID): Alert? = alertRepository.findById(id)

    override fun acknowledge(
        alertId: UUID,
        responderId: UUID,
        at: Instant
    ): AcknowledgementOutcome {
        val alert = alertRepository.findById(alertId)
            ?: return AcknowledgementOutcome(AcknowledgementStatus.ALERT_NOT_FOUND)

        val assignment = alert.assignments
            .firstOrNull { it.target.responder.id == responderId }
            ?: return AcknowledgementOutcome(AcknowledgementStatus.ASSIGNMENT_NOT_FOUND)

        return completeAcknowledgement(alert, assignment, at)
    }

    override fun acknowledgeByToken(
        alertId: UUID,
        token: UUID,
        at: Instant
    ): AcknowledgementOutcome {
        val alert = alertRepository.findById(alertId)
            ?: return AcknowledgementOutcome(AcknowledgementStatus.ALERT_NOT_FOUND)

        val assignment = alert.assignments
            .firstOrNull { it.acknowledgementToken == token }
            ?: return AcknowledgementOutcome(AcknowledgementStatus.TOKEN_NOT_FOUND)

        return completeAcknowledgement(alert, assignment, at)
    }

    override fun advance(now: Instant): List<Alert> {
        val escalatedAlerts = mutableListOf<Alert>()

        alertRepository.findAll(AlertStatus.PENDING)
            .forEach { alert ->
                val currentAssignments = alert.assignmentsForLevel(alert.currentLevelIndex)

                if (currentAssignments.any { it.isAcknowledged }) {
                    return@forEach
                }

                val levelDeadline = currentAssignments.maxOfOrNull { it.deadline }
                    ?: return@forEach

                if (now.isBefore(levelDeadline)) {
                    return@forEach
                }

                val nextLevelIndex = alert.currentLevelIndex + 1
                if (nextLevelIndex >= alert.policy.levels.size) {
                    alert.status = AlertStatus.EXHAUSTED
                    alertRepository.save(alert)
                    escalatedAlerts += alert
                } else {
                    alert.currentLevelIndex = nextLevelIndex
                    dispatchLevel(alert, nextLevelIndex, now)
                    alertRepository.save(alert)
                    escalatedAlerts += alert
                }
            }

        return escalatedAlerts
    }

    private fun completeAcknowledgement(
        alert: Alert,
        assignment: Assignment,
        at: Instant
    ): AcknowledgementOutcome {
        if (alert.status == AlertStatus.ACKNOWLEDGED) {
            return AcknowledgementOutcome(
                status = AcknowledgementStatus.ALREADY_ACKNOWLEDGED,
                responder = alert.acknowledgedBy,
                acknowledgedAt = alert.acknowledgedAt
            )
        }

        if (assignment.isAcknowledged) {
            return AcknowledgementOutcome(
                status = AcknowledgementStatus.ALREADY_ACKNOWLEDGED,
                responder = assignment.target.responder,
                acknowledgedAt = assignment.acknowledgedAt
            )
        }

        assignment.acknowledge(at)
        alert.status = AlertStatus.ACKNOWLEDGED
        alert.acknowledgedBy = assignment.target.responder
        alert.acknowledgedAt = at

        alertRepository.save(alert)

        return AcknowledgementOutcome(
            status = AcknowledgementStatus.ACKNOWLEDGED,
            responder = assignment.target.responder,
            acknowledgedAt = at
        )
    }

    private fun dispatchLevel(alert: Alert, levelIndex: Int, dispatchedAt: Instant) {
        val level = alert.policy.levels[levelIndex]
        level.targets.forEach { target ->
            val assignment = Assignment(
                target = target,
                levelIndex = levelIndex,
                dispatchedAt = dispatchedAt,
                deadline = dispatchedAt.plus(level.acknowledgementTimeout)
            )
            alert.assignments += assignment
            notificationPort.notify(alert, assignment)
        }
    }
}
