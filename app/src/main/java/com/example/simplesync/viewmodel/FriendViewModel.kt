package com.example.simplesync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplesync.model.Friend
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val FRIENDS_TABLE = "friendships"

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends

    private val _friendResult = MutableStateFlow<Result<Boolean>?>(null)
    val friendResult: StateFlow<Result<Boolean>?> = _friendResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Fetch friends for a specific user
    fun fetchFriendsForUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetched = supabase.from("friendships").select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<Friend>()
                _friends.value = fetched
                _friendResult.value = Result.success(true)
            } catch (e: Exception) {
                _friendResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Create a new friend relationship
    fun createFriend(friend: Friend) {
        viewModelScope.launch {
            try {
                supabase.from(FRIENDS_TABLE).insert(friend)
                fetchFriendsForUser(friend.userId)
                _friendResult.value = Result.success(true)
            } catch (e: Exception) {
                _friendResult.value = Result.failure(e)
            }
        }
    }

    // Update an existing friend (e.g., status change)
    fun updateFriend(friend: Friend) {
        viewModelScope.launch {
            try {
                supabase.from(FRIENDS_TABLE).update(friend) {
                    filter {
                        eq("user_id", friend.userId)
                        eq("friend_id", friend.friendId)
                    }
                }
                fetchFriendsForUser(friend.userId)
                _friendResult.value = Result.success(true)
            } catch (e: Exception) {
                _friendResult.value = Result.failure(e)
            }
        }
    }

    // Delete a friend relationship
    fun deleteFriend(userId: String, friendId: String) {
        viewModelScope.launch {
            try {
                supabase.from(FRIENDS_TABLE).delete {
                    filter {
                        eq("user_id", userId)
                        eq("friend_id", friendId)
                    }
                }
                fetchFriendsForUser(userId)
                _friendResult.value = Result.success(true)
            } catch (e: Exception) {
                _friendResult.value = Result.failure(e)
            }
        }
    }
}