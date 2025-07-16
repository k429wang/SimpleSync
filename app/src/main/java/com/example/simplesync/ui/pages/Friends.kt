package com.example.simplesync.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.ui.components.ScreenTitle
import com.example.simplesync.model.Friendship
import com.example.simplesync.model.Status
import com.example.simplesync.model.UserMetadata
import com.example.simplesync.ui.components.AcceptAndDeclineButtons
import com.example.simplesync.ui.components.EventField
import com.example.simplesync.ui.components.SearchBar
import com.example.simplesync.viewmodel.FriendshipViewModel
import com.example.simplesync.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Constants for tabs
const val EXISTING = 0
const val PENDING = 1
const val DECLINED = 2

@Composable
fun FriendsPage(
    navController: SimpleSyncNavController,
    viewModel: FriendshipViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    // Overall page states
    var searchQuery by remember { mutableStateOf("") }
    val friendships by viewModel.friendships.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Current User states
    val currUser by userViewModel.currUser.collectAsState()
    val userId = currUser?.authUser?.id
    var loginError by remember { mutableStateOf(false) }

    // AddFriend states
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var friendUsername by remember { mutableStateOf("") }
    var addFriendError by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Tabs
    val tabTitles = listOf<String>("Friends", "Pending", "Declined")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(currUser) {
        if (currUser == null) {
            // Don't do anything yet; wait until it's settled
            return@LaunchedEffect
        }

        if (userId != null) {
            viewModel.fetchFriendshipsForUser(userId)
            loginError = false // Reset error in case of recomposition
        } else {
            loginError = true
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title
                ScreenTitle("Friends")

                // "Add Friend" button
                FloatingActionButton(
                    onClick = { showAddFriendDialog = true },
                    modifier = Modifier.padding(4.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Friend")
                }
            }


            // "Add Friend" dialog popup
            if (showAddFriendDialog) {
                AlertDialog(
                    onDismissRequest = { showAddFriendDialog = false },
                    title = { Text("Add Friend") },
                    text = {
                        Column {
                            EventField(
                                label = "Friend Username:",
                                value = friendUsername,
                                onValueChange = { friendUsername = it })
                            if (addFriendError.isNotEmpty()) {
                                Text(
                                    addFriendError,
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (friendUsername.isBlank()) {
                                addFriendError = "Username cannot be empty"
                            } else if (userId == null) {
                                addFriendError = "User ID is null"
                            } else {
                                coroutineScope.launch {
                                    val friend = userViewModel.getUserByUsername(friendUsername)
                                    if (friend == null) {
                                        addFriendError = "User not found!"
                                    } else if (friend.id == userId) {
                                        addFriendError = "You may not add yourself"
                                    } else {
                                        // Friend exists, and not adding themselves
                                        val existsAlready = friendships.any {
                                            (it.userId == userId && it.friendId == friend.id) ||
                                                    (it.userId == friend.id && it.friendId == userId)
                                        }

                                        if (existsAlready) {
                                            addFriendError =
                                                "Friendship already exists or is pending."
                                        } else {
                                            // Valid friendship that doesn't exist yet
                                            val result = viewModel.createFriendship(
                                                Friendship(
                                                    userId = userId,
                                                    friendId = friend.id,
                                                    status = Status.PENDING,
                                                )
                                            )

                                            // Display message for results
                                            if (result.isSuccess) {
                                                snackbarHostState.showSnackbar("Friend request sent to @${friend.username}")
                                                showAddFriendDialog = false
                                                friendUsername = ""
                                                addFriendError = ""
                                            } else {
                                                val message = result.exceptionOrNull()?.message
                                                    ?: "Unknown error"
                                                snackbarHostState.showSnackbar("Failed to send request: $message")
                                            }
                                        }
                                    }
                                }
                            }
                        }) {
                            Text("Send Friend Request")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showAddFriendDialog = false
                            friendUsername = ""
                            addFriendError = ""
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Search Bar
            SearchBar(searchQuery = searchQuery, onQueryChange = { searchQuery = it })

            Spacer(modifier = Modifier.height(16.dp))

            // Friend list
            val isLoading by viewModel.isLoading.collectAsState()
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (loginError) {
                Text(
                    text = "You must be logged in to view your friends.",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else if (friendships.isEmpty()) {
                Text(
                    text = "No friends found.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                // Tabs to toggle between different views
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { idx, title ->
                        Tab(
                            selected = (selectedTabIndex == idx),
                            onClick = { selectedTabIndex = idx },
                            text = { Text(title) }
                        )
                    }
                }

                // Display friendships according to the selected tab
                val friendsCache = remember { mutableStateMapOf<String, UserMetadata?>() }
                LazyColumn {
                    item {
                        when (selectedTabIndex) {
                            EXISTING -> { // Existing Friendships
                                FriendshipListSection(
                                    friendships = friendships.filter { it.status == Status.ACCEPTED },
                                    userId = userId ?: "",
                                    userViewModel = userViewModel,
                                    friendshipViewModel = viewModel,
                                    friendsCache = friendsCache,
                                    coroutineScope = coroutineScope,
                                    snackbarHostState = snackbarHostState,
                                    searchQuery = searchQuery
                                )
                            }

                            PENDING -> { // Pending Friendships
                                // Incoming Friend Requests
                                FriendshipListSection(
                                    friendships = friendships.filter { it.status == Status.PENDING && it.friendId == userId },
                                    userId = userId ?: "",
                                    userViewModel = userViewModel,
                                    friendshipViewModel = viewModel,
                                    friendsCache = friendsCache,
                                    sectionTitle = "Incoming Requests",
                                    coroutineScope = coroutineScope,
                                    snackbarHostState = snackbarHostState,
                                    searchQuery = searchQuery
                                )
                                // Outgoing Friend Requests
                                FriendshipListSection(
                                    friendships = friendships.filter { it.status == Status.PENDING && it.userId == userId },
                                    userId = userId ?: "",
                                    userViewModel = userViewModel,
                                    friendshipViewModel = viewModel,
                                    friendsCache = friendsCache,
                                    sectionTitle = "Outgoing Requests",
                                    coroutineScope = coroutineScope,
                                    snackbarHostState = snackbarHostState,
                                    searchQuery = searchQuery
                                )
                            }

                            DECLINED -> { // Declined Friendships
                                // Incoming Friend Requests that were Declined
                                FriendshipListSection(
                                    friendships = friendships.filter { it.status == Status.DECLINED && it.friendId == userId },
                                    userId = userId ?: "",
                                    userViewModel = userViewModel,
                                    friendshipViewModel = viewModel,
                                    friendsCache = friendsCache,
                                    sectionTitle = "Incoming Requests You Declined",
                                    coroutineScope = coroutineScope,
                                    snackbarHostState = snackbarHostState,
                                    searchQuery = searchQuery
                                )
                                // Outgoing Friend Requests that were Declined
                                FriendshipListSection(
                                    friendships = friendships.filter { it.status == Status.DECLINED && it.userId == userId },
                                    userId = userId ?: "",
                                    userViewModel = userViewModel,
                                    friendshipViewModel = viewModel,
                                    friendsCache = friendsCache,
                                    sectionTitle = "Your Outgoing Requests That Were Declined",
                                    coroutineScope = coroutineScope,
                                    snackbarHostState = snackbarHostState,
                                    searchQuery = searchQuery
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendshipListSection(
    friendships: List<Friendship>,
    userId: String,
    userViewModel: UserViewModel,
    friendshipViewModel: FriendshipViewModel,
    friendsCache: MutableMap<String, UserMetadata?>,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    searchQuery: String,
    sectionTitle: String? = null
) {
    if (friendships.isEmpty()) return

    sectionTitle?.let {
        Text(
            text = it,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }

    val normalizedSearchQuery = searchQuery.lowercase()
    friendships.forEach { friendship ->
        val otherUserId = if (friendship.userId == userId) friendship.friendId else friendship.userId
        if (!friendsCache.containsKey(otherUserId)) {
            // Add to cache if not already present
            LaunchedEffect(otherUserId) {
                val fetched = userViewModel.getUserById(otherUserId)
                friendsCache[otherUserId] = fetched
            }
        }
        val friend = friendsCache[otherUserId]

        // Determine if the friend matches the search query.
        val friendName = "${friend?.firstName} ${friend?.lastName}".lowercase()
        val friendUsername = friend?.username?.lowercase() ?: ""
        val matchesSearchQuery = (normalizedSearchQuery in friendName || normalizedSearchQuery in friendUsername)

        if (friend != null && matchesSearchQuery) {
            FriendListItem(
                fullName = "${friend.firstName} ${friend.lastName}",
                username = friend.username,
                status = friendship.status,
                isIncoming = friendship.status == Status.PENDING && friendship.friendId == userId,
                onAccept = {
                    coroutineScope.launch {
                        val updatedFriendship = friendship.copy(status = Status.ACCEPTED)
                        val result = friendshipViewModel.updateFriendship(updatedFriendship, userId)
                        if (result.isSuccess) {
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("Friend request accepted")
                            } else {
                                snackbarHostState.showSnackbar("Failed to accept friend request")
                            }
                        }
                    }
                },
                onDecline = {
                    coroutineScope.launch {
                        val updatedFriendship = friendship.copy(status = Status.DECLINED)
                        val result = friendshipViewModel.updateFriendship(updatedFriendship, userId)
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar("Friend request declined")
                        } else {
                            snackbarHostState.showSnackbar("Failed to decline friend request")
                        }
                    }
                },
            )
        }
    }
}

@Composable
fun FriendListItem(
    fullName: String,
    username: String,
    status: Status,
    isIncoming: Boolean, // If the friend request is incoming
    onAccept: (() -> Unit)? = null, // For accept/decline buttons
    onDecline: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Icon and Username
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            modifier = Modifier
                .size(48.dp)
                .padding(end = 12.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = fullName, fontWeight = FontWeight.Bold)
            Text(text = "@$username", fontSize = 12.sp, color = Color.Gray)
        }

        // Request Status
        Text(
            text = status.name, // Will show "PENDING", "ACCEPTED", etc.
            fontSize = 12.sp,
            color = when (status) {
                Status.ACCEPTED -> Color(0xFF4CAF50)
                Status.PENDING -> Color(0xFFFFC107)
                Status.DECLINED -> Color(0xFFF44336)
            },
            modifier = Modifier.padding(start = 8.dp)
        )

        // Buttons to Accept/Decline incoming requests
        if (isIncoming && onAccept != null && onDecline != null) {
            AcceptAndDeclineButtons(onAccept, onDecline)
        }
    }
}


