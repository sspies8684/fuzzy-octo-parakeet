package com.example.oncall

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Call
import com.twilio.type.PhoneNumber
import java.net.URI

/**
 * Notification sink that places outbound voice calls through Twilio for `PHONE_CALL` targets.
 *
 * Twilio requires you to host a TwiML document (or provide a dynamic webhook) that controls
 * how the call behaves. Provide a `twimlUrlProvider` to build the per-alert URL.
 *
 * @param accountSid Twilio Account SID.
 * @param authToken Twilio Auth Token.
 * @param fromNumber Verified Twilio phone number in E.164 format (`+15551234567`).
 * @param twimlUrlProvider Returns the TwiML URL to execute for the given alert assignment.
 */
class TwilioNotificationSink(
    accountSid: String,
    authToken: String,
    private val fromNumber: String,
    private val twimlUrlProvider: (Alert, Assignment) -> URI
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

        val call = Call.creator(
            PhoneNumber(toNumber),
            PhoneNumber(fromNumber),
            twimlUrlProvider(alert, assignment)
        ).create()

        println(
            "Initiated Twilio call ${call.sid} to $toNumber for alert ${alert.id} (level ${assignment.levelIndex})"
        )
    }
}
