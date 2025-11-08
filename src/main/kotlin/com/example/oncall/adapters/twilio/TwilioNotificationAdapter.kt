package com.example.oncall.adapters.twilio

import com.example.oncall.application.port.out.NotificationPort
import com.example.oncall.domain.model.Alert
import com.example.oncall.domain.model.Assignment
import com.example.oncall.domain.model.NotificationChannel
import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Call
import com.twilio.type.PhoneNumber
import com.twilio.type.Twiml
import java.net.URI

class TwilioNotificationAdapter(
    accountSid: String,
    authToken: String,
    private val fromNumber: String,
    private val instructionProvider: (Alert, Assignment) -> TwilioCallInstruction
) : NotificationPort {

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

sealed class TwilioCallInstruction {
    data class Url(val uri: URI) : TwilioCallInstruction()
    data class Script(val twiml: String) : TwilioCallInstruction()
}
