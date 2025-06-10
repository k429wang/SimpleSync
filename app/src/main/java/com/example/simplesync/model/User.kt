package com.example.simplesync.model

import java.time.OffsetDateTime

data class User(
    val userName: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val createdAt: OffsetDateTime,
)
