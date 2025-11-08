package com.example.oncall

import java.util.Locale
import java.util.UUID

/**
 * Helpers that produce TwiML voice scripts for the on-call acknowledgement workflow.
 */
object TwilioScripts {
    private const val DEFAULT_VOICE = "Polly.Joanna"

    fun acknowledgementPrompt(
        alert: Alert,
        assignment: Assignment,
        webhookBaseUrl: String,
        voice: String = DEFAULT_VOICE
    ): String {
        val ackUrl = acknowledgementActionUrl(webhookBaseUrl, alert, assignment)
        val promptUrl = acknowledgementPromptUrl(webhookBaseUrl, alert, assignment)
        val priorityName = alert.priority.name.lowercase(Locale.US)
        val escapedMessage = escapeForTwiML(alert.message)
        val sayText =
            "This is the on-call system with a $priorityName priority alert. $escapedMessage. " +
                "Press 1 to acknowledge this incident. Press 2 to hear this message again."

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <Gather input="dtmf" numDigits="1" timeout="10" action="$ackUrl" method="POST">
                    <Say voice="$voice">$sayText</Say>
                </Gather>
                <Say voice="$voice">We did not receive your input.</Say>
                <Redirect method="POST">$promptUrl</Redirect>
            </Response>
        """.trimIndent()
    }

    fun acknowledgementAccepted(responderName: String?, voice: String = DEFAULT_VOICE): String {
        val prefix = responderName?.let { "Thank you, $it." } ?: "Thank you."
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <Say voice="$voice">$prefix The alert has been acknowledged. Goodbye.</Say>
                <Hangup/>
            </Response>
        """.trimIndent()
    }

    fun acknowledgementAlreadyHandled(
        responderName: String?,
        voice: String = DEFAULT_VOICE
    ): String {
        val suffix = responderName?.let { "It was already acknowledged by $it." }
            ?: "It was already acknowledged earlier."
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <Say voice="$voice">This alert has already been acknowledged. $suffix Goodbye.</Say>
                <Hangup/>
            </Response>
        """.trimIndent()
    }

    fun acknowledgementInvalidInput(
        alert: Alert,
        assignment: Assignment,
        webhookBaseUrl: String,
        voice: String = DEFAULT_VOICE
    ): String {
        val promptUrl = acknowledgementPromptUrl(webhookBaseUrl, alert, assignment)
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <Say voice="$voice">We did not understand that input.</Say>
                <Redirect method="POST">$promptUrl</Redirect>
            </Response>
        """.trimIndent()
    }

    fun acknowledgementAssignmentMissing(voice: String = DEFAULT_VOICE): String =
        """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <Say voice="$voice">We could not locate an active responder assignment for this alert. Please contact the operations team.</Say>
                <Hangup/>
            </Response>
        """.trimIndent()

    fun acknowledgementAlertMissing(voice: String = DEFAULT_VOICE): String =
        """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <Say voice="$voice">We could not find the alert referenced in this call. Please contact the operations team.</Say>
                <Hangup/>
            </Response>
        """.trimIndent()

    fun acknowledgementPromptUrl(
        baseUrl: String,
        alert: Alert,
        assignment: Assignment
    ): String = buildUrl(baseUrl, "prompt", alert.id, assignment.acknowledgementToken)

    fun acknowledgementActionUrl(
        baseUrl: String,
        alert: Alert,
        assignment: Assignment
    ): String = buildUrl(baseUrl, "acknowledge", alert.id, assignment.acknowledgementToken)

    private fun buildUrl(
        baseUrl: String,
        pathSuffix: String,
        alertId: UUID,
        token: UUID
    ): String {
        val normalizedBase = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) else baseUrl
        return "$normalizedBase/$pathSuffix?alertId=$alertId&token=$token"
    }

    private fun escapeForTwiML(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
}
