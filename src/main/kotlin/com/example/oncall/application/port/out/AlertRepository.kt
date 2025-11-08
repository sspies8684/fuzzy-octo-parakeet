package com.example.oncall.application.port.out

import com.example.oncall.domain.model.Alert
import com.example.oncall.domain.model.AlertStatus
import java.util.UUID

interface AlertRepository {
    fun save(alert: Alert): Alert
    fun findById(id: UUID): Alert?
    fun findAll(status: AlertStatus? = null): List<Alert>
}
