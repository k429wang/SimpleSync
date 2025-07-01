package com.example.simplesync.ui.pages

import DropdownField
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.ui.components.ScreenTitle
import com.example.simplesync.ui.components.EventField

@Composable
fun NewEventPage(navController: SimpleSyncNavController) {
    // Enums
    val typeOptions = listOf<String>("IRL", "Virtual")
    val recurrenceOptions = listOf<String>("Once", "Daily", "Weekly")
    val visibilityOptions = listOf<String>("Solo", "Private", "Public")

    // values sent to backend
    // TODO: connect to backend
    val name = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val startTime = remember { mutableStateOf("") }
    val endTime = remember { mutableStateOf("") }
    val type = remember { mutableStateOf("") }
    val location = remember { mutableStateOf("") }
    val recurrence = remember { mutableStateOf("") }
    val visibility = remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    // Ensures last element does not overlap with bottom nav
                    bottom = innerPadding.calculateBottomPadding() + 12.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            ScreenTitle("Create Event")

            // PLACE HOLDER FOR CALENDAR!!!!!!
            // TODO: update with working calendar

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

            // Availability boxes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                repeat(7) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(2.dp)
                            .background(Color.LightGray)
                            .border(1.dp, Color.Black)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // fields that will be synced with the backend
            EventField("Name:", name)           // -> send as "name"
            EventField("Description:", description) // -> send as "description"
            EventField("Start Time:", startTime)
            EventField("End Time:", endTime)           // -> send as "time"
            DropdownField("Type:", typeOptions, type)           // -> send as "type"
            EventField("Location:", location)   // -> send as "location"
            DropdownField("Recurrence:", recurrenceOptions ,recurrence) // -> send as "recurrence"
            DropdownField("Visibility:", visibilityOptions, visibility) // -> send as "visibility

            Spacer(modifier = Modifier.height(8.dp))

            // Invite button, also sends you to list of friends
            Button(
                onClick = {
                    // TODO: send data to backend
                    /*
                    val newEvent = Event(
                        name = name.value,
                        description = description.value,
                        time = time.value,
                        recurrence = recurrence.value,
                        type = type.value,
                        location = location.value,
                        visibility = visibility.value
                    )
                    eventViewModel.createEvent(newEvent)
                    */
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
            ) {
                Text("+ Invite", color = Color.White)
            }
        }
    }
}

