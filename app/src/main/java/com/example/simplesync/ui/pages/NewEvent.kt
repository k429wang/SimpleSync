package com.example.simplesync.ui.pages

import DropdownField
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import com.example.simplesync.model.ConcreteCalendar
import com.example.simplesync.model.Event
import com.example.simplesync.model.EventType
import com.example.simplesync.model.Recurrence
import com.example.simplesync.model.Visibility
import com.example.simplesync.ui.components.AvailabilityGrid
import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.ui.components.ScreenTitle
import com.example.simplesync.ui.components.EventFormFields
import com.example.simplesync.viewmodel.EventViewModel
import com.example.simplesync.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDateTime

@Composable
fun NewEventPage(
    navController: SimpleSyncNavController,
    eventViewModel: EventViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
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
    var availabilityReturn by remember { mutableStateOf<LocalDateTime?>( null ) }

    // Keep track of scroll position
    val scrollState = rememberScrollState()

    // Submission results
    val createEventResult by eventViewModel.eventResult.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // for instant conversion
    val timeZone = TimeZone.currentSystemDefault()

    // Popup for event creation results
    // This is creating some issues for nav. Rather than doing the nav
    // here, maybe move it to the event creation onclick & error check?
    LaunchedEffect (createEventResult) {
        createEventResult?.let { it ->
            it.onSuccess {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Successfully created event!")
                }
                // Navigate using the actual event
                navController.nav(navController.eventDetailsRoute(it.id))
            }.onFailure { e ->
                Log.e("NewEventPage", "Event creation failed", e)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error: ${e.message}")
                }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

            availabilityReturn = AvailabilityGrid(
                navController = navController,
                calendar = ConcreteCalendar(),
                strategy = "RETURN"
            )

            // logic for clicking the thing.
            // This isn't a composable. If it were, it would recompose.
            //Try to do an if statement that modifies the value with the onclick
            // again, quick and dirty but it works
            val availInstant = availabilityReturn?.toKotlinLocalDateTime()?.toInstant(timeZone = timeZone)
            if (availInstant != null ) {
                if (startTime == null) {
                    startTime = availInstant
                } else if (endTime == null) {
                    if (startTime != availInstant) {
                        endTime = availInstant
                    }
                } else {
                    if (startTime!! >= availInstant) {
                        startTime = availInstant
                    } else {
                        endTime = availInstant
                    }
                }
            } // this should work fine now.

            Spacer(modifier = Modifier.height(8.dp))

            // Fields that will be synced with the backend
            EventFormFields(
                name = name,
                onNameChange = { name = it },
                description = description,
                onDescriptionChange = { description = it },
                startTime = startTime,
                onStartTimeChange = { startTime = it },
                endTime = endTime,
                onEndTimeChange = { endTime = it },
                type = type,
                onTypeChange = { type = it },
                location = location,
                onLocationChange = { location = it },
                recurrence = recurrence,
                onRecurrenceChange = { recurrence = it },
                visibility = visibility,
                onVisibilityChange = { visibility = it }
            )

            // Form submission button
            Button(
                onClick = {
                    try {
                        handleCreateNewEvent(
                            eventViewModel = eventViewModel,
                            owner = currUser?.userMetadata?.id,
                            name = name,
                            description = description,
                            startTime = startTime,
                            endTime = endTime,
                            type = type,
                            location = location,
                            recurrence = recurrence,
                            visibility = visibility
                        )
                    } catch (e: Exception) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Error: ${e.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Event")
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
    if (endTime <= startTime) throw IllegalStateException("End time must be after start time")
    // other error checks we should perform:
    val timeZone = TimeZone.currentSystemDefault()
    if (endTime.toLocalDateTime(timeZone).date != startTime.toLocalDateTime(timeZone).date) throw IllegalStateException("Start and end times must be on same day")

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
    // navigate out
}

