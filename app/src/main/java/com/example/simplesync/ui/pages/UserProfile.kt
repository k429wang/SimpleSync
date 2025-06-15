package com.example.simplesync.ui.pages

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.viewmodel.UserViewModel

@Composable
fun UserProfile(navController: SimpleSyncNavController) {
    Text("User Profile Page")
    UsersList()
}

@Composable
fun UsersList(viewModel: UserViewModel = hiltViewModel()) {
    val users by viewModel.users.collectAsState()

    LazyColumn {
        items(users, key = { user -> user.userName }) { user ->
            Text(
                text = "${user.userName} - ${user.email}: ${user.firstName} ${user.lastName}",
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}
