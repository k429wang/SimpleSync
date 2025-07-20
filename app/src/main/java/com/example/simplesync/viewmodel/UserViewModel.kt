package com.example.simplesync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplesync.model.FullUser
import com.example.simplesync.model.UserMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

var USERS_TABLE_NAME = "users"
var PFP_BUCKET_NAME = "profile-pictures"

@HiltViewModel
class UserViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {
    private val _currUser = MutableStateFlow<FullUser?>(null)
    val currUser: StateFlow<FullUser?> = _currUser

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error

    init {
        fetchCurrentUser()
    }

    fun fetchCurrentUser() {
        viewModelScope.launch {
            try {
                // Retrieve user from auth.users
                val authUser = supabase.auth.currentUserOrNull()
                    ?: throw IllegalStateException("User not logged in")

                // Retrieve user from public.users
                val userId = authUser.id
                val metadata = supabase.from(USERS_TABLE_NAME).select {
                    filter {
                        eq("id", userId)
                    }
                }
                    .decodeSingle<UserMetadata>()

                _currUser.value = FullUser(authUser, metadata)
            } catch (e: Exception) {
                _error.value = e
            }
        }
    }

    fun fetchUserMetadataById(userId: String, onResult: (UserMetadata?) -> Unit) {
        viewModelScope.launch {
            try {
                val metadata = supabase.from(USERS_TABLE_NAME).select {
                    filter {
                        eq("id", userId)
                    }
                }.decodeSingle<UserMetadata>()
                onResult(metadata)
            } catch (e: Exception) {
                _error.value = e
                onResult(null)
            }
        }
    }

    suspend fun getUserById(userId: String): UserMetadata? {
        return try {
            supabase.from(USERS_TABLE_NAME).select {
                filter { eq("id", userId) }
                limit(1)
            }.decodeSingleOrNull()
        } catch (e: Exception) {
            _error.value = e
            null
        }
    }

    suspend fun getUserByUsername(username: String): UserMetadata? {
        return try {
            supabase.from(USERS_TABLE_NAME).select {
                filter { eq("username", username) }
                limit(1)
            }.decodeSingleOrNull()
        } catch (e: Exception) {
            _error.value = e
            null
        }
    }

    fun updateUserMetadata(
        firstName: String,
        lastName: String,
        username: String,
        onResult: (Boolean) -> Unit
    ) {
        val userId = _currUser.value?.authUser?.id ?: return onResult(false)

        viewModelScope.launch {
            try {
                supabase.from(USERS_TABLE_NAME).update(
                    {
                        set("first_name", firstName)
                        set("last_name", lastName)
                        set("username", username)
                    }
                ) {
                    filter { eq("id", userId) }
                }

                // Refresh user data after update
                fetchCurrentUser()
                onResult(true)
            } catch (e: Exception) {
                _error.value = e
                onResult(false)
            }
        }
    }

    suspend fun uploadProfilePicture(imageBytes: ByteArray): Boolean {
        val userId = _currUser.value?.authUser?.id ?: return false // Exit early if no user
        val filePath = "$userId/profile_pic.jpg" // profile-pictures/<user-id>/profile_pic.jpg

        return try {
            // Upload to profile-pictures bucket
            supabase.storage.from(PFP_BUCKET_NAME)
                .upload(filePath, imageBytes) {
                    upsert=true
                }

            val publicUrl = supabase.storage.from(PFP_BUCKET_NAME)
                .publicUrl(filePath)

            // Add profile picture's public URL to Users table
            supabase.from(USERS_TABLE_NAME).update(
                {
                    set("profile_pic_url", publicUrl)
                }
            ) {
                filter { eq("id", userId) }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}