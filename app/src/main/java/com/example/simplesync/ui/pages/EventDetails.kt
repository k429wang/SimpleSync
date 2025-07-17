package com.example.simplesync.ui.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var noteText = remember { mutableStateOf("") }

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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "${metadata?.firstName ?: "Unknown"}'s event",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Event Details
            DetailRow("Time:", formatEventTime(event))
            DetailRow("Location:", event.location ?: "N/A")
            DetailRow("Recurrence:", event.recurrence.name.lowercase().replaceFirstChar { it.uppercase() })
            DetailRow("Description:", event.description ?: "None")
            DetailRow("Visibility:", event.visibility.name.lowercase().replaceFirstChar { it.uppercase() })

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8E8E8))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Fill your availability",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Your days + calendar grid placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(Color.White)
                    ) {
                        // Placeholder content
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { /* TODO: Update calendar slots */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text("Add new", color = Color.White)
                    }

                    Spacer(Modifier.height(16.dp))

                    // TODO: backend connection - user can add optional note when responding to event invite
                    // Use shared EventField
                    EventField(label = "Note:", value = noteText)

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { /* TODO */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SEND", color = Color.White)
                    }
                }

            }

        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.width(90.dp)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }
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
