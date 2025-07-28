package com.example.simplesync.ui.pages

import DropdownField
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.model.Attendee
import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.model.Event
import com.example.simplesync.model.EventRole
import com.example.simplesync.model.EventType
import com.example.simplesync.model.Friendship
import com.example.simplesync.model.NotifType
import com.example.simplesync.model.Recurrence
import com.example.simplesync.model.Status
import com.example.simplesync.model.UserMetadata
import com.example.simplesync.model.Visibility
import com.example.simplesync.model.displayName
import com.example.simplesync.ui.components.EventField
import com.example.simplesync.ui.components.EventFormFields
import com.example.simplesync.ui.components.ReadOnlyProfilePicture
import com.example.simplesync.viewmodel.EventViewModel
import com.example.simplesync.viewmodel.FriendshipViewModel
import com.example.simplesync.viewmodel.UserViewModel
import com.example.simplesync.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.collections.set
import android.util.Log;
import kotlinx.coroutines.launch
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner


// Citation: built with ChatGPT 4o
@Composable
fun EventDetailsPage(navController: SimpleSyncNavController, event: Event) {
    Log.d("EventDetailsPage", "Opened EventDetailsPage for event: ${event.id} (${event.name})")
    // Clock
    val clock: Clock = Clock.System
    val now = clock.now()

    // Events
    val eventViewModel: EventViewModel = hiltViewModel()

    // Notifications
    val notificationViewModel: NotificationViewModel = hiltViewModel()

    // Baseline copy that will change after each successful save
    var savedEvent by remember { mutableStateOf(event) }
    // Form state (if user is owner or editor of this event)
    var name by remember { mutableStateOf(event.name) }
    var description by remember { mutableStateOf(event.description ?: "") }
    var startTime by remember { mutableStateOf(event.startTime) }
    var endTime by remember { mutableStateOf(event.endTime) }
    var type by remember { mutableStateOf(event.type.name.lowercase().replaceFirstChar { it.uppercase() }) }
    var location by remember { mutableStateOf(event.location ?: "") }
    var recurrence by remember { mutableStateOf(event.recurrence.name.lowercase().replaceFirstChar { it.uppercase() }) }
    var visibility by remember { mutableStateOf(event.visibility.name.lowercase().replaceFirstChar { it.uppercase() }) }
    // Observe the ViewModel’s update result ------------------------------
    val eventResult by eventViewModel.eventResult.collectAsState(initial = null)
    var oldSavedEvent by remember { mutableStateOf(event) }

    // User
    val userViewModel: UserViewModel = hiltViewModel()
    val currUser by userViewModel.currUser.collectAsState()
    val userId = currUser?.authUser?.id
    var ownerMetadata by remember { mutableStateOf<UserMetadata?>(null) }

    // Friends
    val friendshipViewModel: FriendshipViewModel = hiltViewModel()
    val friendsCache = remember { mutableStateMapOf<String, UserMetadata?>() }

    // Attendees
    val attendees by eventViewModel.attendeesForEvent.collectAsState()
    val accepted = attendees.filter { it.inviteStatus == Status.ACCEPTED }
    val pending = attendees.filter { it.inviteStatus == Status.PENDING }

    // User-specific attendee info
    val userRole = attendees.find { it.userId == userId }?.role

    // Fill your availability
    var noteText by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(event.owner, event.id) {
        // Always try to fetch event owner's metadata
        if (event.owner.isNotBlank()) {
            userViewModel.fetchUserMetadataById(event.owner) { ownerMetadata = it }
        }

        eventViewModel.fetchAttendeesForEvent(event.id)
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            friendshipViewModel.fetchFriendshipsForUser(userId)
        }
    }



    LaunchedEffect(eventResult) {
        eventResult?.let { result ->
            result.fold(
                onSuccess = { updated ->
                    savedEvent = updated  // Replace with actual updated event from server
                    snackbarHostState.showSnackbar("Event updated successfully.")
                },
                onFailure = {
                    savedEvent = oldSavedEvent  // Roll back optimistic update
                    snackbarHostState.showSnackbar("Failed to update event.")
                }
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Event Name
            Text(
                text = event.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Owner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 8.dp)
            ) {
                if (!ownerMetadata?.profilePicURL.isNullOrEmpty()) {
                    ReadOnlyProfilePicture(
                        imageUrl = ownerMetadata!!.profilePicURL,
                        size = 48.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Owner",
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "Event Owner",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = ownerMetadata?.let {
                            "${it.firstName} ${it.lastName}"
                        } ?: "Unknown",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }


            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Event Details
            if(userRole == EventRole.OWNER || userRole == EventRole.EDITOR) {
                // Determine if the fields have had any changes
                val hasChanges =
                            name        != savedEvent.name                               ||
                            description != (savedEvent.description ?: "")                ||
                            startTime   != savedEvent.startTime                          ||
                            endTime     != savedEvent.endTime                            ||
                            type        != savedEvent.type.displayName()                 ||
                            location    != (savedEvent.location ?: "")                   ||
                            recurrence  != savedEvent.recurrence.displayName()           ||
                            visibility  != savedEvent.visibility.displayName()

                // Show editable fields
                EventFormFields(
                    name = name,
                    onNameChange = { name = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    startTime = startTime,
                    onStartTimeChange = { startTime = it ?: now },
                    endTime = endTime,
                    onEndTimeChange = { endTime = it ?: now },
                    type = type,
                    onTypeChange = { type = it },
                    location = location,
                    onLocationChange = { location = it },
                    recurrence = recurrence,
                    onRecurrenceChange = { recurrence = it },
                    visibility = visibility,
                    onVisibilityChange = { visibility = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val updatedEvent = event.copy(
                            name = name,
                            description = description.ifBlank { null },
                            startTime = startTime,
                            endTime = endTime,
                            type = EventType.valueOf(type.uppercase()),
                            location = location.ifBlank { null },
                            recurrence = Recurrence.valueOf(recurrence.uppercase()),
                            visibility = Visibility.valueOf(visibility.uppercase())
                        )
                        // Backup in case we want to rollback later
                        oldSavedEvent = savedEvent

                        // Optimistically assume this update will succeed
                        savedEvent = updatedEvent

                        // Trigger backend call
                        eventViewModel.updateEvent(updatedEvent, userId)
                    },
                    enabled = hasChanges, // Only clickable if something changed
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("Update Event", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))
                val coroutineScope = rememberCoroutineScope()
                val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

                if (userRole == EventRole.OWNER) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (userId != null) {
                                    eventViewModel.deleteEventCompletely(event.id, userId)
//                                    navController.popBackStack() // Navigates back after deletion
                                    backDispatcher?.onBackPressed()
                                }
                            }
                        },
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Event",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Event", color = Color.White)
                    }
                }
            } else {
                // Show readonly fields
                DetailRow("Time:", formatEventTime(event))
                DetailRow("Location:", event.location ?: "N/A")
                DetailRow(
                    "Recurrence:",
                    event.recurrence.name.lowercase().replaceFirstChar { it.uppercase() })
                DetailRow("Description:", event.description ?: "None")
                DetailRow(
                    "Visibility:",
                    event.visibility.name.lowercase().replaceFirstChar { it.uppercase() })
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Invite Friends Button
            if(userRole == EventRole.OWNER || userRole == EventRole.EDITOR) {
                val openInviteDialog = remember { mutableStateOf(false) }
                Button(
                    onClick = { openInviteDialog.value = true },
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                ) {
                    Text("Invite Friends to This Event")
                }
                // Invite Friends Popup Dialog
                if (openInviteDialog.value) {
                    AlertDialog(
                        onDismissRequest = { openInviteDialog.value = false },
                        title = { Text("Invite Friends") },
                        confirmButton = {
                            TextButton(onClick = { openInviteDialog.value = false }) {
                                Text("Close")
                            }
                        },
                        text = {
                            InviteDialogContent(
                                event = event,
                                userId = userId ?: "",
                                eventViewModel = eventViewModel,
                                userViewModel = userViewModel,
                                friendshipViewModel = friendshipViewModel,
                                friendsCache = friendsCache,
                                notificationViewModel = notificationViewModel,
                                currUsername = currUser?.userMetadata?.username
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Participants
            Text("Participants", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            if (accepted.isEmpty()) {
                Text("No participants yet.")
            } else {
                accepted.forEach { attendee ->
                    if (!friendsCache.containsKey(attendee.userId)) {
                        LaunchedEffect(attendee.userId) { // Add to cache if not already present
                            val fetched = userViewModel.getUserById(attendee.userId)
                            friendsCache[attendee.userId] = fetched
                        }
                    }

                    val user = friendsCache[attendee.userId]
                    if (user != null) {
                        AttendeeListItem(
                            metadata = user,
                            role = attendee.role,
                            showDeleteButton = (userRole == EventRole.OWNER) && (attendee.userId != userId),
                            onDelete = {
                                // Call delete from eventViewModel
                                eventViewModel.removeAttendee(event.id, attendee.userId)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Pending Invitations
            Text("Pending Invitations", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            if (pending.isEmpty()) {
                Text("No pending invites.")
            } else {
                pending.forEach { attendee ->
                    if (!friendsCache.containsKey(attendee.userId)) {
                        LaunchedEffect(attendee.userId) { // Add to cache if not already present
                            val fetched = userViewModel.getUserById(attendee.userId)
                            friendsCache[attendee.userId] = fetched
                        }
                    }
                    val user = friendsCache[attendee.userId]

                    if (user != null) {
                        AttendeeListItem(
                            metadata = user,
                            role = attendee.role,
                            showDeleteButton = userRole == EventRole.OWNER,
                            onDelete = {
                                // Call delete from eventViewModel
                                eventViewModel.removeAttendee(event.id, attendee.userId)
                            }
                        )
                    }
                }
            }


            // Fill your availability
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

@Composable
fun InviteDialogContent(
    event: Event,
    userId: String,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    friendshipViewModel: FriendshipViewModel,
    friendsCache: MutableMap<String, UserMetadata?>,
    notificationViewModel: NotificationViewModel,
    currUsername: String?
) {
    val attendees by eventViewModel.attendeesForEvent.collectAsState()
    val friendships by friendshipViewModel.friendships.collectAsState()

    // Invite Section
    InviteFriendsSection(
        userId = userId,
        event = event,
        friendships = friendships,
        friendsCache = friendsCache,
        userViewModel = userViewModel,
        eventViewModel = eventViewModel,
        attendees = attendees,
        notificationViewModel = notificationViewModel,
        currUsername = currUsername
    )

}


@Composable
fun InviteFriendsSection(
    userId: String,
    event: Event,
    friendships: List<Friendship>,
    friendsCache: MutableMap<String, UserMetadata?>,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    attendees: List<Attendee>,
    notificationViewModel: NotificationViewModel,
    currUsername: String?
) {
    val coroutineScope = rememberCoroutineScope() // for push notif function call
    val invitedUsers = remember { mutableStateListOf<String>() }
    // Skip friends who are already invited (pending) or accepted
    val alreadyInvolvedIds = attendees.map { it.userId }.toSet()
    val inviteableFriendships = friendships
        .filter { it.status == Status.ACCEPTED }
        .filter {
            val friendId = if (it.userId == userId) it.friendId else it.userId
            !alreadyInvolvedIds.contains(friendId)
        }

    if (event.visibility != Visibility.SOLO && friendships.isNotEmpty()) {
        // At least 1 friend to invite
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 4.dp)
        ) {
            inviteableFriendships.forEach { friendship ->
                val otherUserId =
                    if (friendship.userId == userId) friendship.friendId else friendship.userId
                if (!friendsCache.containsKey(otherUserId)) {
                    LaunchedEffect(otherUserId) { // Add to cache if not already present
                        val fetched = userViewModel.getUserById(otherUserId)
                        friendsCache[otherUserId] = fetched
                    }
                }
                val friend = friendsCache[otherUserId]

                if (friend != null) {
                    InviteFriendsItem(
                        friendMetadata = friend,
                        alreadyInvited = invitedUsers.contains(friend.id),
                        onInvite = {
                            eventViewModel.inviteUserToEvent(
                                eventId = event.id,
                                toUser = friend.id,
                                fromUser = userId,
                                role = EventRole.EDITOR,
                            )
                            invitedUsers.add(friend.id)
                            notificationViewModel.insertNotif(
                                type = NotifType.EVENT_INVITE,
                                receiver = friend.id,
                                sender = userId,
                                eventId = event.id,
                            )

                            coroutineScope.launch {
                                notificationViewModel.sendNotificationToUser(
                                    playerId = friend.playerId,
                                    message = "@$currUsername invited you to \"${event.name}\""
                                )
                            }
                        }
                    )
                }
            }
        }
    } else if (event.visibility != Visibility.SOLO && friendships.isEmpty()) {
        // No friends to invite
        Text(
            text = "No friends found.",
            color = Color.Gray,
            fontSize = 14.sp,
        )
    }
}

@Composable
fun InviteFriendsItem(
    friendMetadata: UserMetadata,
    alreadyInvited: Boolean = false,
    onInvite: (EventRole) -> Unit
) {
    val roles = listOf("Owner", "Editor", "Viewer")
    var selectedRole by remember { mutableStateOf(EventRole.EDITOR) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // --- Row 1: Profile + Name + Username ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Profile
            if (!friendMetadata.profilePicURL.isNullOrEmpty()) {
                ReadOnlyProfilePicture(
                    imageUrl = friendMetadata.profilePicURL,
                    size = 48.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 8.dp)
                )
            }

            // Name + Username
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = "${friendMetadata.firstName} ${friendMetadata.lastName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "@${friendMetadata.username}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Row 2: Role dropdown + Invite button ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            DropdownField(
                label = "Role:",
                options = roles,
                value = selectedRole.name.lowercase().replaceFirstChar { it.uppercase() },
                onValueChange = { selectedDisplay ->
                    selectedRole = EventRole.valueOf(selectedDisplay.uppercase())
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            Button(
                onClick = { onInvite(selectedRole) },
                enabled = !alreadyInvited,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (alreadyInvited) Color.Gray else Color.DarkGray
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Text(if (alreadyInvited) "Invited" else "Invite", color = Color.White)
            }
        }
    }
}

@Composable
fun AttendeeListItem(
    metadata: UserMetadata,
    role: EventRole,
    showDeleteButton: Boolean = false,
    onDelete: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile picture
        if (!metadata.profilePicURL.isNullOrEmpty()) {
            ReadOnlyProfilePicture(imageUrl = metadata.profilePicURL, size = 48.dp)
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 8.dp)
            )
        }

        // Name + Role
        Column(modifier = Modifier
            .weight(1f)
            .padding(start = 8.dp)) {
            Text(
                text = "${metadata.firstName} ${metadata.lastName}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Role: ${role.name.lowercase().replaceFirstChar { it.uppercase() }}",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        // Delete button
        if (showDeleteButton && onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove attendee",
                    tint = Color.Red
                )
            }
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
