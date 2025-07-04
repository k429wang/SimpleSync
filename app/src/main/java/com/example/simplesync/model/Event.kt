package com.example.simplesync.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Event(
    @Contextual @SerialName("id") val id: UUID = UUID.randomUUID(),
    @Contextual @SerialName("owner") val owner: UUID,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String?,
    @SerialName("start_time") val startTime: Instant,
    @SerialName("end_time") val endTime: Instant,
    @SerialName("type") val type: EventType,
    @SerialName("location") val location: String?,
    @SerialName("recurrence") val recurrence: Recurrence,
    @SerialName("visibility") val visibility: Visibility,
    @SerialName("created_at") val createdAt: Instant = Clock.System.now(),
    @SerialName("updated_at") val updatedAt: Instant = Clock.System.now()
)