package com.example.simplesync.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.jan.supabase.auth.user.UserInfo

// User from public.users
@Serializable
data class UserMetadata(
    @SerialName("id") val id: String,
    @SerialName("username") val username: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("profile_pic_url") val profilePicURL: String? = null,
    @SerialName("player_id") val playerId: String? = null,
)

// Combined data model
data class FullUser(
    val authUser: UserInfo, // From Supabase (auth.users)
    val userMetadata: UserMetadata, // From public.users
)