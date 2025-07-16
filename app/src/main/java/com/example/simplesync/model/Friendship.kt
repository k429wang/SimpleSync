package com.example.simplesync.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Friendship(
    @SerialName("user_id") val userId: String = UUID.randomUUID().toString(),
    @SerialName("friend_id") val friendId: String = UUID.randomUUID().toString(),
    @SerialName("status") val status: Status,
    @Contextual @SerialName("created_at") val createdAt: Instant = Clock.System.now(),
)