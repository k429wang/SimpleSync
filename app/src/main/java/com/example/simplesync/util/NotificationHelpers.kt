package com.example.simplesync.util

import android.util.Log
import com.example.simplesync.model.Notification
import com.example.simplesync.model.NotifType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.datetime.Clock

// Citation: ChatGPT-4o
suspend fun createNotification(
    supabase: SupabaseClient,
    type: NotifType,
    receiver: String,
    sender: String,
    eventId: String? = null
) {
    try {
        val notif = Notification(
            type = type,
            receiver = receiver,
            sender = sender,
            event = eventId,
            read = null,
            timestamp = Clock.System.now()
        )
        supabase.from("notifications").insert(notif)
    } catch (e: Exception) {
        Log.e("NotificationHelper", "Failed to insert notification", e)
    }
}