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
import com.example.simplesync.model.Event
import com.example.simplesync.model.UserMetadata
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _notifications = MutableStateFlow(GroupedNotifications(emptyList(), emptyList(), emptyList(), emptyList()))

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
            try {
                val fetched = supabase.from(NOTIFS_TABLE).select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<Notification>()

                val now = Clock.System.now()
                val todayStart = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.atStartOfDayIn(TimeZone.currentSystemDefault())
                val yesterdayStart = todayStart.minus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                val weekStart = todayStart.minus(7, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

                val today = mutableListOf<Notification>()
                val yesterday = mutableListOf<Notification>()
                val last7Days = mutableListOf<Notification>()
                val older = mutableListOf<Notification>()

                for (notif in fetched) {
                    when {
                        notif.timestamp >= todayStart -> today.add(notif)
                        notif.timestamp >= yesterdayStart -> yesterday.add(notif)
                        notif.timestamp >= weekStart -> last7Days.add(notif)
                        else -> older.add(notif)
                    }
                }

                _notifications.value = GroupedNotifications(
                    today = today,
                    yesterday = yesterday,
                    last7Days = last7Days,
                    older = older
                )
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Failed to fetch notifications", e)
            }
        }
    }

}
