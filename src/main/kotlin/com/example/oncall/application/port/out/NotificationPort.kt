package com.example.oncall.application.port.out

import com.example.oncall.domain.model.Alert
import com.example.oncall.domain.model.Assignment

/**
 * Outbound port for delivering alert notifications to responders.
 */
fun interface NotificationPort {
    fun notify(alert: Alert, assignment: Assignment)
}
