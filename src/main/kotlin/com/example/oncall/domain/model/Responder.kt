package com.example.oncall.domain.model

import java.util.UUID

/**
 * Represents a responder in the on-call rotation.
 */
data class Responder(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val defaultContact: String
) {
    init {
        require(name.isNotBlank()) { "Responder name must be provided" }
        require(defaultContact.isNotBlank()) { "Responder contact must be provided" }
    }
}

/**
 * Communication channel for delivering notifications.
 */
enum class NotificationChannel {
    EMAIL,
    SMS,
    PUSH,
    CHAT,
    PHONE_CALL
}

/**
 * Concrete target in an escalation level, pairing a responder with a specific channel and address.
 */
data class OnCallTarget(
    val responder: Responder,
    val channel: NotificationChannel,
    val address: String = responder.defaultContact
) {
    init {
        require(address.isNotBlank()) { "Target address must be provided" }
    }
}
