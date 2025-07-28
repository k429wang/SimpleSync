package com.example.simplesync.ui.pages

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.components.ScreenTitle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.model.Notification
import com.example.simplesync.model.*
import com.example.simplesync.ui.components.ReadOnlyProfilePicture
import com.example.simplesync.viewmodel.NotificationViewModel
import com.example.simplesync.viewmodel.UserViewModel

@Composable
@Preview
fun NotificationsPage(navController: SimpleSyncNavController? = null) {
    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val currUser by userViewModel.currUser.collectAsState()
    val groupedNotifs by notificationViewModel.notifications.collectAsState()
    var metadata by remember { mutableStateOf<UserMetadata?>(null) }

    LaunchedEffect(true) {
        Log.d("NOTIF", "currUser is $currUser")
        currUser?.let {
            Log.d("NOTIF", "fetching notifs for ${it.authUser.id}")
            notificationViewModel.fetchNotifsForUser(it.authUser.id)
        }
    }
    LaunchedEffect(currUser) {
        currUser?.let {
            userViewModel.fetchUserMetadataById(it.authUser.id) {
                metadata = it
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (navController != null) {
                BottomNavBar(navController)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            ScreenTitle("Notifications")
            Spacer(modifier = Modifier.height(16.dp))

            NotificationSection("Today", groupedNotifs.today)
            NotificationSection("Yesterday", groupedNotifs.yesterday)
            NotificationSection("Last 7 days", groupedNotifs.last7Days)
            NotificationSection("Older", groupedNotifs.older)
        }
    }
}

@Composable
fun NotificationSection(title: String, notifs: List<Notification>) {
    Log.d("NOTIF", "For $title, we have $notifs")
    if (notifs.isNotEmpty()) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        for (notif in notifs) {
            Log.d("NOTIF", "For $notif: ${notif.timestamp}")
            Log.d("NOTIF", "For $notif: .substringBefore ${notif.timestamp.toString().substringBefore("T")}")
            NotificationItem(
                senderName = notif.senderUsername?: "Unknown",
                senderPfpUrl = notif.senderPfpUrl,
                message = when (notif.type) {
                    NotifType.EVENT_INVITE -> "invited you to an event"
                    NotifType.EVENT_ACCEPT -> "accepted your event invite"
                    NotifType.EVENT_DECLINE -> "declined your event invite"
                    NotifType.EVENT_EDIT -> "added changes to your event"
                    NotifType.EVENT_CANCEL -> "cancelled the event"
                    NotifType.FRIEND_REQUEST -> "requested to be your friend"
                    NotifType.FRIEND_ACCEPT -> "accepted your friend request"
                },
                date = notif.timestamp.toString().substringBefore("T") // Simplified date
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun NotificationItem(senderName: String, senderPfpUrl: String?, message: String, date: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        ReadOnlyProfilePicture(
                imageUrl = senderPfpUrl,
                size = 48.dp
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(senderName)
                    }
                    append(" $message")
                }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Date",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
