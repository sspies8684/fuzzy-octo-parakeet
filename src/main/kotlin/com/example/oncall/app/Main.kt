package com.example.oncall.app

import com.example.oncall.adapters.console.CompositeNotificationAdapter
import com.example.oncall.adapters.console.ConsoleNotificationAdapter
import com.example.oncall.adapters.inmemory.InMemoryAlertRepository
import com.example.oncall.adapters.twilio.TwilioCallInstruction
import com.example.oncall.adapters.twilio.TwilioNotificationAdapter
import com.example.oncall.adapters.twilio.TwilioScripts
import com.example.oncall.application.port.out.NotificationPort
import com.example.oncall.application.service.OnCallService
import com.example.oncall.domain.model.AlertPriority
import com.example.oncall.domain.model.EscalationLevel
import com.example.oncall.domain.model.EscalationPolicy
import com.example.oncall.domain.model.NotificationChannel
import com.example.oncall.domain.model.OnCallTarget
import com.example.oncall.domain.model.Responder
import java.time.Duration
import java.time.Instant

fun main() {
    val repository = InMemoryAlertRepository()

    val sinks = mutableListOf<NotificationPort>(ConsoleNotificationAdapter())
    val twilioAccountSid = System.getenv("TWILIO_ACCOUNT_SID")
    val twilioAuthToken = System.getenv("TWILIO_AUTH_TOKEN")
    val twilioFromNumber = System.getenv("TWILIO_FROM_NUMBER")
    val twilioWebhookBase = System.getenv("TWILIO_ACK_WEBHOOK_BASE")

    if (!twilioAccountSid.isNullOrBlank() && !twilioAuthToken.isNullOrBlank() && !twilioFromNumber.isNullOrBlank()) {
        val webhookBase = twilioWebhookBase ?: "https://example.com/oncall/twilio"
        sinks += TwilioNotificationAdapter(
            accountSid = twilioAccountSid,
            authToken = twilioAuthToken,
            fromNumber = twilioFromNumber
        ) { alert, assignment ->
            TwilioCallInstruction.Script(
                TwilioScripts.acknowledgementPrompt(
                    alert = alert,
                    assignment = assignment,
                    webhookBaseUrl = webhookBase
                )
            )
        }
    }

    val notificationPort: NotificationPort = if (sinks.size == 1) {
        sinks.single()
    } else {
        CompositeNotificationAdapter(sinks)
    }

    val service = OnCallService(
        policiesByPriority = buildPolicies(),
        notificationPort = notificationPort,
        alertRepository = repository
    )

    val now = Instant.now()

    val alert = service.raiseAlert(
        message = "Database connection error",
        priority = AlertPriority.CRITICAL,
        createdAt = now
    )

    service.advance(now.plus(Duration.ofMinutes(6)))
    service.advance(now.plus(Duration.ofMinutes(12)))

    val secondaryAssignment = alert.assignments
        .first { it.target.responder.name == "Secondary Pager" }

    val acknowledgement = service.acknowledgeByToken(
        alertId = alert.id,
        token = secondaryAssignment.acknowledgementToken,
        at = now.plus(Duration.ofMinutes(13))
    )

    println(
        "Alert status: ${alert.status}, " +
            "acknowledged by: ${alert.acknowledgedBy?.name}, " +
            "outcome: ${acknowledgement.status}"
    )
}

private fun buildPolicies(): Map<AlertPriority, EscalationPolicy> {
    val primary = Responder(name = "Primary Pager", defaultContact = "+12025550100")
    val secondary = Responder(name = "Secondary Pager", defaultContact = "+12025550101")
    val manager = Responder(name = "Escalation Manager", defaultContact = "+12025550102")

    val lowPolicy = EscalationPolicy(
        levels = listOf(
            EscalationLevel(
                targets = listOf(
                    OnCallTarget(
                        responder = primary,
                        channel = NotificationChannel.EMAIL,
                        address = "primary@example.com"
                    )
                ),
                acknowledgementTimeout = Duration.ofMinutes(30)
            )
        )
    )

    val highPolicy = EscalationPolicy(
        levels = listOf(
            EscalationLevel(
                targets = listOf(
                    OnCallTarget(primary, NotificationChannel.SMS)
                ),
                acknowledgementTimeout = Duration.ofMinutes(5)
            ),
            EscalationLevel(
                targets = listOf(
                    OnCallTarget(secondary, NotificationChannel.SMS)
                ),
                acknowledgementTimeout = Duration.ofMinutes(5)
            ),
            EscalationLevel(
                targets = listOf(
                    OnCallTarget(manager, NotificationChannel.PHONE_CALL)
                ),
                acknowledgementTimeout = Duration.ofMinutes(5)
            )
        )
    )

    return mapOf(
        AlertPriority.LOW to lowPolicy,
        AlertPriority.MEDIUM to lowPolicy,
        AlertPriority.HIGH to highPolicy,
        AlertPriority.CRITICAL to highPolicy
    )
}
