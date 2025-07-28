package com.example.simplesync.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplesync.model.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import org.json.JSONObject
import com.example.simplesync.BuildConfig
import com.example.simplesync.model.NotifType
import com.example.simplesync.model.UserMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray

val apiKey = BuildConfig.ONESIGNAL_API_KEY
val appId = BuildConfig.ONESIGNAL_APP_ID
const val USERS_TABLE = "users"
const val NOTIFS_TABLE = "notifications"

data class GroupedNotifications(
    val today: List<Notification>,
    val yesterday: List<Notification>,
    val last7Days: List<Notification>,
    val older: List<Notification>
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _notifications =
        MutableStateFlow(GroupedNotifications(emptyList(), emptyList(), emptyList(), emptyList()))
    val notifications: StateFlow<GroupedNotifications> = _notifications // GroupedNotifications

    // Track if notifications have been loaded
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    suspend fun sendNotificationToUser(playerId: String?, message: String): Boolean {
        return try {
            if (playerId != null) {
                sendPushNotification(playerId, message)
            } else {
                println("Player ID not found for user $playerId")
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun getPlayerIdForUser(userId: String): String? {
        Log.d("PushDebug", "Fetched user metadata: $UserMetadata")
        return try {
            val userMetadata = supabase.from(USERS_TABLE).select {
                filter { eq("id", userId) }
                limit(1)
            }.decodeSingleOrNull<UserMetadata>()

            userMetadata?.playerId
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun sendPushNotification(playerId: String, message: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jsonBody = JSONObject().apply {
                    put("app_id", appId)
                    put("include_player_ids", JSONArray().put(playerId))
                    put("headings", JSONObject().put("en", "SimpleSync"))
                    put("contents", JSONObject().put("en", message))
                }

                val requestBody = jsonBody.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val headers = Headers.Builder()
                    .add("Authorization", "Basic ${apiKey.trim()}")
                    .add("Content-Type", "application/json")
                    .build()

                val request = Request.Builder()
                    .url("https://api.onesignal.com/notifications")
                    .headers(headers)
                    .post(requestBody)
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                response.close()
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("PushNotification", "Failed to send push notification", e)
                false
            }
        }
    }

    fun fetchNotifsForUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetched = supabase.from(NOTIFS_TABLE).select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<Notification>()
                Log.d("NOTIF_VIEWMODEL", "fetched notifs: $fetched")

                _notifications.value = groupNotifications(populateSenderInfo(fetched))
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Failed to fetch notifications", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun insertNotif(type: NotifType, receiver: String, sender: String, eventId:String? = null) {
        viewModelScope.launch {
            try {
                supabase.from(NOTIFS_TABLE).insert(
                    Notification(
                        type = type,
                        receiver = receiver,
                        sender = sender,
                        event = eventId,
                        read = false
                    )
                )
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Failed to insert notifications", e)
            }
        }
    }

    private fun groupNotifications(notifs: List<Notification>): GroupedNotifications {
        val now = Clock.System.now()
        val todayStart =
            now.toLocalDateTime(TimeZone.currentSystemDefault()).date.atStartOfDayIn(TimeZone.currentSystemDefault())
        val yesterdayStart = todayStart.minus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
        val weekStart = todayStart.minus(7, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

        val todayList = mutableListOf<Notification>()
        val yesterdayList = mutableListOf<Notification>()
        val last7DaysList = mutableListOf<Notification>()
        val olderList = mutableListOf<Notification>()

        for (notif in notifs) {
            when {
                notif.timestamp >= todayStart -> todayList.add(notif)
                notif.timestamp >= yesterdayStart -> yesterdayList.add(notif)
                notif.timestamp >= weekStart -> last7DaysList.add(notif)
                else -> olderList.add(notif)
            }
        }
        return GroupedNotifications(
            today = todayList.sortedByDescending { it.timestamp },
            yesterday = yesterdayList.sortedByDescending { it.timestamp },
            last7Days = last7DaysList.sortedByDescending { it.timestamp },
            older = olderList.sortedByDescending { it.timestamp }
        )
    }

    private suspend fun populateSenderInfo(notifications: List<Notification>): List<Notification> {
        return notifications.map { notif ->
            try {
                val senderMeta = supabase.from("users").select {
                    filter { eq("id", notif.sender) }
                }.decodeSingle<UserMetadata>()
                notif.copy(senderUsername = senderMeta.username, senderPfpUrl = senderMeta.profilePicURL)
            } catch (e: Exception) {
                notif.copy(senderUsername = "Unknown")
            }
        }
    }

}
