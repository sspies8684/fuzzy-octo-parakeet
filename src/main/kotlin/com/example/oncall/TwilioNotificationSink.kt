package com.example.oncall

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Call
import com.twilio.type.PhoneNumber
import com.twilio.type.Twiml
import java.net.URI

/**
 * Notification sink that places outbound voice calls through Twilio for `PHONE_CALL` targets.
 *
 * Twilio requires you to host a TwiML document (or provide a dynamic webhook) that controls
 * how the call behaves. Provide an `instructionProvider` to return either a hosted URL or an
 * inline TwiML script (for example, one produced by `TwilioScripts`).
 *
 * @param accountSid Twilio Account SID.
 * @param authToken Twilio Auth Token.
 * @param fromNumber Verified Twilio phone number in E.164 format (`+15551234567`).
 * @param instructionProvider Returns the Twilio call instruction for the given alert assignment.
 */
class TwilioNotificationSink(
    accountSid: String,
    authToken: String,
    private val fromNumber: String,
    private val instructionProvider: (Alert, Assignment) -> TwilioCallInstruction
) : NotificationSink {

    init {
        require(fromNumber.isNotBlank()) { "`fromNumber` must be provided" }
        Twilio.init(accountSid, authToken)
    }

    override fun notify(alert: Alert, assignment: Assignment) {
        if (assignment.target.channel != NotificationChannel.PHONE_CALL) {
            return
        }

        val toNumber = assignment.target.address
        require(toNumber.isNotBlank()) { "Responder phone number is required for Twilio calls" }

        val call = when (val instruction = instructionProvider(alert, assignment)) {
            is TwilioCallInstruction.Url -> Call.creator(
                PhoneNumber(toNumber),
                PhoneNumber(fromNumber),
                instruction.uri
            ).create()
            is TwilioCallInstruction.Script -> Call.creator(
                PhoneNumber(toNumber),
                PhoneNumber(fromNumber),
                Twiml(instruction.twiml)
            ).create()
        }

        println(
            "Initiated Twilio call ${call.sid} to $toNumber for alert ${alert.id} (level ${assignment.levelIndex})"
        )
    }
}

/**
 * Twilio call instructions support either hosting TwiML on your own endpoint (Url) or providing an
 * inline TwiML document (Script).
 */
sealed class TwilioCallInstruction {
    data class Url(val uri: URI) : TwilioCallInstruction()
    data class Script(val twiml: String) : TwilioCallInstruction()
}
