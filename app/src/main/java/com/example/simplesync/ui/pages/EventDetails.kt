package com.example.simplesync.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.model.Event
import com.example.simplesync.model.UserMetadata
import com.example.simplesync.ui.components.EventField
import com.example.simplesync.viewmodel.UserViewModel

// Citation: built with ChatGPT 4o
@Composable
fun EventDetailsPage(navController: SimpleSyncNavController, event: Event) {
    val userViewModel: UserViewModel = hiltViewModel()
    var metadata by remember { mutableStateOf<UserMetadata?>(null) }
    var noteText by remember { mutableStateOf("") }

    LaunchedEffect(event.owner) {
        userViewModel.fetchUserMetadataById(event.owner) {
            metadata = it
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            metadata?.let {
                Text(
                    text = "${it.firstName}'s event",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            } ?: Text(
                "Unknown's event",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(16.dp))

            // Event Details
            DetailRow("Time:", formatEventTime(event))
            DetailRow("Location:", event.location ?: "N/A")
            DetailRow("Recurrence:", event.recurrence.name.lowercase().replaceFirstChar { it.uppercase() })
            DetailRow("Description:", event.description ?: "None")
            DetailRow("Visibility:", event.visibility.name.lowercase().replaceFirstChar { it.uppercase() })

            Spacer(Modifier.height(24.dp))

            Text(
                "Your availability",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray)
                    .padding(8.dp)
            ) {
                // PLACE HOLDER FOR CALENDAR!!!!!!
                // TODO: update with working calendar
                Spacer(modifier = Modifier.height(8.dp))

                // Header Row for Days
                Row(modifier = Modifier.fillMaxWidth()) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    days.forEach {
                        Text(
                            text = it,
                            modifier = Modifier
                                .weight(1f)
                                .padding(2.dp),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { /* TODO: Update calendar slots */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Add new", color = Color.White)
            }

            Spacer(Modifier.height(16.dp))

            // TODO: backend connection - user can add optional note when responding to event invite
//            OutlinedTextField(
//                value = noteText,
//                onValueChange = { noteText = it },
//                label = { Text("Note") },
//                modifier = Modifier.fillMaxWidth()
//            )
            EventField("Note:", noteText, {noteText = it}) // TODO: formatting is weird

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                Spacer(Modifier.width(4.dp))
                Text("SEND", color = Color.White)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label ",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.DarkGray
        )
    }
}

// Citation: from Chat GPT 4o
fun formatEventTime(event: Event): String {
    val formatter = java.text.SimpleDateFormat("MMM dd, yyyy h:mm a")
    formatter.timeZone = java.util.TimeZone.getTimeZone("America/New_York")

    val start = formatter.format(java.util.Date(event.startTime.toEpochMilliseconds()))
    val end = formatter.format(java.util.Date(event.endTime.toEpochMilliseconds()))

    return "$start – $end"
}
