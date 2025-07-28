package com.example.simplesync.viewmodel

import androidx.lifecycle.ViewModel
import com.example.simplesync.model.Friendship
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import com.example.simplesync.util.createNotification
import com.example.simplesync.model.NotifType
import com.example.simplesync.model.Status

const val FRIENDS_TABLE = "friendships"

@HiltViewModel
class FriendshipViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _friendships = MutableStateFlow<List<Friendship>>(emptyList())
    val friendships: StateFlow<List<Friendship>> = _friendships

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Fetch friends for a specific user
    suspend fun fetchFriendshipsForUser(userId: String): Result<Boolean> {
        _isLoading.value = true
        return try {
            val fetched = supabase.from(FRIENDS_TABLE).select {
                filter {
                    or {
                        eq("user_id", userId) // User initiated
                        eq("friend_id", userId) // Other user initiated
                    }
                }
            }.decodeList<Friendship>()

            _friendships.value = fetched
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    // Create a new friendship
    suspend fun createFriendship(friendship: Friendship): Result<Boolean> {
        return try {
                supabase.from(FRIENDS_TABLE).insert(friendship)

                // Insert friend request to notification table
                createNotification(
                    supabase = supabase,
                    receiver = friendship.friendId,
                    sender = friendship.userId,
                    type = NotifType.FRIEND_REQUEST
                )

                fetchFriendshipsForUser(friendship.userId) // Pull updated friends
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
    }

    // Update an existing friendship (e.g., status change)
    suspend fun updateFriendship(friendship: Friendship, userIdToFetch: String): Result<Boolean> {
        return try {
            supabase.from(FRIENDS_TABLE).update(friendship) {
                filter {
                    eq("user_id", friendship.userId)
                    eq("friend_id", friendship.friendId)
                }
            }

            val notifType = when (friendship.status) {
                Status.ACCEPTED -> NotifType.FRIEND_ACCEPT
                Status.DECLINED -> NotifType.FRIEND_DECLINE
                else -> null
            }

            notifType?.let {
                createNotification(
                    supabase = supabase,
                    receiver = friendship.userId,
                    sender = friendship.friendId,
                    type = it
                )
            }

            fetchFriendshipsForUser(userIdToFetch)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete a friendship
    suspend fun deleteFriendship(friendship: Friendship, userIdToFetch: String): Result<Boolean> {
        return try {
            supabase.from(FRIENDS_TABLE).delete {
                filter {
                    eq("user_id", friendship.userId)
                    eq("friend_id", friendship.friendId)
                }
            }
            fetchFriendshipsForUser(userIdToFetch)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}