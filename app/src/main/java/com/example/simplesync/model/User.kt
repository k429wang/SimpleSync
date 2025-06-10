package com.example.simplesync.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userName: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val createdAt: String? = null,
)

