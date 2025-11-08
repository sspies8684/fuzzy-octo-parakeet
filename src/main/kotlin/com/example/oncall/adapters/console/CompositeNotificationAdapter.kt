package com.example.oncall.adapters.console

import com.example.oncall.application.port.out.NotificationPort
import com.example.oncall.domain.model.Alert
import com.example.oncall.domain.model.Assignment

class CompositeNotificationAdapter(
    private val delegates: List<NotificationPort>
) : NotificationPort {

    constructor(vararg ports: NotificationPort) : this(ports.toList())

    init {
        require(delegates.isNotEmpty()) { "CompositeNotificationAdapter requires at least one delegate" }
    }

    override fun notify(alert: Alert, assignment: Assignment) {
        delegates.forEach { it.notify(alert, assignment) }
    }
}
