package com.example.simplesync.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.navigation.SimpleSyncNavController

@Composable
fun NewEventPage(navController: SimpleSyncNavController) {
    // values sent to backend
    // TODO: connect to backend
    val name = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val time = remember { mutableStateOf("") }
    val recurrence = remember { mutableStateOf("") }
    val type = remember { mutableStateOf("") }
    val location = remember { mutableStateOf("") }
    val visibility = remember { mutableStateOf("") }

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Create event",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

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
            CompactEventField("Name:", name)           // -> send as "name"
            CompactEventField("Description:", description) // -> send as "description"
            CompactEventField("Time:", time)           // -> send as "time"
            CompactEventField("Recurrence:", recurrence) // -> send as "recurrence"
            CompactEventField("Type:", type)           // -> send as "type"
            CompactEventField("Location:", location)   // -> send as "location"
            CompactEventField("Visibility:", visibility) // -> send as "visibility

            Spacer(modifier = Modifier.height(8.dp))

            // Invite button, also sends you to the next screen???
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

@Composable
fun CompactEventField(label: String, value: MutableState<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier
                .width(100.dp)
                .padding(end = 4.dp)
        )
        OutlinedTextField(
            value = value.value,
            onValueChange = { value.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            singleLine = true
        )
    }
}
