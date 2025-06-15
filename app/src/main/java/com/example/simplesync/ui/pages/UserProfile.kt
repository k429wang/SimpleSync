package com.example.simplesync.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
    val viewModel: UserViewModel = hiltViewModel()
    val currUser by viewModel.currUser.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("User Navigation Page", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.getUserById("kaithekiwi") },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Fetch User kaithekiwi")
        }

        currUser?.let { user ->
            Text(
                "Selected User:\n${user.userName} - ${user.email} (${user.firstName} ${user.lastName})",
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("List of all users:")
        UsersList(viewModel)
    }
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
