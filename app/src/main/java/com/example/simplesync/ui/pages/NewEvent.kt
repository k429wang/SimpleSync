package com.example.simplesync.ui.pages

import DropdownField
import android.util.Log
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.model.Event
import com.example.simplesync.model.EventType
import com.example.simplesync.model.Recurrence
import com.example.simplesync.model.Visibility
import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.components.DateTimePickerField
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.ui.components.ScreenTitle
import com.example.simplesync.ui.components.EventField
import com.example.simplesync.viewmodel.EventViewModel
import com.example.simplesync.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

@Composable
fun NewEventPage(
    navController: SimpleSyncNavController,
    eventViewModel: EventViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    // Enums to display in dropdowns
    val typeOptions = listOf<String>("IRL", "Virtual")
    val recurrenceOptions = listOf<String>("Once", "Daily", "Weekly")
    val visibilityOptions = listOf<String>("Solo", "Private", "Public")

    // Event parameters sent to backend
    val currUser by userViewModel.currUser.collectAsState()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf<Instant?>(null) }
    var endTime by remember { mutableStateOf<Instant?>(null) }
    var type by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var recurrence by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf("") }

    // Keep track of scroll position
    val scrollState = rememberScrollState()

    // Submission results
    val createEventResult by eventViewModel.eventResult.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle createEventResult
    LaunchedEffect(createEventResult) {
        createEventResult?.let { result ->
            result.onSuccess { event ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Successfully created event!")
                }
                // Navigate using the actual event
                navController.nav(navController.eventDetailsRoute(event.id))
            }.onFailure { e ->
                Log.e("NewEventPage", "Event creation failed", e)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error: ${e.message}")
                }
            }
        }
    }

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
                    bottom = 12.dp,
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

            // Fields that will be synced with the backend
            EventField("Name:", name) {name = it}
            EventField("Description:", description) {description = it}

            DateTimePickerField("Start Time", startTime) { startTime = it }
            DateTimePickerField("End Time", endTime) { endTime = it }

            DropdownField(
                label = "Type:",
                options = typeOptions,
                value = type,
                onValueChange = {type = it}
            )
            EventField("Location:", location, {location = it})
            DropdownField(
                label = "Recurrence:",
                options = recurrenceOptions,
                value = recurrence,
                onValueChange = {recurrence = it}
            )
            DropdownField(
                label = "Visibility:",
                options = visibilityOptions,
                value = visibility,
                onValueChange = {visibility = it}
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Create event button, should send you to the Event's page
            Button(
                onClick = {
                    try {
                        handleCreateNewEvent(
                            eventViewModel,
                            currUser?.authUser?.id,
                            name,
                            description,
                            startTime,
                            endTime,
                            type,
                            location,
                            recurrence,
                            visibility,
                        )
                    } catch (e: Exception) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Invalid input: ${e.message}")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
            ) {
                Text("Create Event", color = Color.White)
            }
        }
    }
}

fun handleCreateNewEvent(
    eventViewModel: EventViewModel,
    owner: String?,
    name: String,
    description: String,
    startTime: Instant?,
    endTime: Instant?,
    type: String,
    location: String,
    recurrence: String,
    visibility: String,
) {
    // Error checking
    if (owner.isNullOrBlank()) throw IllegalStateException("User not signed in")
    if (startTime == null) throw IllegalStateException("Start time not provided")
    if (endTime == null) throw IllegalStateException("Start time not provided")

    // Create a new event
    val event = Event(
        owner = owner.trim(),
        name = name.trim(),
        description = description.trim(),
        startTime = startTime,
        endTime = endTime,
        type = when (type) {
            "IRL" -> EventType.IRL
            "Virtual" -> EventType.VIRTUAL
            else -> throw IllegalArgumentException("Invalid event type")
        },
        location = location,
        recurrence = when (recurrence) {
            "Once" -> Recurrence.ONCE
            "Daily" -> Recurrence.DAILY
            "Weekly" -> Recurrence.WEEKLY
            else -> throw IllegalArgumentException("Invalid recurrence")
        },
        visibility = when (visibility) {
            "Solo" -> Visibility.SOLO
            "Private" -> Visibility.PRIVATE
            "Public" -> Visibility.PUBLIC
            else -> throw IllegalArgumentException("Invalid visibility")
        }
    )

    // Insert the event into Supabase
    eventViewModel.createEvent(event)
}

