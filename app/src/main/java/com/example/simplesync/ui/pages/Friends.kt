package com.example.simplesync.ui.pages

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextField
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.ui.components.ScreenTitle
import com.example.simplesync.model.Friend
import com.example.simplesync.model.UserMetadata
import com.example.simplesync.viewmodel.FriendsViewModel
import com.example.simplesync.viewmodel.UserViewModel

@Composable
fun FriendsPage(
    navController: SimpleSyncNavController,
    viewModel: FriendsViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val friends by viewModel.friends.collectAsState()
    val currUser by userViewModel.currUser.collectAsState()

    val userId = currUser?.authUser?.id
    var loginError by remember { mutableStateOf(false) }

    LaunchedEffect(currUser) {
        if (currUser == null) {
            // Don't do anything yet; wait until it's settled
            return@LaunchedEffect
        }

        if (userId != null) {
            viewModel.fetchFriendsForUser(userId)
            loginError = false // Reset error in case of recomposition
        } else {
            loginError = true
        }
    }

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
            ScreenTitle("Friends")

            // Search Bar
            SearchBar(searchQuery = searchQuery, onQueryChange = { searchQuery = it })

            Spacer(modifier = Modifier.height(16.dp))

            val isLoading by viewModel.isLoading.collectAsState()

            // Friend list
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (loginError) {
                Text(
                    text = "You must be logged in to view your friends. d${userId}d",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else if (friends.isEmpty()){
                Text(
                    text = "No friends found.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                // Generate friends List
                val userCache = remember { mutableStateMapOf<String, UserMetadata?>() }

                LazyColumn {
                    items(friends) { friend ->
                        val user = userCache[friend.friendId]

                        if (!userCache.containsKey(friend.friendId)) {
                            LaunchedEffect(friend.friendId) {
                                val fetched = userViewModel.getUserById(friend.friendId)
                                userCache[friend.friendId] = fetched
                            }
                        }

                        FriendListItem(
                            friend = friend,
                            fullname = user?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown",
                            username = user?.username ?: "Unknown"
                        )
                    }
                }
            }
            }

        }
    }

@Composable
fun SearchBar(searchQuery: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
        },
        placeholder = {
            Text(
                "Search",
                color = Color.Gray,
                fontSize = 16.sp
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .border(1.dp, Color.Black, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun FriendListItem(friend: Friend, fullname: String, username: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            modifier = Modifier
                .size(48.dp)
                .padding(end = 12.dp)
        )
        // TODO: update UI to support display of different friend statuses ("ACCEPTED", "PENDING", "BLOCKED")
        Column {
            Text(text = fullname, fontWeight = FontWeight.Bold)
            Text(text = username, fontSize = 12.sp)
        }
    }
}
