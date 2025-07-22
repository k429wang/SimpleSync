package com.example.simplesync.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Notification(
    @SerialName("id") val id: String = UUID.randomUUID().toString(), // Notification ID
    @SerialName("type") val type: NotifType,
    @SerialName("user_id") val receiver: String, // ID of user receiving notification
    @SerialName("sender_id") val sender: String, // User ID of notification sender
    @SerialName("event_id") val event: String?,
    @SerialName("is_read") val read: Boolean,
    @Contextual @SerialName("created_at") val timestamp: Instant = Clock.System.now()
)