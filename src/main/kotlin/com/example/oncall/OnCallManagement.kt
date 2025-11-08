package com.example.oncall

import java.net.URI
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Priority buckets supported by the on-call system. Priorities typically map to
 * different escalation policies.
 */
enum class AlertPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * State of an alert inside the routing engine.
 */
enum class AlertStatus {
    PENDING,
    ACKNOWLEDGED,
    EXHAUSTED
}

/**
 * Notification channel used to reach an on-call target. Extend as needed for integrations.
 */
enum class NotificationChannel {
    EMAIL,
    SMS,
    PUSH,
    CHAT,
    PHONE_CALL
}

/**
 * Represents a responder in the on-call rotation.
 */
data class Responder(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val contact: String
)

/**
 * A concrete target in an escalation level (responder plus channel).
 */
data class OnCallTarget(
    val responder: Responder,
    val channel: NotificationChannel,
    val address: String = responder.contact
) {
    init {
        require(address.isNotBlank()) { "On-call target requires a delivery address" }
    }
}

/**
 * Escalation level describing who to notify and how long to wait for acknowledgement.
 */
data class EscalationLevel(
    val targets: List<OnCallTarget>,
    val acknowledgementTimeout: Duration
) {
    init {
        require(targets.isNotEmpty()) { "Escalation level requires at least one target" }
        require(!acknowledgementTimeout.isNegative) { "Acknowledgement timeout must be positive" }
    }
}

/**
 * Policy describing the escalation path for a priority bucket.
 */
data class EscalationPolicy(
    val levels: List<EscalationLevel>
) {
    init {
        require(levels.isNotEmpty()) { "Escalation policy requires at least one level" }
    }
}

/**
 * Assignment state for a target that was paged about an alert.
 */
data class Assignment(
    val target: OnCallTarget,
    val levelIndex: Int,
    val dispatchedAt: Instant,
    val deadline: Instant,
    var acknowledgedAt: Instant? = null
) {
    val isAcknowledged: Boolean
        get() = acknowledgedAt != null

    fun acknowledge(at: Instant) {
        acknowledgedAt = at
    }
}

/**
 * Alert tracked by the on-call management system.
 */
data class Alert(
    val id: UUID = UUID.randomUUID(),
    val message: String,
    val priority: AlertPriority,
    val createdAt: Instant,
    val policy: EscalationPolicy,
    val assignments: MutableList<Assignment> = mutableListOf(),
    var status: AlertStatus = AlertStatus.PENDING,
    var currentLevelIndex: Int = 0,
    var acknowledgedBy: Responder? = null,
    var acknowledgedAt: Instant? = null
) {
    fun assignmentsForLevel(levelIndex: Int): List<Assignment> =
        assignments.filter { it.levelIndex == levelIndex }
}

/**
 * Abstraction for delivering notifications to responders.
 */
fun interface NotificationSink {
    fun notify(alert: Alert, assignment: Assignment)
}

/**
 * Simple notification sink that prints routing activity to stdout.
 */
class PrintNotificationSink : NotificationSink {
    override fun notify(alert: Alert, assignment: Assignment) {
        println(
            "Dispatching alert ${alert.id} (${alert.priority}) " +
                "to ${assignment.target.responder.name} via ${assignment.target.channel} " +
                "(deadline ${assignment.deadline})"
        )
    }
}

/**
 * Utility sink that fans out notifications to multiple downstream sinks. Useful for combining
 * observability sinks (logs/metrics) with delivery mechanisms like Twilio.
 */
class CompositeNotificationSink(
    private val delegates: List<NotificationSink>
) : NotificationSink {
    constructor(vararg sinks: NotificationSink) : this(sinks.toList())

    init {
        require(delegates.isNotEmpty()) { "CompositeNotificationSink requires at least one delegate" }
    }

    override fun notify(alert: Alert, assignment: Assignment) {
        delegates.forEach { it.notify(alert, assignment) }
    }
}

/**
 * Core on-call management engine. Routes alerts, tracks acknowledgements and escalates as needed.
 */
class OnCallManager(
    private val policiesByPriority: Map<AlertPriority, EscalationPolicy>,
    private val notificationSink: NotificationSink = PrintNotificationSink()
) {
    private val alertsById = mutableMapOf<UUID, Alert>()

    fun raiseAlert(
        message: String,
        priority: AlertPriority,
        createdAt: Instant = Instant.now()
    ): Alert {
        val policy = policiesByPriority[priority]
            ?: error("No escalation policy configured for $priority")
        val alert = Alert(
            message = message,
            priority = priority,
            createdAt = createdAt,
            policy = policy
        )
        alertsById[alert.id] = alert
        dispatchLevel(alert, levelIndex = 0, dispatchedAt = createdAt)
        return alert
    }

    fun getAlert(id: UUID): Alert? = alertsById[id]

    fun listAlerts(status: AlertStatus? = null): List<Alert> =
        alertsById.values
            .filter { status == null || it.status == status }
            .sortedBy { it.createdAt }

    fun acknowledge(
        alertId: UUID,
        responderId: UUID,
        at: Instant = Instant.now()
    ) {
        val alert = alertsById[alertId] ?: error("Alert $alertId not found")
        if (alert.status != AlertStatus.PENDING) {
            error("Alert $alertId is not pending; current status ${alert.status}")
        }

        val assignment = alert.assignments
            .firstOrNull { it.target.responder.id == responderId && !it.isAcknowledged }
            ?: error("Responder $responderId has no pending assignment for alert $alertId")

        assignment.acknowledge(at)
        alert.status = AlertStatus.ACKNOWLEDGED
        alert.acknowledgedBy = assignment.target.responder
        alert.acknowledgedAt = at
    }

    /**
     * Move pending alerts forward in time, performing escalations wherever deadlines are missed.
     * Returns the alerts that escalated during this tick.
     */
    fun advance(now: Instant = Instant.now()): List<Alert> {
        val escalatedAlerts = mutableListOf<Alert>()

        alertsById.values
            .filter { it.status == AlertStatus.PENDING }
            .forEach { alert ->
                val currentLevelAssignments = alert.assignmentsForLevel(alert.currentLevelIndex)

                if (currentLevelAssignments.any { it.isAcknowledged }) {
                    return@forEach
                }

                val levelDeadline = currentLevelAssignments.maxOfOrNull { it.deadline }
                    ?: return@forEach

                if (now.isBefore(levelDeadline)) {
                    return@forEach
                }

                val nextLevel = alert.currentLevelIndex + 1
                if (nextLevel >= alert.policy.levels.size) {
                    alert.status = AlertStatus.EXHAUSTED
                    escalatedAlerts += alert
                } else {
                    alert.currentLevelIndex = nextLevel
                    dispatchLevel(alert, nextLevel, now)
                    escalatedAlerts += alert
                }
            }

        return escalatedAlerts
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
            notificationSink.notify(alert, assignment)
        }
    }
}

/**
 * Example usage that simulates a simple on-call policy, alert creation and escalations.
 */
fun main() {
    val primary = Responder(name = "Primary Pager", contact = "+12025550100")
    val secondary = Responder(name = "Secondary Pager", contact = "+12025550101")
    val manager = Responder(name = "Escalation Manager", contact = "+12025550102")

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

    val sinks = mutableListOf<NotificationSink>(PrintNotificationSink())

    val twilioAccountSid = System.getenv("TWILIO_ACCOUNT_SID")
    val twilioAuthToken = System.getenv("TWILIO_AUTH_TOKEN")
    val twilioFromNumber = System.getenv("TWILIO_FROM_NUMBER")

    if (twilioAccountSid != null && twilioAuthToken != null && twilioFromNumber != null) {
        sinks += TwilioNotificationSink(
            accountSid = twilioAccountSid,
            authToken = twilioAuthToken,
            fromNumber = twilioFromNumber
        ) { alert, _ ->
            URI.create("https://example.com/twiml/alerts/${alert.id}")
        }
    }

    val notificationSink: NotificationSink = if (sinks.size == 1) {
        sinks.single()
    } else {
        CompositeNotificationSink(sinks)
    }

    val managerEngine = OnCallManager(
        policiesByPriority = mapOf(
            AlertPriority.LOW to lowPolicy,
            AlertPriority.MEDIUM to lowPolicy,
            AlertPriority.HIGH to highPolicy,
            AlertPriority.CRITICAL to highPolicy
        ),
        notificationSink = notificationSink
    )

    val now = Instant.now()
    val alert = managerEngine.raiseAlert(
        message = "Database connection error",
        priority = AlertPriority.CRITICAL,
        createdAt = now
    )

    // Simulate time passing without acknowledgement to trigger escalations.
    managerEngine.advance(now.plus(Duration.ofMinutes(6)))
    managerEngine.advance(now.plus(Duration.ofMinutes(12)))

    // Secondary responder acknowledges the alert.
    managerEngine.acknowledge(alert.id, secondary.id, now.plus(Duration.ofMinutes(13)))

    println("Alert status: ${alert.status}, acknowledged by: ${alert.acknowledgedBy?.name}")
}
