package com.example.oncall.adapters.console

import com.example.oncall.application.port.out.NotificationPort
import com.example.oncall.domain.model.Alert
import com.example.oncall.domain.model.Assignment

class ConsoleNotificationAdapter : NotificationPort {
    override fun notify(alert: Alert, assignment: Assignment) {
        println(
            """
            Dispatching alert ${alert.id} (${alert.priority})
            to ${assignment.target.responder.name} via ${assignment.target.channel}
            deadline: ${assignment.deadline}
            """.trimIndent()
        )
    }
}
