package com.example.oncall

import java.time.Instant
import java.util.UUID

/**
 * Helpers for building Twilio webhook responses for the acknowledgement gather flow.
 *
 * These functions can be wired to a lightweight HTTP server (Ktor, Spring, etc.) that handles the
 * Twilio `Gather` callbacks.
 */
object TwilioWebhookHandlers {

    fun prompt(
        manager: OnCallManager,
        alertId: UUID,
        token: UUID,
        webhookBaseUrl: String
    ): String {
        val alert = manager.getAlert(alertId)
            ?: return TwilioScripts.acknowledgementAlertMissing()

        val assignment = alert.assignments.firstOrNull { it.acknowledgementToken == token }
            ?: return TwilioScripts.acknowledgementAssignmentMissing()

        return TwilioScripts.acknowledgementPrompt(alert, assignment, webhookBaseUrl)
    }

    fun acknowledge(
        manager: OnCallManager,
        alertId: UUID,
        token: UUID,
        digits: String?,
        webhookBaseUrl: String,
        at: Instant = Instant.now()
    ): String {
        val alert = manager.getAlert(alertId)
            ?: return TwilioScripts.acknowledgementAlertMissing()

        val assignment = alert.assignments.firstOrNull { it.acknowledgementToken == token }
            ?: return TwilioScripts.acknowledgementAssignmentMissing()

        val normalizedDigits = digits?.trim().orEmpty()
        if (normalizedDigits.isEmpty()) {
            return TwilioScripts.acknowledgementInvalidInput(alert, assignment, webhookBaseUrl)
        }

        return when (normalizedDigits) {
            "1" -> {
                val outcome = manager.acknowledgeByToken(alertId, token, at)
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
