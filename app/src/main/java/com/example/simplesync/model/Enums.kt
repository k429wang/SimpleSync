package com.example.simplesync.model

import kotlinx.serialization.Serializable

@Serializable enum class EventType { IRL, VIRTUAL }
@Serializable enum class Recurrence { ONCE, DAILY, WEEKLY }
@Serializable enum class Visibility { SOLO, PRIVATE, PUBLIC }
@Serializable enum class Status { PENDING, ACCEPTED, DECLINED }
