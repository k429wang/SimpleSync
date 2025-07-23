package com.example.simplesync.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attendee(
    @SerialName("event_id") val eventId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("role") val role: EventRole,
    @SerialName("invited_by") val invitedBy: String?,
    @SerialName("invite_status") val inviteStatus: Status,
)