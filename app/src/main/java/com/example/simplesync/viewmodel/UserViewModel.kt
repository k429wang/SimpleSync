package com.example.simplesync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplesync.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {
    // Single user
    private val _currUser = MutableStateFlow<User?>(null)
    val currUser: StateFlow<User?> = _currUser

    // All users
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        fetchUsers()
    }

    // Fetch all users
    private fun fetchUsers(){
        viewModelScope.launch {
            try {
                // Retrieve all users
                val result = supabase.from("users").select().decodeList<User>()
                _users.value = result
            } catch (e: Exception) {
                // Handle error
                android.util.Log.e("UserViewModel", "Failed to fetch users", e)
            }
        }
    }

    // Fetch a user using their username
    fun getUserById(id: String) {
        viewModelScope.launch {
            try {
                val user = supabase.from("users").select {
                    filter{
                        eq("userName", id) // TODO: Change to logged in user
                    }
                }.decodeSingle<User>()

                _currUser.value = user
            } catch (e: Exception) {
                android.util.Log.e("UserViewModel", "Error fetching user by ID", e)
                _currUser.value = null
            }
        }
    }

    // Create a new user
    fun createUser(newUser: User, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            try {
                supabase.from("users")
                    .insert(newUser)
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("UserViewModel", "Failed to create user", e)
                onError(e)
            }
        }
    }
}