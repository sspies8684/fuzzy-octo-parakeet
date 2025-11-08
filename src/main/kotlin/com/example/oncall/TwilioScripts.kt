package com.example.oncall

import com.twilio.twiml.HttpMethod
import com.twilio.twiml.VoiceResponse
import com.twilio.twiml.voice.Gather
import com.twilio.twiml.voice.Hangup
import com.twilio.twiml.voice.Redirect
import com.twilio.twiml.voice.Say
import java.util.Locale
import java.util.UUID

/**
 * Helpers that produce TwiML voice scripts for the on-call acknowledgement workflow using the
 * Twilio SDK's builder pattern.
 */
object TwilioScripts {
    private val DEFAULT_VOICE: Say.Voice = Say.Voice.POLLY_JOANNA

    fun acknowledgementPrompt(
        alert: Alert,
        assignment: Assignment,
        webhookBaseUrl: String,
        voice: Say.Voice = DEFAULT_VOICE
    ): String {
        val ackUrl = acknowledgementActionUrl(webhookBaseUrl, alert, assignment)
        val promptUrl = acknowledgementPromptUrl(webhookBaseUrl, alert, assignment)
        val priorityName = alert.priority.name.lowercase(Locale.US)
        val sayText =
            "This is the on-call system with a $priorityName priority alert. ${alert.message}. " +
                "Press 1 to acknowledge this incident. Press 2 to hear this message again."

        val gather = Gather.Builder()
            .input(Gather.Input.DTMF)
            .numDigits(1)
            .timeout(10)
            .action(ackUrl)
            .method(HttpMethod.POST)
            .say(Say.Builder(sayText).voice(voice).build())
            .build()

        val response = VoiceResponse.Builder()
            .gather(gather)
            .say(Say.Builder("We did not receive your input.").voice(voice).build())
            .redirect(Redirect.Builder(promptUrl).method(HttpMethod.POST).build())
            .build()

        return response.toXml()
    }

    fun acknowledgementAccepted(
        responderName: String?,
        voice: Say.Voice = DEFAULT_VOICE
    ): String {
        val prefix = responderName?.let { "Thank you, $it." } ?: "Thank you."
        val response = VoiceResponse.Builder()
            .say(Say.Builder("$prefix The alert has been acknowledged. Goodbye.").voice(voice).build())
            .hangup(Hangup.Builder().build())
            .build()

        return response.toXml()
    }

    fun acknowledgementAlreadyHandled(
        responderName: String?,
        voice: Say.Voice = DEFAULT_VOICE
    ): String {
        val suffix = responderName?.let { "It was already acknowledged by $it." }
            ?: "It was already acknowledged earlier."
        val response = VoiceResponse.Builder()
            .say(Say.Builder("This alert has already been acknowledged. $suffix Goodbye.").voice(voice).build())
            .hangup(Hangup.Builder().build())
            .build()

        return response.toXml()
    }

    fun acknowledgementInvalidInput(
        alert: Alert,
        assignment: Assignment,
        webhookBaseUrl: String,
        voice: Say.Voice = DEFAULT_VOICE
    ): String {
        val promptUrl = acknowledgementPromptUrl(webhookBaseUrl, alert, assignment)
        val response = VoiceResponse.Builder()
            .say(Say.Builder("We did not understand that input.").voice(voice).build())
            .redirect(Redirect.Builder(promptUrl).method(HttpMethod.POST).build())
            .build()

        return response.toXml()
    }

    fun acknowledgementAssignmentMissing(voice: Say.Voice = DEFAULT_VOICE): String {
        val response = VoiceResponse.Builder()
            .say(
                Say.Builder("We could not locate an active responder assignment for this alert. Please contact the operations team.")
                    .voice(voice)
                    .build()
            )
            .hangup(Hangup.Builder().build())
            .build()

        return response.toXml()
    }

    fun acknowledgementAlertMissing(voice: Say.Voice = DEFAULT_VOICE): String {
        val response = VoiceResponse.Builder()
            .say(
                Say.Builder("We could not find the alert referenced in this call. Please contact the operations team.")
                    .voice(voice)
                    .build()
            )
            .hangup(Hangup.Builder().build())
            .build()

        return response.toXml()
    }

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
}
