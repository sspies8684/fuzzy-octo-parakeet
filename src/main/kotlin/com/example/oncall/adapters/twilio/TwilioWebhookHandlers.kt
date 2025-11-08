package com.example.oncall.adapters.twilio

import com.example.oncall.application.port.`in`.OnCallUseCase
import com.example.oncall.domain.model.AcknowledgementStatus
import java.time.Instant
import java.util.UUID

object TwilioWebhookHandlers {

    fun prompt(
        onCallUseCase: OnCallUseCase,
        alertId: UUID,
        token: UUID,
        webhookBaseUrl: String
    ): String {
        val alert = onCallUseCase.getAlert(alertId)
            ?: return TwilioScripts.acknowledgementAlertMissing()

        val assignment = alert.assignments.firstOrNull { it.acknowledgementToken == token }
            ?: return TwilioScripts.acknowledgementAssignmentMissing()

        return TwilioScripts.acknowledgementPrompt(alert, assignment, webhookBaseUrl)
    }

    fun acknowledge(
        onCallUseCase: OnCallUseCase,
        alertId: UUID,
        token: UUID,
        digits: String?,
        webhookBaseUrl: String,
        at: Instant = Instant.now()
    ): String {
        val alert = onCallUseCase.getAlert(alertId)
            ?: return TwilioScripts.acknowledgementAlertMissing()

        val assignment = alert.assignments.firstOrNull { it.acknowledgementToken == token }
            ?: return TwilioScripts.acknowledgementAssignmentMissing()

        val normalizedDigits = digits?.trim().orEmpty()
        if (normalizedDigits.isEmpty()) {
            return TwilioScripts.acknowledgementInvalidInput(alert, assignment, webhookBaseUrl)
        }

        return when (normalizedDigits) {
            "1" -> {
                val outcome = onCallUseCase.acknowledgeByToken(alertId, token, at)
                when (outcome.status) {
                    AcknowledgementStatus.ACKNOWLEDGED ->
                        TwilioScripts.acknowledgementAccepted(outcome.responder?.name)

                    AcknowledgementStatus.ALREADY_ACKNOWLEDGED ->
                        TwilioScripts.acknowledgementAlreadyHandled(outcome.responder?.name)

                    AcknowledgementStatus.ALERT_NOT_FOUND ->
                        TwilioScripts.acknowledgementAlertMissing()

                    AcknowledgementStatus.ASSIGNMENT_NOT_FOUND,
                    AcknowledgementStatus.TOKEN_NOT_FOUND ->
                        TwilioScripts.acknowledgementAssignmentMissing()
                }
            }

            "2" -> TwilioScripts.acknowledgementPrompt(alert, assignment, webhookBaseUrl)

            else -> TwilioScripts.acknowledgementInvalidInput(alert, assignment, webhookBaseUrl)
        }
    }
}
