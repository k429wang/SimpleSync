package com.example.simplesync.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Event(
    @SerialName("id") val id: String = UUID.randomUUID().toString(), // Event ID
    @SerialName("owner") val owner: String, // User ID
    @SerialName("name") val name: String,
    @SerialName("description") val description: String?,
    @Contextual @SerialName("start_time") val startTime: Instant,
    @Contextual @SerialName("end_time") val endTime: Instant,
    @SerialName("type") val type: EventType,
    @SerialName("location") val location: String?,
    @SerialName("recurrence") val recurrence: Recurrence,
    @SerialName("visibility") val visibility: Visibility,
    @SerialName("external_id") val externalId: String? = null,
    @Contextual @SerialName("created_at") val createdAt: Instant = Clock.System.now(),
    @Contextual @SerialName("updated_at") val updatedAt: Instant = Clock.System.now()
)