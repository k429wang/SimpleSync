package com.example.simplesync.ui.pages
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.components.ScreenTitle
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.model.Event
import com.example.simplesync.model.Visibility
import com.example.simplesync.model.Recurrence
import com.example.simplesync.model.*
import com.example.simplesync.viewmodel.EventViewModel
import com.example.simplesync.viewmodel.UserViewModel
import kotlinx.datetime.Instant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Citation: built with ChatGPT 4o
@Composable
fun EventPage(navController: SimpleSyncNavController) {
    val eventViewModel: EventViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val currUser by userViewModel.currUser.collectAsState()
    val events by eventViewModel.events.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(currUser) {
        currUser?.let {
            eventViewModel.fetchEventsForUser(it.authUser.id)
        }
    }
    // Sample data
    val sampleEvents = listOf(
        Event(
            owner = "alexj",
            name = "Guild Raid Planning",
            description = "Sync availability for weekend event",
            startTime = Instant.parse("2025-08-01T15:30:00Z"),
            endTime = Instant.parse("2025-08-01T17:00:00Z"),
            type = EventType.VIRTUAL,
            location = "Discord",
            recurrence = Recurrence.ONCE,
            visibility = Visibility.PRIVATE
        ),
        Event(
            owner = "marial",
            name = "Study Group",
            description = "Weekly meeting for study sync",
            startTime = Instant.parse("2025-07-09T09:30:00Z"),
            endTime = Instant.parse("2025-07-09T12:00:00Z"),
            type = EventType.IRL,
            location = "Library",
            recurrence = Recurrence.WEEKLY,
            visibility = Visibility.PUBLIC
        )
    )

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Title
            ScreenTitle("My events")

            SearchBar(searchQuery, onQueryChange = { searchQuery = it })

            Spacer(modifier = Modifier.height(16.dp))

            // Filter search based on event name or location
            val filteredEvents = events.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        (it.location?.contains(searchQuery, ignoreCase = true) ?: false)
            }
                .sortedBy { it.startTime }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredEvents) { event ->
                    EventCard(event)
                }
            }
        }
    }
}

// Citation: built with ChatGPT 4o
@Composable
fun EventCard(event: Event) {
    val formatter = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
    val formattedDate = formatter.format(Date(event.startTime.toEpochMilliseconds()))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray)
    ) {
        // Date badge
        Text(
            text = formattedDate,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color.DarkGray, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )

        // Event name and owner
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        ) {
            Text(text = event.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            event.location?.let {
                Text(text = it, fontSize = 14.sp)
            }
        }

        // Bottom-right person icon
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Event Owner",
            tint = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(24.dp)
        )
    }
}