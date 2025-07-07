package com.example.simplesync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplesync.model.FullUser
import com.example.simplesync.model.UserMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


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
                val metadata = supabase.from("users").select {
                        filter{
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
                val metadata = supabase.from("users").select {
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
}