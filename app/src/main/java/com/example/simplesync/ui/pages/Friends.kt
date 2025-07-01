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
import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextField
import com.example.simplesync.ui.components.ScreenTitle

@Composable
fun FriendsPage(navController: SimpleSyncNavController) {
    var searchQuery by remember { mutableStateOf("") }

    // PLACEHOLDER DATA FOR FRONTEND TESTING
    val friends = listOf(
        "Alex Johnson" to "alexj",
        "Maria Lee" to "marial",
        "Chris Wong" to "cwong",
        "Sarah Patel" to "sarahp",
        "Daniel Kim" to "dkim",
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
            ScreenTitle("Friends")

            // Search Bar
            SearchBar(searchQuery = searchQuery, onQueryChange = { searchQuery = it })

            Spacer(modifier = Modifier.height(16.dp))

            // Friend List
            LazyColumn( // LazyColumn allows for scrolling the friend list
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(friends.filter {
                    it.first.contains(searchQuery, ignoreCase = true) ||
                            it.second.contains(searchQuery, ignoreCase = true)
                }) { friend ->
                    val (name, username) = friend
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(48.dp)
                                .padding(end = 12.dp)
                        )
                        Column {
                            Text(text = name, fontWeight = FontWeight.Bold)
                            Text(text = username, fontSize = 12.sp)
                        }
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
