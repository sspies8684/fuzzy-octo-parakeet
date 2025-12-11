package com.example.oncall.adapters.inmemory

import com.example.oncall.application.port.out.AlertRepository
import com.example.oncall.domain.model.Alert
import com.example.oncall.domain.model.AlertStatus
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryAlertRepository : AlertRepository {
    private val alerts = ConcurrentHashMap<UUID, Alert>()

    override fun save(alert: Alert): Alert {
        alerts[alert.id] = alert
        return alert
    }

    override fun findById(id: UUID): Alert? = alerts[id]

    override fun findAll(status: AlertStatus?): List<Alert> =
        alerts.values
            .filter { status == null || it.status == status }
            .sortedBy { it.createdAt }
}
