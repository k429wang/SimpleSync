package com.example.simplesync.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import org.json.JSONObject
import com.example.simplesync.BuildConfig
import com.example.simplesync.model.UserMetadata
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray

val apiKey = BuildConfig.ONESIGNAL_API_KEY
val appId = BuildConfig.ONESIGNAL_APP_ID
const val USERS_TABLE = "users"

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {

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

}
